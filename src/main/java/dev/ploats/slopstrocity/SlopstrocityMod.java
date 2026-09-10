package dev.ploats.slopstrocity;

import com.mojang.logging.LogUtils;
import dev.ploats.slopstrocity.content.registry.SlopstrocityEntityTypes;
import dev.ploats.slopstrocity.content.registry.SlopstrocitySoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Locale;

@Mod(SlopstrocityMod.MOD_ID)
public class SlopstrocityMod { // The entity class' name would've been the exact same, and I CBA to keep looking at fully-qualified imports for any class that uses them both
    public static final String MOD_ID = "slopstrocity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SlopstrocityMod(IEventBus modEventBus, ModContainer modContainer) {
        SlopstrocityEntityTypes.ENTITY_TYPES.register(modEventBus);
        SlopstrocitySoundEvents.SOUND_EVENTS.register(modEventBus);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path.toLowerCase(Locale.ROOT));
    }
}
