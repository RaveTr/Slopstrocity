package dev.ploats.slopstrocity.content.entity.boss;

import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import dev.ploats.slopstrocity.content.entity.ai.goal.hostile.AnimatableAttackGoal;
import dev.ploats.slopstrocity.content.entity.ai.goal.hostile.BandaidMoveToTargetGoal;
import dev.ploats.slopstrocity.content.entity.ai.goal.slopstrocity.SlopstrocityLeapChequeAttackGoal;
import dev.ploats.slopstrocity.content.entity.ai.goal.slopstrocity.SlopstrocityRollingBlunderAttackGoal;
import dev.ploats.slopstrocity.content.entity.base.AnimatableBoss;
import dev.ploats.slopstrocity.content.registry.SlopstrocitySoundEvents;
import dev.ploats.slopstrocity.util.MathUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class Slopstrocity extends AnimatableBoss {
    public static final byte SLOP_SLAM_ATTACK_ID = 1;
    public static final byte SLOP_SPIT_ATTACK_ID = 2;
    public static final byte SLOP_STOMP_ATTACK_ID = 3;
    public static final byte ROLLING_BLUNDER_ATTACK_ID = 4;
    public static final byte LEAP_CHEQUE_ATTACK_ID = 5;
    public static final byte SLOPPY_CLEANUP_ATTACK_ID = 6;
    public static final String IDLE_ANIM = "Idle";
    public static final String DEATH_ANIM = "Death";
    public static final String SLOP_SLAM_ATTACK_ANIM = "Slop Slam Attack";
    public static final String SLOP_SPIT_ATTACK_ANIM = "Slop Spit Attack";
    public static final String SLOP_STOMP_LEFT_ATTACK_ANIM = "Slop Stomp Attack (Left)";
    public static final String SLOP_STOMP_RIGHT_ATTACK_ANIM = "Slop Stomp Attack (Right)";
    public static final String ROLLING_BLUNDER_ATTACK_ANIM = "Rolling Blunder Attack";
    public static final String LEAP_CHEQUE_START_ATTACK_ANIM = "Leap Cheque Attack (Start)";
    public static final String LEAP_CHEQUE_LOOP_ATTACK_ANIM = "Leap Cheque Attack (Loop)";
    public static final String LEAP_CHEQUE_END_ATTACK_ANIM = "Leap Cheque Attack (End)";
    public static final String SLOPPY_CLEANUP_LEFT_ATTACK_ANIM = "Sloppy Cleanup Attack (Left)";
    public static final String SLOPPY_CLEANUP_RIGHT_ATTACK_ANIM = "Sloppy Cleanup Attack (Right)";
    private static final List<DeferredHolder<SoundEvent, ? extends SoundEvent>> IDLE_SOUND_EVENTS = SlopstrocitySoundEvents.SOUND_EVENTS.getEntries().stream()
            .filter(soundEventDeferredHolder -> soundEventDeferredHolder.getRegisteredName().contains("slopstrocity_idle_"))
            .collect(Collectors.toCollection(ObjectArrayList::new));
    private final AnimationState idleAnimState = wrapState(IDLE_ANIM);
    private final AnimationState deathAnimState = wrapState(DEATH_ANIM);
    private final AnimationState slopSlamAttackAnimState = wrapState(SLOP_SLAM_ATTACK_ANIM);
    private final AnimationState slopSpitAttackAnimState = wrapState(SLOP_SPIT_ATTACK_ANIM);
    private final AnimationState slopStompLeftAttackAnimState = wrapState(SLOP_STOMP_LEFT_ATTACK_ANIM);
    private final AnimationState slopStompRightAttackAnimState = wrapState(SLOP_STOMP_RIGHT_ATTACK_ANIM);
    private final AnimationState rollingBlunderAttackAnimState = wrapState(ROLLING_BLUNDER_ATTACK_ANIM);
    private final AnimationState leapChequeStartAttackAnimState = wrapState(LEAP_CHEQUE_START_ATTACK_ANIM);
    private final AnimationState leapChequeLoopAttackAnimState = wrapState(LEAP_CHEQUE_LOOP_ATTACK_ANIM);
    private final AnimationState leapChequeEndAttackAnimState = wrapState(LEAP_CHEQUE_END_ATTACK_ANIM);
    private final AnimationState sloppyCleanupLeftAttackAnimState = wrapState(SLOPPY_CLEANUP_LEFT_ATTACK_ANIM);
    private final AnimationState sloppyCleanupRightAttackAnimState = wrapState(SLOPPY_CLEANUP_RIGHT_ATTACK_ANIM);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.slopstrocity.slopstrocity"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private double aeOffset = 1.0D;
    private double maxAEOffset = 1.0D;
    private Runnable curQuakeCall = () -> {}; // Way too generic but genuinely who tf cares atp
    private boolean kickoffQuake = false; // Sometimes, we wanna make it go above and beyond (the goal's remaining length, that is)
    private OptionalDouble baseQuakeYaw = OptionalDouble.empty();
    private Optional<BlockPos> baseQuakePos = Optional.empty();
    private Optional<AABB> baseQuakeAabb = Optional.empty();
    private final LongOpenHashSet quakedColumns = new LongOpenHashSet();
    private final BlockPos.MutableBlockPos quakeScanPos = new BlockPos.MutableBlockPos();

    public Slopstrocity(EntityType<? extends AnimatableBoss> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 55.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.MAX_HEALTH, 500.0F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new BandaidMoveToTargetGoal(this, 1.4D)
                .satisfactoryDist(4.5D));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.1D));

        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOP_SLAM_ATTACK_ANIM), 48.8D, true, SLOP_SLAM_ATTACK_ID)
                .attackArc(360.0D)
                .potentialTargetRadius(12.0D)
                .attackFrame(22.8D, 25.0D)
                .attackTickCooldown(4.0D)
                .initiationRange(7.0D)
                .performDefaultAttack(false)
                .additionalStartConditions((animatable) -> animatable.random.nextDouble() >= 0.85D)
                .actionOnStart((animatable, target, potentialTargets, curTick) -> {
                    animatable.stopAnimation(IDLE_ANIM);
                })
                .actionOnAttack((animatable, target, potentialTargets, curTick) -> {
                    new ScreenShakeEffect(animatable.blockPosition(), 27.5D, 0.0108F, 49.5F, 1.1F).enqueue(animatable.level());

                    if (curTick == 23.0D) {
                        playSound(SlopstrocitySoundEvents.SLOPSTROCITY_SLOP_SLAM_ATTACK.get());

                        initializeQuake(4.0D, 20.0D, () -> causeAestheticEarthquake(aeOffset, 360.0F, 1.0D + (aeOffset * 0.235D)));
                    }

                    hurtTargets(animatable, target, potentialTargets);
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));
        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOP_STOMP_LEFT_ATTACK_ANIM, SLOP_STOMP_RIGHT_ATTACK_ANIM), 58.4D, true, SLOP_STOMP_ATTACK_ID)
                .attackArc(360.0D)
                .potentialTargetRadius(10.0D)
                .attackFrame(30.8D, 33.4D)
                .attackTickCooldown(4.0D)
                .initiationRange(9.0D)
                .performDefaultAttack(false)
                .additionalStartConditions((animatable) -> animatable.random.nextDouble() >= 0.84D)
                .actionOnStart((animatable, target, potentialTargets, curTick) -> {
                    animatable.stopAnimation(IDLE_ANIM);
                })
                .actionOnAttack((animatable, target, potentialTargets, curTick) -> {
                    new ScreenShakeEffect(animatable.blockPosition(), 23.5D, 0.0088F, 45.5F, 1.0F).enqueue(animatable.level());

                    if (curTick == 31.0D) {
                        playSound(SlopstrocitySoundEvents.SLOPSTROCITY_SLOP_STOMP_ATTACK.get());

                        initializeQuake(4.0D, 18.0D, () -> causeAestheticEarthquake(aeOffset, 360.0F, 1.0D + (aeOffset * 0.19D)));
                    }

                    hurtTargets(animatable, target, potentialTargets);
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));

        goalSelector.addGoal(0, new SlopstrocityRollingBlunderAttackGoal(this, 1.45D));
        goalSelector.addGoal(0, new SlopstrocityLeapChequeAttackGoal(this));

        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOPPY_CLEANUP_LEFT_ATTACK_ANIM, SLOPPY_CLEANUP_RIGHT_ATTACK_ANIM), 39.2D, true, SLOPPY_CLEANUP_ATTACK_ID)
                .attackArc(100.0D)
                .potentialTargetRadius(6.0D)
                .attackFrame(17.6D, 18.0D)
                .attackTickCooldown(4.0D)
                .initiationRange(9.0D)
                .performDefaultAttack(false)
                .additionalStartConditions((animatable) -> animatable.random.nextDouble() >= 0.91D)
                .actionOnStart((animatable, target, potentialTargets, curTick) -> {
                    animatable.stopAnimation(IDLE_ANIM);
                })
                .actionOnAttack((animatable, target, potentialTargets, curTick) -> {
                    new ScreenShakeEffect(animatable.blockPosition(), 67.9D, 0.0151F, 44.5F, 1.112F).enqueue(animatable.level());

                    initializeQuake(4.0D, 16.0D, () -> causeAestheticEarthquake(aeOffset, (float) Math.min(180.0D, aeOffset * 13.0D), 1.0D + (aeOffset * 0.15D)));

                    hurtTargets(animatable, target, potentialTargets);
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));

        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOP_SPIT_ATTACK_ANIM), 40.0D, true, SLOP_SPIT_ATTACK_ID)
                .attackArc(100.0D)
                .potentialTargetRadius(6.0D)
                .attackFrame(17.6D, 18.0D)
                .attackTickCooldown(80.0D)
                .initiationRange(16.0D)
                .additionalStartConditions((animatable) -> animatable.random.nextDouble() >= 0.92D || (animatable.getTarget() != null && animatable.getTarget().distanceTo(animatable) >= 14.0D && random.nextDouble() >= 0.35D))
                .actionOnStart((animatable, target, potentialTargets, curTick) -> {
                    animatable.stopAnimation(IDLE_ANIM);
                })
                .actionOnAttack((animatable, target, potentialTargets, curTick) -> {
                    new ScreenShakeEffect(animatable.blockPosition(), 50.9D, 0.01754F, 40.5F, 1.121F).enqueue(animatable.level());

                    playSound(SlopstrocitySoundEvents.SLOPSTROCITY_SLOP_SPIT_ATTACK.get());
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));

        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void tickClientAnimations() {
        if (!isMoving() && !isFunctionallyAnimatingAttack() && !isDeadOrDying()) playAnimation(IDLE_ANIM);

        if (isDeadOrDying()) playAnimation(DEATH_ANIM, true);
        else stopAnimation(DEATH_ANIM);
    }

    @Override
    protected void tickServerAnimations() { // Top 10 unrelated uses of overridden methods
        if (kickoffQuake) {
            if (curQuakeCall != null) curQuakeCall.run();
            if (aeOffset++ > maxAEOffset) this.kickoffQuake = false;
        }
    }

    @Override
    protected void tickGeneralClient() {

    }

    @Override
    public boolean isFunctionallyAnimatingAttack() {
        return false; // Animations layer on some masterclass timing bruh :sob: ts isn't needed rn
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    /*    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return IDLE_SOUND_EVENTS.get(random.nextInt(IDLE_SOUND_EVENTS.size())).get();
    }*/

    @Override
    protected @NotNull SoundEvent getHurtSound(DamageSource damageSource) {
        return SlopstrocitySoundEvents.SLOPSTROCITY_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SlopstrocitySoundEvents.SLOPSTROCITY_SPLOOGE.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);

        if (!isAttacking()) playSound(SlopstrocitySoundEvents.SLOPSTROCITY_STEP.get(), 0.67F, Math.min(0.45F, random.nextFloat()));
    }

    @Override
    public int getDeathDuration() {
        return 40;
    }

    @Override
    public boolean hasLineOfSight(Entity pEntity) {
        return distanceTo(pEntity) <= getAttributeValue(Attributes.FOLLOW_RANGE) && (super.hasLineOfSight(pEntity) || (Math.abs(pEntity.getY() - getY()) <= 8.0D));
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.DESTROY;
    }

    @Override
    public BossEvent getBossInfo() {
        return bossEvent;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected float getJumpPower() {
        return 0.0F;
    }

    @Override
    public boolean isAttackingStatically() {
        return super.isAttackingStatically() && getAttackId() != ROLLING_BLUNDER_ATTACK_ID && getAttackId() != LEAP_CHEQUE_ATTACK_ID;
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    public AnimationState getIdleAnimState() {
        return idleAnimState;
    }

    public AnimationState getDeathAnimState() {
        return deathAnimState;
    }

    public AnimationState getSlopSlamAttackAnimState() {
        return slopSlamAttackAnimState;
    }

    public AnimationState getSlopSpitAttackAnimState() {
        return slopSpitAttackAnimState;
    }

    public AnimationState getSlopStompLeftAttackAnimState() {
        return slopStompLeftAttackAnimState;
    }

    public AnimationState getSlopStompRightAttackAnimState() {
        return slopStompRightAttackAnimState;
    }

    public AnimationState getRollingBlunderAttackAnimState() {
        return rollingBlunderAttackAnimState;
    }

    public AnimationState getLeapChequeStartAttackAnimState() {
        return leapChequeStartAttackAnimState;
    }

    public AnimationState getLeapChequeLoopAttackAnimState() {
        return leapChequeLoopAttackAnimState;
    }

    public AnimationState getLeapChequeEndAttackAnimState() {
        return leapChequeEndAttackAnimState;
    }

    public AnimationState getSloppyCleanupLeftAttackAnimState() {
        return sloppyCleanupLeftAttackAnimState;
    }

    public AnimationState getSloppyCleanupRightAttackAnimState() {
        return sloppyCleanupRightAttackAnimState;
    }

    public double getCurrentAnimatedBlockOffset() {
        return aeOffset;
    }

    public void hurtTargets(Slopstrocity animatable, @Nullable LivingEntity target, List<LivingEntity> potentialTargets) {
        if (target != null && !target.noPhysics) hurtTargetAngularly(animatable, target);

        for (LivingEntity potentialTarget : potentialTargets) {
            if (potentialTarget == null || potentialTarget.noPhysics || potentialTarget == this || potentialTarget == target) continue;

            hurtTargetAngularly(animatable, potentialTarget);
        }
    }

    public void initializeQuake(double initialAEOffset, double maxAEOffset, Runnable quakeCall) {
        initializeQuake(initialAEOffset, maxAEOffset, quakeCall, null, null, null);
    }

    public void initializeQuake(double initialAEOffset, double maxAEOffset, Runnable quakeCall, Double baseQuakeYaw, BlockPos baseQuakePos, AABB baseQuakeAabb) {
        if (!kickoffQuake) {
            this.kickoffQuake = true;
            this.aeOffset = initialAEOffset;
            this.maxAEOffset = maxAEOffset;
            this.curQuakeCall = quakeCall;
            this.baseQuakeYaw = baseQuakeYaw == null ? OptionalDouble.empty() : OptionalDouble.of(baseQuakeYaw);
            this.baseQuakePos = Optional.ofNullable(baseQuakePos);
            this.baseQuakeAabb = Optional.ofNullable(baseQuakeAabb);

            this.quakedColumns.clear();
        }
    }

    public void causeAestheticEarthquake(double radius, float arc, double intensity) {
        Level curLevel = level();

        if (curLevel.isClientSide()) return;

        double curYaw = Math.toRadians(baseQuakeYaw.orElse(getYRot()));
        double facingX = -Math.sin(curYaw);
        double facingZ = Math.cos(curYaw);
        double centerAngle = Math.atan2(facingZ, facingX);

        double arcRad = Math.toRadians(arc);
        int steps = Math.max(1, (int) Math.ceil(arc / 10.0D));
        double bopVelocity = Math.sqrt(2.0D * 0.04D * intensity) * 1.1D;

        int centerX = baseQuakePos.map(BlockPos::getX).orElse(getBlockX());
        int centerZ = baseQuakePos.map(BlockPos::getZ).orElse(getBlockZ());
        AABB bossBox = baseQuakeAabb.orElse(getBoundingBox());

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double angle = centerAngle - arcRad / 2.0D + arcRad * t;

            int x = centerX + (int) Math.round(radius * Math.cos(angle));
            int z = centerZ + (int) Math.round(radius * Math.sin(angle));

            if (!quakedColumns.add(BlockPos.asLong(x, 0, z))) continue;

            BlockPos surface = findTopSolidBlock(curLevel, x, z);

            if (surface == null) continue;
            if (x < bossBox.maxX && x + 1.0D > bossBox.minX && z < bossBox.maxZ && z + 1.0D > bossBox.minZ) continue;

            BlockState targetState = curLevel.getBlockState(surface);

            if (targetState.isAir() || targetState.getBlock() == Blocks.BEDROCK) continue;

            boingEntitiesInColumn(curLevel, surface, intensity);

            FallingBlockEntity fallingBlock = new FallingBlockEntity(EntityType.FALLING_BLOCK, curLevel); // FallingBlockEntity#fall sets initial delta movement to 0 for whatever reason and does some other things that make it not very feasible with falling blocks that move around instantaneously the moment they spawn

            fallingBlock.blockState = targetState.hasProperty(BlockStateProperties.WATERLOGGED) ? targetState.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE) : targetState;
            fallingBlock.dropItem = false;
            fallingBlock.blocksBuilding = true;

            fallingBlock.setPos(x + 0.5D, surface.getY(), z + 0.5D);
            fallingBlock.setStartPos(fallingBlock.blockPosition());

            fallingBlock.setDeltaMovement(0.0D, bopVelocity, 0.0D);

            curLevel.setBlock(surface, targetState.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
            curLevel.addFreshEntity(fallingBlock);
        }
    }

    private void boingEntitiesInColumn(Level level, BlockPos surface, double intensity) {
        double bopHeight = Math.max(2.0D, intensity + 1.0D);
        AABB targetCol = new AABB(surface.getX() - 0.35D, surface.getY() + 0.5D, surface.getZ() - 0.35D, surface.getX() + 1.35D, surface.getY() + bopHeight, surface.getZ() + 1.35D);
        double upward = 0.55D + 0.15D * intensity;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, targetCol)) {
            if (entity == this) continue;

            Vec3 delta = entity.getDeltaMovement();
            entity.setDeltaMovement(delta.x, Math.max(delta.y, upward), delta.z);
        }
    }

    @Nullable
    private BlockPos findTopSolidBlock(Level level, int x, int z) {
        BlockPos.MutableBlockPos curPos = this.quakeScanPos.set(x, getBlockY() + 4, z);

        while (curPos.getY() > getY() - 8) {
            BlockState state = level.getBlockState(curPos);

            if (!state.isAir() && !state.getCollisionShape(level, curPos).isEmpty()) return curPos;

            curPos.move(Direction.DOWN);
        }

        return null;
    }

    private void hurtTargetAngularly(Slopstrocity animatable, @Nullable LivingEntity target) {
        target.hurt(level().damageSources().mobAttack(animatable), Math.max(5.0F, 37.5F - animatable.distanceTo(target)));

        double targetAngle = (MathUtil.getAngleBetweenEntities(animatable, target) + 90) * Math.PI / 180;
        double kbMultiplier = -2.22D;

        target.setDeltaMovement(kbMultiplier * Math.cos(targetAngle), target.getDeltaMovement().normalize().y + (random.nextDouble() * 2 + 0.2D), kbMultiplier * Math.sin(targetAngle));
    }
}
