package com.zedfalcon.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zedfalcon.guilds.helpers.Geometry;
import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import net.minecraft.world.World;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// for now, assume all claims raidable
public class Claim {
    private final Set<ClaimPoint> claimPoints;
    private List<ClaimPoint> outlineClaimPoints;
    private Set<Point> touchingChunks;
    private final Vault vault;
    private final World world;
    private final ClaimResistances claimResistances;

    public Claim(Set<ClaimPoint> claimPoints, Vault vault, World world, ClaimResistances claimResistances) {
        this.claimPoints = claimPoints;
        this.outlineClaimPoints = new ArrayList<>();
        this.vault = vault;
        this.world = world;
        this.claimResistances = claimResistances;
    }

    public void addClaimPoint(ClaimPoint claimPoint) {
        claimPoints.add(claimPoint);

        List<Point> outlinePoints = getOutlinePoints();
        outlineClaimPoints = mapOutlinePointsToClaimPoints(outlinePoints);

        Set<Point> oldTouchingChunks = touchingChunks;
        Set<Point> newTouchingChunks = Geometry.findAllPointsWithinPolygon(outlinePoints);

        newTouchingChunks.removeAll(oldTouchingChunks);
        Set<Point> chunksToAdd = newTouchingChunks;

        ClaimStorage.INSTANCE.addChunksToClaim(this, chunksToAdd);
        claimResistances.addClaimPointWithChunks(claimPoint, chunksToAdd);
    }

    public void removeClaimPoint(ClaimPoint claimPoint) {
        claimPoints.remove(claimPoint);

        List<Point> outlinePoints = getOutlinePoints();
        outlineClaimPoints = mapOutlinePointsToClaimPoints(outlinePoints);

        Set<Point> oldTouchingChunks = touchingChunks;
        Set<Point> newTouchingChunks = Geometry.findAllPointsWithinPolygon(outlinePoints);

        oldTouchingChunks.removeAll(newTouchingChunks);
        Set<Point> chunksToRemove = oldTouchingChunks;

        ClaimStorage.INSTANCE.removeChunksFromClaim(this, chunksToRemove);
        claimResistances.removeClaimPointWithChunks(claimPoint, chunksToRemove);
    }

    public List<Point> getOutlinePoints() {
        List<Point> enclosedPoints = new ArrayList<>();
        for (ClaimPoint claimPoint : claimPoints) {
            enclosedPoints.addAll(claimPoint.getCornerPoints());
        }
        enclosedPoints.addAll(vault.getClaimPoint().getCornerPoints());

        return Geometry.convexHull(enclosedPoints);
    }

    public List<ClaimPoint> mapOutlinePointsToClaimPoints(List<Point> outlinePoints) {
        List<ClaimPoint> outlineClaimPoints = new ArrayList<>();
        outer:
        for (Point outlinePoint : outlinePoints) {
            for (ClaimPoint claimPoint : claimPoints) {
                if (outlinePoint.x == claimPoint.getBlockPos().getX() && outlinePoint.y == claimPoint.getBlockPos().getZ()) {
                    outlineClaimPoints.add(claimPoint);
                    continue outer;
                }
            }
        }
        return outlineClaimPoints;
    }

    public World getWorld() {
        return world;
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
