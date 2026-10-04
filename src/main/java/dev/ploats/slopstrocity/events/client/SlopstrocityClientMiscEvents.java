package dev.ploats.slopstrocity.events.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import dev.ploats.slopstrocity.content.entity.base.AnimatableBoss;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = SlopstrocityMod.MOD_ID, value = Dist.CLIENT)
public class SlopstrocityClientMiscEvents {
    private static final String BOSSBAR_TEXTURE_DIR = "textures/gui/bossbar/";
    private static final String BOSSBAR_OVERLAY_TEXTURE_SUFFIX = "/overlay.png";
    private static final String BOSSBAR_SHELL_TEXTURE_SUFFIX = "/shell.png";
    private static final String BOSSBAR_FILLING_TEXTURE_SUFFIX = "/filling.png";
    private static final int BOSSBAR_WIDTH = 200;
    private static final int BOSSBAR_HEIGHT = 64;
    private static final int BOSSBAR_FRAME_TOP = 2;
    private static final int BOSSBAR_DRAWN_HEIGHT = BOSSBAR_HEIGHT - BOSSBAR_FRAME_TOP;
    private static final int BOSSBAR_FILL_START = 13;
    private static final int BOSSBAR_FILL_END = 190;
    private static final int BOSSBAR_VERTICAL_SPACING = -30;
    private static final int BOSSBAR_VERTICAL_SIZE = BOSSBAR_DRAWN_HEIGHT + BOSSBAR_VERTICAL_SPACING;
    private static final Object2ObjectOpenHashMap<ResourceLocation, Triple<ResourceLocation, ResourceLocation, ResourceLocation>> MAPPED_BOSSBAR_CACHE = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onComputeCameraAnglesEvent(ViewportEvent.ComputeCameraAngles event) {
        if (ScreenShakeEffect.getEnqueuedShakes().isEmpty()) return;

        Camera mainCam = event.getCamera();

        ScreenShakeEffect.getEnqueuedShakes().removeIf(shake -> shake == null || shake.data().getDuration() <= 0);

        for (ScreenShakeEffect shake : ScreenShakeEffect.getEnqueuedShakes()) {
            if (shake != null && shake.data().getDuration() > 0 && !Minecraft.getInstance().isPaused()) {
                shake.data().setDuration(shake.data().getDuration() - 1);

                if (mainCam.getEntity().distanceToSqr(Vec3.atCenterOf(shake.data().getOriginPos())) <= Math.pow(shake.data().getRange(), 2)) {
                    float delta = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                    float ticksExistedDelta = (float) mainCam.getEntity().tickCount + delta;

                    double distance = mainCam.getEntity().getEyePosition().distanceTo(Vec3.atCenterOf(shake.data().getOriginPos()));
                    float distanceScale = Math.max(0, 1 - (float) (distance / shake.data().getRange()));

                    float finalAmp = (float) ((shake.data().getMagnitude() * (shake.data().getDuration() / (shake.data().getFadeOut() == 0 ? 1 : shake.data().getFadeOut()))) * Math.pow(distanceScale, 2)); // Avoid division by 0

                    event.setYaw((float) (event.getYaw() + finalAmp * Math.cos(ticksExistedDelta * 5.0F + 1.0F) * 25.0F));
                    event.setPitch((float) (event.getPitch() + finalAmp * Math.cos(ticksExistedDelta * 3.0F + 2.0F) * 25.0D));
                    event.setRoll((float) (event.getRoll() + finalAmp * Math.cos(ticksExistedDelta * 4.0F) * 25.0D));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListenersEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> MAPPED_BOSSBAR_CACHE.clear());
    }

    @SubscribeEvent
    public static void onRenderBossBarEvent(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (event.isCanceled()) return;

        Minecraft curMCInstance = Minecraft.getInstance();
        ClientLevel curLevel = curMCInstance.level;

        if (curLevel == null) return;

        LerpingBossEvent curTrackedBossEvent = event.getBossEvent();
        Entity trackedBossOwner = curLevel.getEntities().get(curTrackedBossEvent.getId());

        if (trackedBossOwner == null) return;

        if (trackedBossOwner instanceof AnimatableBoss trackedAnimatableBoss) {
            ObjectObjectImmutablePair<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, BiConsumer<Triple<ResourceLocation, ResourceLocation, ResourceLocation>, CustomizeGuiOverlayEvent.BossEventProgress>> curEntry = trackedAnimatableBoss.getTextureCache().object2ObjectEntrySet().stream()
                    .filter(entry -> entry.getValue().test(trackedAnimatableBoss))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (curEntry != null) {
                curEntry.right().accept(curEntry.left(), event);
                event.setCanceled(true);

                return;
            }
        }

        Triple<ResourceLocation, ResourceLocation, ResourceLocation> barTexture = MAPPED_BOSSBAR_CACHE.computeIfAbsent(BuiltInRegistries.ENTITY_TYPE.getKey(trackedBossOwner.getType()), SlopstrocityClientMiscEvents::resolveBossBarTextures);

        if (barTexture.getMiddle() == null && barTexture.getRight() == null) return;

        renderBossBar(barTexture, event);
    }

    private static void renderBossBar(Triple<ResourceLocation, ResourceLocation, ResourceLocation> barTexture, CustomizeGuiOverlayEvent.BossEventProgress event) {
        ResourceLocation overlayTexture = barTexture.getLeft(); // Optional
        ResourceLocation shellTexture = barTexture.getMiddle();
        ResourceLocation fillingTexture = barTexture.getRight();

        GuiGraphics guiGraphics = event.getGuiGraphics();
        float progress = Mth.clamp(event.getBossEvent().getProgress(), 0.0F, 1.0F);

        int barX = guiGraphics.guiWidth() / 2 - BOSSBAR_WIDTH / 2;
        int barY = event.getY() - 10;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (fillingTexture != null && progress > 0.0F) {
            int fillWidth = Math.round(BOSSBAR_FILL_START + progress * (BOSSBAR_FILL_END - BOSSBAR_FILL_START));

            guiGraphics.blit(fillingTexture, barX, barY, 0, BOSSBAR_FRAME_TOP, fillWidth, BOSSBAR_DRAWN_HEIGHT, BOSSBAR_WIDTH, BOSSBAR_HEIGHT);
        }

        if (shellTexture != null) guiGraphics.blit(shellTexture, barX, barY, 0, BOSSBAR_FRAME_TOP, BOSSBAR_WIDTH, BOSSBAR_DRAWN_HEIGHT, BOSSBAR_WIDTH, BOSSBAR_HEIGHT);
        if (overlayTexture != null) guiGraphics.blit(overlayTexture, barX, barY, 0, BOSSBAR_FRAME_TOP, BOSSBAR_WIDTH, BOSSBAR_DRAWN_HEIGHT, BOSSBAR_WIDTH, BOSSBAR_HEIGHT);

        RenderSystem.disableBlend();

        Minecraft curMCInstance = Minecraft.getInstance();
        Component barDisplayName = event.getBossEvent().getName();

   //     guiGraphics.drawString(curMCInstance.font, barDisplayName, guiGraphics.guiWidth() / 2 - curMCInstance.font.width(barDisplayName) / 2, barY + 17 - curMCInstance.font.lineHeight, BOSSBAR_LABEL_COLOR);

        event.setIncrement(BOSSBAR_VERTICAL_SIZE);
        event.setCanceled(true);
    }

    private static Triple<ResourceLocation, ResourceLocation, ResourceLocation> resolveBossBarTextures(ResourceLocation entityTypeId) {
        ResourceLocation barDirectory = entityTypeId.withPrefix(BOSSBAR_TEXTURE_DIR);

        return Triple.of(
                resolveBossBarTexture(barDirectory.withSuffix(BOSSBAR_OVERLAY_TEXTURE_SUFFIX)),
                resolveBossBarTexture(barDirectory.withSuffix(BOSSBAR_SHELL_TEXTURE_SUFFIX)),
                resolveBossBarTexture(barDirectory.withSuffix(BOSSBAR_FILLING_TEXTURE_SUFFIX)));
    }

    @Nullable
    private static ResourceLocation resolveBossBarTexture(ResourceLocation candidate) {
        return Minecraft.getInstance().getResourceManager().getResource(candidate).isPresent() ? candidate : null;
    }
}
