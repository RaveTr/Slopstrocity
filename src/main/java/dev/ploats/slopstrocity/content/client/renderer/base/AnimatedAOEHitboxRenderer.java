package dev.ploats.slopstrocity.content.client.renderer.base;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ploats.slopstrocity.content.entity.base.AnimatableAOEEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public abstract class AnimatedAOEHitboxRenderer<AAOEE extends AnimatableAOEEntity, AAOEEM extends EntityModel<AAOEE>> extends AnimatedEntityRenderer<AAOEE, AAOEEM> {

    protected AnimatedAOEHitboxRenderer(EntityRendererProvider.Context pContext, AAOEEM parentModel) {
        super(pContext, parentModel);
    }

    @Override
    public void render(AAOEE pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (scaleByHitbox(pEntity)) pPoseStack.scale(getXScale(pEntity), getYScale(pEntity), getZScale(pEntity));

        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    public float getXScale(AAOEE owner) {
        return owner.getRadius();
    }

    public float getYScale(AAOEE owner) {
        return owner.getRadius();
    }

    public float getZScale(AAOEE owner) {
        return owner.getRadius();
    }

    public boolean scaleByHitbox(AAOEE owner) {
        return false;
    }
}