package com.zedfalcon.guilds;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClaimStorage {
    public final static ClaimStorage INSTANCE = new ClaimStorage();

    private final Long2ObjectMap<List<Claim>> claims = new Long2ObjectOpenHashMap<>();

    public void addClaimToChunks(Claim claim, Set<ChunkPos> chunksToAdd) {
        for(ChunkPos chunkToAdd : chunksToAdd) {
            addClaimToChunk(claim, chunkToAdd);
        }
    }

    public void removeClaimFromChunks(Claim claim, Set<ChunkPos> chunksToRemove) {
        for(ChunkPos chunkToRemove : chunksToRemove) {
            removeClaimFromChunk(claim, chunkToRemove);
        }
    }

    private void addClaimToChunk(Claim claim, ChunkPos chunk) {
        long chunkLong = ChunkPos.toLong(chunk.x, chunk.z);
        if(!claims.containsKey(chunkLong)) {
            claims.put(chunkLong, new ArrayList<>());
        }
        claims.get(chunkLong).add(claim);
    }

    private void removeClaimFromChunk(Claim claim, ChunkPos chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.z);
        List<Claim> chunkClaims = claims.get(chunkKey);
        chunkClaims.remove(claim);
        if(chunkClaims.size() == 0) {
            claims.remove(chunkKey);
        }
    }

//    @Nullable
    public Claim getClaimAt(BlockPos blockPos) {
        List<Claim> claimsAt = claimsAtChunk(new ChunkPos(blockPos));
        if(claimsAt == null) return null;
        for(Claim claim : claimsAt) {
            //if(claim.enclosesBlock(blockPos)) {
                return claim;
            //}
        }
        return null;
    }

//    @Nullable
    public List<Claim> claimsAtChunk(ChunkPos chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.z);
        return claims.getOrDefault(chunkKey, null);
    }
}
