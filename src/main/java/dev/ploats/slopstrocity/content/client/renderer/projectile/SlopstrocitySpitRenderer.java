package dev.ploats.slopstrocity.content.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.client.model.projectile.SlopstrocitySpitModel;
import dev.ploats.slopstrocity.content.client.renderer.base.AnimatedProjectileRenderer;
import dev.ploats.slopstrocity.content.entity.projectile.SlopstrocitySpit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SlopstrocitySpitRenderer extends AnimatedProjectileRenderer<SlopstrocitySpit, SlopstrocitySpitModel> {
    private static final ResourceLocation TEXTURE_LOCATION = SlopstrocityMod.prefix("textures/entity/projectile/slopstrocity_spit/slopstrocity_spit.png");
    private static final float ROLL_DEGREES_PER_BLOCK = 20.0F;
    /**
     * The model's own forward: the shell is stretched along its local Y ({@code scaleVec(0.8, 1.4, 0.8)} on a
     * {@code 20x20x20} box), so local Y is the axis that has to be aimed down the flight path.
     */
    private static final Vector3f LOCAL_FORWARD = new Vector3f(0.0F, 1.0F, 0.0F);

    public SlopstrocitySpitRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SlopstrocitySpitModel(pContext.bakeLayer(SlopstrocitySpitModel.LAYER_LOCATION)));
    }

    @Override
    protected void applyModelRotation(SlopstrocitySpit pEntity, float pPartialTick, PoseStack pPoseStack, float wrappedYawRot, float pitchRot) {
        Vec3 movement = pEntity.getDeltaMovement();

        if (movement.lengthSqr() < 1.0E-7D) return;

        // Spin about the travel axis, driven by distance travelled so it reads as speed rather than as a fixed rate.
        float spin = (float) (movement.length() * ROLL_DEGREES_PER_BLOCK);

        // The shell is stretched along local Y, so that is the axis that has to end up down the flight path. rotationTo
        // builds the shortest rotation taking local Y onto the (normalised) movement vector, which keeps both the
        // horizontal and the vertical component of the direction. A Axis.YP + Axis.ZP pair cannot reach an arbitrary 3D
        // direction here (measured: it mapped local Y to (-0.83, 0.52, 0.19) for a movement of (-0.12, 0.86, -0.51)),
        // so no choice of signs or pivot made the tip face the movement.
        Vector3f forward = new Vector3f((float) movement.x, (float) movement.y, (float) movement.z).normalize();
        Quaternionf alignment = new Quaternionf().rotationTo(LOCAL_FORWARD, forward);

        // Roll about local Y first, in the model's own frame: the alignment then carries that axis onto the movement
        // direction, so the spin becomes a roll about the flight path instead of a wobble about whichever axis the
        // earlier turns happened to leave behind. Verified: the tip stays on the movement vector at every spin angle.
        Quaternionf roll = new Quaternionf().rotationAxis((float) Math.toRadians(spin), LOCAL_FORWARD);

        pPoseStack.mulPose(alignment.mul(roll, new Quaternionf()));
    }

    @Override
    public float getYRenderOffset(SlopstrocitySpit ownerProjectile) {
        return 1.625F;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SlopstrocitySpit entity) {
        return TEXTURE_LOCATION;
    }
}
