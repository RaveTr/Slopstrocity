package dev.ploats.slopstrocity.content.entity.base;

public interface WrappedMonster {
    byte NO_ATTACK_ID = 0;

    byte getAttackId();

    void setAttackId(byte attackId);

    default void resetAttackId() {
        setAttackId(NO_ATTACK_ID);
    }

    boolean isAttacking();

    default boolean isAttackingStatically() {
        return getAttackId() != NO_ATTACK_ID;
    }

    float getAttackTick();

    void setAttackTick(float attackTick);

    default void resetAttackTick() {
        setAttackTick(0.0F);
    }

    default void incrementAttackTick() {
        setAttackTick(getAttackTick() + 1.0F);
    }
}