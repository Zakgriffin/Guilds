package com.zedfalcon.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zedfalcon.guilds.helpers.Geometry;
import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// for now, assume all claims raidable
public class Claim {
    private final Set<ClaimPoint> claimPoints;
    private List<BlockPos> outlineBlocks;
    private Set<Point> touchingChunks;
    private final Vault vault;
    private final World world;
    private final ClaimResistances claimResistances;

    public Claim(Set<ClaimPoint> claimPoints, Vault vault, World world, ClaimResistances claimResistances) {
        this.claimPoints = claimPoints;
        this.outlineBlocks = new ArrayList<>();
        this.vault = vault;
        this.world = world;
        this.claimResistances = claimResistances;
        this.touchingChunks = new HashOrderedTreeSet<>();
    }

    public Claim(BlockPos blockPos, World world) {
        this.claimPoints = new HashOrderedTreeSet<>();
        this.outlineBlocks = new ArrayList<>();
        ClaimPoint vaultClaimPoint = new ClaimPoint(blockPos, 10);
        this.vault = new Vault(vaultClaimPoint);
        this.world = world;
        this.claimResistances = new ClaimResistances(world);
        this.touchingChunks = new HashOrderedTreeSet<>();

        addClaimPoint(vaultClaimPoint);
    }

    public void addClaimPoint(ClaimPoint claimPoint) {
        claimPoints.add(claimPoint);

        List<BlockPos> enclosedBlocks = findEnclosedBlocks();
        List<Point> outlinePoints = getOutlinePoints(enclosedBlocks);
        outlineBlocks = mapPointsToOutlineBlocks(outlinePoints, enclosedBlocks);

        List<Point> outlineChunks = outlinePoints.stream().map(p -> new Point(p.x >> 4, p.y >> 4)).toList();
        Set<Point> oldTouchingChunks = touchingChunks;
        Set<Point> newTouchingChunks = Geometry.findAllPointsWithinPolygonInclusive(outlineChunks);

        newTouchingChunks.removeAll(oldTouchingChunks);
        Set<Point> chunksToAdd = newTouchingChunks;

        ClaimStorage.INSTANCE.addChunksToClaim(this, chunksToAdd);
//        claimResistances.addClaimPointWithChunks(claimPoint, chunksToAdd);
        touchingChunks = newTouchingChunks;
    }

    public void removeClaimPoint(ClaimPoint claimPoint) {
        claimPoints.remove(claimPoint);

        List<BlockPos> enclosedBlocks = findEnclosedBlocks();
        List<Point> outlinePoints = getOutlinePoints(enclosedBlocks);
        outlineBlocks = mapPointsToOutlineBlocks(outlinePoints, enclosedBlocks);

        List<Point> outlineChunks = outlinePoints.stream().map(p -> new Point(p.x >> 4, p.y >> 4)).toList();
        Set<Point> oldTouchingChunks = touchingChunks;
        Set<Point> newTouchingChunks = Geometry.findAllPointsWithinPolygonInclusive(outlineChunks);

        oldTouchingChunks.removeAll(newTouchingChunks);
        Set<Point> chunksToRemove = oldTouchingChunks;

        ClaimStorage.INSTANCE.removeChunksFromClaim(this, chunksToRemove);
        claimResistances.removeClaimPointWithChunks(claimPoint, chunksToRemove);
        touchingChunks = newTouchingChunks;
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
        enclosedBlocks.addAll(vault.getClaimPoint().getCorners());
        List<Point> enclosedPoints = enclosedBlocks.stream().map(b -> new Point(b.getX(), b.getZ())).toList();
        return Geometry.convexHull(enclosedPoints);
    }

    private List<BlockPos> mapPointsToOutlineBlocks(List<Point> outlinePoints, List<BlockPos> enclosedBlocks) {
        List<BlockPos> outlineBlocks = new ArrayList<>();
        outer: for (Point point : outlinePoints) {
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

    public void showClaimOutlineTo(ServerPlayerEntity player) {
        for (int i = 0; i < outlineBlocks.size(); i++) {
            BlockPos b1 = outlineBlocks.get(i);
            BlockPos b2 = outlineBlocks.get((i + 1) % outlineBlocks.size());
            double dist = Math.sqrt(b1.getSquaredDistance(b2));
            BlockPos vec = b2.subtract(b1);
            BlockPos step = new BlockPos(
                    vec.getX() / dist,
                    vec.getY() / dist,
                    vec.getZ() / dist
            );
            for (int j = 0; j < dist; j++) {
                BlockPos l = b1.add(step.multiply(j));
                DustParticleEffect particle = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(0xFFFF00)), 1);

                ((ServerWorld) world).spawnParticles(player, particle, true, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
            }
        }
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

        return new Claim(claimPoints, vault, world, claimResistances);
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
