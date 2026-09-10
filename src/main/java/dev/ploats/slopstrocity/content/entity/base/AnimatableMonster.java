package dev.ploats.slopstrocity.content.entity.base;

import dev.ploats.slopstrocity.content.entity.ai.controller.body.BandaidBodyRotationControl;
import dev.ploats.slopstrocity.content.entity.ai.controller.movement.ReinforcedMoveControl;
import dev.ploats.slopstrocity.content.entity.ai.pathnav.DirectGroundPathNavigation;
import dev.ploats.slopstrocity.network.s2c.AnimationPlayPayload;
import dev.ploats.slopstrocity.network.s2c.AnimationStopPayload;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class AnimatableMonster extends Monster implements WrappedAnimatable, WrappedMonster {
    private static final EntityDataAccessor<Byte> ATTACK_ID = SynchedEntityData.defineId(AnimatableMonster.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> ATTACK_TICK = SynchedEntityData.defineId(AnimatableMonster.class, EntityDataSerializers.FLOAT); // Mainly used for client-side updates based on tracked server data. Actual attack behaviour is handled within goals.
    private final Object2ObjectOpenHashMap<String, IntObjectImmutablePair<AnimationState>> cachedAnimationStates = new Object2ObjectOpenHashMap<>();
    public static final byte NO_ATTACK_ID = 0;
    protected int customDeathTime = 0;
    protected float yDeathRot = 0.0F;
    private boolean requiresServerAnimTicking = true;

    protected AnimatableMonster(EntityType<? extends AnimatableMonster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        this.moveControl = new ReinforcedMoveControl(this);
    }

    protected abstract void tickClientAnimations();
    protected abstract void tickServerAnimations();

    protected abstract void tickGeneralClient();

    public abstract boolean isFunctionallyAnimatingAttack();

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
    protected @NotNull BodyRotationControl createBodyControl() {
        return new BandaidBodyRotationControl(this);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level pLevel) {
        return new DirectGroundPathNavigation(this, pLevel);
    }

    public boolean canBeKnockedBack() {
        return true;
    }

    public int getDeathDuration() {
        return 20;
    }

    public boolean useCustomDeathTime() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(ATTACK_ID, NO_ATTACK_ID);
        builder.define(ATTACK_TICK, 0.0F);
    }

    @Override
    public byte getAttackId() {
        return this.entityData.get(ATTACK_ID);
    }

    @Override
    public void setAttackId(byte attackId) {
        this.entityData.set(ATTACK_ID, attackId);
    }

    @Override
    public void resetAttackId() {
        setAttackId(NO_ATTACK_ID);
    }

    @Override
    public boolean isAttacking() {
        return getAttackId() != NO_ATTACK_ID && !isDeadOrDying();
    }

    @Override
    public boolean isAttackingStatically() {
        return getAttackId() != NO_ATTACK_ID;
    }

    @Override
    public float getAttackTick() {
        return this.entityData.get(ATTACK_TICK);
    }

    @Override
    public void setAttackTick(float attackTick) {
        this.entityData.set(ATTACK_TICK, Math.max(0.0F, attackTick));
    }

    @Override
    public void resetAttackTick() {
        setAttackTick(0.0F);
    }

    @Override
    public void incrementAttackTick() {
        setAttackTick(getAttackTick() + 1.0F);
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

    public double getCustomDeathTime() {
        return customDeathTime;
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
    protected void tickDeath() {
        setYRot(yDeathRot);
        setYHeadRot(yDeathRot);

        if (useCustomDeathTime()) customDeathTime++;
        else deathTime++;

        if ((useCustomDeathTime() ? customDeathTime : deathTime) >= getDeathDuration() && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, EntityEvent.POOF);
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() != null && !canBeKnockedBack()) {
            super.hurt(pSource, pAmount);
            return false;
        }

        return super.hurt(pSource, pAmount);
    }

    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);

        this.yDeathRot = getYRot();
    }

    @Override
    public boolean isPushable() {
        return canBeKnockedBack();
    }

    @Override
    public void push(double pX, double pY, double pZ) {
        if (!canBeKnockedBack()) return;
        super.push(pX, pY, pZ);
    }

    @Override
    public void knockback(double pStrength, double pRatioX, double pRatioZ) {
        if (!canBeKnockedBack()) return;
        super.knockback(pStrength, pRatioX, pRatioZ);
    }

    @Override
    public Map<String, IntObjectImmutablePair<AnimationState>> getCachedAnimationStates() {
        return cachedAnimationStates;
    }

    public double getMeleeAttackReachSqr(Entity targetEntity) {
        return getBbWidth() * 2.0F * getBbWidth() * 2.0F + targetEntity.getBbWidth();
    }
}