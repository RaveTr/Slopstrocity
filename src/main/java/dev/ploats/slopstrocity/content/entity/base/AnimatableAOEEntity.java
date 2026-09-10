package dev.ploats.slopstrocity.content.entity.base;

import dev.ploats.slopstrocity.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public abstract class AnimatableAOEEntity extends AnimatableEntity {
    private static final EntityDataAccessor<Float> CUR_RADIUS = SynchedEntityData.defineId(AnimatableAOEEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EXPANSION_SPEED = SynchedEntityData.defineId(AnimatableAOEEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RADIUS_CAP = SynchedEntityData.defineId(AnimatableAOEEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(AnimatableAOEEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_EXECUTION_INTERVAL = SynchedEntityData.defineId(AnimatableAOEEntity.class, EntityDataSerializers.INT);
    private Consumer<LivingEntity> actionOnIntersection;

    public AnimatableAOEEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        this.noPhysics = true;
        this.noCulling = true;
    }

    protected AnimatableAOEEntity(EntityType<?> pType, Level curLevel, BlockPos spawnPos, float maxRad, float expSpeed, int maxAge, int execInterv, Consumer<LivingEntity> actionOnInters) {
        this(pType, curLevel);

        this.noPhysics = true;
        this.noCulling = true;

        setMaxRadius(maxRad);
        setExpansionSpeed(expSpeed);
        setMaxAge(maxAge);
        setActionExecutionInterval(execInterv);
        setActionOnIntersection(actionOnInters);

        setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CUR_RADIUS, 0.5F);
        builder.define(EXPANSION_SPEED, 0.1F);
        builder.define(RADIUS_CAP, 6.0F);
        builder.define(ACTION_EXECUTION_INTERVAL, 20);
        builder.define(LIFETIME, 200);
    }

    public float getRadius() {
        return this.entityData.get(CUR_RADIUS);
    }

    public void setRadius(float curRadius) {
        this.entityData.set(CUR_RADIUS, Float.valueOf(curRadius));
    }

    protected void incrementRadius() {
        setRadius(getRadius() + getExpansionSpeed());
    }

    public float getExpansionSpeed() {
        return this.entityData.get(EXPANSION_SPEED);
    }

    public void setExpansionSpeed(float expansionSpeed) {
        this.entityData.set(EXPANSION_SPEED, Float.valueOf(expansionSpeed));
    }

    public float getMaxRadius() {
        return this.entityData.get(RADIUS_CAP);
    }

    public void setMaxRadius(float radiusCap) {
        this.entityData.set(RADIUS_CAP, Float.valueOf(radiusCap));
    }

    public int getMaxAge() {
        return this.entityData.get(LIFETIME);
    }

    public void setMaxAge(int lifetime) {
        this.entityData.set(LIFETIME, lifetime);
    }

    protected void setAge(int tickCount) {
        this.tickCount = tickCount;
    }

    public int getActionExecutionInterval() {
        return this.entityData.get(ACTION_EXECUTION_INTERVAL);
    }

    public void setActionExecutionInterval(int actionExecutionInterval) {
        this.entityData.set(ACTION_EXECUTION_INTERVAL, actionExecutionInterval);
    }

    public Consumer<LivingEntity> getActionOnIntersection() {
        return actionOnIntersection;
    }

    public void setActionOnIntersection(Consumer<LivingEntity> actionOnIntersection) {
        this.actionOnIntersection = actionOnIntersection;
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount >= getMaxAge()) {
            discard();
            return;
        }

        updateHitbox();
    }

    protected void updateHitbox() {
        if (getRadius() < getMaxRadius()) incrementRadius();
        if (getRadius() > getMaxRadius()) setRadius(getMaxRadius());

        if (getRadius() < 0.5F) {
            discard();
            return;
        }

        List<LivingEntity> potentialAffectedTargets = EntityUtil.getAllEntitiesAround(this, getBoundingBox().getXsize(), getBoundingBox().getYsize(), getBoundingBox().getZsize(), getBoundingBox().getSize());

        for (LivingEntity potentialTarget : potentialAffectedTargets) {
            if (actionOnIntersection == null) break;
            if (potentialTarget == null) continue;

            if ((getActionExecutionInterval() > 0 && tickCount % getActionExecutionInterval() == 0) || getActionExecutionInterval() <= 0) actionOnIntersection.accept(potentialTarget);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        setAge(pCompound.getInt("Age"));
        setRadius(pCompound.getFloat("Radius"));
        setExpansionSpeed(pCompound.getFloat("RadiusExpansionSpeed"));
        setMaxRadius(pCompound.getFloat("RadiusCap"));
        setMaxAge(pCompound.getInt("Lifetime"));
        setActionExecutionInterval(pCompound.getInt("ActionExecutionInterval"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("Age", tickCount);
        pCompound.putFloat("Radius", getRadius());
        pCompound.putFloat("RadiusExpansionSpeed", getExpansionSpeed());
        pCompound.putFloat("RadiusCap", getMaxRadius());
        pCompound.putInt("Lifetime", getMaxAge());
        pCompound.putInt("ActionExecutionInterval", getActionExecutionInterval());
    }

    @Override
    public @NotNull EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(getRadius() * 2.0F, getRadius() * 2.0F);
    }

    @Override
    public void refreshDimensions() {
        double curX = getX();
        double curY = getY();
        double curZ = getZ();

        super.refreshDimensions();

        setPos(curX, curY, curZ);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (CUR_RADIUS.equals(pKey)) refreshDimensions();
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

}