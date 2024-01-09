package com.zedfalcon.guilds.claim;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class BlastShieldReaches {
    private final Long2ObjectMap<ChunkBlastShieldReaches> chunkBlastShieldReaches;
    private final World world;
    private final Queue<Long> blocksToIncrease;
    private final Queue<Long> blocksToDecrease;

    public BlastShieldReaches(
            Long2ObjectMap<ChunkBlastShieldReaches> chunkBlastShieldReaches,
            World world
    ) {
        this.chunkBlastShieldReaches = chunkBlastShieldReaches;
        this.world = world;
        this.blocksToIncrease = new LinkedList<>();
        this.blocksToDecrease = new LinkedList<>();
    }

    private static long combineIntsToLong(int upper, int lower) {
        return (((long) upper) << 32) | (lower & 0xffffffffL);
    }

    public BlastShieldReaches(World world) {
        this(new Long2ObjectOpenHashMap<>(), world);
    }

    private void addClaimPoint(BlockPos blockPos) {
        blocksToIncrease.add(blockPos.asLong());
        setBlastShieldReachAt(blockPos.asLong(), 0);
    }

    private void tickAddClaimPoint() {
        if(blocksToIncrease.isEmpty()) return;

        int reachForTick = getBlastShieldReachAt(blocksToIncrease.peek());

        while(!blocksToIncrease.isEmpty()){
            long current = blocksToIncrease.remove();
            int currentBlastShieldReachAt = getBlastShieldReachAt(current);
            if(current != reachForTick) return;

            for (long offset : ADJACENT_OFFSETS) {
                long adjacent = current + offset;
                if (getBlastShieldReachAt(adjacent) > currentBlastShieldReachAt) {
                    BlockState block = world.getBlockState(BlockPos.fromLong(current));
                    int permeability = block.isAir() ? 1 : 10;
                    setBlastShieldReachAt(adjacent, currentBlastShieldReachAt + permeability);
                    blocksToIncrease.add(adjacent);
                }
            }
        }
    }

    private void removeClaimPoint(BlockPos blockPos) {
        blocksToIncrease.add(blockPos.asLong());
    }

    private void tickRemoveClaimPoint() {
        long current = blocksToDecrease.remove();
        for (long offset : ADJACENT_OFFSETS) {
            long adjacent = current + offset;
            int currentBlastShieldReachAt = getBlastShieldReachAt(current);
            int adjacentBlastShieldReachAt = getBlastShieldReachAt(adjacent);
            if (adjacentBlastShieldReachAt > currentBlastShieldReachAt) {
                blocksToDecrease.add(adjacent);
            } else if(adjacentBlastShieldReachAt != Integer.MAX_VALUE) {
                blocksToIncrease.add(adjacent);
            }
        }
        setBlastShieldReachAt(current, Integer.MAX_VALUE);
    }

    public int getBlastShieldReachAt(long packedPos) {
        long chunkLong = combineIntsToLong(ChunkPos.getPackedX(packedPos), ChunkPos.getPackedZ(packedPos));
        return chunkBlastShieldReaches.get(chunkLong).getBlastShieldReachLocal(packedPos);
    }

    private void setBlastShieldReachAt(long packedPos, int blastShieldReach) {
        long chunkLong = combineIntsToLong(ChunkPos.getPackedX(packedPos), ChunkPos.getPackedZ(packedPos));
        chunkBlastShieldReaches.get(chunkLong).setBlastShieldReachLocal(packedPos, blastShieldReach);
    }

    public boolean inBounds(long packedPos) {
        long chunkLong = combineIntsToLong(ChunkPos.getPackedX(packedPos), ChunkPos.getPackedZ(packedPos));
        if (!chunkBlastShieldReaches.containsKey(chunkLong)) return false;
        return !world.isOutOfHeightLimit(BlockPos.unpackLongY(packedPos));
    }

    private static final Set<Long> ADJACENT_OFFSETS = Set.of(
            BlockPos.asLong(1, 0, 0),
            BlockPos.asLong(-1, 0, 0),
            BlockPos.asLong(0, 1, 0),
            BlockPos.asLong(0, -1, 0),
            BlockPos.asLong(0, 0, 1),
            BlockPos.asLong(0, 0, -1)
    );

    class ChunkBlastShieldReaches {
        private static final int CHUNK_LENGTH = 16;

        private final int[] reaches;

        private ChunkBlastShieldReaches() {
            int totalSize = CHUNK_LENGTH * world.getHeight() * CHUNK_LENGTH;
            this.reaches = new int[totalSize];
            Arrays.fill(reaches, Integer.MAX_VALUE);
        }

        private void setBlastShieldReachLocal(long blockPosLong, int blastShieldReach) {
            reaches[packedPosToIndex(blockPosLong)] = blastShieldReach;
        }


        private int getBlastShieldReachLocal(long blockPosLong) {
            return reaches[packedPosToIndex(blockPosLong)];
        }

        // taken from BlockPos, wasn't public
        private static final int SIZE_BITS_X = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
        private static final int SIZE_BITS_Z = SIZE_BITS_X;
        private static final int SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
        private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
        private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
        private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;

        private static int packedPosToIndex(long q) {
            // oh yeah i wrote bit manipulation jank
            return (int) (((q >> BIT_SHIFT_X) & 0xFL) | ((q >> BIT_SHIFT_Z - 4) & 0xF0L) | ((q & BITS_Y) << 8));
        }
    }

    public static BlastShieldReaches fromJson(JsonObject claimResistancesObj) {
        // TODO all of this
        return null;
    }
}
