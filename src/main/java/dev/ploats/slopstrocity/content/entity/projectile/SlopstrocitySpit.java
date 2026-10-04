package dev.ploats.slopstrocity.content.entity.projectile;

import dev.ploats.slopstrocity.content.entity.base.AnimatableHurtingProjectile;
import dev.ploats.slopstrocity.content.entity.misc.SlopstrocityGoop;
import dev.ploats.slopstrocity.content.registry.SlopstrocityEntityTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SlopstrocitySpit extends AnimatableHurtingProjectile {
    public static final String SPLAT_ANIM = "Splat";
    private final AnimationState splatAnimState = wrapState(SPLAT_ANIM, 4);

    public SlopstrocitySpit(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SlopstrocitySpit(double x, double y, double z, Level level) {
        super(SlopstrocityEntityTypes.SLOPSTROCITY_SPIT.get(), x, y, z, level);
    }

    public SlopstrocitySpit(double x, double y, double z, Vec3 movement, Level level) {
        super(SlopstrocityEntityTypes.SLOPSTROCITY_SPIT.get(), x, y, z, movement, level);
    }

    public SlopstrocitySpit(LivingEntity owner, Vec3 movement, Level level) {
        super(SlopstrocityEntityTypes.SLOPSTROCITY_SPIT.get(), owner, movement, level);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02D;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide()) {
            setNoGravity(true);
            playAnimation(SPLAT_ANIM, true);

            SlopstrocityGoop goop = SlopstrocityEntityTypes.SLOPSTROCITY_GOOP.get().create(level());

            if (goop != null) { // Somhow (JIC)
                goop.setLifetime(random.nextInt(150, 500));
                goop.setPos(position().x, Math.floor(position().y), position().z);

                level().addFreshEntity(goop);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity targetEntity = result.getEntity();

        if (!level().isClientSide() && targetEntity != null && targetEntity.isAlive()) {
            targetEntity.hurt(level().damageSources().mobProjectile(this, getOwner() instanceof LivingEntity livingOwner ? livingOwner : null), random.nextInt(10, 22));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (amount >= 20.0F) {

        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void tickClientAnimations() {

    }

    @Override
    protected void tickServerAnimations() {
        if (splatAnimState.getAccumulatedTime() >= 210) discard();
    }

    @Override
    protected void tickGeneralClient() {

    }

    public AnimationState getSplatAnimState() {
        return splatAnimState;
    }
}
