package dev.ploats.slopstrocity.mixins.worldgen;

import dev.ploats.slopstrocity.SlopstrocityMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin { // Mark my words, I'm redesigning Minecraft worldgen through abstraction the correct way later down the line thru NAPI
    @Unique
    private static final ResourceLocation slopstrocity$ID = SlopstrocityMod.prefix("slopping_arena");
    @Unique
    private static final ResourceKey<Structure> slopstrocity$KEY = ResourceKey.create(Registries.STRUCTURE, slopstrocity$ID);

    private TreeFeatureMixin() {
        throw new IllegalAccessError("Attempted to construct a Mixin Class!");
    }

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void slopstrocity$fuhOffTrees(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel curServerLevel = context.level().getLevel();

        curServerLevel.registryAccess().lookup(Registries.STRUCTURE).flatMap(lookup -> lookup.get(slopstrocity$KEY)).ifPresent(structRef -> {
            StructureManager structureManager = curServerLevel.structureManager();
            BlockPos startOriginPos = context.origin();
            StructureStart sloppingArenaStart = structureManager.getStructureAt(startOriginPos, structRef.getDelegate().value());

            if (sloppingArenaStart == StructureStart.INVALID_START) return;
            if (sloppingArenaStart.getPieces().isEmpty()) return; // Eagerly checking causes it to throw sometimes cuz of the pieces not yet being populated within the structure itself at the struct start gen point

            BoundingBox checkBox = sloppingArenaStart.getBoundingBox().inflatedBy(-18, -12, -18);
            boolean goyAway = checkBox.isInside(startOriginPos);

            if (goyAway) cir.cancel();
        });
    }
}
