package dev.ploats.slopstrocity.content.registry;

import dev.ploats.slopstrocity.SlopstrocityMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SlopstrocitySoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, SlopstrocityMod.MOD_ID);

    // Idle
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_1 = SOUND_EVENTS.register("slopstrocity_idle_1", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_2 = SOUND_EVENTS.register("slopstrocity_idle_2", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_3 = SOUND_EVENTS.register("slopstrocity_idle_3", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_4 = SOUND_EVENTS.register("slopstrocity_idle_4", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_5 = SOUND_EVENTS.register("slopstrocity_idle_5", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_6 = SOUND_EVENTS.register("slopstrocity_idle_6", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_7 = SOUND_EVENTS.register("slopstrocity_idle_7", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_8 = SOUND_EVENTS.register("slopstrocity_idle_8", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_9 = SOUND_EVENTS.register("slopstrocity_idle_9", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_IDLE_10 = SOUND_EVENTS.register("slopstrocity_idle_10", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_idle_10")));

    // Step
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_STEP = SOUND_EVENTS.register("slopstrocity_step", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_step")));

    // Hurt
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_HURT = SOUND_EVENTS.register("slopstrocity_hurt", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_SPLOOGE = SOUND_EVENTS.register("slopstrocity_splooge", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_splooge")));

    // Attacks
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_SLOP_SLAM_ATTACK = SOUND_EVENTS.register("slopstrocity_slop_slam_attack", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_slop_slam_attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_SLOP_SPIT_ATTACK = SOUND_EVENTS.register("slopstrocity_slop_spit_attack", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_slop_spit_attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOPSTROCITY_SLOP_STOMP_ATTACK = SOUND_EVENTS.register("slopstrocity_slop_stomp_attack", () -> SoundEvent.createVariableRangeEvent(SlopstrocityMod.prefix("slopstrocity_slop_stomp_attack")));
}