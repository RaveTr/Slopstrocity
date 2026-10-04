package dev.ploats.slopstrocity.content.registry;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.worldgen.structure_processor.SloppingArenaMossifierProcessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SlopstrocityStructureProcessorsTypes {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES = DeferredRegister.create(BuiltInRegistries.STRUCTURE_PROCESSOR, SlopstrocityMod.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<SloppingArenaMossifierProcessor>> SLOPPING_ARENA_MOSSIFIER = STRUCTURE_PROCESSOR_TYPES.register("slopping_arena_mossifier", () -> () -> SloppingArenaMossifierProcessor.CODEC);
}
