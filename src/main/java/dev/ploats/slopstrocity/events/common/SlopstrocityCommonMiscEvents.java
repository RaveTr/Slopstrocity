package dev.ploats.slopstrocity.events.common;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.entity.misc.SlopstrocityGoop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = SlopstrocityMod.MOD_ID)
public class SlopstrocityCommonMiscEvents {

    @SubscribeEvent
    public static void onPostEntityTickEvent(EntityTickEvent.Post event) {
        Entity targetEntity = event.getEntity();

        if (targetEntity instanceof LivingEntity targetLivingEntity) {
            Level curLevel = targetLivingEntity.level();

            if (!curLevel.isClientSide() && targetLivingEntity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED) && curLevel.getEntities(targetLivingEntity, targetLivingEntity.getBoundingBox()).stream().map(Entity::getClass).noneMatch(SlopstrocityGoop.class::isInstance)) {
                AttributeInstance movementSpeedAttrInst = targetLivingEntity.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
                ResourceLocation goopSlowdownModId = SlopstrocityGoop.GOOP_MOVEMENT_SLOWDOWN.id();

                if (movementSpeedAttrInst.hasModifier(goopSlowdownModId)) movementSpeedAttrInst.removeModifier(goopSlowdownModId);
            }
        }
    }
}
