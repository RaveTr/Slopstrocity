package dev.ploats.slopstrocity.content.client.model.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.animation.boss.SlopstrocityAnimations;
import dev.ploats.slopstrocity.content.client.model.base.WrappedHierarchicalModel;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlopstrocityModel extends WrappedHierarchicalModel<Slopstrocity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(SlopstrocityMod.prefix("slopstrocity"), "main"); // Look at the imports
    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart bone;
    private final ModelPart upperbody;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hornR;
    private final ModelPart hornL;
    private final ModelPart eyes;
    private final ModelPart eyeL;
    private final ModelPart eyeM;
    private final ModelPart eyeR;
    private final ModelPart jaw;
    private final ModelPart armright;
    private final ModelPart shoulderright;
    private final ModelPart forearmright;
    private final ModelPart finger1right;
    private final ModelPart finger2right;
    private final ModelPart finger3right;
    private final ModelPart bone4;
    private final ModelPart armleft;
    private final ModelPart shoulderleft;
    private final ModelPart forearmleft;
    private final ModelPart bone3;
    private final ModelPart finger1left;
    private final ModelPart finger3left;
    private final ModelPart finger2left;
    private final ModelPart armR2;
    private final ModelPart shoulderR2;
    private final ModelPart forearmR2;
    private final ModelPart crotch;
    private final ModelPart legleft;
    private final ModelPart legright;
    private final ModelPart bone2;

    public SlopstrocityModel(ModelPart root) {
        this.root = root.getChild("root");
        this.all = this.root.getChild("all");
        this.bone = this.all.getChild("bone");

        this.upperbody = this.bone.getChild("upperbody");
        this.body = this.upperbody.getChild("body");

        this.head = this.upperbody.getChild("head");
        this.hornR = this.head.getChild("hornR");
        this.hornL = this.head.getChild("hornL");

        this.eyes = this.head.getChild("eyes");
        this.eyeL = this.eyes.getChild("eyeL");
        this.eyeM = this.eyes.getChild("eyeM");
        this.eyeR = this.eyes.getChild("eyeR");

        this.jaw = this.head.getChild("jaw");

        this.armright = this.upperbody.getChild("armright");
        this.shoulderright = this.armright.getChild("shoulderright");
        this.forearmright = this.shoulderright.getChild("forearmright");

        this.finger1right = this.forearmright.getChild("finger1right");
        this.finger2right = this.forearmright.getChild("finger2right");
        this.finger3right = this.forearmright.getChild("finger3right");

        this.bone4 = this.forearmright.getChild("bone4");

        this.armleft = this.upperbody.getChild("armleft");
        this.shoulderleft = this.armleft.getChild("shoulderleft");
        this.forearmleft = this.shoulderleft.getChild("forearmleft");

        this.bone3 = this.forearmleft.getChild("bone3");

        this.finger1left = this.forearmleft.getChild("finger1left");
        this.finger3left = this.forearmleft.getChild("finger3left");
        this.finger2left = this.forearmleft.getChild("finger2left");

        this.armR2 = this.upperbody.getChild("armR2");
        this.shoulderR2 = this.armR2.getChild("shoulderR2");
        this.forearmR2 = this.shoulderR2.getChild("forearmR2");

        this.crotch = this.bone.getChild("crotch");

        this.legleft = this.bone.getChild("legleft");
        this.legright = this.bone.getChild("legright");

        this.bone2 = this.root.getChild("bone2");
    }

    @Override
    public void setupAnim(Slopstrocity owner, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(owner, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Base
        animate(owner.getIdleAnimState(), SlopstrocityAnimations.IDLE, ageInTicks);
        animate(owner.getDeathAnimState(), SlopstrocityAnimations.SLOP_CHEQUE, ageInTicks);

        // Attack
        animate(owner.getSlopSlamAttackAnim(), SlopstrocityAnimations.SLAM, ageInTicks);
        animate(owner.getSlopSpitAttackAnim(), SlopstrocityAnimations.SPIT, ageInTicks);
        animate(owner.getSlopStompLeftAttackAnim(), SlopstrocityAnimations.STOMP_LEFT, ageInTicks);
        animate(owner.getSlopStompRightAttackAnim(), SlopstrocityAnimations.STOMP_RIGHT, ageInTicks);
        animate(owner.getRollingBlunderAttackAnim(), SlopstrocityAnimations.ROLL, ageInTicks);

        // Walk Cycle
        if (((owner.isMoving() && !owner.isFunctionallyAnimatingAttack()) || owner.hurtTime > 0) && owner.getAttackId() != Slopstrocity.ROLLING_BLUNDER_ATTACK_ID) animateWalk(SlopstrocityAnimations.WALK, limbSwing, limbSwingAmount, 1.75F, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Override
    public @Nullable ModelPart head() {
        return head;
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootPartDefinition = meshDefinition.getRoot();

        PartDefinition root = rootPartDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, -55.0F, 0.0F));
        PartDefinition bone = all.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upperbody = bone.addOrReplaceChild("upperbody", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
        PartDefinition body = upperbody.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-37.0F, -57.0F, -13.5F, 74.0F, 57.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(162, 290).addBox(-14.0F, -51.0F, 16.0F, 28.0F, 18.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition head = upperbody.addOrReplaceChild("head", CubeListBuilder.create().texOffs(206, 188).addBox(-14.0909F, -8.0F, -21.25F, 28.0F, 31.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0909F, -44.0F, -14.75F));
        PartDefinition hornR = head.addOrReplaceChild("hornR", CubeListBuilder.create().texOffs(242, 292).addBox(-19.0F, -7.0F, -8.0F, 20.0F, 13.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(306, 26).addBox(-19.0F, -22.0F, -8.0F, 11.0F, 15.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(-15.0909F, 1.0F, -8.25F));
        PartDefinition hornL = head.addOrReplaceChild("hornL", CubeListBuilder.create().texOffs(302, 0).addBox(1.0F, -6.0F, -8.0F, 20.0F, 13.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(306, 54).addBox(10.0F, -21.0F, -8.0F, 11.0F, 15.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(12.9091F, 0.0F, -8.25F));

        PartDefinition eyes = head.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(-13.0909F, 15.0F, -20.25F));
        PartDefinition eyeL = eyes.addOrReplaceChild("eyeL", CubeListBuilder.create().texOffs(208, 82).addBox(-3.0F, -4.0F, -1.25F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(26.0F, 0.0F, 0.0F));
        PartDefinition eyeM = eyes.addOrReplaceChild("eyeM", CubeListBuilder.create().texOffs(200, 134).addBox(-3.0F, -3.0F, -3.25F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(14.0F, -4.0F, 2.0F));
        PartDefinition eyeR = eyes.addOrReplaceChild("eyeR", CubeListBuilder.create().texOffs(212, 134).addBox(-1.0F, -4.0F, -1.25F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(278, 240).addBox(-14.0F, 5.0F, -22.5F, 28.0F, 10.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(278, 271).addBox(-14.0F, 12.0F, -22.5F, 28.0F, 0.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0909F, 12.0F, 1.25F));

        PartDefinition armright = upperbody.addOrReplaceChild("armright", CubeListBuilder.create(), PartPose.offset(-37.0F, -51.0F, 0.0F));
        PartDefinition shoulderright = armright.addOrReplaceChild("shoulderright", CubeListBuilder.create().texOffs(0, 138).addBox(-36.6667F, 3.8333F, -13.5F, 38.0F, 23.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(208, 0).addBox(-18.6667F, -19.1667F, -13.5F, 20.0F, 23.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(188, 240).addBox(-36.6667F, -19.1667F, -13.5F, 18.0F, 23.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.3333F, 0.1667F, -0.5F));
        PartDefinition forearmright = shoulderright.addOrReplaceChild("forearmright", CubeListBuilder.create().texOffs(86, 285).addBox(-7.8333F, 1.3333F, -8.1667F, 22.0F, 21.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(94, 188).addBox(-11.8333F, 22.3333F, -13.1667F, 29.0F, 20.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(304, 184).addBox(-9.8333F, 1.3333F, -9.1667F, 9.0F, 13.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.8333F, 25.5F, -0.3333F));

        PartDefinition finger1right = forearmright.addOrReplaceChild("finger1right", CubeListBuilder.create().texOffs(304, 215).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.3333F, 41.8333F, -4.6667F));
        PartDefinition finger2right = forearmright.addOrReplaceChild("finger2right", CubeListBuilder.create().texOffs(308, 308).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.3333F, 41.8333F, 4.3333F));
        PartDefinition finger3right = forearmright.addOrReplaceChild("finger3right", CubeListBuilder.create().texOffs(312, 98).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(7.6667F, 41.8333F, -0.6667F));

        PartDefinition bone4 = forearmright.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 275).addBox(0.0F, -25.0F, -1.5F, 0.0F, 34.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offset(3.1667F, 33.3333F, 14.3333F));

        PartDefinition armleft = upperbody.addOrReplaceChild("armleft", CubeListBuilder.create(), PartPose.offset(36.0F, -51.0F, 0.0F));
        PartDefinition shoulderleft = armleft.addOrReplaceChild("shoulderleft", CubeListBuilder.create().texOffs(130, 138).addBox(-0.3333F, 3.8333F, -13.5F, 38.0F, 23.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(94, 235).addBox(-0.3333F, -19.1667F, -13.5F, 20.0F, 23.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(260, 134).addBox(19.6667F, -19.1667F, -13.5F, 18.0F, 23.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(1.3333F, 0.1667F, -0.5F));
        PartDefinition forearmleft = shoulderleft.addOrReplaceChild("forearmleft", CubeListBuilder.create().texOffs(86, 285).mirror().addBox(-14.1667F, 1.3333F, -8.1667F, 22.0F, 21.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(200, 87).addBox(-18.1667F, 22.3333F, -13.1667F, 29.0F, 20.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(304, 184).mirror().addBox(0.8333F, 1.3333F, -9.1667F, 9.0F, 13.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(20.8333F, 25.5F, -0.3333F));

        PartDefinition bone3 = forearmleft.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 275).addBox(0.0F, -26.0F, -1.5F, 0.0F, 34.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.1667F, 34.3333F, 14.3333F));

        PartDefinition finger1left = forearmleft.addOrReplaceChild("finger1left", CubeListBuilder.create().texOffs(308, 292).addBox(-2.5F, -0.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.3333F, 41.8333F, 4.3333F));
        PartDefinition finger3left = forearmleft.addOrReplaceChild("finger3left", CubeListBuilder.create().texOffs(312, 82).addBox(-2.5F, -5.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.6667F, 46.8333F, -0.6667F));
        PartDefinition finger2left = forearmleft.addOrReplaceChild("finger2left", CubeListBuilder.create().texOffs(312, 114).addBox(-2.5F, -0.5F, -2.5F, 3.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.3333F, 41.8333F, -4.6667F));

        PartDefinition armR2 = upperbody.addOrReplaceChild("armR2", CubeListBuilder.create(), PartPose.offset(37.0F, -51.0F, 0.0F));
        PartDefinition shoulderR2 = armR2.addOrReplaceChild("shoulderR2", CubeListBuilder.create(), PartPose.offset(1.3333F, 0.1667F, -0.5F));
        PartDefinition forearmR2 = shoulderR2.addOrReplaceChild("forearmR2", CubeListBuilder.create(), PartPose.offset(20.8333F, 25.5F, -0.3333F));

        PartDefinition crotch = bone.addOrReplaceChild("crotch", CubeListBuilder.create().texOffs(208, 50).addBox(-15.0F, 1.0F, -11.0F, 28.0F, 11.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 18.0F, 1.0F));

        PartDefinition legleft = bone.addOrReplaceChild("legleft", CubeListBuilder.create().texOffs(0, 269).mirror().addBox(-11.0F, -0.5F, -10.5F, 22.0F, 29.0F, 21.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(20.0F, 26.5F, 0.5F));
        PartDefinition legright = bone.addOrReplaceChild("legright", CubeListBuilder.create().texOffs(0, 269).addBox(-11.0F, -0.5F, -10.5F, 22.0F, 29.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.0F, 26.5F, 0.5F));

        PartDefinition bone2 = root.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 461).addBox(-25.0F, -1.0F, -25.0F, 50.0F, 1.0F, 50.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }
}
