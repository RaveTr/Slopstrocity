package dev.ploats.slopstrocity.content.entity.misc;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.entity.base.AnimatableEntity;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SlopstrocityGoop extends AnimatableEntity { // No animations fn, but we'll extend AE anyway JIC
    public static final AttributeModifier GOOP_MOVEMENT_SLOWDOWN = new AttributeModifier(SlopstrocityMod.prefix("goop_slowdown"), -0.55F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SlopstrocityGoop.class, EntityDataSerializers.INT);

    public SlopstrocityGoop(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFETIME, 150);
    }

    public int getLifetime() {
        return entityData.get(LIFETIME);
    }

    public void setLifetime(int totalLifetime) {
        entityData.set(LIFETIME, totalLifetime);
    }

    @Override
    protected void tickClientAnimations() {

    }

    @Override
    protected void tickServerAnimations() { // Yh idrc (look at ts in Slopstrocity)
        for (LivingEntity potentialGoopTarget : EntityUtil.getAllEntitiesAround(this, 0, 0, 0, getBoundingBox().getSize())) {
            if (potentialGoopTarget == null || potentialGoopTarget instanceof Slopstrocity || !potentialGoopTarget.isAlive() || !potentialGoopTarget.isAttackable() || potentialGoopTarget.noPhysics || !potentialGoopTarget.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) continue;

            AttributeInstance movementSpeedAttrInst = potentialGoopTarget.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

            if (!movementSpeedAttrInst.hasModifier(GOOP_MOVEMENT_SLOWDOWN.id())) movementSpeedAttrInst.addTransientModifier(GOOP_MOVEMENT_SLOWDOWN);
        }

        if (tickCount > getLifetime()) discard();
    }

    @Override
    protected void tickGeneralClient() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setLifetime(compoundTag.getInt("Lifetime"));

        this.tickCount = compoundTag.getInt("TicksExisted");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Lifetime", getLifetime());
        compoundTag.putInt("TicksExisted", tickCount);
    }
}
