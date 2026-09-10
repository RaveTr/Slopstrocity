package dev.ploats.slopstrocity.content.entity.base;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import net.minecraft.world.entity.AnimationState;

import java.util.Map;

public interface WrappedAnimatable {
    void playAnimation(String animationStateName, boolean forcePose);
    void stopAnimation(String animationStateName);

    default void playAnimation(String animationStateName) {
        playAnimation(animationStateName, false);
    }

    void playAnimation(AnimationState animationState, boolean forcePose);
    void stopAnimation(AnimationState animationState);

    default void playAnimation(AnimationState animationState) {
        playAnimation(animationState, false);
    }

    default AnimationState wrapState(String animationName) {
        return wrapState(animationName, -1);
    }

    AnimationState wrapState(String animationName, int tickDuration);

    Map<String, IntObjectImmutablePair<AnimationState>> getCachedAnimationStates();

    default boolean requiresServerAnimationTicking() {
        return true;
    }
}
