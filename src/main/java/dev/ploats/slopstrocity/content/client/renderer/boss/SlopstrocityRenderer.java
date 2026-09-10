package dev.ploats.slopstrocity.content.client.renderer.boss;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.model.boss.SlopstrocityModel;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SlopstrocityRenderer extends MobRenderer<Slopstrocity, SlopstrocityModel> {
    private static final ResourceLocation TEXTURE_LOCATION = SlopstrocityMod.prefix("textures/entity/slopstrocity/slopstrocity.png");

    public SlopstrocityRenderer(EntityRendererProvider.Context context) {
        super(context, new SlopstrocityModel(context.bakeLayer(SlopstrocityModel.LAYER_LOCATION)), 0.7F);

        this.shadowStrength = 0.85F;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Slopstrocity slopstrocity) {
        return TEXTURE_LOCATION;
    }
}
