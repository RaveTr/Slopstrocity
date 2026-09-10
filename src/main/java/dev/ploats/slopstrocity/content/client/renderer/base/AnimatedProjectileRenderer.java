package dev.ploats.slopstrocity.content.client.renderer.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedProjectileRenderer<P extends Projectile, EM extends EntityModel<P>> extends EntityRenderer<P> implements RenderLayerParent<P, EM> {
    protected final EM parentModel;
    protected final ObjectArrayList<RenderLayer<P, EM>> renderLayers = new ObjectArrayList<>();

    protected AnimatedProjectileRenderer(EntityRendererProvider.Context pContext, EM parentModel) {
        super(pContext);
        this.parentModel = parentModel;
    }

    @Override
    public void render(P pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.scale(-1.0F, -1.0F, 1.0F);

        float wrappedYawRot = Mth.lerp(pPartialTick, pEntity.yRotO, pEntity.getYRot());
        float pitchRot = Mth.lerp(pPartialTick, pEntity.xRotO, pEntity.getXRot());
        float offsetAge = pEntity.tickCount + pPartialTick;

        parentModel.setupAnim(pEntity, 0.0F, 0.0F, offsetAge, wrappedYawRot, pitchRot);

        pPoseStack.translate(0.0D, -1.55F, 0.0D);

        pPoseStack.mulPose(Axis.YP.rotationDegrees(wrappedYawRot));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(pitchRot));

        boolean isVisible = isVisible(pEntity);
        boolean isTranslucent = isTranslucent(pEntity);
        boolean isGlowing = isGlowing(pEntity);
        RenderType curRenderType = getRenderType(pEntity, isVisible, isTranslucent, isGlowing);

        if (curRenderType != null) {
            VertexConsumer resultVertex = pBuffer.getBuffer(curRenderType);

            parentModel.renderToBuffer(pPoseStack, resultVertex, pPackedLight, getOverlayTextureCoords(pEntity), getPackedColor(pEntity, isTranslucent));
        }

        if (!renderLayers.isEmpty()) renderLayers.forEach(curLayer -> curLayer.render(pPoseStack, pBuffer, pPackedLight, pEntity, 0.0F,0.0F, pPartialTick, offsetAge, wrappedYawRot, pitchRot));

        pPoseStack.popPose();

        baseRender(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    public void baseRender(P pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    public int getPackedColor(P owner, boolean isTranslucent) {
        return FastColor.ARGB32.colorFromFloat(getRed(owner, isTranslucent), getGreen(owner, isTranslucent), getBlue(owner, isTranslucent), getAlpha(owner, isTranslucent));
    }

    public float getAlpha(P owner, boolean isTranslucent) {
        return isTranslucent ? 0.15F : 1.0F;
    }

    public float getRed(P owner, boolean isTranslucent) {
        return 1.0F;
    }

    public float getGreen(P owner, boolean isTranslucent) {
        return 1.0F;
    }

    public float getBlue(P owner, boolean isTranslucent) {
        return 1.0F;
    }

    public int getOverlayTextureCoords(P owner) { // Allow for polymorphic behaviour :trol:
        return OverlayTexture.NO_OVERLAY;
    }

    public boolean isVisible(P owner) {
        return !owner.isInvisible();
    }

    public boolean isTranslucent(P owner) {
        return !isVisible(owner) && !owner.isInvisibleTo(Minecraft.getInstance().player);
    }

    public boolean isGlowing(P owner) {
        return Minecraft.getInstance().shouldEntityAppearGlowing(owner);
    }

    @Nullable
    protected RenderType getRenderType(P owner, boolean isVisible, boolean isTranslucent, boolean isGlowing) {
        ResourceLocation parentTexLoc = getTextureLocation(owner);

        return isTranslucent
                ? RenderType.itemEntityTranslucentCull(parentTexLoc)
                : isVisible
                ? parentModel.renderType(parentTexLoc)
                : isGlowing
                ? RenderType.outline(parentTexLoc)
                : null;
    }

    public void addRenderLayer(RenderLayer<P, EM> layer) {
        renderLayers.add(layer);
    }

    @Override
    public @NotNull EM getModel() {
        return parentModel;
    }
}