package dev.ploats.slopstrocity.content.worldgen.structure_processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.ploats.slopstrocity.content.registry.SlopstrocityStructureProcessorsTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SloppingArenaMossifierProcessor extends BlockAgeProcessor {
    public static final MapCodec<SloppingArenaMossifierProcessor> CODEC = Codec.FLOAT
            .fieldOf("mossiness")
            .xmap(SloppingArenaMossifierProcessor::new, mossifierProcessor -> mossifierProcessor.mossiness);

    public SloppingArenaMossifierProcessor(float mossiness) {
        super(mossiness);
    }

    @Override
    protected @Nullable BlockState maybeReplaceFullStoneBlock(RandomSource random) {
        if (random.nextFloat() >= 0.5F) return null;
        else {
            BlockState[] standardStates = new BlockState[]{
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.COBBLESTONE.defaultBlockState()
            };
            BlockState[] mossyStates = new BlockState[]{
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState()
            };

            return getRandomBlock(random, standardStates, mossyStates);
        }
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return SlopstrocityStructureProcessorsTypes.SLOPPING_ARENA_MOSSIFIER.get();
    }
}
