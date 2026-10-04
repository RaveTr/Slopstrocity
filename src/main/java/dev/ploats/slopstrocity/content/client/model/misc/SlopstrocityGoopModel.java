package dev.ploats.slopstrocity.content.client.model.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.model.base.WrappedHierarchicalModel;
import dev.ploats.slopstrocity.content.entity.misc.SlopstrocityGoop;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlopstrocityGoopModel extends WrappedHierarchicalModel<SlopstrocityGoop> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(SlopstrocityMod.prefix("slopstrocity_goop"), "main");
    private final ModelPart root;
    private final ModelPart splat;

    public SlopstrocityGoopModel(ModelPart root) {
        // Translucent (not emissive): the fade rides on the vertex alpha, and the emissive shader does not blend it.
        super(RenderType::entityTranslucent);

        this.root = root.getChild("root");
        this.splat = this.root.getChild("splat");
    }

    @Override
    public void setupAnim(SlopstrocityGoop pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public @Nullable ModelPart head() {
        return null;
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootPartDefinition = meshDefinition.getRoot();

        // The splat is authored lying flat in the XZ plane facing +Y, and is centred on the anchor (the anchor sits in
        // the middle of the footprint at "ground" level) so that rotating the model onto a block face keeps it centred
        // on the impact point. It is given a small real thickness rather than being a zero-height plane: a flat plane
        // has nothing to squash, and it would vanish when viewed edge-on after being stood up against a wall.
        PartDefinition root = rootPartDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition splat = root.addOrReplaceChild("splat", CubeListBuilder.create().texOffs(0, 0).addBox(-40.0F, -0.5F, -40.0F, 80.0F, 1.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }
}
