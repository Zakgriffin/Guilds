package com.zedfalcon.guilds;

import com.zedfalcon.guilds.helpers.BlockPosTransforms;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClaimStorage {
    public final static ClaimStorage INSTANCE = new ClaimStorage();

    private final Long2ObjectMap<List<Claim>> claims = new Long2ObjectOpenHashMap<>();

    public void addClaimToChunks(Claim claim, Set<Point> chunksToAdd) {
        for(Point chunkToAdd : chunksToAdd) {
            addClaimToChunk(claim, chunkToAdd);
        }
    }

    public void removeClaimFromChunks(Claim claim, Set<Point> chunksToRemove) {
        for(Point chunkToRemove : chunksToRemove) {
            removeClaimFromChunk(claim, chunkToRemove);
        }
    }

    private void addClaimToChunk(Claim claim, Point chunk) {
        long chunkLong = ChunkPos.toLong(chunk.x, chunk.y);
        if(!claims.containsKey(chunkLong)) {
            claims.put(chunkLong, new ArrayList<>());
        }
        claims.get(chunkLong).add(claim);
    }

    private void removeClaimFromChunk(Claim claim, Point chunk) {
        long chunkKey = ChunkPos.toLong(chunk.x, chunk.y);
        List<Claim> chunkClaims = claims.get(chunkKey);
        chunkClaims.remove(claim);
        if(chunkClaims.size() == 0) {
            claims.remove(chunkKey);
        }
    }

    public List<Claim> claimsAt(BlockPos blockPos) {
        BlockPos chunk = BlockPosTransforms.chunkOf(blockPos);
        long chunkKey = ChunkPos.toLong(chunk.getX(), chunk.getZ());
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
