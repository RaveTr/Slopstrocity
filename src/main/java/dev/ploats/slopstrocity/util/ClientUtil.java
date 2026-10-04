package dev.ploats.slopstrocity.util;

import dev.ploats.slopstrocity.content.client.sound.SlopstrocityRollingBlunderAttackSoundInstance;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.content.registry.SlopstrocitySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;

public final class ClientUtil {

    private ClientUtil() {
        throw new IllegalAccessError("Attempted to construct a Utility Class!");
    }

    public static void enqueueRollingBlunderLoop(Slopstrocity owner) {
        SlopstrocityRollingBlunderAttackSoundInstance toPlay = new SlopstrocityRollingBlunderAttackSoundInstance(owner, SlopstrocitySoundEvents.SLOPSTROCITY_ROLLING_BLUNDER_ATTACK_LOOP.get(), 1.0F, 1.0F);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (!soundManager.isActive(toPlay)) soundManager.queueTickingSound(toPlay);
    }

    public static void enqueueRollingBlunderOutro(Slopstrocity owner) {
        SlopstrocityRollingBlunderAttackSoundInstance toPlay = new SlopstrocityRollingBlunderAttackSoundInstance(owner, SlopstrocitySoundEvents.SLOPSTROCITY_ROLLING_BLUNDER_ATTACK_OUTRO.get(), 1.0F, 1.0F);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (!soundManager.isActive(toPlay)) soundManager.queueTickingSound(toPlay);
    }

    public static void enqueueTopsyTurvy(Slopstrocity owner) {
        SlopstrocityRollingBlunderAttackSoundInstance toPlay = new SlopstrocityRollingBlunderAttackSoundInstance(owner, SlopstrocitySoundEvents.SLOPSTROCITY_TOPSY_TURVY.get(), 1.0F, 1.0F);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (!soundManager.isActive(toPlay)) soundManager.queueTickingSound(toPlay);
    }
}
