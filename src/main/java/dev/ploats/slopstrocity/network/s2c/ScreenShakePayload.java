package dev.ploats.slopstrocity.network.s2c;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.api.vfx.ScreenShakeEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ScreenShakePayload(BlockPos originPos, double range, float magnitude, float duration, float fadeOut) implements CustomPacketPayload {
    public static final Type<ScreenShakePayload> TYPE = new Type<>(SlopstrocityMod.prefix("screen_shake"));
    public static final StreamCodec<ByteBuf, ScreenShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            ScreenShakePayload::originPos,
            ByteBufCodecs.DOUBLE,
            ScreenShakePayload::range,
            ByteBufCodecs.FLOAT,
            ScreenShakePayload::magnitude,
            ByteBufCodecs.FLOAT,
            ScreenShakePayload::duration,
            ByteBufCodecs.FLOAT,
            ScreenShakePayload::fadeOut,
            ScreenShakePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ScreenShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> new ScreenShakeEffect(payload.originPos, payload.range, payload.magnitude,payload. duration, payload.fadeOut).enqueue(context.player().level()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("slopstrocity.networking.screen_shake_payload.failed", e.getMessage()));

                    return null;
                });
    }
}
