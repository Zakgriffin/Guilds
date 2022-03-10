package com.zedfalcon.guilds;

import com.google.gson.JsonObject;
import com.zedfalcon.guilds.helpers.BlockPosTransforms;
import com.zedfalcon.guilds.helpers.Traversal;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

class ResistancePair {
    int resistance;
    List<ClaimPoint> claimPoints;

    public ResistancePair(int resistance, List<ClaimPoint> claimPoints) {
        this.resistance = resistance;
        this.claimPoints = claimPoints;
    }

    public static ResistancePair createUninitialized() {
        return new ResistancePair(Integer.MAX_VALUE, null);
    }
}

public class ClaimResistances {
    private final Long2ObjectMap<SubChunkClaimResistances[]> allChunkClaimResistances;
    private final World world;

    private final Queue<BlockPos> blocksToUpdate;

    public ClaimResistances(World world, Long2ObjectMap<SubChunkClaimResistances[]> allChunkClaimResistances) {
        this.world = world;
        this.allChunkClaimResistances = allChunkClaimResistances;
        this.blocksToUpdate = new LinkedList<>();
    }


    public void addClaimPointWithChunks(ClaimPoint claimPoint, Set<Point> chunksToAdd) {
        for (Point chunkToAdd : chunksToAdd) {
            addClaimResistancesForChunk(chunkToAdd);
        }

        for (BlockPos blockAccessed : blocksAccessedFrom(claimPoint.getBlockPos())) {
            if (inBounds(blockAccessed)) {
                blocksToUpdate.add(blockAccessed);
            }
        }

        updateFromQueue();
    }

    private void addClaimResistancesForChunk(Point chunk) {
        long chunkLong = ChunkPos.toLong(chunk.x, chunk.y);
        int CHUNKS_Y = 20;
        SubChunkClaimResistances[] chunkClaimResistances = new SubChunkClaimResistances[CHUNKS_Y];
        for (int i = 0; i < CHUNKS_Y; i++) {
            SubChunkClaimResistances subChunkClaimResistances = new SubChunkClaimResistances();
            chunkClaimResistances[i] = subChunkClaimResistances;
        }
        allChunkClaimResistances.put(chunkLong, chunkClaimResistances);
    }

