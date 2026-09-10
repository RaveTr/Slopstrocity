package dev.ploats.slopstrocity.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class MathUtil {

    private MathUtil() {
        throw new IllegalAccessError("Attempted to construct a Utility Class!");
    }

    public static double getRelativeAngleBetween(double x1, double z1, double x2, double z2) {
        return (Math.atan2(z2 - z1, x2 - x1) * (180 / Math.PI) - 90) % 360;
    }

    public static double getRelativeAngleBetweenBlockPositions(BlockPos posA, BlockPos posB) {
        return getRelativeAngleBetween(posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
    }

    public static double getRelativeAngleBetweenEntities(Entity first, Entity second) {
        return getRelativeAngleBetween(first.getX(), first.getZ(), second.getX(), second.getZ());
    }

    public static boolean isWithinAngleRestriction(double relAngle, double angleRestriction) {
        return (relAngle <= angleRestriction / 2 && relAngle >= -angleRestriction / 2) || (relAngle >= 360 - angleRestriction / 2 || relAngle <= -360 + angleRestriction / 2);
    }

    public static double getAngleBetweenEntities(Entity first, Entity second) {
        return Math.atan2(second.getZ() - first.getZ(), second.getX() - first.getX()) * (180 / Math.PI) + 90;
    }

    public static boolean isBetween(int num, int min, int max) {
        return num >= min && num <= max;
    }

    public static boolean isBetween(double num, double min, double max) {
        return num >= min && num <= max;
    }

    public static boolean isBetween(float num, float min, float max) {
        return num >= min && num <= max;
    }

    public static boolean isBetween(long num, long min, long max) {
        return num >= min && num <= max;
    }

    public static ObjectArrayList<BlockPos> getDirectLineBetween(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        ObjectArrayList<BlockPos> bresenhamLine = new ObjectArrayList<>();
        int dx = endX - startX;
        int dy = endY - startY;
        int dz = endZ - startZ;
        int steps = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));

        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;
        double zIncrement = (double) dz / steps;

        for (int i = 0; i <= steps; i++) {
            int x = startX + (int) Math.round(xIncrement * i);
            int y = startY + (int) Math.round(yIncrement * i);
            int z = startZ + (int) Math.round(zIncrement * i);

            bresenhamLine.add(BlockPos.containing(x, y, z));
        }

        return bresenhamLine;
    }

    public static ObjectArrayList<BlockPos> getDirectLineBetween(Vec3 start, Vec3 end) {
        return getDirectLineBetween((int) start.x, (int) start.y, (int) start.z, (int) end.x, (int) end.y, (int) end.z);
    }

    public static ObjectArrayList<BlockPos> getDirectLineBetween(BlockPos start, BlockPos end) {
        return getDirectLineBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
    }

    public static Vec3 ground(Vec3 basePos, Level curLevel, boolean fullColumnCheck) {
        if (!curLevel.getBlockState(BlockPos.containing(basePos)).getCollisionShape(curLevel, BlockPos.containing(basePos)).isEmpty() || !curLevel.getBlockState(BlockPos.containing(basePos).below()).getCollisionShape(curLevel, BlockPos.containing(basePos)).isEmpty()) return basePos; // Avoid unnecessary computation

        ClipContext colliderCtx = fullColumnCheck
                ? new ClipContext(Vec3.atCenterOf(BlockPos.containing(basePos.x(), curLevel.getMinBuildHeight(), basePos.z())), Vec3.atCenterOf(BlockPos.containing(basePos.x(), curLevel.getMaxBuildHeight(), basePos.z())), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.of(null))
                : new ClipContext(Vec3.atCenterOf(BlockPos.containing(basePos)), Vec3.atCenterOf(BlockPos.containing(basePos.x(), curLevel.getMinBuildHeight(), basePos.z())), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.of(null));
        BlockHitResult potentialHitResult = curLevel.clip(colliderCtx);

        return BlockHitResult.Type.MISS.equals(potentialHitResult.getType()) ? basePos : potentialHitResult.getLocation();
    }

    public static double normalize(double value, double min, double max, double targetMin, double targetMax) {
        if (min == max) throw new IllegalArgumentException("Input range min cannot equal max");
        if (targetMin == targetMax) throw new IllegalArgumentException("Target range min cannot equal max");

        double normalizedValue = (value - min) / (max - min); // Normalize between 0 - 1 first

        return normalizedValue * (targetMax - targetMin) + targetMin;
    }

    public static double normalize(double value, double targetMin, double targetMax) {
        return normalize(value, Double.MIN_VALUE, Double.MAX_VALUE, targetMin, targetMax);
    }

    public static List<Entity> ringAround(Level curLevel, BlockPos centerPos, int radius, int yOffset, BlockState ringState, boolean processEntities) {
        List<Entity> foundEntities = ObjectArrayList.of();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (isOnCircumference(x, z, radius)) {
                    BlockPos targetPos = centerPos.offset(x, yOffset, z);
                    BlockPos groundPos = findGroundPosition(targetPos, 6, 6, (pos) -> curLevel.getBlockState(pos).isCollisionShapeFullBlock(curLevel, pos) && curLevel.getBlockState(pos.above()).isAir());

                    if (groundPos != null) {
                        curLevel.setBlockAndUpdate(groundPos.above(), ringState);

                        if (processEntities) foundEntities = curLevel.getEntitiesOfClass(Entity.class, new AABB(groundPos.above()));
                    }
                }
            }
        }

        return foundEntities;
    }

    public static void ringAround(Level curLevel, BlockPos centerPos, int radius, int yOffset, BlockState ringState) {
        ringAround(curLevel, centerPos, radius, yOffset, ringState, false);
    }

    public static List<Entity> reverseRingAround(Level curLevel, BlockPos centerPos, int radius, int yOffset, BlockState selectState, boolean processEntities) {
        List<Entity> foundEntities = ObjectArrayList.of();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (isOnCircumference(x, z, radius)) {
                    BlockPos targetPos = centerPos.offset(x, yOffset, z);
                    BlockPos groundPos = findGroundPosition(targetPos, 6, 6, (pos) -> curLevel.getBlockState(pos).isCollisionShapeFullBlock(curLevel, pos) && curLevel.getBlockState(pos.above()).is(selectState.getBlock()));

                    if (groundPos != null) {
                        curLevel.removeBlock(groundPos.above(), false);

                        if (processEntities) foundEntities = curLevel.getEntitiesOfClass(Entity.class, new AABB(groundPos.above()));
                    }
                }
            }
        }

        return foundEntities;
    }

    public static void reverseRingAround(Level curLevel, BlockPos centerPos, int radius, BlockState selectState, int yOffset) {
        reverseRingAround(curLevel, centerPos, radius, yOffset, selectState, false);
    }

    public static List<Entity> ringAround(Level curLevel, BlockPos centerPos, int radius, int yOffset, Entity owner, BiConsumer<Entity, BlockPos> action, boolean processEntities) {
        List<Entity> foundEntities = ObjectArrayList.of();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (isOnCircumference(x, z, radius)) {
                    BlockPos targetPos = centerPos.offset(x, yOffset, z);
                    BlockPos groundPos = findGroundPosition(targetPos, 6, 6, (pos) -> curLevel.getBlockState(pos).isCollisionShapeFullBlock(curLevel, pos) && curLevel.getBlockState(pos.above()).isAir());

                    if (groundPos != null) {
                        action.accept(owner, groundPos);

                        if (processEntities) foundEntities = curLevel.getEntitiesOfClass(Entity.class, new AABB(groundPos.above()));
                    }
                }
            }
        }

        return foundEntities;
    }

    @Nullable
    public static BlockPos findGroundPosition(BlockPos basePos, int minYFromBasePos, int maxYFromBasePos, Predicate<BlockPos> posValidator) { // Repurposed code from RandomPos
        if (posValidator.test(basePos)) return basePos;

        BlockPos landPos;

        for (landPos = basePos.below(minYFromBasePos); landPos.getY() < basePos.above(maxYFromBasePos).getY() && !posValidator.test(landPos); landPos = landPos.above()) {
            // NO-OP
        }

        return posValidator.test(landPos) ? landPos : null;
    }

    public static boolean isOnCircumference(int x, int z, int radius) {
        double euclideanDist = Math.sqrt(x * x + z * z);

        return Math.abs(euclideanDist - radius) < 0.5;
    }

    public static float smoothQuad(float tick, float startFadeTick, float fadeDuration, float incrementRate) {
        float normalizedTick = (tick - startFadeTick) / fadeDuration;
        float peakValue = 1.0F;

        float result = Mth.clamp(
                tick <= startFadeTick
                        ? (tick / 100.0F) * incrementRate // Rising phase
                        : peakValue * (1.0F - normalizedTick * normalizedTick), // Fading phase
                0.0F,
                peakValue
        );

        return result;
    }

    public static OscillationResult oscillate(OscillationResult result, float startValue, float endValue, float delta) {
        if (result.increasing) {
            result.value += delta;
            if (result.value >= endValue) {
                result.value = endValue;
                result.increasing = false;
            }
        } else {
            result.value -= delta;
            if (result.value <= startValue) {
                result.value = startValue;
                result.increasing = true;
            }
        }

        return result;
    }

    public static class OscillationResult {
        private float value;
        private boolean increasing;

        public OscillationResult(float value, boolean increasing) {
            this.value = value;
            this.increasing = increasing;
        }

        public float getValue() {
            return value;
        }

        public boolean isIncreasing() {
            return increasing;
        }

        public boolean isDecreasing() {
            return !increasing;
        }
    }
}