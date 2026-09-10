package dev.ploats.slopstrocity.content.entity.boss;

import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import dev.ploats.slopstrocity.content.entity.ai.goal.hostile.AnimatableAttackGoal;
import dev.ploats.slopstrocity.content.entity.ai.goal.hostile.BandaidMoveToTargetGoal;
import dev.ploats.slopstrocity.content.entity.base.AnimatableBoss;
import dev.ploats.slopstrocity.content.registry.SlopstrocitySoundEvents;
import dev.ploats.slopstrocity.util.MathUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class Slopstrocity extends AnimatableBoss {
    public static final byte SLOP_SLAM_ATTACK_ID = 1;
    public static final byte SLOP_SPIT_ATTACK_ID = 2;
    public static final byte SLOP_STOMP_ATTACK_ID = 3;
    public static final byte ROLLING_BLUNDER_ATTACK_ID = 4;
    public static final String IDLE_ANIM = "Idle";
    public static final String DEATH_ANIM = "Death";
    public static final String SLOP_SLAM_ATTACK_ANIM = "Slop Slam Attack";
    public static final String SLOP_SPIT_ATTACK_ANIM = "Slop Spit Attack";
    public static final String SLOP_STOMP_LEFT_ATTACK_ANIM = "Slop Stomp Attack (Left)";
    public static final String SLOP_STOMP_RIGHT_ATTACK_ANIM = "Slop Stomp Attack (Right)";
    public static final String ROLLING_BLUNDER_ATTACK_ANIM = "Rolling Blunder Attack";
    private static final List<DeferredHolder<SoundEvent, ? extends SoundEvent>> IDLE_SOUND_EVENTS = SlopstrocitySoundEvents.SOUND_EVENTS.getEntries().stream()
            .filter(soundEventDeferredHolder -> soundEventDeferredHolder.getRegisteredName().contains("slopstrocity_idle_"))
            .collect(Collectors.toCollection(ObjectArrayList::new));
    private final AnimationState idleAnimState = wrapState(IDLE_ANIM);
    private final AnimationState deathAnimState = wrapState(DEATH_ANIM);
    private final AnimationState slopSlamAttackAnim = wrapState(SLOP_SLAM_ATTACK_ANIM);
    private final AnimationState slopSpitAttackAnim = wrapState(SLOP_SPIT_ATTACK_ANIM);
    private final AnimationState slopStompLeftAttackAnim = wrapState(SLOP_STOMP_LEFT_ATTACK_ANIM);
    private final AnimationState slopStompRightAttackAnim = wrapState(SLOP_STOMP_RIGHT_ATTACK_ANIM);
    private final AnimationState rollingBlunderAttackAnim = wrapState(ROLLING_BLUNDER_ATTACK_ANIM);
    protected final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.slopstrocity.slopstrocity"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    public Slopstrocity(EntityType<? extends AnimatableBoss> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.MAX_HEALTH, 500.0F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new BandaidMoveToTargetGoal(this, 1.4D)
                .satisfactoryDist(3.5D));
        goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.1D));

        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOP_SLAM_ATTACK_ANIM), 48.8D, true, SLOP_SLAM_ATTACK_ID)
                .attackArc(360.0D)
                .potentialTargetRadius(12.0D)
                .attackFrame(22.8D, 25.0D)
                .attackTickCooldown(4.0D)
                .initiationRange(7.0D)
                .performDefaultAttack(false)
                .additionalStartConditions((animatable) -> animatable.random.nextDouble() >= 0.9D)
                .actionOnStart((animatable, target, potentialTargets, curTick) -> {
                    animatable.stopAnimation(IDLE_ANIM);
                })
                .actionOnAttack((animatable, target, potentialTargets, curTick) -> {
                    new ScreenShakeEffect(animatable.blockPosition(), 27.5D, 0.0108F, 49.5F, 1.1F).enqueue(animatable.level());

                    if (curTick == 23.0D) playSound(SlopstrocitySoundEvents.SLOPSTROCITY_SLOP_SLAM_ATTACK.get());

                    hurtTargets(animatable, target, potentialTargets);
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));

        goalSelector.addGoal(0, new AnimatableAttackGoal<>(this, ObjectArrayList.of(SLOP_STOMP_LEFT_ATTACK_ANIM, SLOP_STOMP_RIGHT_ATTACK_ANIM), 58.4D, true, SLOP_STOMP_ATTACK_ID)
                .attackArc(360.0D)
                .potentialTargetRadius(12.0D)
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

                    if (curTick == 31.0D) playSound(SlopstrocitySoundEvents.SLOPSTROCITY_SLOP_STOMP_ATTACK.get());

                    hurtTargets(animatable, target, potentialTargets);
                })
                .actionOnEnd((animatable, target, potentialTargets, curTick) -> animatable.playAnimation(IDLE_ANIM, true)));

        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private void hurtTargets(Slopstrocity animatable, @Nullable LivingEntity target, List<LivingEntity> potentialTargets) {
        if (target != null && !target.noPhysics) hurtTargetAngularly(animatable, target);

        for (LivingEntity potentialTarget : potentialTargets) {
            if (potentialTarget == null || potentialTarget.noPhysics || potentialTarget == this || potentialTarget == target) continue;

            hurtTargetAngularly(animatable, potentialTarget);
        }
    }

    private void hurtTargetAngularly(Slopstrocity animatable, @Nullable LivingEntity target) {
        target.hurt(level().damageSources().mobAttack(animatable), Math.min(5.0F, 37.5F - animatable.distanceTo(target)));

        double targetAngle = (MathUtil.getAngleBetweenEntities(animatable, target) + 90) * Math.PI / 180;
        double kbMultiplier = -2.22D;

        target.setDeltaMovement(kbMultiplier * Math.cos(targetAngle), target.getDeltaMovement().normalize().y + (random.nextDouble() * 2 + 0.2D), kbMultiplier * Math.sin(targetAngle));
    }

    @Override
    protected void tickClientAnimations() {
        if (!isMoving() && !isFunctionallyAnimatingAttack() && !isDeadOrDying()) playAnimation(IDLE_ANIM);

        if (isDeadOrDying()) playAnimation(DEATH_ANIM, true);
        else stopAnimation(DEATH_ANIM);
    }

    @Override
    protected void tickServerAnimations() {

    }

    @Override
    protected void tickGeneralClient() {

    }

    @Override
    public boolean isFunctionallyAnimatingAttack() {
        return false;
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

        playSound(SlopstrocitySoundEvents.SLOPSTROCITY_STEP.get(), 0.67F, Math.min(0.45F, random.nextFloat()));
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
        return super.isAttackingStatically() && getAttackId() != ROLLING_BLUNDER_ATTACK_ID;
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

    public AnimationState getSlopSlamAttackAnim() {
        return slopSlamAttackAnim;
    }

    public AnimationState getSlopSpitAttackAnim() {
        return slopSpitAttackAnim;
    }

    public AnimationState getSlopStompLeftAttackAnim() {
        return slopStompLeftAttackAnim;
    }

    public AnimationState getSlopStompRightAttackAnim() {
        return slopStompRightAttackAnim;
    }

    public AnimationState getRollingBlunderAttackAnim() {
        return rollingBlunderAttackAnim;
    }
}
