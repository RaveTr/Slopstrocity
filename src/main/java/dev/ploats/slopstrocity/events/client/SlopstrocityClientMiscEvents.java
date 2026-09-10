package dev.ploats.slopstrocity.events.client;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = SlopstrocityMod.MOD_ID)
public class SlopstrocityClientMiscEvents {

    @SubscribeEvent
    public static void onComputeCameraAnglesEvent(ViewportEvent.ComputeCameraAngles event) {
        if (ScreenShakeEffect.getEnqueuedShakes().isEmpty()) return;

        Camera mainCam = event.getCamera();

        ScreenShakeEffect.getEnqueuedShakes().removeIf(shake -> shake == null || shake.data().getDuration() <= 0);

        for (ScreenShakeEffect shake : ScreenShakeEffect.getEnqueuedShakes()) {
            if (shake != null && shake.data().getDuration() > 0 && !Minecraft.getInstance().isPaused()) {
                shake.data().setDuration(shake.data().getDuration() - 1);

                if (mainCam.getEntity().distanceToSqr(Vec3.atCenterOf(shake.data().getOriginPos())) <= Math.pow(shake.data().getRange(), 2)) {
                    float delta = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                    float ticksExistedDelta = (float) mainCam.getEntity().tickCount + delta;

                    double distance = mainCam.getEntity().getEyePosition().distanceTo(Vec3.atCenterOf(shake.data().getOriginPos()));
                    float distanceScale = Math.max(0, 1 - (float)(distance / shake.data().getRange()));

                    float finalAmp = (float) ((shake.data().getMagnitude() * (shake.data().getDuration() / (shake.data().getFadeOut() == 0 ? 1 : shake.data().getFadeOut()))) * Math.pow(distanceScale, 2)); // Avoid division by 0

                    event.setYaw((float) (event.getYaw() + finalAmp * Math.cos(ticksExistedDelta * 5.0F + 1.0F) * 25.0F));
                    event.setPitch((float) (event.getPitch() + finalAmp * Math.cos(ticksExistedDelta * 3.0F + 2.0F) * 25.0D));
                    event.setRoll((float) (event.getRoll() + finalAmp * Math.cos(ticksExistedDelta * 4.0F) * 25.0D));
                }
            }
        }
    }
}
