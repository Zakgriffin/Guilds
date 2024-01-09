package com.zedfalcon.guilds.claim;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimStorage {
    public static final ClaimStorage INSTANCE = new ClaimStorage();

    private final Map<Claim, Set<ChunkPos>> claimToChunks = new HashMap<>();
    private final Long2ObjectMap<Set<Claim>> chunkToClaims = new Long2ObjectOpenHashMap<>();

    public void removeClaimFromChunks(Claim claim) {
        for (ChunkPos chunk : claimToChunks.get(claim)) {
            long chunkLong = chunk.toLong();
            Set<Claim> claims = chunkToClaims.get(chunkLong);
            claims.remove(claim);
            if (claims.size() == 0) chunkToClaims.remove(chunkLong);
        }
        claimToChunks.remove(claim);
    }

    public void addClaimToChunks(Claim claim, Set<ChunkPos> chunks) {
        for (ChunkPos chunk : chunks) {
            long chunkLong = ChunkPos.toLong(chunk.x, chunk.z);
            if (!chunkToClaims.containsKey(chunkLong)) chunkToClaims.put(chunkLong, new HashSet<>());
            Set<Claim> claims = chunkToClaims.get(chunkLong);
            claims.add(claim);
        }
        claimToChunks.put(claim, chunks);
    }

    @Nullable
    public Claim getClaimAt(BlockPos blockPos) {
        Set<Claim> claims = chunkToClaims.get(new ChunkPos(blockPos).toLong());
        if (claims == null) return null;

        for (Claim claim : claims) {
            if(claim.enclosesBlock(blockPos)) {
                return claim;
            }
        }
        return null;
    }
}