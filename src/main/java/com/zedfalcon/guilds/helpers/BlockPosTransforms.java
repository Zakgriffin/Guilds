package com.zedfalcon.guilds.helpers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.Set;

public class BlockPosTransforms {
//    public static BlockPos snapToChunk(BlockPos blockPos) {
//        ChunkSectionPos chunk = ChunkSectionPos.from(blockPos);
//        return new BlockPos(
//                chunk.getX() << 4,
//                chunk.getY() << 4,
//                chunk.getZ() << 4
//        );
//    }

    public static ChunkSectionPos localizeToChunk(BlockPos blockPos) {
        return ChunkSectionPos.from(
                ChunkSectionPos.getLocalCoord(blockPos.getX()),
                ChunkSectionPos.getLocalCoord(blockPos.getY()),
                ChunkSectionPos.getLocalCoord(blockPos.getZ())
        );
    }

    public static BlockPos getMinPos(ChunkPos chunkPos) {
        return new BlockPos(
                ChunkSectionPos.getBlockCoord(chunkPos.x),
                ChunkSectionPos.getBlockCoord(0), // TODO negative y
                ChunkSectionPos.getBlockCoord(chunkPos.z)
        );
    }

    public static Set<BlockPos> getAdjacentBlocks(BlockPos blockPos) {
        return Set.of(
                blockPos.add(1,0,0),
                blockPos.add(-1,0,0),
                blockPos.add(0,1,0),
                blockPos.add(0,-1,0),
                blockPos.add(0,0,1),
                blockPos.add(0,0,-1)
        );
    }
}
