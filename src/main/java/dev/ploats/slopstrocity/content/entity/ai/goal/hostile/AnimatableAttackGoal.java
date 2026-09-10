package dev.ploats.slopstrocity.content.entity.ai.goal.hostile;

import dev.ploats.slopstrocity.api.misc.QuadConsumer;
import dev.ploats.slopstrocity.content.entity.base.WrappedAnimatable;
import dev.ploats.slopstrocity.content.entity.base.WrappedMonster;
import dev.ploats.slopstrocity.util.EntityUtil;
import dev.ploats.slopstrocity.util.MathUtil;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class AnimatableAttackGoal<AO extends Mob & WrappedAnimatable> extends Goal {
    protected final AO animatableOwner;
    protected final ObjectArrayList<String> attackAnimations;
    protected final double attackTickDuration;
    protected final boolean staticAttack;
    protected final byte attackId;
    protected final DoubleDoubleMutablePair attackFrame = DoubleDoubleMutablePair.of(2.0D, 4.0D);
    @Nullable
    protected QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnStart;
    @Nullable
    protected QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnEnd;
    @Nullable
    protected QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnAttack;
    @Nullable
    protected QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnTick;
    @Nullable
    protected AnimationState selectedAttackAnimation;
    protected String selectedAttackAnimationName = "";
    protected double potentialTargetRadius = 3.0D;
    protected boolean usePreciseTicking = true;
    protected double preciseAttackTick = 0.0D;
    protected double attackArc = 100.0D;
    protected double initiationRange = Double.MAX_VALUE;
    protected double attackTickCooldown = 20.0D;
    protected double attackCooldownTimer = 0.0D;
    protected boolean performDefaultAttack = true;
    @Nullable
    protected Predicate<AO> additionalStartConditions;
    @Nullable
    protected Double2DoubleFunction attackTickDurationFunction;
    protected double chosenTickDuration;

    public AnimatableAttackGoal(AO animatableOwner, ObjectArrayList<String> attackAnimations, double attackTickDuration, boolean staticAttack, byte attackId) {
        this.animatableOwner = animatableOwner;
        this.attackAnimations = attackAnimations;
        this.attackTickDuration = attackTickDuration;
        this.staticAttack = staticAttack;
        this.attackId = attackId;
    }

    public AnimatableAttackGoal<AO> actionOnStart(QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnStart) {
        this.actionOnStart = actionOnStart;
        return this;
    }

    public AnimatableAttackGoal<AO> actionOnEnd(QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnEnd) {
        this.actionOnEnd = actionOnEnd;
        return this;
    }

    public AnimatableAttackGoal<AO> actionOnAttack(QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnAttack) {
        this.actionOnAttack = actionOnAttack;
        return this;
    }

    public AnimatableAttackGoal<AO> attackFrame(double start, double end) {
        this.attackFrame.left(start);
        this.attackFrame.right(end);

        return this;
    }

    public AnimatableAttackGoal<AO> potentialTargetRadius(double potentialTargetRadius) {
        this.potentialTargetRadius = potentialTargetRadius;
        return this;
    }

    public AnimatableAttackGoal<AO> usePreciseTicking(boolean usePreciseTicking) {
        this.usePreciseTicking = usePreciseTicking;
        return this;
    }

    public AnimatableAttackGoal<AO> attackArc(double attackArc) {
        this.attackArc = attackArc;
        return this;
    }

    public AnimatableAttackGoal<AO> initiationRange(double initiationRange) {
        this.initiationRange = initiationRange;
        return this;
    }

    public AnimatableAttackGoal<AO> attackTickCooldown(double attackTickCooldown) {
        this.attackTickCooldown = attackTickCooldown;
        return this;
    }

    public AnimatableAttackGoal<AO> actionOnTick(QuadConsumer<AO, @Nullable LivingEntity, List<LivingEntity>, Double> actionOnTick) {
        this.actionOnTick = actionOnTick;
        return this;
    }

    public AnimatableAttackGoal<AO> performDefaultAttack(boolean performDefaultAttack) {
        this.performDefaultAttack = performDefaultAttack;
        return this;
    }

    public AnimatableAttackGoal<AO> additionalStartConditions(Predicate<AO> additionalStartConditions) {
        this.additionalStartConditions = additionalStartConditions;
        return this;
    }

    public AnimatableAttackGoal<AO> applyToPreciseDuration(Double2DoubleFunction attackTickDurationFunction) {
        this.attackTickDurationFunction = attackTickDurationFunction;
        return this;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = animatableOwner.getTarget();
        double validRange = initiationRange != Double.MAX_VALUE ? initiationRange * initiationRange : target == null ? animatableOwner.getBbWidth() / 2.0D : EntityUtil.getMeleeAttackReachSqr(animatableOwner, target);

        return ((attackCooldownTimer = Math.max(--attackCooldownTimer, 0.0D)) <= 0.0D) && animatableOwner.isAlive() && (!(animatableOwner instanceof WrappedMonster monsterOwner) || !monsterOwner.isAttacking()) && target != null && target.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) && !target.isInvulnerable() && animatableOwner.distanceToSqr(target) <= validRange && (additionalStartConditions == null || additionalStartConditions.test(animatableOwner));
    }

    @Override
    public boolean canContinueToUse() {
        double currentAttackTick = usePreciseTicking ? preciseAttackTick : Math.floor((double) selectedAttackAnimation.getAccumulatedTime() / 1000 * 20.0D);

        return animatableOwner.isAlive() && (attackAnimations.isEmpty() || selectedAttackAnimation != null) && currentAttackTick < chosenTickDuration;
    }

    @Override
    public void start() {
        this.chosenTickDuration = attackTickDurationFunction != null ? attackTickDurationFunction.applyAsDouble(attackTickDuration) : attackTickDuration;

        LivingEntity target = animatableOwner.getTarget();
        String selectedAnimName = attackAnimations.isEmpty() ? "" : attackAnimations.get(animatableOwner.getRandom().nextInt(attackAnimations.size()));

        this.selectedAttackAnimation = attackAnimations.isEmpty() ? null : animatableOwner.getCachedAnimationStates().get(selectedAnimName).right();
        this.selectedAttackAnimationName = selectedAnimName;
        this.preciseAttackTick = 0.0D;

        if (animatableOwner instanceof WrappedMonster monsterOwner) {
            monsterOwner.setAttackId(attackId);
            monsterOwner.setAttackTick(0.0F);
        }

        if (selectedAttackAnimation != null) {
            animatableOwner.stopAnimation(selectedAttackAnimationName);
            animatableOwner.playAnimation(selectedAttackAnimation, false);
        }

        if (actionOnStart != null) actionOnStart.accept(animatableOwner, target, EntityUtil.getAllEntitiesAround(animatableOwner, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius).stream().filter(entity -> !animatableOwner.isAlliedTo(entity) && !entity.isAlliedTo(animatableOwner) && animatableOwner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList(), 0.0D);
        if (staticAttack) {
            animatableOwner.setYRot(animatableOwner.yRotO);
            animatableOwner.getNavigation().stop();
            animatableOwner.setDeltaMovement(animatableOwner.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        }
    }

    @Override
    public void stop() {
        if (actionOnEnd != null) actionOnEnd.accept(animatableOwner, animatableOwner.getTarget(), EntityUtil.getAllEntitiesAround(animatableOwner, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius).stream().filter(entity -> !animatableOwner.isAlliedTo(entity) && !entity.isAlliedTo(animatableOwner) && animatableOwner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList(), chosenTickDuration);

        if (selectedAttackAnimation != null) animatableOwner.stopAnimation(selectedAttackAnimationName);
        if (animatableOwner instanceof WrappedMonster monsterOwner) {
            monsterOwner.resetAttackId();
            monsterOwner.resetAttackTick();
        }

        this.preciseAttackTick = 0.0D;
        this.selectedAttackAnimation = null;
        this.selectedAttackAnimationName = "";
        this.attackCooldownTimer = attackTickCooldown;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return animatableOwner.isDeadOrDying();
    }

    @Override
    public void tick() {
        LivingEntity target = animatableOwner.getTarget();

        if (staticAttack) {
            animatableOwner.setYRot(animatableOwner.yRotO);
            animatableOwner.getNavigation().stop();
            animatableOwner.setDeltaMovement(animatableOwner.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        }

        if (usePreciseTicking) this.preciseAttackTick++;

        double currentAttackTick = usePreciseTicking || selectedAttackAnimation == null ? preciseAttackTick : (double) selectedAttackAnimation.getAccumulatedTime() / 1000 * 20.0D;

        if (target != null && !staticAttack && ((!performDefaultAttack && actionOnAttack == null) || !MathUtil.isBetween(currentAttackTick, attackFrame.leftDouble(), attackFrame.rightDouble()))) animatableOwner.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (actionOnTick != null) actionOnTick.accept(animatableOwner, target, EntityUtil.getAllEntitiesAround(animatableOwner, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius).stream().filter(entity -> !animatableOwner.isAlliedTo(entity) && !entity.isAlliedTo(animatableOwner) && animatableOwner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList(), currentAttackTick);
        if (actionOnAttack != null && MathUtil.isBetween(currentAttackTick, attackFrame.leftDouble(), attackFrame.rightDouble())) actionOnAttack.accept(animatableOwner, target, EntityUtil.getAllEntitiesAround(animatableOwner, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius).stream().filter(entity -> !animatableOwner.isAlliedTo(entity) && !entity.isAlliedTo(animatableOwner) && animatableOwner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList(), currentAttackTick);
        if (performDefaultAttack && target != null) {
            List<LivingEntity> potentialTargets = EntityUtil.getAllEntitiesAround(animatableOwner, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius, potentialTargetRadius).stream().filter(entity -> !animatableOwner.isAlliedTo(entity) && !entity.isAlliedTo(animatableOwner) && animatableOwner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList();

            for (LivingEntity potentialTarget : potentialTargets) {
                if (animatableOwner.isAlliedTo(potentialTarget) || animatableOwner.getClass() == potentialTarget.getClass()) continue;

                double targetAngle = MathUtil.getRelativeAngleBetweenEntities(animatableOwner, potentialTarget);
                double attackAngle = animatableOwner.yBodyRot % 360;

                if (targetAngle < 0) targetAngle += 360;
                if (attackAngle < 0) attackAngle += 360;

                double relativeHitAngle = targetAngle - attackAngle;

                if (MathUtil.isBetween(currentAttackTick, attackFrame.leftDouble(), attackFrame.rightDouble()) && animatableOwner.distanceToSqr(potentialTarget) <= potentialTargetRadius * potentialTargetRadius && MathUtil.isWithinAngleRestriction(relativeHitAngle, attackArc)) {
                    animatableOwner.doHurtTarget(potentialTarget);
                }
            }
        }

        // Hardcoded for now
        if (animatableOwner instanceof WrappedMonster monsterOwner) monsterOwner.setAttackTick((float) preciseAttackTick);
    }
}