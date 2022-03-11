package com.zedfalcon.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zedfalcon.guilds.helpers.BlockPosTransforms;
import com.zedfalcon.guilds.helpers.Geometry;
import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// for now, assume all claims raidable
public class Claim {
    private final Set<ClaimPoint> claimPoints;
    private List<BlockPos> outlineBlocks;
    private Set<ChunkPos> touchingChunks;
    private final Vault vault;
    private final World world;
    private final ClaimResistances claimResistances;
    private final Guild guild;

    public Claim(Set<ClaimPoint> claimPoints, Vault vault, World world, ClaimResistances claimResistances, Guild guild) {
        this.claimPoints = claimPoints;
        this.outlineBlocks = new ArrayList<>();
        this.vault = vault;
        this.world = world;
        this.claimResistances = claimResistances;
        this.touchingChunks = new HashOrderedTreeSet<>();
        this.guild = guild;
    }

    public Claim(BlockPos blockPos, World world, Guild guild) {
        this.claimPoints = new HashOrderedTreeSet<>();
        this.outlineBlocks = new ArrayList<>();
        ClaimPoint vaultClaimPoint = new ClaimPoint(blockPos, 10);
        this.vault = new Vault(vaultClaimPoint);
        this.world = world;
        this.claimResistances = new ClaimResistances(world);
        this.touchingChunks = new HashOrderedTreeSet<>();
        this.guild = guild;

        addClaimPoint(vaultClaimPoint);
    }

    public ClaimResistances getClaimResistances() {
        return claimResistances;
    }

    public void addClaimPoint(ClaimPoint claimPoint) {
        claimPoints.add(claimPoint);

        List<BlockPos> enclosedBlocks = findEnclosedBlocks();
        List<Point> outlineBlockPoints = getOutlinePoints(enclosedBlocks);
        outlineBlocks = mapPointsToOutlineBlocks(outlineBlockPoints, enclosedBlocks);
        List<Point> outlineChunkPoints = outlineBlockPoints.stream().map(p -> new Point(p.x >> 4, p.y >> 4)).toList();

        Set<ChunkPos> oldTouchingChunks = touchingChunks;
        Set<ChunkPos> newTouchingChunks = Geometry.findAllPointsWithinPolygonInclusive(outlineChunkPoints).stream()
                .map(BlockPosTransforms::pointToChunkPosMapper).collect(Collectors.toSet());

        newTouchingChunks.removeAll(oldTouchingChunks);
        Set<ChunkPos> chunksToAdd = newTouchingChunks;

        ClaimStorage.INSTANCE.addClaimToChunks(this, chunksToAdd);
        claimResistances.addClaimPointWithChunks(claimPoint, chunksToAdd);
        touchingChunks = newTouchingChunks;
    }

    public void removeClaimPoint(ClaimPoint claimPoint) {

    }

    private List<BlockPos> findEnclosedBlocks() {
        List<BlockPos> enclosedBlocks = new ArrayList<>();
        for (ClaimPoint claimPoint : claimPoints) {
            enclosedBlocks.addAll(claimPoint.getCorners());
        }
        enclosedBlocks.addAll(vault.getClaimPoint().getCorners());
        return enclosedBlocks;
    }

    private List<Point> getOutlinePoints(List<BlockPos> enclosedBlocks) {
        List<Point> enclosedPoints = enclosedBlocks.stream().map(b -> new Point(b.getX(), b.getZ())).toList();
        return Geometry.convexHull(enclosedPoints);
    }

    private List<BlockPos> mapPointsToOutlineBlocks(List<Point> outlinePoints, List<BlockPos> enclosedBlocks) {
        List<BlockPos> outlineBlocks = new ArrayList<>();
        outer:
        for (Point point : outlinePoints) {
            for (BlockPos enclosedBlock : enclosedBlocks) {
                if (point.x == enclosedBlock.getX() && point.y == enclosedBlock.getZ()) {
                    outlineBlocks.add(enclosedBlock);
                    continue outer;
                }
            }
        }

        return outlineBlocks;
    }

    public World getWorld() {
        return world;
    }

    public List<BlockPos> getOutlineBlocks() {
        return outlineBlocks;
    }

    public void remove() {
        ClaimStorage.INSTANCE.removeClaimFromChunks(this, touchingChunks);
        this.guild.removeClaim(this);
        ClaimVisualization.INSTANCE.removeAllVisualizingClaim(this);
    }

    public Set<ChunkPos> getTouchingChunks() {
        return touchingChunks;
    }

    public static Claim fromJson(JsonObject claimObj) {
        // claimPoints
        JsonArray claimPointsArr = claimObj.getAsJsonArray("claimPoints");
        Set<ClaimPoint> claimPoints = new HashOrderedTreeSet<>();
        for (JsonElement vaultEl : claimPointsArr) {
            JsonObject vaultObj = vaultEl.getAsJsonObject();
            claimPoints.add(ClaimPoint.fromJson(vaultObj));
        }

        // vault
        JsonObject vaultObj = claimObj.getAsJsonObject("vault");
        Vault vault = Vault.fromJson(vaultObj);

        // world
        World world = null;

        // allChunkClaimResistances
        JsonObject claimResistancesObj = claimObj.getAsJsonObject("claimResistances");
        ClaimResistances claimResistances = ClaimResistances.fromJson(claimResistancesObj);

        // guild
        Guild guild = null;

        return new Claim(claimPoints, vault, world, claimResistances, guild);
    }

    public JsonObject toJson() {
        JsonObject claimObj = new JsonObject();
        // claimPoints
        JsonArray claimPointsArr = new JsonArray();
        for (ClaimPoint claimPoint : claimPoints) {
            claimPointsArr.add(claimPoint.toJson());
        }
        claimObj.add("claimPoints", claimPointsArr);

        // vault
        JsonObject vaultObj = vault.toJson();
        claimObj.add("vault", vaultObj);

        // world

        // allChunkClaimResistances

        return claimObj;
    }
}
