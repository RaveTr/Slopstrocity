package dev.ploats.slopstrocity.network.s2c;

import dev.ploats.slopstrocity.SlopstrocityMod;
import dev.ploats.slopstrocity.content.entity.base.WrappedAnimatable;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AnimationPlayPayload(int ownerEntityId, String animationName,
                                   boolean forcePose) implements CustomPacketPayload {
    public static final Type<AnimationPlayPayload> TYPE = new Type<>(SlopstrocityMod.prefix("animation_play_payload"));
    public static final StreamCodec<ByteBuf, AnimationPlayPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AnimationPlayPayload::ownerEntityId,
            ByteBufCodecs.STRING_UTF8,
            AnimationPlayPayload::animationName,
            ByteBufCodecs.BOOL,
            AnimationPlayPayload::forcePose,
            AnimationPlayPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AnimationPlayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level curLevel = context.player().level();
            Entity potentialOwner = curLevel.getEntity(payload.ownerEntityId);

            if (potentialOwner instanceof WrappedAnimatable wrappedAnimatable) {
                IntObjectImmutablePair<AnimationState> targetAnim = wrappedAnimatable.getCachedAnimationStates().get(payload.animationName);

                if (targetAnim != null && targetAnim.right() != null) wrappedAnimatable.playAnimation(payload.animationName, payload.forcePose);
                else SlopstrocityMod.LOGGER.warn("Attempted to play unknown animation: {}", payload.animationName);
            } else SlopstrocityMod.LOGGER.warn("Attempted to play animation for unknown entity: {}", payload.ownerEntityId);
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("slopstrocity.networking.animation_play_payload.failed", e.getMessage()));

            return null;
        });
    }
}
