package dev.ploats.slopstrocity.content.entity.ai.goal.slopstrocity;

import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.util.EntityUtil;
import dev.ploats.slopstrocity.util.MathUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlopstrocityLeapChequeAttackGoal extends Goal {
    private static final double START_ANIM_TICKS = 30.0D; // Default parameterization peaks (we have like a million OCTORS so)
    private static final double TAKEOFF_TICK = 17.2D;
    private static final double END_ANIM_TICKS = 18.4D;
    private static final double DEFAULT_LEAP_POWER = 1.8D;
    private static final double DEFAULT_LEAP_ARC = 1.05D;
    private static final double DEFAULT_ARC_START_TICK = 0.0D;
    private static final double DEFAULT_COOLDOWN_TICKS = 100.0D;
    private static final double DEFAULT_MIN_INITIATION_RANGE = 28.0D;
    private static final double DEFAULT_MAX_INITIATION_RANGE = 50.0D;
    private final Slopstrocity owner;
    private final double leapPower;
    private final double leapArc;
    private final double arcStartTick;
    private final double cooldownTicks;
    private final double minInitiationRange;
    private final double maxInitiationRange;
    private double curCooldown;
    private double curTick;
    private Phase curPhase = Phase.START;
    @Nullable
    private LivingEntity focusedTarget;
    @Nullable
    private Vec3 leapDirection;
    private boolean takeoffTriggered;
    private double arcTicks;
    private boolean prevGrounded;

    public SlopstrocityLeapChequeAttackGoal(Slopstrocity owner, double leapPower, double leapArc, double arcStartTick, double cooldownTicks, double minInitiationRange, double maxInitiationRange) {
        this.owner = owner;
        this.leapPower = leapPower;
        this.leapArc = leapArc;
        this.arcStartTick = arcStartTick;
        this.cooldownTicks = cooldownTicks;
        this.minInitiationRange = minInitiationRange;
        this.maxInitiationRange = maxInitiationRange;
    }

    public SlopstrocityLeapChequeAttackGoal(Slopstrocity owner, double leapPower, double leapArc, double arcStartTick, double cooldownTicks) {
        this(owner, leapPower, leapArc, arcStartTick, cooldownTicks, DEFAULT_MIN_INITIATION_RANGE, DEFAULT_MAX_INITIATION_RANGE);
    }

    public SlopstrocityLeapChequeAttackGoal(Slopstrocity owner, double leapPower, double leapArc, double arcStartTick) {
        this(owner, leapPower, leapArc, arcStartTick, DEFAULT_COOLDOWN_TICKS);
    }

    public SlopstrocityLeapChequeAttackGoal(Slopstrocity owner, double leapPower, double leapArc) {
        this(owner, leapPower, leapArc, DEFAULT_ARC_START_TICK);
    }

    public SlopstrocityLeapChequeAttackGoal(Slopstrocity owner) {
        this(owner, DEFAULT_LEAP_POWER, DEFAULT_LEAP_ARC);
    }

    @Override
    public boolean canUse() {
        if (owner == null || !owner.isAlive() || owner.isAttacking()) return false;

        LivingEntity target = owner.getTarget();

        if (target == null || !target.isAlive() || !target.isAttackable()) return false;

        return (--curCooldown <= 0.0D) && MathUtil.isBetween(owner.distanceTo(target), minInitiationRange, maxInitiationRange) && owner.getRandom().nextDouble() >= 0.85D;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || owner.isDeadOrDying()) return false;

        return curPhase != Phase.END || curTick < END_ANIM_TICKS;
    }

    @Override
    public void start() {
        this.curTick = 0.0D;
        this.curPhase = Phase.START;
        this.focusedTarget = owner.getTarget();
        this.leapDirection = null;
        this.takeoffTriggered = false;
        this.arcTicks = 0.0D;
        this.prevGrounded = owner.onGround();

        owner.setAttackId(Slopstrocity.LEAP_CHEQUE_ATTACK_ID);

        owner.stopAnimation(Slopstrocity.IDLE_ANIM);
        owner.playAnimation(Slopstrocity.LEAP_CHEQUE_START_ATTACK_ANIM, true);

        owner.getNavigation().stop();
        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void stop() {
        this.focusedTarget = null;
        this.leapDirection = null;
        this.curCooldown = cooldownTicks;

        owner.resetAttackId();
        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);

        owner.stopAnimation(Slopstrocity.LEAP_CHEQUE_START_ATTACK_ANIM);
        owner.stopAnimation(Slopstrocity.LEAP_CHEQUE_LOOP_ATTACK_ANIM);
        owner.stopAnimation(Slopstrocity.LEAP_CHEQUE_END_ATTACK_ANIM);
        owner.playAnimation(Slopstrocity.IDLE_ANIM, true);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return owner.isDeadOrDying();
    }

    @Override
    public void tick() {
        this.curTick += 1.0D;

        switch (curPhase) {
            case START -> tickStart();
            case LOOP -> tickLoop();
            case END -> tickEnd();
        }

        this.prevGrounded = owner.onGround();
    }

    private void tickStart() {
        owner.getNavigation().stop();

        if (!takeoffTriggered && curTick >= TAKEOFF_TICK) {
            this.takeoffTriggered = true;

            takeoff();
        }

        if (takeoffTriggered) tickAirborne();
        else owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);

        if (curTick >= START_ANIM_TICKS) beginLoop();
    }

    private void takeoff() {
        LivingEntity target = currentTarget();

        this.leapDirection = directionTo(target);

        if (leapDirection == null) this.leapDirection = randomHorizontalDirection();

        faceDirection(leapDirection);
        owner.setDeltaMovement(0.0D, leapPower, 0.0D);

        this.arcTicks = 0.0D;

        new ScreenShakeEffect(owner.blockPosition(), 55.0D, 0.0086F, 64.0F, 1.35F).enqueue(owner.level());

        owner.initializeQuake(3.0D, 10.0D, () -> owner.causeAestheticEarthquake(owner.getCurrentAnimatedBlockOffset(), 360.0F, 1.0D + (owner.getCurrentAnimatedBlockOffset() * 0.15D)), (double) owner.getYRot(), owner.blockPosition(), owner.getBoundingBox());
    }

    private void beginLoop() {
        this.curPhase = Phase.LOOP;
        this.curTick = 0.0D;

        owner.stopAnimation(Slopstrocity.LEAP_CHEQUE_START_ATTACK_ANIM);
        owner.playAnimation(Slopstrocity.LEAP_CHEQUE_LOOP_ATTACK_ANIM, true);
    }

    private void tickLoop() {
        tickAirborne();
    }

    private void tickAirborne() {
        owner.getNavigation().stop();

        if (owner.onGround() && !prevGrounded) {
            land();

            return;
        }

        if (arcTicks >= arcStartTick && leapDirection != null) {
            owner.setDeltaMovement(leapDirection.x * leapArc, owner.getDeltaMovement().y, leapDirection.z * leapArc);
            faceDirection(leapDirection);
        } else owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);

        this.arcTicks += 1.0D;
    }

    private void land() {
        this.curPhase = Phase.END;
        this.curTick = 0.0D;

        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);

        owner.stopAnimation(Slopstrocity.LEAP_CHEQUE_LOOP_ATTACK_ANIM);
        owner.playAnimation(Slopstrocity.LEAP_CHEQUE_END_ATTACK_ANIM, true);

        new ScreenShakeEffect(owner.blockPosition(), 70.5D, 0.0046F, 87.12F, 1.4F).enqueue(owner.level());

        owner.initializeQuake(3.0D, 15.0D, () -> owner.causeAestheticEarthquake(owner.getCurrentAnimatedBlockOffset(), 360.0F, 1.0D + (owner.getCurrentAnimatedBlockOffset() * 0.2D)), (double) owner.getYRot(), owner.blockPosition(), owner.getBoundingBox());
        owner.hurtTargets(owner, owner.getTarget(), EntityUtil.getAllEntitiesAround(owner, 6.0D, 6.0D, 6.0D, 5.55D).stream().filter(entity -> !owner.isAlliedTo(entity) && !entity.isAlliedTo(owner) && owner.getClass() != entity.getClass() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)).toList());
    }

    private void tickEnd() {
        owner.getNavigation().stop();
        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);
    }

    private void faceDirection(Vec3 direction) {
        owner.setYRot((float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI) - 90.0D));
    }

    @Nullable
    private LivingEntity currentTarget() {
        if (focusedTarget != null && isValidTarget(focusedTarget)) return focusedTarget;

        this.focusedTarget = owner.getTarget();

        return isValidTarget(focusedTarget) ? focusedTarget : null;
    }

    private boolean isValidTarget(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && target.isAttackable() && !target.isInvulnerable() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target);
    }

    @Nullable
    private Vec3 directionTo(@Nullable LivingEntity target) {
        if (!isValidTarget(target)) return null;

        double dx = target.getX() - owner.getX();
        double dz = target.getZ() - owner.getZ();
        double length = Math.hypot(dx, dz);

        if (length < 0.5D) return null;

        return new Vec3(dx / length, 0.0D, dz / length);
    }

    private Vec3 randomHorizontalDirection() {
        double angle = owner.getRandom().nextDouble() * Math.PI * 2.0D;

        return new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
    }

    private enum Phase {
        START,
        LOOP,
        END
    }
}
