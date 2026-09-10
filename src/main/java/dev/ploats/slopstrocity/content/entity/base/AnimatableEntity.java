package dev.ploats.slopstrocity.content.entity.base;

import dev.ploats.slopstrocity.network.s2c.AnimationPlayPayload;
import dev.ploats.slopstrocity.network.s2c.AnimationStopPayload;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class AnimatableEntity extends Entity implements WrappedAnimatable {
    private final Object2ObjectOpenHashMap<String, IntObjectImmutablePair<AnimationState>> cachedAnimationStates = new Object2ObjectOpenHashMap<>();
    private boolean requiresServerAnimTicking = true;

    public AnimatableEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected abstract void tickClientAnimations();
    protected abstract void tickServerAnimations();

    protected abstract void tickGeneralClient();

    @Override
    public boolean requiresServerAnimationTicking() {
        return requiresServerAnimTicking;
    }

    @Override
    public AnimationState wrapState(String animationName, int tickDuration) {
        return cachedAnimationStates.computeIfAbsent(animationName, v -> IntObjectImmutablePair.of(tickDuration, new AnimationState())).right();
    }

    @Override
    public void playAnimation(String animationStateName, boolean forcePose) {
        IntObjectImmutablePair<AnimationState> targetAnim = getCachedAnimationStates().getOrDefault(animationStateName, null);

        if (targetAnim == null) return;
        if (!level().isClientSide()) PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, new AnimationPlayPayload(getId(), animationStateName, forcePose));
        if (targetAnim.right() != null) { // Extra guard check
            if (forcePose) getCachedAnimationStates().keySet().stream().filter(animName -> !animName.equals(animationStateName)).forEach(this::stopAnimation);

            targetAnim.right().startIfStopped(tickCount);
        }
    }

    @Override
    public void stopAnimation(String animationStateName) {
        IntObjectImmutablePair<AnimationState> targetAnim = getCachedAnimationStates().getOrDefault(animationStateName, null);

        if (targetAnim == null) return;
        if (!level().isClientSide()) PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, new AnimationStopPayload(getId(), animationStateName));
        if (targetAnim.right() != null) targetAnim.right().stop(); // Extra guard check
    }

    @Override
    public void playAnimation(AnimationState animationState, boolean forcePose) { // TODO Make this work again at some point (equality check go brrr)
        String associatedKey = getCachedAnimationStates().keySet().stream().filter(animStateName -> animationState != null && animationState == getCachedAnimationStates().get(animStateName).right()).findFirst().orElse(null);

        if (associatedKey == null) return;
        if (!level().isClientSide()) PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, new AnimationPlayPayload(getId(), associatedKey, forcePose));
        if (forcePose) getCachedAnimationStates().keySet().stream().filter(animName -> !animName.equals(associatedKey)).forEach(this::stopAnimation); // Extra guard check

        animationState.startIfStopped(tickCount);
    }

    @Override
    public void stopAnimation(AnimationState animationState) {
        String associatedKey = getCachedAnimationStates().keySet().stream().filter(animStateName -> animationState != null && animationState == getCachedAnimationStates().get(animStateName).right()).findFirst().orElse(null);

        if (associatedKey == null) return;
        if (!level().isClientSide()) PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, new AnimationStopPayload(getId(), associatedKey));

        animationState.stop(); // Extra guard check
    }


    @Override
    public Object2ObjectOpenHashMap<String, IntObjectImmutablePair<AnimationState>> getCachedAnimationStates() {
        return cachedAnimationStates;
    }

    public boolean isStuck() {
        double dx = getX() - xo;
        double dz = getZ() - zo;
        double dxSqr = dx * dx;
        double dzSqr = dz * dz;

        return dxSqr + dzSqr < getMovementThreshold();
    }

    public boolean isMoving() {
        return !isStuck();
    }

    public double getMovementThreshold() {
        return 2.500000277905201E-7;
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            tickClientAnimations();
            tickGeneralClient();
        } else {
            if (requiresServerAnimationTicking()) {
                if (cachedAnimationStates.entrySet().stream().noneMatch(curEntry -> curEntry.getValue().leftInt() > 0)) this.requiresServerAnimTicking = false;

                cachedAnimationStates.entrySet()
                        .stream()
                        .filter(curEntry -> curEntry.getValue().leftInt() > 0 && curEntry.getValue().right().isStarted())
                        .peek(curEntry -> {
                            if (curEntry.getValue().right().getAccumulatedTime() / 1000L * 20 >= curEntry.getValue().leftInt()) stopAnimation(curEntry.getKey());
                        })
                        .forEach(curEntry -> curEntry.getValue().right().updateTime(tickCount, 1.0F));
            }

            tickServerAnimations();
        }

        super.tick();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }
}