package com.zedfalcon.guilds;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClaimStorage {
    public final static ClaimStorage INSTANCE = new ClaimStorage();

    private final Long2ObjectMap<List<Claim>> claims = new Long2ObjectOpenHashMap<>();

    public void addChunksToClaim(Claim claim, Set<Point> chunksToAdd) {
        for(Point chunkToAdd : chunksToAdd) {
            addChunkToClaim(claim, chunkToAdd);
        }
    }

    public void removeChunksFromClaim(Claim claim, Set<Point> chunksToRemove) {
        for(Point chunkToRemove : chunksToRemove) {
            removeChunkFromClaim(claim, chunkToRemove);
        }
    }

    private void addChunkToClaim(Claim claim, Point chunk) {
        long chunkLong = ChunkPos.toLong(chunk.x, chunk.y);
        if(!claims.containsKey(chunkLong)) {
            claims.put(chunkLong, new ArrayList<>());
        }
        claims.get(chunkLong).add(claim);
    }

    private void removeChunkFromClaim(Claim claim, Point chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.y);
        List<Claim> chunkClaims = claims.get(chunkKey);
        chunkClaims.remove(claim);
        if(chunkClaims.size() == 0) {
            claims.remove(chunkKey);
        }
    }
}
