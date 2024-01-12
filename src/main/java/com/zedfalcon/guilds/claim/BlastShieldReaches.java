package com.zedfalcon.guilds.claim;

import com.google.gson.JsonObject;
import com.zedfalcon.guilds.mixin.BlockDisplayEntityInvoker;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class BlastShieldReaches {
    private final Long2ObjectMap<ChunkBlastShieldReaches> chunkBlastShieldReaches;
    private final ServerWorld world;
    private final Int2ObjectSortedMap<Set<BlockPos>> decreaseGroups;


    public BlastShieldReaches(
            Long2ObjectMap<ChunkBlastShieldReaches> chunkBlastShieldReaches,
            ServerWorld world
    ) {
        this.chunkBlastShieldReaches = chunkBlastShieldReaches;
        this.world = world;

        decreaseGroups = new Int2ObjectAVLTreeMap<>();
    }

    public BlastShieldReaches(ServerWorld world) {
        this(new Long2ObjectOpenHashMap<>(), world);
    }

    public void addClaimPoint(ClaimPoint claimPoint, Set<ChunkPos> updatedChunks) {
        for (ChunkPos updatedChunk : updatedChunks) {
            chunkBlastShieldReaches.computeIfAbsent(updatedChunk.toLong(), i -> new ChunkBlastShieldReaches());
        }

        BlockPos pos = claimPoint.getBlockPos();
        insertReachUpdate(pos, 0);
    }

//    private void tickAddClaimPoint() {
//        if (blocksToIncrease.isEmpty()) return;
//
//        int reachForTick = getBlastShieldReachAt(blocksToIncrease.peek());
//
//        while (!blocksToIncrease.isEmpty()) {
//            long current = blocksToIncrease.remove();
//            int currentBlastShieldReachAt = getBlastShieldReachAt(current);
//            if (current != reachForTick) return;
//
//            for (long offset : ADJACENT_OFFSETS) {
//                long adjacent = current + offset;
//                if (getBlastShieldReachAt(adjacent) > currentBlastShieldReachAt) {
//                    BlockState block = world.getBlockState(BlockPos.fromLong(current));
//                    int permeability = block.isAir() ? 1 : 10;
//                    setBlastShieldReachAt(adjacent, currentBlastShieldReachAt + permeability);
//                    blocksToIncrease.add(adjacent);
//                }
//            }
//        }
//    }

//    private void removeClaimPoint(ClaimPoint claimPoint) {
//        long packedPos = claimPoint.getBlockPos().asLong();
//        blocksToIncrease.add(packedPos);
//    }


    private int bestReach(BlockPos pos) {
        int minAdjacentReach = Integer.MAX_VALUE;
        for (BlockPos offset : ADJACENT_OFFSETS) {
            BlockPos adjacent = pos.add(offset);
            if (!inBounds(adjacent)) continue;
            int adjacentReach = getBlastShieldReachAt(adjacent);
            if (adjacentReach < minAdjacentReach) minAdjacentReach = adjacentReach;
        }
        return minAdjacentReach + (world.getBlockState(pos).isAir() ? 1 : 5);
    }

    private final Set<DisplayEntity> bunk = new HashSet<>();

    public void tickBetter() {
        for (DisplayEntity o : bunk) {
            o.kill();
        }
        bunk.clear();


        System.out.println("TICKING BOYO");
        if (decreaseGroups.isEmpty()) return;


        int reach = decreaseGroups.firstIntKey();
        Set<BlockPos> reachUpdates = decreaseGroups.get(reach);
        System.out.println("Remaining: " + reachUpdates.size());
        for (BlockPos current : reachUpdates) {

            DisplayEntity.BlockDisplayEntity displayEntity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            displayEntity.updatePosition(current.getX(), current.getY(), current.getZ());
            ((BlockDisplayEntityInvoker) displayEntity).setBlockStateUh(Blocks.PURPLE_STAINED_GLASS.getDefaultState());
            world.spawnEntity(displayEntity);

            bunk.add(displayEntity);

            setBlastShieldReachAt(current, reach);
            for (BlockPos offset : ADJACENT_OFFSETS) {
                BlockPos adjacent = current.add(offset);
                if (!inBounds(adjacent)) continue;
                if (getBlastShieldReachAt(adjacent) > reach) {
                    int bestAdjacentReach = bestReach(adjacent);
                    insertReachUpdate(adjacent, bestAdjacentReach);
                }
            }
        }

        decreaseGroups.remove(reach);
    }

    private void insertReachUpdate(BlockPos pos, int reach) {
        Set<BlockPos> adjacentReachUpdates = decreaseGroups.computeIfAbsent(reach, i -> new TreeSet<>());
        adjacentReachUpdates.add(pos);
    }

//    private void tickRemoveClaimPoint() {
//        long current = blocksToDecrease.remove();
//        for (long offset : ADJACENT_OFFSETS) {
//            long adjacent = current + offset;
//            int currentBlastShieldReachAt = getBlastShieldReachAt(current);
//            int adjacentBlastShieldReachAt = getBlastShieldReachAt(adjacent);
//            if (adjacentBlastShieldReachAt > currentBlastShieldReachAt) {
//                blocksToDecrease.add(adjacent);
//            } else if (adjacentBlastShieldReachAt != Integer.MAX_VALUE) {
//                blocksToIncrease.add(adjacent);
//            }
//        }
//        setBlastShieldReachAt(current, Integer.MAX_VALUE);
//    }

    public int getBlastShieldReachAt(BlockPos pos) {
        long chunkLong = new ChunkPos(pos).toLong();
        return chunkBlastShieldReaches.get(chunkLong).getBlastShieldReachLocal(pos);
    }

    private void setBlastShieldReachAt(BlockPos pos, int blastShieldReach) {
        long chunkLong = new ChunkPos(pos).toLong();
        chunkBlastShieldReaches.get(chunkLong).setBlastShieldReachLocal(pos, blastShieldReach);
    }

    public boolean inBounds(BlockPos pos) {
        long chunkLong = new ChunkPos(pos).toLong();
        if (!chunkBlastShieldReaches.containsKey(chunkLong)) return false;
        return !world.isOutOfHeightLimit(pos);
    }

    private static final Set<BlockPos> ADJACENT_OFFSETS = Set.of(
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    );

    class ChunkBlastShieldReaches {
        private static final int CHUNK_LENGTH = 16;

        private final int[] reaches;

        private ChunkBlastShieldReaches() {
            int totalSize = CHUNK_LENGTH * world.getHeight() * CHUNK_LENGTH;
            this.reaches = new int[totalSize];
            Arrays.fill(reaches, Integer.MAX_VALUE);
        }

        private void setBlastShieldReachLocal(BlockPos pos, int blastShieldReach) {
            reaches[packedPosToIndex(pos.asLong())] = blastShieldReach;
        }


        private int getBlastShieldReachLocal(BlockPos pos) {
            try {

                return reaches[packedPosToIndex(pos.asLong())];
            } catch (Exception e) {

                System.out.println(pos);
                return -1;
            }
        }

        // taken from BlockPos, wasn't public
        private static final int SIZE_BITS_X = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
        private static final int SIZE_BITS_Z = SIZE_BITS_X;
        private static final int SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
        private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
        private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
        private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
        private static final int MIN_Y = -64;

        private static int packedPosToIndex(long q) {
            // oh yeah i wrote bit manipulation jank
            return (int) (((q >> BIT_SHIFT_X) & 0xFL) | ((q >> BIT_SHIFT_Z - 4) & 0xF0L) | ((q - MIN_Y & BITS_Y) << 8));
        }
    }

    public static BlastShieldReaches fromJson(JsonObject claimResistancesObj) {
        // TODO all of this
        return null;
    }
}
