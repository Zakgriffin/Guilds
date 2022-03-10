package com.zedfalcon.guilds;

import com.google.gson.JsonObject;

public class Vault {
    private final ClaimPoint claimPoint;

    public Vault(ClaimPoint claimPoint) {
        this.claimPoint = claimPoint;
    }

    public ClaimPoint getClaimPoint() {
        return claimPoint;
    }

    // JSON

    public JsonObject toJson() {
        JsonObject vaultObj = new JsonObject();
        // claimPoint
        JsonObject claimPointObj = claimPoint.toJson();
        vaultObj.add("claimPoint", claimPointObj);

        return vaultObj;
    }

    public static Vault fromJson(JsonObject vaultObj) {
        // claimPoint
        JsonObject claimPointObj = vaultObj.get("claimPoint").getAsJsonObject();
        ClaimPoint claimPoint = ClaimPoint.fromJson(claimPointObj);
        return new Vault(claimPoint);
    }
}