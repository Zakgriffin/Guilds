package com.zedfalcon.guilds.helpers;

import net.minecraft.util.math.BlockPos;

import java.awt.Point;
import java.util.Set;

public class BlockPosTransforms {
    public static BlockPos chunkOf(BlockPos blockPos) {
        return new BlockPos(
                blockPos.getX() >> 4,
                blockPos.getY() >> 4,
                blockPos.getZ() >> 4
        );
    }

    public static BlockPos lowestCornerFromChunk(Point chunk) {
        return new BlockPos(
                chunk.x << 4,
                0,
                chunk.y << 4
        );
    }

    public static BlockPos snapToChunk(BlockPos blockPos) {
        BlockPos chunk = chunkOf(blockPos);
        return new BlockPos(
                chunk.getX() << 4,
                chunk.getY() << 4,
                chunk.getZ() << 4
        );
    }

    public static BlockPos localizeToChunk(BlockPos blockPos) {
        BlockPos snappedToChunk = snapToChunk(blockPos);
        return new BlockPos(
                blockPos.getX() - snappedToChunk.getX(),
                blockPos.getY() - snappedToChunk.getY(),
                blockPos.getZ() - snappedToChunk.getZ()
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
