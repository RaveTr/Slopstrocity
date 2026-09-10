package dev.ploats.slopstrocity.util;

import dev.ploats.slopstrocity.content.entity.base.AnimatableMonster;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public final class EntityUtil {

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
        Vec3 initialOffsetPos = new Vec3(0.5D, initialYOffset, 0.0D).yRot(owner instanceof LivingEntity livingOwner ? -livingOwner.yBodyRot : -owner.getYRot() * ((float) Math.PI / 180.0F) - ((float) Math.PI / 2.0F));
        Vec3 initialPos = owner.position().add(initialOffsetPos.x, initialOffsetPos.y, initialOffsetPos.z);
        Vec3 overshootPos = horizontalOvershoot == 0 ? targetPos : targetPos.add(targetPos.subtract(owner.position()).normalize().scale(horizontalOvershoot));

        double xDelta = overshootPos.x - initialPos.x;
        double yDelta = overshootPos.y - initialPos.y;
        double zDelta = overshootPos.z - initialPos.z;

        double horizontalDelta = Math.sqrt(xDelta * xDelta + zDelta * zDelta);

        owner.moveTo(initialPos);

        shoot(owner, xDelta * Math.max(horizontalOvershoot, 1), yDelta + horizontalDelta * yMovementMultiplier, zDelta * Math.max(horizontalOvershoot, 1), (float) velocity, (float) inaccuracy);
    }

    public static double getMeleeAttackReachSqr(Mob attackingMob, Entity targetEntity) {
        return attackingMob instanceof AnimatableMonster attackingAnimatableMonster
                ? attackingAnimatableMonster.getMeleeAttackReachSqr(targetEntity)
                : attackingMob.getBbWidth() * 2.0F * attackingMob.getBbWidth() * 2.0F + targetEntity.getBbWidth();
    }
}