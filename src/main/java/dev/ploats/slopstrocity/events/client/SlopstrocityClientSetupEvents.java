package dev.ploats.slopstrocity.events.client;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.model.boss.SlopstrocityModel;
import dev.ploats.slopstrocity.content.client.model.misc.SlopstrocityGoopModel;
import dev.ploats.slopstrocity.content.client.model.projectile.SlopstrocitySpitModel;
import dev.ploats.slopstrocity.content.client.renderer.boss.SlopstrocityRenderer;
import dev.ploats.slopstrocity.content.client.renderer.misc.SlopstrocityGoopRenderer;
import dev.ploats.slopstrocity.content.client.renderer.projectile.SlopstrocitySpitRenderer;
import dev.ploats.slopstrocity.content.registry.SlopstrocityEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SlopstrocityMod.MOD_ID)
public class SlopstrocityClientSetupEvents {

    @SubscribeEvent
    public static void onRegisterEntityLayerDefinitionsEvent(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SlopstrocityModel.LAYER_LOCATION, SlopstrocityModel::createBodyLayer);

        event.registerLayerDefinition(SlopstrocitySpitModel.LAYER_LOCATION, SlopstrocitySpitModel::createBodyLayer);

        event.registerLayerDefinition(SlopstrocityGoopModel.LAYER_LOCATION, SlopstrocityGoopModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderersEvent(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SlopstrocityEntityTypes.SLOPSTROCITY.get(), SlopstrocityRenderer::new);

        event.registerEntityRenderer(SlopstrocityEntityTypes.SLOPSTROCITY_SPIT.get(), SlopstrocitySpitRenderer::new);

        event.registerEntityRenderer(SlopstrocityEntityTypes.SLOPSTROCITY_GOOP.get(), SlopstrocityGoopRenderer::new);
    }
}
