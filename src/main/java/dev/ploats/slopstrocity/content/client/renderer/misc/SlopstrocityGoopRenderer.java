package dev.ploats.slopstrocity.content.client.renderer.misc;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.model.misc.SlopstrocityGoopModel;
import dev.ploats.slopstrocity.content.client.renderer.base.AnimatedEntityRenderer;
import dev.ploats.slopstrocity.content.entity.misc.SlopstrocityGoop;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SlopstrocityGoopRenderer extends AnimatedEntityRenderer<SlopstrocityGoop, SlopstrocityGoopModel> {
    private static final ResourceLocation TEXTURE_LOCATION = SlopstrocityMod.prefix("textures/entity/misc/slopstrocity_goop/slopstrocity_goop.png");

    public SlopstrocityGoopRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SlopstrocityGoopModel(pContext.bakeLayer(SlopstrocityGoopModel.LAYER_LOCATION)));
    }

    @Override
    public float getYRenderOffset(SlopstrocityGoop owner) {
        return 1.51F;
    }

    @Override
    public float getAlpha(SlopstrocityGoop owner, boolean isTranslucent) {
        boolean pendingDisappearance = owner.tickCount >= owner.getLifetime() - 80;

        return pendingDisappearance
                ? Math.max(0.0F, (owner.getLifetime() - owner.tickCount) / 80.0F)
                : Math.min(1.0F, owner.tickCount / 40.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SlopstrocityGoop owner) {
        return TEXTURE_LOCATION;
    }
}
