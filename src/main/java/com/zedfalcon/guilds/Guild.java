package com.zedfalcon.guilds;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import net.minecraft.server.network.ServerPlayerEntity;

public class Guild {
    private final UUID id;
    private String name;
    private final Set<UUID> members;
    private final Set<Claim> claims;

    public Guild(String name) {
        // new guild
        this.id = UUID.randomUUID();
        this.name = name;
        this.members = new TreeSet<>();
        this.claims = new HashOrderedTreeSet<>();
        GuildStorage.INSTANCE.markModified(this);
    }
    public Guild(UUID id, String name, Set<UUID> members, Set<Claim> claims) {
        // load in guild from storage
        this.id = id;
        this.name = name;
        this.members = members;
        this.claims = claims;
    }

    public void addMember(ServerPlayerEntity player) {
        ((GuildPlayer) player).setGuild(this);
        members.add(player.getUuid());
        GuildStorage.INSTANCE.markModified(this);
    }

    public void removeMember(ServerPlayerEntity player) {
        ((GuildPlayer) player).setGuild(null);
        members.remove(player.getUuid());
        GuildStorage.INSTANCE.markModified(this);
    }

    public boolean hasMember(ServerPlayerEntity player) {
        return members.contains(player.getUuid());
    }

    public UUID getID() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
        GuildStorage.INSTANCE.markModified(this);
    }

    public String getName() {
        return name;
    }

    // JSON

    public JsonObject toJson() {
        JsonObject guildObj = new JsonObject();
        // id
        guildObj.add("id", new JsonPrimitive(id.toString()));
        // name
        guildObj.add("name", new JsonPrimitive(name));
        // members
        JsonArray membersArr = new JsonArray();
        for(UUID member : members) {
            membersArr.add(member.toString());
        }
        guildObj.add("members", membersArr);
        // claims
        JsonArray claimsArr = new JsonArray();
        for(Claim claim : claims) {
            membersArr.add(claim.toJson());
        }
        guildObj.add("claims", claimsArr);
        return guildObj;
    }

    public static Guild fromJson(JsonObject obj) {
        // id
        UUID id = UUID.fromString(obj.get("id").getAsString());
        // name
        String name = obj.get("name").getAsString();
        // members
        JsonArray membersArr = obj.getAsJsonArray("members");
        Set<UUID> members = new TreeSet<>();
        for (JsonElement memberEl : membersArr) {
            UUID memberID = UUID.fromString(memberEl.getAsString());
            members.add(memberID);
        }
        // claims
        JsonArray claimsArr = obj.getAsJsonArray("claims");
        Set<Claim> claims = new HashOrderedTreeSet<>();
        for (JsonElement claimEl : claimsArr) {
            JsonObject claimObj = claimEl.getAsJsonObject();
            Claim claim = Claim.fromJson(claimObj);
            claims.add(claim);
        }
        return new Guild(id, name, members, claims);
    }
}