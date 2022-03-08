package com.zedfalcon.guilds;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.math.BlockPos;

public class Vault {
    private final BlockPos coreBlockPos;

    public Vault(BlockPos coreBlockPos) {
        this.coreBlockPos = coreBlockPos;
    }

    // JSON

    public JsonObject toJson() {
        JsonObject vaultObj = new JsonObject();
        // coreBlockPos
        JsonObject coreBlockPosObj = new JsonObject();
        vaultObj.add("coreBlockPos", coreBlockPosObj);
        coreBlockPosObj.add("x", new JsonPrimitive(coreBlockPos.getX()));
        coreBlockPosObj.add("y", new JsonPrimitive(coreBlockPos.getY()));
        coreBlockPosObj.add("z", new JsonPrimitive(coreBlockPos.getZ()));

        return vaultObj;
    }

    public static Vault fromJson(JsonObject vaultObj) {
        // coreBlockPos
        JsonObject coreBlockPos = vaultObj.get("coreBlockPos").getAsJsonObject();
        int x = coreBlockPos.get("x").getAsInt();
        int y = coreBlockPos.get("y").getAsInt();
        int z = coreBlockPos.get("z").getAsInt();
        return new Vault(new BlockPos(x,y,z));
    }
}