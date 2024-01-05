package com.zedfalcon.guilds;

import com.google.gson.JsonObject;
import com.zedfalcon.guilds.helpers.BlockPosTransforms;
import com.zedfalcon.guilds.helpers.Traversal;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class ClaimResistances {
    private final Long2ObjectMap<SubChunkResistances[]> resistancesForChunks;
    private final Long2ObjectMap<SubChunkClaimPoints[]> claimPointsForChunks;
    private final World world;
    private final int sectionsY;

    private final Queue<BlockPos> blocksToUpdate;

    public ClaimResistances(
            World world,
            Long2ObjectMap<SubChunkResistances[]> resistancesForChunks,
            Long2ObjectMap<SubChunkClaimPoints[]> claimPointsForChunks
    ) {
        this.world = world;
        this.resistancesForChunks = resistancesForChunks;
        this.claimPointsForChunks = claimPointsForChunks;
        this.blocksToUpdate = new LinkedList<>();
        this.sectionsY = world.countVerticalSections();
    }

    public ClaimResistances(World world) {
        this(world, new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>());
    }

    public void addToUpdateQueue(BlockPos blockPos) {
        blocksToUpdate.add(blockPos);
    }

    public void addClaimPointWithChunks(ClaimPoint claimPoint, Set<ChunkPos> chunksToAdd) {
        for (ChunkPos chunkToAdd : chunksToAdd) {
            addClaimResistancesForChunk(chunkToAdd);
        }

        for (BlockPos blockAccessed : blocksAccessedFrom(claimPoint.getBlockPos())) {
            if (inBounds(blockAccessed)) {
                blocksToUpdate.add(blockAccessed);
            }
        }

        setResistanceAt(claimPoint.getBlockPos(), 0);
        setClaimPointsAt(claimPoint.getBlockPos(), new ClaimPoint[] {claimPoint});
        updateFromQueue();
    }

    private void addClaimResistancesForChunk(ChunkPos chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.z);
        SubChunkResistances[] resistancesForChunk = new SubChunkResistances[sectionsY];
        SubChunkClaimPoints[] claimPointsForChunk = new SubChunkClaimPoints[sectionsY];
        for (int i = 0; i < sectionsY; i++) {
            resistancesForChunk[i] = new SubChunkResistances();
            claimPointsForChunk[i] = new SubChunkClaimPoints();
        }
        resistancesForChunks.put(chunkKey, resistancesForChunk);
        claimPointsForChunks.put(chunkKey, claimPointsForChunk);
    }

    public void removeClaimPointWithChunks(ClaimPoint claimPoint, Set<ChunkPos> chunksToRemove) {
        for (ChunkPos chunkToRemove : chunksToRemove) {
            removeClaimResistancesForChunk(chunkToRemove);
        }

        FindStarterTraversal traversal = new FindStarterTraversal(claimPoint);
        traversal.traverse();
        blocksToUpdate.add(traversal.getBlockPosToUpdateFrom());

        updateFromQueueWithCalculator((from, to, resistance, claimPoints) -> {
            if (resistance == Integer.MAX_VALUE || Arrays.stream(claimPoints).anyMatch(c -> c == claimPoint)) {
                return Integer.MAX_VALUE;
            }
            return calculateBlockToBlockResistance(from, to) + resistance;
        });
    }

    private void removeClaimResistancesForChunk(ChunkPos chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.z);
        resistancesForChunks.remove(chunkKey);
        claimPointsForChunks.remove(chunkKey);
    }

    class FindStarterTraversal extends Traversal<BlockPos> {
        private BlockPos blockPosToUpdateFrom;
        private final ClaimPoint claimPoint;

        public FindStarterTraversal(ClaimPoint claimPoint) {
            this.claimPoint = claimPoint;
        }

        @Override
        protected Set<BlockPos> getSuccessors(BlockPos blockPos) {
            Set<BlockPos> validAdjacentBlocks = new TreeSet<>();
            for (BlockPos adjacentBlock : BlockPosTransforms.getAdjacentBlocks(blockPos)) {
                if (inBounds(adjacentBlock)) {
                    if (Arrays.stream(getClaimPointsAt(adjacentBlock)).noneMatch(c -> c == claimPoint)) {
                        blockPosToUpdateFrom = blockPos;
                        super.terminate();
                    }
                    validAdjacentBlocks.add(adjacentBlock);
                }
            }
            return validAdjacentBlocks;
        }

        public BlockPos getBlockPosToUpdateFrom() {
            return blockPosToUpdateFrom;
        }
    }

    public int getResistanceAt(BlockPos blockPos) {
        ChunkSectionPos chunkSectionPos = BlockPosTransforms.localizeToChunk(blockPos);
        return getSubChunkResistances(blockPos).getResistanceAtLocalized(chunkSectionPos);
    }

    public void setResistanceAt(BlockPos blockPos, int resistance) {
        ChunkSectionPos chunkSectionPos = BlockPosTransforms.localizeToChunk(blockPos);
        getSubChunkResistances(blockPos).setResistanceAtLocalized(chunkSectionPos, resistance);
    }

    public ClaimPoint[] getClaimPointsAt(BlockPos blockPos) {
        ChunkSectionPos chunkSectionPos = BlockPosTransforms.localizeToChunk(blockPos);
        return getSubChunkClaimPoints(blockPos).getClaimPointsAtLocalized(chunkSectionPos);
    }

    public void setClaimPointsAt(BlockPos blockPos, ClaimPoint[] claimPoints) {
        ChunkSectionPos chunkSectionPos = BlockPosTransforms.localizeToChunk(blockPos);
        getSubChunkClaimPoints(blockPos).setClaimPointsAtLocalized(chunkSectionPos, claimPoints);
    }

    private SubChunkResistances getSubChunkResistances(BlockPos blockPos) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(blockPos);
        long chunkKey = ChunkPos.toLong(chunkSectionPos.getX(), chunkSectionPos.getZ());
        SubChunkResistances[] resistancesForChunk = resistancesForChunks.get(chunkKey);
        return resistancesForChunk[forcePositive(chunkSectionPos.getY())];
    }

    private SubChunkClaimPoints getSubChunkClaimPoints(BlockPos blockPos) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(blockPos);
        long chunkKey = ChunkPos.toLong(chunkSectionPos.getX(), chunkSectionPos.getZ());
        SubChunkClaimPoints[] claimPointsForChunk = claimPointsForChunks.get(chunkKey);
        return claimPointsForChunk[forcePositive(chunkSectionPos.getY())];
    }

    private int forcePositive(int i) {
        return i < 0 ? i + sectionsY : i;
    }

    public boolean inBounds(BlockPos blockPos) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(blockPos);
        long chunkKey = ChunkPos.toLong(chunkSectionPos.getX(), chunkSectionPos.getZ());
        if (!resistancesForChunks.containsKey(chunkKey)) return false;
        return !world.isOutOfHeightLimit(blockPos);
    }

    private int calculateBlockToBlockResistance(BlockPos from, BlockPos to) {
        // cannot return 0 or will have issues

        // for now, all from and to pairs will be adjacent
        if (world.getBlockState(from).isAir()) {
            return 1;
        } else {
            return 20;
        }
    }

    interface ResistanceCalculator {
        int calculateResistance(BlockPos from, BlockPos to, int resistance, ClaimPoint[] claimPoints);
    }

    private Set<BlockPos> blocksAccessibleTo(BlockPos blockPos) {
        return BlockPosTransforms.getAdjacentBlocks(blockPos);
    }

    private Set<BlockPos> blocksAccessedFrom(BlockPos blockPos) {
        return BlockPosTransforms.getAdjacentBlocks(blockPos);
    }

    private boolean isClaimPoint(int resistance) {
        return resistance == 0;
    }

    public void updateFromQueue() {
        updateFromQueueWithCalculator((from, to, resistanceTo, claimPointsTo) -> {
            if (resistanceTo == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return calculateBlockToBlockResistance(from, to) + resistanceTo;
        });
    }

    public void updateFromQueueWithCalculator(ResistanceCalculator resistanceCalculator) {
        while (blocksToUpdate.size() > 0) {
            BlockPos currentBlockPos = blocksToUpdate.remove();
            int resistance = getResistanceAt(currentBlockPos);
            if (isClaimPoint(resistance)) continue;

            int lowestResistance = Integer.MAX_VALUE;
            ClaimPoint[] lowestResistanceClaimPoints = null;
            for (BlockPos blockAccessibleTo : blocksAccessibleTo(currentBlockPos)) {
                if (!inBounds(blockAccessibleTo)) continue;

                int resistanceAtTo = getResistanceAt(blockAccessibleTo);
                ClaimPoint[] claimPointsAtTo = getClaimPointsAt(blockAccessibleTo);
                int resistanceBetween = resistanceCalculator.calculateResistance(
                        currentBlockPos, blockAccessibleTo, resistanceAtTo, claimPointsAtTo);
                if (resistanceBetween < lowestResistance) {
                    lowestResistance = resistanceBetween;
                    lowestResistanceClaimPoints = claimPointsAtTo;
                }
            }

            ClaimPoint[] claimPoints = getClaimPointsAt(currentBlockPos);
            if (resistance != lowestResistance || claimPoints != lowestResistanceClaimPoints) {
                // change occurred
                setResistanceAt(currentBlockPos, lowestResistance);
                setClaimPointsAt(currentBlockPos, lowestResistanceClaimPoints);

                for (BlockPos blockAccessed : blocksAccessedFrom(currentBlockPos)) {
                    if (inBounds(blockAccessed)) {
                        blocksToUpdate.add(blockAccessed);
                    }
                }
            }
        }
    }

    static class SubChunkResistances {
        private static final int CHUNK_LENGTH = 16;
        private static final int TOTAL_SIZE = CHUNK_LENGTH * CHUNK_LENGTH * CHUNK_LENGTH;

        private final int[] resistances;

        public SubChunkResistances() {
            this.resistances = new int[TOTAL_SIZE];
            for (int i = 0; i < TOTAL_SIZE; i++) {
                this.resistances[i] = Integer.MAX_VALUE;
            }
        }

        private int xyzToIndex(int x, int y, int z) {
            return (x * CHUNK_LENGTH * CHUNK_LENGTH) + (y * CHUNK_LENGTH) + z;
        }

        private int chunkLocalizedBlockPosToIndex(ChunkSectionPos chunkSectionPos) {
            return xyzToIndex(chunkSectionPos.getX(), chunkSectionPos.getY(), chunkSectionPos.getZ());
        }

        public int getResistanceAtLocalized(ChunkSectionPos chunkSectionPos) {
            return resistances[chunkLocalizedBlockPosToIndex(chunkSectionPos)];
        }

        public void setResistanceAtLocalized(ChunkSectionPos chunkSectionPos, int resistance) {
            resistances[chunkLocalizedBlockPosToIndex(chunkSectionPos)] = resistance;
        }
    }

    static class SubChunkClaimPoints {
        private static final int CHUNK_LENGTH = 16;
        private static final int TOTAL_SIZE = CHUNK_LENGTH * CHUNK_LENGTH * CHUNK_LENGTH;

        private final ClaimPoint[][] claimPointSets;

        public SubChunkClaimPoints() {
            this.claimPointSets = new ClaimPoint[TOTAL_SIZE][];
        }

        private int xyzToIndex(int x, int y, int z) {
            return (x * CHUNK_LENGTH * CHUNK_LENGTH) + (y * CHUNK_LENGTH) + z;
        }

        private int chunkLocalizedBlockPosToIndex(ChunkSectionPos chunkSectionPos) {
            return xyzToIndex(chunkSectionPos.getX(), chunkSectionPos.getY(), chunkSectionPos.getZ());
        }

        public ClaimPoint[] getClaimPointsAtLocalized(ChunkSectionPos chunkSectionPos) {
            return claimPointSets[chunkLocalizedBlockPosToIndex(chunkSectionPos)];
        }

        public void setClaimPointsAtLocalized(ChunkSectionPos chunkSectionPos, ClaimPoint[] claimPoints) {
            claimPointSets[chunkLocalizedBlockPosToIndex(chunkSectionPos)] = claimPoints;
        }
    }

    public static ClaimResistances fromJson(JsonObject claimResistancesObj) {
        //
        return null;
    }
}
