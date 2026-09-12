package dev.ploats.slopstrocity.content.entity.ai.goal.slopstrocity;

import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlopstrocityRollingBlunderAttackGoal extends Goal {
    private static final double EPSILON = 1.0E-4D;
    private static final double ROLL_START_TICK = 9.0D;
    private static final double ROLL_END_TICK = 96.0D;
    private static final double ATTACK_END_TICK = 112.0D;
    private static final double RICOCHET_MAX_ARC_DEG = 70.0D;
    private static final double RICOCHET_TARGET_BIAS = 0.3D;
    private static final int RICOCHET_COOLDOWN = 2;
    private static final double PARABOLIC_TURN_TICKS = 22.0D;
    private static final double PARABOLIC_TURN_STEP = 0.28D; // Higher = tighter parabola btw
    private static final double MIN_PROGRESS = 0.05D; // Buffer range for horizontal collision, in case the actual flag in the entity itself isn't updated within the tick
    private final Slopstrocity owner;
    private final double rollSpeedMultiplier;
    private final double minInitiationRange;
    private final double maxInitiationRange;
    private final double cooldownTicks;
    private double curCooldown;
    @Nullable
    private LivingEntity focusedTarget;
    @Nullable
    private Vec3 rollDirection;
    private double curTick;
    private double prevX;
    private double prevZ;
    private boolean pushedLastTick;
    private double ticksSinceImpact;
    private boolean turnActive;
    private int ricochetCooldown;
    private int probeGate; // 0 = probe this tick, >0 = skip and decrement (wall probe runs every other tick)
    private final BlockPos.MutableBlockPos solidProbePos = new BlockPos.MutableBlockPos();

    public SlopstrocityRollingBlunderAttackGoal(Slopstrocity owner, double rollSpeedMultiplier, double minInitiationRange, double maxInitiationRange, double cooldownTicks) {
        this.owner = owner;
        this.rollSpeedMultiplier = rollSpeedMultiplier;
        this.minInitiationRange = minInitiationRange;
        this.maxInitiationRange = maxInitiationRange;
        this.cooldownTicks = cooldownTicks;
    }

    public SlopstrocityRollingBlunderAttackGoal(Slopstrocity owner, double rollSpeedMultiplier, double cooldownTicks) {
        this(owner, rollSpeedMultiplier, 12.0D, 36.0D, cooldownTicks);
    }

    public SlopstrocityRollingBlunderAttackGoal(Slopstrocity owner, double rollSpeedMultiplier) {
        this(owner, rollSpeedMultiplier, 12.0D, 36.0D, 100.0D);
    }

    @Override
    public boolean canUse() {
        if (owner == null || !owner.isAlive() || owner.isAttacking()) return false;

        LivingEntity target = owner.getTarget();

        if (target == null || !target.isAlive() || !target.isAttackable()) return false;

        return (--curCooldown <= 0.0D) && MathUtil.isBetween(owner.distanceTo(target), minInitiationRange, maxInitiationRange) && owner.getRandom().nextDouble() >= 0.9D;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && !owner.isDeadOrDying() && curTick < ATTACK_END_TICK;
    }

    @Override
    public void start() {
        this.curTick = 0.0D;
        this.rollDirection = null;
        this.focusedTarget = owner.getTarget();

        this.prevX = owner.getX();
        this.prevZ = owner.getZ();
        this.pushedLastTick = false;
        this.ticksSinceImpact = 0.0D;
        this.turnActive = false;
        this.ricochetCooldown = 0;
        this.probeGate = 0;

        owner.setAttackId(Slopstrocity.ROLLING_BLUNDER_ATTACK_ID);

        owner.stopAnimation(Slopstrocity.IDLE_ANIM);
        owner.playAnimation(Slopstrocity.ROLLING_BLUNDER_ATTACK_ANIM, true);

        owner.getNavigation().stop();
        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void stop() {
        this.focusedTarget = null;
        this.rollDirection = null;
        this.curCooldown = cooldownTicks;

        owner.resetAttackId();

        owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);

        owner.stopAnimation(Slopstrocity.ROLLING_BLUNDER_ATTACK_ANIM);
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
        owner.getNavigation().stop(); // JIC
        curTick++;

        if (curTick == ROLL_START_TICK) new ScreenShakeEffect(owner.blockPosition(), 23.5D, 0.0158F, 24.235F, 1.0F).enqueue(owner.level());

        if (curTick <= ROLL_START_TICK) owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);
        else if (curTick < ROLL_END_TICK) {
            if (rollDirection == null) rollDirection = initialRollDirection();

            tickRolling();
        } else owner.setDeltaMovement(0.0D, owner.getDeltaMovement().y, 0.0D);
    }

    private void tickRolling() {
        LivingEntity target = currentTarget();

        if (ricochetCooldown > 0) {
            ricochetCooldown--;

            rollStraight();
        } else {
            double moved = Math.hypot(owner.getX() - prevX, owner.getZ() - prevZ);
            boolean hit = owner.horizontalCollision || (pushedLastTick && moved < MIN_PROGRESS); // We're measuring our own displacement since apparently, an entity's xo and zo aren't necessarily reliable all the time in goal ticks based on testing
            boolean wallAhead = false;

            if (!hit && rollDirection != null) {
                if (probeGate <= 0) {
                    wallAhead = isDirectionBlocked(rollDirection.x, rollDirection.z); // (Heavy voice) INCOMING!!!

                    this.probeGate = 1;
                } else probeGate--;
            }

            if (hit || wallAhead) {
                ricochet(target);

                this.ticksSinceImpact = 0.0D;
                this.turnActive = false;
                this.ricochetCooldown = RICOCHET_COOLDOWN;
                this.probeGate = 0;
            } else if (turnActive) {
                parabolicXZTurn(target);
            } else {
                ticksSinceImpact++;

                if (target != null && ticksSinceImpact > PARABOLIC_TURN_TICKS) {
                    this.turnActive = true;

                    parabolicXZTurn(target); // We're turnin' dis car around
                } else rollStraight();
            }
        }

        hitTargetsDuringRoll();

        this.prevX = owner.getX();
        this.prevZ = owner.getZ();
        this.pushedLastTick = true;
    }

    private void rollStraight() {
        if (rollDirection == null) this.rollDirection = randomHorizontalDirection();

        owner.setDeltaMovement(rollDirection.x * rollSpeedMultiplier, owner.getDeltaMovement().y, rollDirection.z * rollSpeedMultiplier);

        faceDirection(rollDirection);
    }

    private void ricochet(@Nullable LivingEntity target) {
        new ScreenShakeEffect(owner.blockPosition(), 28.5D, 0.0198F, 29.235F, 0.95F).enqueue(owner.level());

        Vec3 dir = rollDirection != null ? rollDirection : new Vec3(1.0D, 0.0D, 0.0D);
        double dx = dir.x;
        double dz = dir.z;

        double[][] reflections = {{-dx, dz}, {dx, -dz}, {-dx, -dz}}; // Iffy way for us to account for which open direction has the most potential "momentum" (if any)
        Vec3 bounce = bestOpenDirection(reflections, dx, dz);

        if (bounce == null) { // Whoopsie daisy, that's probably a corner (so just slide along the best open compass dir instead)
            double[][] axes = {{1.0D, 0.0D}, {-1.0D, 0.0D}, {0.0D, 1.0D}, {0.0D, -1.0D}};
            bounce = bestOpenDirection(axes, dx, dz);
        }

        if (bounce == null) bounce = randomHorizontalDirection();

        Vec3 targetDir = directionTo(target);
        Vec3 newDirection;

        if (targetDir != null && bounce.dot(targetDir) > 0.0D) { // Mostly a clean reflection with a tiny hint of bias towards the target position
            double r = 1.0D - RICOCHET_TARGET_BIAS;
            newDirection = new Vec3(bounce.x * r + targetDir.x * RICOCHET_TARGET_BIAS, 0.0D, bounce.z * r + targetDir.z * RICOCHET_TARGET_BIAS).normalize();
        } else newDirection = randomDirectionWithinArc(bounce); // Either we have no target or they're behind the wall, so just pick a random angle within an acute diameter of 70 degrees to ricochet to

        this.rollDirection = newDirection;

        rollStraight();
    }

    @Nullable
    private Vec3 bestOpenDirection(double[][] candidates, double dx, double dz) {
        Vec3 best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (double[] c : candidates) {
            if (isDirectionBlocked(c[0], c[1])) continue;

            double score = c[0] * dx + c[1] * dz; // Momentum preservation

            if (score > bestScore) {
                bestScore = score;
                best = new Vec3(c[0], 0.0D, c[1]).normalize();
            }
        }

        return best;
    }

    private void parabolicXZTurn(@Nullable LivingEntity target) {
        Vec3 targetDir = directionTo(target);

        if (rollDirection == null) {
            this.rollDirection = targetDir != null ? targetDir : randomHorizontalDirection();

            rollStraight();
            return;
        }

        if (targetDir == null) { // No valid target anymore, let go of the parabolic motion :(
            this.turnActive = false;

            rollStraight();
            return;
        }

        double angle = Math.acos(Mth.clamp(rollDirection.dot(targetDir), -1.0D, 1.0D));
        double step = Math.min(PARABOLIC_TURN_STEP, angle);

        if (step <= 0.01D) { // Basically aligned with the target, so just commit to the direction
            this.rollDirection = targetDir;
            this.turnActive = false;
            this.ticksSinceImpact = 0.0D; // Don't immediately re-turn while aimed straight at it
        } else this.rollDirection = rotateHorizontalTowards(rollDirection, targetDir, step);

        rollStraight();
    }

    private Vec3 rotateHorizontalTowards(Vec3 from, Vec3 to, double step) {
        double side = from.x * to.z - from.z * to.x; // Live signum reaction
        double s = side > 0.0D ? 1.0D : -1.0D;
        double cs = Math.cos(step); // Not sorry for these names btw
        double ss = Math.sin(step);
        double rx = from.x * cs - from.z * ss * s;
        double rz = from.x * ss * s + from.z * cs;

        return new Vec3(rx, 0.0D, rz).normalize();
    }

    private void faceDirection(Vec3 direction) {
        owner.setYRot((float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI) + 90.0D));
    }

    @Nullable
    private LivingEntity currentTarget() {
        if (focusedTarget != null && isValidTarget(focusedTarget)) return focusedTarget;

        this.focusedTarget = owner.getTarget();

        return isValidTarget(focusedTarget) ? focusedTarget : null;
    }

    private boolean isValidTarget(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && target.isAttackable()
                && !target.isInvulnerable()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target);
    }

    @Nullable
    private Vec3 directionTo(@Nullable LivingEntity target) {
        if (!isValidTarget(target)) return null;

        double dx = target.getX() - owner.getX();
        double dz = target.getZ() - owner.getZ();
        double length = Math.hypot(dx, dz);

        if (length < 0.5D) return null; // On top, don't steer 'em feet

        return new Vec3(dx / length, 0.0D, dz / length);
    }

    private Vec3 initialRollDirection() {
        Vec3 dirToTarget = directionTo(currentTarget());

        return dirToTarget != null ? dirToTarget : randomHorizontalDirection();
    }

    private Vec3 randomHorizontalDirection() {
        double chosenAngle = owner.getRandom().nextDouble() * Math.PI * 2.0D;

        return new Vec3(Math.cos(chosenAngle), 0.0D, Math.sin(chosenAngle));
    }

    private Vec3 randomDirectionWithinArc(Vec3 base) {
        double degrees = owner.getRandom().nextDouble() * RICOCHET_MAX_ARC_DEG - RICOCHET_MAX_ARC_DEG / 2.0D;
        double radians = degrees * Math.PI / 180.0D;
        double c = Math.cos(radians);
        double s = Math.sin(radians);

        return new Vec3(base.x * c - base.z * s, 0.0D, base.x * s + base.z * c).normalize();
    }

    private boolean isDirectionBlocked(double dirX, double dirZ) {
        double len = Math.hypot(dirX, dirZ);

        if (len < EPSILON) return false;

        double nx = dirX / len;
        double nz = dirZ / len;

        double radius = owner.getBbWidth() / 2.0D;
        double probe = radius + 0.35D; // A lil' past the leading edge
        double cx = owner.getX() + nx * probe;
        double cz = owner.getZ() + nz * probe;

        double feetY = owner.getY();
        int topBlockY = Mth.ceil(owner.getBoundingBox().maxY);
        int samples = 5;

        for (int i = 0; i < samples; i++) { // We'll do sampling across multiple points on the leading edge, not just the center, that way we can properly detect imminent collisions at an angle
            double offset = -radius + (2.0D * radius) * i / (samples - 1.0D);
            double px = cx + (-nz) * offset;
            double pz = cz + (nx) * offset;

            if (isSolidAt(px, pz, feetY, topBlockY)) return true;
        }

        return false;
    }

    private boolean isSolidAt(double x, double z, double feetY, int topBlockY) {
        Level level = owner.level();

        this.solidProbePos.set(Mth.floor(x), Mth.floor(feetY), Mth.floor(z));

        for (int blockY = Mth.floor(feetY); blockY <= topBlockY; blockY++) {
            this.solidProbePos.setY(blockY);

            BlockState state = level.getBlockState(this.solidProbePos);

            if (state.isAir()) continue; // Air's collision shape is always empty; skip the shape lookup.

            if (!state.getCollisionShape(level, this.solidProbePos).isEmpty() && (blockY + 1.0D) > feetY) return true;
        }
        return false;
    }

    private void hitTargetsDuringRoll() {
        AABB ownerBox = owner.getBoundingBox();

        // getEntitiesOfClass already only returns entities whose bounding box intersects ownerBox,
        // so filter self/invalid here and skip the redundant intersects() re-check.
        for (LivingEntity candidate : owner.level().getEntitiesOfClass(LivingEntity.class, ownerBox, candidate -> candidate != owner && isValidTarget(candidate))) {
            knockbackTarget(candidate);
        }
    }

    private void knockbackTarget(LivingEntity target) {
        if (target.noPhysics) return;

        target.hurt(owner.level().damageSources().mobAttack(owner), Math.max(5.0F, 20.5F - owner.distanceTo(target)));

        double targetAngle = (MathUtil.getAngleBetweenEntities(owner, target) + 90.0D) * Math.PI / 180.0D;
        double knockbackMultiplier = -2.22D * 0.7D;

        Vec3 targetDelta = target.getDeltaMovement();
        double baseVertical = targetDelta.lengthSqr() < EPSILON ? 0.0D : targetDelta.normalize().y;
        double upwardKick = (owner.getRandom().nextDouble() * 2.0D + 0.2D) * 0.7D;

        target.setDeltaMovement(knockbackMultiplier * Math.cos(targetAngle), baseVertical + upwardKick, knockbackMultiplier * Math.sin(targetAngle));
    }
}
