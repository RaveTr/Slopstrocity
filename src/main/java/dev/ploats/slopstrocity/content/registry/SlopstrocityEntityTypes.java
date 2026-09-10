package dev.ploats.slopstrocity.content.registry;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SlopstrocityEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, SlopstrocityMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Slopstrocity>> SLOPSTROCITY = ENTITY_TYPES.register(
            "slopstrocity",
            () -> EntityType.Builder.of(Slopstrocity::new, MobCategory.MONSTER)
                    .clientTrackingRange(10)
                    .sized(6.7F, 6.7F)
                    .eyeHeight(4.36F)
                    .build(SlopstrocityMod.prefix("slopstrocity").toString())
    );
}
