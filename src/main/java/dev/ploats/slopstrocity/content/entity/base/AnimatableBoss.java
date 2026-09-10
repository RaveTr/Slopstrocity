package dev.ploats.slopstrocity.content.entity.base;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatableBoss extends AnimatableMonster {

    protected AnimatableBoss(EntityType<? extends AnimatableMonster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public abstract BossEvent getBossInfo();

    public void updateBossInfo() {
        if (getBossInfo() != null) {
            getBossInfo().setProgress(getHealth() / getMaxHealth());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pServerPlayer) {
        super.startSeenByPlayer(pServerPlayer);

        if (getBossInfo() instanceof ServerBossEvent serverBossInfo) serverBossInfo.addPlayer(pServerPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pServerPlayer) {
        super.stopSeenByPlayer(pServerPlayer);

        if (getBossInfo() instanceof ServerBossEvent serverBossInfo) serverBossInfo.removePlayer(pServerPlayer);
    }

    @Override
    public boolean canBeKnockedBack() {
        return false;
    }

    @Override
    public void tick() {
        updateBossInfo();

        super.tick();
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);

        if (getBossInfo() != null) getBossInfo().setName(getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    protected boolean canRide(Entity pVehicle) {
        return false;
    }
}
