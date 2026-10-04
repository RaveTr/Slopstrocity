package dev.ploats.slopstrocity.content.entity.misc;

import dev.ploats.slopstrocity.SlopstrocityMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WrappedFallingBlockEntity extends FallingBlockEntity {
    protected boolean hadMobGriefing; // Keep the WFBE itself consistent at least

    public WrappedFallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);

        this.hadMobGriefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    @Override
    public void tick() {
        if (blockState.isAir()) discard();
        else {
            Block heldBlock = blockState.getBlock();

            this.time++;

            applyGravity();
            move(MoverType.SELF, getDeltaMovement());
            handlePortal();

            if (level() instanceof ServerLevel curServerLevel && (isAlive() || forceTickAfterTeleportToDuplicate)) {
                BlockPos curPos = blockPosition();
                boolean holdingConcretePowderBlock = heldBlock instanceof ConcretePowderBlock;
                boolean shouldHydrateCPB = holdingConcretePowderBlock && blockState.canBeHydrated(curServerLevel, curPos, curServerLevel.getFluidState(curPos), curPos);
                double movementSqrd = getDeltaMovement().lengthSqr();

                if (holdingConcretePowderBlock && movementSqrd > 1.0D) {
                    BlockHitResult hitResult = curServerLevel.clip(new ClipContext(new Vec3(xo, yo, zo), position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));

                    if (hitResult.getType() != HitResult.Type.MISS && blockState.canBeHydrated(curServerLevel, curPos, curServerLevel.getFluidState(hitResult.getBlockPos()), hitResult.getBlockPos())) {
                        curPos = hitResult.getBlockPos();
                        shouldHydrateCPB = true;
                    }
                }

                if (onGround() || shouldHydrateCPB) {
                    BlockState targetState = curServerLevel.getBlockState(curPos);

                    setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));

                    if (!targetState.is(Blocks.MOVING_PISTON)) {
                        if (!cancelDrop) {
                            boolean canReplaceTargetState = targetState.canBeReplaced(new DirectionalPlaceContext(curServerLevel, curPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean canDescend = FallingBlock.isFree(curServerLevel.getBlockState(curPos.below())) && (!holdingConcretePowderBlock || !shouldHydrateCPB);
                            boolean shouldPlaceHeldBlock = blockState.canSurvive(curServerLevel, curPos) && !canDescend;

                            if (canReplaceTargetState && shouldPlaceHeldBlock) {
                                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && curServerLevel.getFluidState(curPos).getType() == Fluids.WATER) {
                                    this.blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
                                }

                                if (!hadMobGriefing || curServerLevel.setBlock(curPos, blockState, Block.UPDATE_ALL)) {
                                    curServerLevel.getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(curPos, targetState));

                                    discard();

                                    if (heldBlock instanceof Fallable heldFallableBlock) {
                                        heldFallableBlock.onLand(curServerLevel, curPos, blockState, targetState, this);
                                    }

                                    if (blockData != null && blockState.hasBlockEntity()) {
                                        BlockEntity targetBlockEntity = curServerLevel.getBlockEntity(curPos);

                                        if (targetBlockEntity != null) {
                                            CompoundTag targetBlockEntityData = targetBlockEntity.saveWithoutMetadata(curServerLevel.registryAccess());

                                            for (String dataKey : this.blockData.getAllKeys()) {
                                                targetBlockEntityData.put(dataKey, blockData.get(dataKey).copy());
                                            }

                                            try {
                                                targetBlockEntity.loadWithComponents(targetBlockEntityData, curServerLevel.registryAccess());
                                            } catch (Exception e) {
                                                SlopstrocityMod.LOGGER.error("Failed to load BlockEntity from WrappedFallingBlockEntity with an exception: ", e);
                                            }

                                            targetBlockEntity.setChanged();
                                        }
                                    }
                                } else if (dropItem && curServerLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    discard();
                                    callOnBrokenAfterFall(heldBlock, curPos);
                                    spawnAtLocation(heldBlock);
                                }
                            } else {
                                discard();

                                if (dropItem && curServerLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    callOnBrokenAfterFall(heldBlock, curPos);
                                    spawnAtLocation(heldBlock);
                                }
                            }
                        } else {
                            discard();
                            callOnBrokenAfterFall(heldBlock, curPos);
                        }
                    }
                } else if (time > 100 && (curPos.getY() <= curServerLevel.getMinBuildHeight() || curPos.getY() > curServerLevel.getMaxBuildHeight()) || time > 600) {
                    if (dropItem && curServerLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) spawnAtLocation(heldBlock);

                    discard();
                }
            }

            setDeltaMovement(getDeltaMovement().scale(0.98));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean("HadMobGriefing", hadMobGriefing);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.hadMobGriefing = compound.getBoolean("HadMobGriefing");
    }
}
