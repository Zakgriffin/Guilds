package com.zedfalcon.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

// for now, assume all claims raidable
public class Claim {
    private final Set<Vault> vaults;

    public Claim(Set<Vault> vaults) {
        this.vaults = vaults;
    }

    public void addVault(Vault vault) {
        vaults.add(vault);
    }

    public void removeVault(Vault vault) {
        vaults.remove(vault);
    }

    public static Claim fromJson(JsonObject claimObj) {
        // vaults
        JsonArray vaultsArr = claimObj.getAsJsonArray("vaults");
        Set<Vault> vaults = new TreeSet<>(Comparator.comparingInt(Object::hashCode));
        for(JsonElement vaultEl : vaultsArr) {
            JsonObject vaultObj = vaultEl.getAsJsonObject();
            vaults.add(Vault.fromJson(vaultObj));
        }
        return new Claim(vaults);
    }

    public JsonObject toJson() {
        JsonObject claimObj = new JsonObject();
        // vaults
        JsonArray vaultsArr = new JsonArray();
        for(Vault vault : vaults) {
            vaultsArr.add(vault.toJson());
        }
        claimObj.add("vaults", vaultsArr);
        return claimObj;
    }
}
