package dev.ploats.slopstrocity.content.entity.base;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class AnimatableBoss extends AnimatableMonster {
    protected final Object2ObjectOpenHashMap<ObjectObjectImmutablePair<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, BiConsumer<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, CustomizeGuiOverlayEvent.BossEventProgress>>, Predicate<AnimatableBoss>> textureCache = new Object2ObjectOpenHashMap<>();

    protected AnimatableBoss(EntityType<? extends AnimatableMonster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public abstract BossEvent getBossInfo();

    protected ServerBossEvent createBossEvent(Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        return new ServerBossEvent(name, color, overlay) { // Borrow owner's UUID for bar ID

            @Override
            public @NotNull UUID getId() {
                return AnimatableBoss.this.getUUID();
            }
        };
    }

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

    public Object2ObjectOpenHashMap<ObjectObjectImmutablePair<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, BiConsumer<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, CustomizeGuiOverlayEvent.BossEventProgress>>, Predicate<AnimatableBoss>> getTextureCache() {
        return textureCache;
    }
}
