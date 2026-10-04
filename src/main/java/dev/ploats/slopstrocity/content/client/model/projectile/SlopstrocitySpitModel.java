package dev.ploats.slopstrocity.content.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.animation.projectile.SlopstrocitySpitAnimations;
import dev.ploats.slopstrocity.content.client.model.base.WrappedHierarchicalModel;
import dev.ploats.slopstrocity.content.entity.projectile.SlopstrocitySpit;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlopstrocitySpitModel extends WrappedHierarchicalModel<SlopstrocitySpit> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(SlopstrocityMod.prefix("slopstrocity_spit"), "main");
	private final ModelPart root; // Had to programmatically add ts because ModelPart resolution breaks without a proper root from which you can retrieve bones, even if there's only 1 root bruh
	private final ModelPart bone;

	public SlopstrocitySpitModel(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
	}

	@Override
	public void setupAnim(SlopstrocitySpit pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

		// Base
		applyStatic(SlopstrocitySpitAnimations.STRETCHED);

		animate(pEntity.getSplatAnimState(), SlopstrocitySpitAnimations.HIT, pAgeInTicks);
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

		PartDefinition bone = rootPartDefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -10.0F, -10.0F, 20.0F, 20.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshDefinition, 64, 64);
	}
}