    public void removeClaimPointWithChunks(ClaimPoint claimPoint, Set<Point> chunksToRemove) {
        for (Point chunkToRemove : chunksToRemove) {
            removeClaimResistancesForChunk(chunkToRemove);
        }

        FindStarterTraversal traversal = new FindStarterTraversal(claimPoint);
        traversal.traverse();
        blocksToUpdate.add(traversal.getBlockPosToUpdateFrom());

        updateFromQueueWithCalculator((from, to, resistancePairAtTo) -> {
            if (resistancePairAtTo.resistance == Integer.MAX_VALUE || resistancePairAtTo.claimPoints.contains(claimPoint)) {
                return Integer.MAX_VALUE;
            }
            return calculateBlockToBlockResistance(from, to) + resistancePairAtTo.resistance;
        });
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
            for(BlockPos adjacentBlock : BlockPosTransforms.getAdjacentBlocks(blockPos)) {
                if(inBounds(adjacentBlock)) {
                    if(!getResistancePairAt(adjacentBlock).claimPoints.contains(claimPoint)) {
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

    private void removeClaimResistancesForChunk(Point chunk) {
        allChunkClaimResistances.remove(ChunkPos.toLong(chunk.x, chunk.y));
    }

    public ResistancePair getResistancePairAt(BlockPos blockPos) {
        BlockPos chunk = BlockPosTransforms.chunkOf(blockPos);
        long chunkKey = ChunkPos.toLong(chunk.getX(), chunk.getZ());
        SubChunkClaimResistances[] chunkClaimResistances = allChunkClaimResistances.get(chunkKey);
        SubChunkClaimResistances subChunkClaimResistances = chunkClaimResistances[chunk.getY()];
        BlockPos chunkLocalizedBlockPos = BlockPosTransforms.localizeToChunk(blockPos);

        return subChunkClaimResistances.getResistancePairAtLocalized(chunkLocalizedBlockPos);
    }

    private boolean inBounds(BlockPos blockPos) {
        BlockPos chunk = BlockPosTransforms.chunkOf(blockPos);
        long chunkKey = ChunkPos.toLong(chunk.getX(), chunk.getZ());
        if (!allChunkClaimResistances.containsKey(chunkKey)) return false;
        return chunk.getY() >= 0 && chunk.getY() <= 255;
    }

    private int calculateBlockToBlockResistance(BlockPos from, BlockPos to) {
        // cannot return 0 or will have issues

        // for now, all from and to pairs will be adjacent
        if (world.getBlockState(from).isAir()) {
            return 1;
        } else {
            return 10;
        }
    }

    interface ResistanceCalculator {
        int calculateResistance(BlockPos from, BlockPos to, ResistancePair resistancePairAtTo);
    }

    private Set<BlockPos> blocksAccessibleTo(BlockPos blockPos) {
        return BlockPosTransforms.getAdjacentBlocks(blockPos);
    }

    private Set<BlockPos> blocksAccessedFrom(BlockPos blockPos) {
        return BlockPosTransforms.getAdjacentBlocks(blockPos);
    }

    private boolean isClaimPoint(ResistancePair resistancePair) {
        return resistancePair.resistance == 0;
    }

    public void updateFromQueue() {
        updateFromQueueWithCalculator((from, to, resistancePairAtTo) -> {
            if (resistancePairAtTo.resistance == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return calculateBlockToBlockResistance(from, to) + resistancePairAtTo.resistance;
        });
    }

    public void updateFromQueueWithCalculator(ResistanceCalculator resistanceCalculator) {
        while (blocksToUpdate.size() > 0) {
            BlockPos currentBlockPos = blocksToUpdate.remove();
            ResistancePair resistancePair = getResistancePairAt(currentBlockPos);
            if (isClaimPoint(resistancePair)) continue;

            int lowestResistance = Integer.MAX_VALUE;
            List<ClaimPoint> lowestResistanceClaimPoints = null;
            for (BlockPos blockAccessibleTo : blocksAccessibleTo(currentBlockPos)) {
                if (!inBounds(blockAccessibleTo)) continue;

                ResistancePair resistancePairAtTo = getResistancePairAt(blockAccessibleTo);
                int resistance = resistanceCalculator.calculateResistance(currentBlockPos, blockAccessibleTo, resistancePairAtTo);
                if (resistance < lowestResistance) {
                    lowestResistance = resistance;
                    lowestResistanceClaimPoints = resistancePairAtTo.claimPoints;
                }
            }

            if (resistancePair.resistance != lowestResistance || resistancePair.claimPoints != lowestResistanceClaimPoints) {
                // change occurred
                resistancePair.resistance = lowestResistance;
                resistancePair.claimPoints = lowestResistanceClaimPoints;

                for (BlockPos blockAccessed : blocksAccessedFrom(currentBlockPos)) {
                    if (inBounds(blockAccessed)) {
                        blocksToUpdate.add(blockAccessed);
                    }
                }
            }
        }
    }


    // probably not static forever
    static class SubChunkClaimResistances {
        private static final int CHUNK_LENGTH = 16;

        private final ResistancePair[][][] resistancePairs;

        public SubChunkClaimResistances() {
            this.resistancePairs = new ResistancePair[CHUNK_LENGTH][CHUNK_LENGTH][CHUNK_LENGTH];
            for (int x = 0; x < CHUNK_LENGTH; x++) {
                for (int y = 0; y < CHUNK_LENGTH; y++) {
                    for (int z = 0; z < CHUNK_LENGTH; z++) {
                        resistancePairs[x][y][z] = ResistancePair.createUninitialized();
                    }
                }
            }
        }

        public ResistancePair getResistancePairAtLocalized(BlockPos chunkLocalizedBlockPos) {
            return resistancePairs
                    [chunkLocalizedBlockPos.getX()]
                    [chunkLocalizedBlockPos.getY()]
                    [chunkLocalizedBlockPos.getZ()];
        }
    }

    public static ClaimResistances fromJson(JsonObject claimResistancesObj, World world) {
        //
        claimResistancesObj.getAsJsonObject("")
        return new ClaimResistances(world, allChunkClaimResistances);
    }
}
