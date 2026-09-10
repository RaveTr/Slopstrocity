package dev.ploats.slopstrocity.events.common;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.content.registry.SlopstrocityEntityTypes;
import dev.ploats.slopstrocity.network.s2c.AnimationPlayPayload;
import dev.ploats.slopstrocity.network.s2c.AnimationStopPayload;
import dev.ploats.slopstrocity.network.s2c.ScreenShakePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SlopstrocityMod.MOD_ID)
public class SlopstrocityCommonSetupEvents {

    @SubscribeEvent
    public static void onRegisterPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar payloadRegistrar = event.registrar("1");

        payloadRegistrar.playToClient(
                AnimationPlayPayload.TYPE,
                AnimationPlayPayload.STREAM_CODEC,
                AnimationPlayPayload::handle
        );
        payloadRegistrar.playToClient(
                AnimationStopPayload.TYPE,
                AnimationStopPayload.STREAM_CODEC,
                AnimationStopPayload::handle
        );

        payloadRegistrar.playToClient(
                ScreenShakePayload.TYPE,
                ScreenShakePayload.STREAM_CODEC,
                ScreenShakePayload::handle
        );
    }

    @SubscribeEvent
    public static void onEntityAttributesCreationEvent(EntityAttributeCreationEvent event) {
        event.put(SlopstrocityEntityTypes.SLOPSTROCITY.get(), Slopstrocity.createAttributes().build());
    }
}
