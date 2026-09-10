package dev.ploats.slopstrocity.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Slopstrocity extends Monster {

    public Slopstrocity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
}
