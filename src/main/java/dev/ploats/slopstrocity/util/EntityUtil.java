package dev.ploats.slopstrocity.util;

import dev.ploats.slopstrocity.content.entity.base.AnimatableMonster;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EntityUtil {
    private static final double MOUTH_FORWARD_FRACTION = 0.5D;
    private static final double MOUTH_HEIGHT_FRACTION = 0.66D; // TODO Hardcoded af

    private EntityUtil() {
        throw new IllegalAccessError("Attempted to construct a Utility Class!");
    }

    public static <E extends Entity> List<E> getEntitiesAround(Entity user, Class<E> entityClass, double dX, double dY, double dZ, double radius) {
        Predicate<E> distPredicate = living -> living != user && (user.getTeam() != null && living.getTeam() != null ? !living.getTeam().equals(user.getTeam()) : living.isAlive()) && living.getClass() != user.getClass() && user.distanceTo(living) <= radius + living.getBbWidth() / 2F;
        return new ObjectArrayList<>(user.level().getEntitiesOfClass(entityClass, user.getBoundingBox().inflate(dX, dY, dZ), distPredicate));
    }

    public static <E extends Entity> List<E> getEntitiesAroundNoPredicate(LivingEntity user, Class<E> entityClass, double dX, double dY, double dZ) {
        return new ObjectArrayList<>(user.level().getEntitiesOfClass(entityClass, user.getBoundingBox().inflate(dX, dY, dZ)));
    }

    public static <E extends Entity> List<E> getEntitiesAround(Entity user, Class<E> entityClass, double dX, double dY, double dZ, Predicate<E> detectionConditions) {
        return new ObjectArrayList<>(user.level().getEntitiesOfClass(entityClass, user.getBoundingBox().inflate(dX, dY, dZ), detectionConditions));
    }

    public static List<LivingEntity> getAllEntitiesAround(Entity user, double dX, double dY, double dZ, double radius) {
        return getEntitiesAround(user, LivingEntity.class, dX, dY, dZ, radius);
    }

    public static void shoot(Entity target, double xMovement, double yMovement, double zMovement, float velocity, float inaccuracy) {
        Vec3 movementVec = new Vec3(xMovement, yMovement, zMovement).normalize().add(target.level().getRandom().triangle(0.0D, 0.0172275D * inaccuracy), target.level().getRandom().triangle(0.0D, 0.0172275D * inaccuracy), target.level().getRandom().triangle(0.0D, 0.0172275D * inaccuracy)).scale(velocity);

        target.setDeltaMovement(movementVec);

        double horizontalMovement = movementVec.horizontalDistance();

        target.setYRot((float) (Mth.atan2(movementVec.z, movementVec.x) * (180F / Math.PI)));
        target.setXRot((float) (Mth.atan2(movementVec.y, horizontalMovement) * (180F / Math.PI)));

        target.yRotO = target.getYRot();
        target.xRotO = target.getXRot();
    }

    public static void launch(Entity owner, Vec3 targetPos, double initialYOffset, double yMovementMultiplier, double horizontalOvershoot, double velocity, double inaccuracy) {
        launch(owner, targetPos, 0.5D, initialYOffset, 0.0D, yMovementMultiplier, horizontalOvershoot, velocity, inaccuracy);
    }

    public static void launch(Entity owner, Vec3 targetPos, double initialXOffset, double initialYOffset, double initialZOffset, double yMovementMultiplier, double horizontalOvershoot, double velocity, double inaccuracy) {
        Vec3 initialOffsetPos = new Vec3(initialXOffset, initialYOffset, initialZOffset).yRot(owner instanceof LivingEntity livingOwner ? -livingOwner.yBodyRot : -owner.getYRot() * ((float) Math.PI / 180.0F) - ((float) Math.PI / 2.0F));
        Vec3 initialPos = owner.position().add(initialOffsetPos.x, initialOffsetPos.y, initialOffsetPos.z);
        Vec3 overshootPos = horizontalOvershoot == 0 ? targetPos : targetPos.add(targetPos.subtract(owner.position()).normalize().scale(horizontalOvershoot));

        double xDelta = overshootPos.x - initialPos.x;
        double yDelta = overshootPos.y - initialPos.y;
        double zDelta = overshootPos.z - initialPos.z;

        double horizontalDelta = Math.sqrt(xDelta * xDelta + zDelta * zDelta);

        owner.moveTo(initialPos);

        shoot(owner, xDelta * Math.max(horizontalOvershoot, 1), yDelta + horizontalDelta * yMovementMultiplier, zDelta * Math.max(horizontalOvershoot, 1), (float) velocity, (float) inaccuracy);
    }

    public static Vec3 getMouthPos(LivingEntity spitter) {
        double yRotRad = spitter.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin((float) yRotRad), 0.0D, Mth.cos((float) yRotRad));

        return spitter.position()
                .add(forward.scale(spitter.getBbWidth() * MOUTH_FORWARD_FRACTION))
                .add(0.0D, spitter.getBbHeight() * MOUTH_HEIGHT_FRACTION, 0.0D);
    }

    public static void spitVolley(LivingEntity spitter, int projectileCount, double volleyArc, double volleyRadius, double volleyLift, double yMovementMultiplier, double velocity, double inaccuracy, Function<Vec3, Entity> projectileFactory) {
        if (spitter.level().isClientSide()) return;

        Vec3 launchPos = getMouthPos(spitter);

        int clampedCount = Mth.clamp(projectileCount, 1, 32); // Clamped so that malformed params don't turn the fan into a circle nobody asked for (or collapse a volley into a singular line) :skull:
        float clampedArc = Mth.clamp((float) volleyArc, 0.0F, 360.0F);

        for (int i = 0; i < clampedCount; i++) {
            float projectileYaw = clampedCount > 1 ? spitter.yBodyRot + ((i / (clampedCount - 1.0F)) - 0.5F) * clampedArc : spitter.yBodyRot;
            double projectileYawRadians = projectileYaw * Mth.DEG_TO_RAD;

            Vec3 aimPos = launchPos.add(-Mth.sin((float) projectileYawRadians) * volleyRadius, volleyLift, Mth.cos((float) projectileYawRadians) * volleyRadius);

            spit(spitter, launchPos, aimPos, yMovementMultiplier, velocity, inaccuracy, projectileFactory);
        }
    }

    public static void spit(LivingEntity spitter, double volleyRadius, double volleyLift, double yMovementMultiplier, double velocity, double inaccuracy, Function<Vec3, Entity> projectileFactory) {
        spitVolley(spitter, 1, 0.0D, volleyRadius, volleyLift, yMovementMultiplier, velocity, inaccuracy, projectileFactory);
    }

    public static void spit(LivingEntity spitter, Vec3 mouthPos, Vec3 aimPos, double yMovementMultiplier, double velocity, double inaccuracy, Function<Vec3, Entity> projectileFactory) {
        Entity potentialProjectile = projectileFactory.apply(mouthPos);

        if (potentialProjectile == null) return;

        double dx = aimPos.x - mouthPos.x;
        double dy = aimPos.y - mouthPos.y;
        double dz = aimPos.z - mouthPos.z;

        double horizontalDelta = Math.sqrt(dx * dx + dz * dz);

        shoot(potentialProjectile, dx, dy + horizontalDelta * yMovementMultiplier, dz, (float) velocity, (float) inaccuracy);

        spitter.level().addFreshEntity(potentialProjectile);
    }

    public static double getMeleeAttackReachSqr(Mob attackingMob, Entity targetEntity) {
        return attackingMob instanceof AnimatableMonster attackingAnimatableMonster
                ? attackingAnimatableMonster.getMeleeAttackReachSqr(targetEntity)
                : attackingMob.getBbWidth() * 2.0F * attackingMob.getBbWidth() * 2.0F + targetEntity.getBbWidth();
    }
}