package dev.ploats.slopstrocity;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Slopstrocity.MODID)
public class Slopstrocity {
    public static final String MODID = "slopstrocity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Slopstrocity(IEventBus modEventBus, ModContainer modContainer) {
    }
}
