package com.zedfalcon.guilds.claim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zedfalcon.guilds.Guild;
import com.zedfalcon.guilds.Vault;
import com.zedfalcon.guilds.helpers.Geometry;
import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import com.zedfalcon.guilds.helpers.Traversal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;

import java.util.*;

public class Claim {
    private final UUID id;
    private final Set<ClaimPoint> claimPoints;
    private final Vault vault;
    private final BlastShieldReaches blastShieldReaches;
    private final ServerWorld world;
    private String name;
    private List<Vec2f> outlinePoints;

    public Claim(UUID id, Set<ClaimPoint> claimPoints, Vault vault, BlastShieldReaches blastShieldReaches, ServerWorld world) {
        this.id = id;
        this.claimPoints = claimPoints;
        this.vault = vault;
        this.blastShieldReaches = blastShieldReaches;
        this.world = world;
        ClaimStorage.INSTANCE.addClaim(this);

        recalculateOutlinePoints();
    }

    public Claim(ServerWorld world) {
        this(UUID.randomUUID(), new HashOrderedTreeSet<>(), null, new BlastShieldReaches(world), world);
    }

    public void addClaimPoint(ClaimPoint claimPoint) {
        claimPoints.add(claimPoint);
        updateClaimArea();
        blastShieldReaches.addClaimPoint(claimPoint, ClaimStorage.INSTANCE.getChunksForClaim(this));
    }

    public void removeClaimPoint(ClaimPoint claimPoint) {
        claimPoints.remove(claimPoint);
        updateClaimArea();
    }

    private void updateClaimArea() {
        recalculateOutlinePoints();
        Set<ChunkPos> chunks = getEnclosedChunks();
        ClaimStorage.INSTANCE.addClaimToChunks(this, chunks);
    }

    private void recalculateOutlinePoints() {
        List<Vec2f> allCornerPoints = new ArrayList<>();
        for (ClaimPoint claimPoint : claimPoints) {
            for (BlockPos blockPos : claimPoint.getCorners()) {
                allCornerPoints.add(new Vec2f(blockPos.getX(), blockPos.getZ()));
            }
        }
        outlinePoints = Geometry.convexHull(allCornerPoints);
    }

    private Set<ChunkPos> getEnclosedChunks() {
        // TODO this could probably be optimized
        Traversal<ChunkPos> traversal = new Traversal<>() {
            private boolean withinOutline(int x, int z) {
                return Geometry.pointWithinPolygonInclusive(new Vec2f(x, z), outlinePoints);
            }

            @Override
            protected Set<ChunkPos> getSuccessors(ChunkPos chunk) {
                Set<ChunkPos> successors = new HashSet<>();
                for (var c : new ChunkPos[]{
                        new ChunkPos(chunk.x + 1, chunk.z),
                        new ChunkPos(chunk.x - 1, chunk.z),
                        new ChunkPos(chunk.x, chunk.z + 1),
                        new ChunkPos(chunk.x, chunk.z - 1)
                }) {
                    if (super.visited.contains(c)) continue;
                    if (
                            withinOutline(c.getStartX(), c.getStartZ()) ||
                                    withinOutline(c.getStartX(), c.getEndZ()) ||
                                    withinOutline(c.getEndX(), c.getStartZ()) ||
                                    withinOutline(c.getEndX(), c.getEndZ())
                    ) {
                        successors.add(c);
                    }
                }
                return successors;
            }
        };

        ClaimPoint startClaimPoint = claimPoints.stream().findAny().orElse(null);
        if (startClaimPoint == null) return null;
        ChunkPos startBlockPos = new ChunkPos(startClaimPoint.getBlockPos());

        traversal.addToVisit(startBlockPos);
        traversal.traverse();
        return traversal.getVisited();
    }

    public boolean enclosesBlock(BlockPos pos) {
        return Geometry.pointWithinPolygonInclusive(new Vec2f(pos.getX(), pos.getZ()), outlinePoints);
    }

    public UUID getUuid() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BlastShieldReaches getBlastShieldReaches() {
        return blastShieldReaches;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public List<Vec2f> getOutlinePoints() {
        return outlinePoints;
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

        // TODO how do i load world and blastShieldReaches?
        // world
        ServerWorld world = null;

        // blastShieldReaches
        JsonObject blastShieldReachesObj = claimObj.getAsJsonObject("blastShieldReaches");
        BlastShieldReaches blastShieldReaches = BlastShieldReaches.fromJson(blastShieldReachesObj);

        // guild
        Guild guild = null;

        UUID id = null;

        return new Claim(id, claimPoints, vault, blastShieldReaches, world);
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

        // TODO how do i save world and blastShieldReaches?
        // world

        // blastShieldReaches

        return claimObj;
    }
}
