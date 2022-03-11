package com.zedfalcon.guilds;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
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

    public List<Claim> claimsAt(BlockPos blockPos) {
        ChunkPos chunk = new ChunkPos(blockPos);
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.z);
        if(!claims.containsKey(chunkKey)) {
            return null;
        }
        return claims.get(chunkKey);
    }

    @Nullable
    public Claim getClaimForGuildAt(Guild guild, BlockPos blockPos) {
        List<Claim> claims = claimsAt(blockPos);
        for(Claim claim : claims) {
            if(guild.ownsClaim(claim)) {
                return claim;
            }
        }
        return null;
    }
}
