package com.zedfalcon.guilds.claim;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class ClaimPoint {
    private final BlockPos blockPos;
    private final int squareRadius;

    public ClaimPoint(BlockPos blockPos, int squareRadius) {
        this.blockPos = blockPos;
        this.squareRadius = squareRadius;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Set<BlockPos> getCorners() {
        return Set.of(
                blockPos.add(squareRadius, 0, squareRadius),
                blockPos.add(squareRadius, 0, -squareRadius),
                blockPos.add(-squareRadius, 0, squareRadius),
                blockPos.add(-squareRadius, 0, -squareRadius)
        );
    }

    public static ClaimPoint fromJson(JsonObject claimPointObj) {
        // blockPos
        JsonObject blockPosObj = claimPointObj.getAsJsonObject("blockPos");
        BlockPos blockPos = new BlockPos(
                blockPosObj.get("x").getAsInt(),
                blockPosObj.get("y").getAsInt(),
                blockPosObj.get("z").getAsInt()
        );

        // squareRadius
        int squareRadius = claimPointObj.get("squareRadius").getAsInt();
        return new ClaimPoint(blockPos, squareRadius);
    }

    public JsonObject toJson() {
        JsonObject claimPointObj = new JsonObject();

        // blockPos
        JsonObject blockPosObj = new JsonObject();
        blockPosObj.add("x", new JsonPrimitive(blockPos.getX()));
        blockPosObj.add("y", new JsonPrimitive(blockPos.getY()));
        blockPosObj.add("z", new JsonPrimitive(blockPos.getZ()));
        claimPointObj.add("blockPos", blockPosObj);

        // squareRadius
        claimPointObj.add("squareRadius", new JsonPrimitive(squareRadius));

        return claimPointObj;
    }
}
