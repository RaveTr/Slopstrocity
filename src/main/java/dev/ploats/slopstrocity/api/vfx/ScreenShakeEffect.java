package dev.ploats.slopstrocity.api.vfx;

import dev.ploats.slopstrocity.network.s2c.ScreenShakePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.concurrent.ConcurrentLinkedQueue;

public record ScreenShakeEffect(ShakeData data) {
    private static final ConcurrentLinkedQueue<ScreenShakeEffect> SHAKES = new ConcurrentLinkedQueue<>();

    public ScreenShakeEffect(BlockPos originPos, double range, float magnitude, float duration, float fadeOut) {
        this(new ShakeData(originPos, range, magnitude, duration, fadeOut));
    }

    public void enqueue(Level curLevel) {
        if (curLevel == null || curLevel.isClientSide()) SHAKES.add(this);
        if (curLevel != null && !curLevel.isClientSide()) PacketDistributor.sendToAllPlayers(new ScreenShakePayload(data.getOriginPos(), data.getRange(), data.getMagnitude(), data.getDuration(), data.getFadeOut()));
    }

    public static ConcurrentLinkedQueue<ScreenShakeEffect> getEnqueuedShakes() {
        return SHAKES;
    }

    public static class ShakeData {
        private final BlockPos originPos;
        private final double range;
        private final float magnitude;
        private float duration;
        private final float fadeOut;

        public ShakeData(BlockPos originPos, double range, float magnitude, float duration, float fadeOut) {
            this.originPos = originPos;
            this.range = range;
            this.magnitude = magnitude;
            this.duration = duration;
            this.fadeOut = fadeOut;
        }

        public BlockPos getOriginPos() {
            return originPos;
        }

        public double getRange() {
            return range;
        }

        public float getMagnitude() {
            return magnitude;
        }

        public float getDuration() {
            return duration;
        }

        public float getFadeOut() {
            return fadeOut;
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }
    }
}