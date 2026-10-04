package dev.ploats.slopstrocity.content.client.sound;

import dev.ploats.slopstrocity.content.entity.ai.goal.slopstrocity.SlopstrocityRollingBlunderAttackGoal;
import dev.ploats.slopstrocity.content.entity.boss.Slopstrocity;
import dev.ploats.slopstrocity.content.registry.SlopstrocitySoundEvents;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

public class SlopstrocityRollingBlunderAttackSoundInstance extends EntityBoundSoundInstance {
    protected final Slopstrocity owner;

    public SlopstrocityRollingBlunderAttackSoundInstance(Slopstrocity owner, SoundEvent curSoundEvent, float volume, float pitch) {
        super(curSoundEvent, SoundSource.HOSTILE, volume, pitch, owner, owner.getRandom().nextLong());

        this.owner = owner;
        this.looping = true;
    }

    @Override
    public boolean canPlaySound() {
        return super.canPlaySound() && owner.isAlive();
    }

    @Override
    public void tick() {
        if (owner.isDeadOrDying()) stop();

        if (Objects.equals(location, SlopstrocitySoundEvents.SLOPSTROCITY_ROLLING_BLUNDER_ATTACK_LOOP.getId())) {
            int curTick = owner.getRollingBlunderTicks();

            this.volume = curTick < SlopstrocityRollingBlunderAttackGoal.ATTACK_END_TICK / 2.0F ? 1.0F : (float) (SlopstrocityRollingBlunderAttackGoal.ATTACK_END_TICK / (curTick * 1.5D));

            if (curTick >= SlopstrocityRollingBlunderAttackGoal.ATTACK_END_TICK - 2) stop();
        }

        if (Objects.equals(location, SlopstrocitySoundEvents.SLOPSTROCITY_TOPSY_TURVY.getId())) {
            int curTick = owner.getTopsyTurvyTicks();

            if (curTick > owner.getTopsyTurvyDuration() - 3) this.volume -= 0.1F;
            if (curTick >= owner.getTopsyTurvyDuration()) stop();
        }

        if (Objects.equals(location, SlopstrocitySoundEvents.SLOPSTROCITY_ROLLING_BLUNDER_ATTACK_OUTRO.getId())) {
            int curTick = owner.getRollingBlunderTicks();

            this.volume = (float) ((double) curTick / 2 - 2 / SlopstrocityRollingBlunderAttackGoal.ATTACK_END_TICK);

            if (curTick >= SlopstrocityRollingBlunderAttackGoal.ATTACK_END_TICK - 1) stop();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SlopstrocityRollingBlunderAttackSoundInstance other
                && Objects.equals(location, other.location);
    }
}
