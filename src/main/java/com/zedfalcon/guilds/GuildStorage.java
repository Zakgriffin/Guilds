package com.zedfalcon.guilds;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.zedfalcon.guilds.helpers.HashOrderedTreeSet;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class GuildStorage {
    private static final String GUILD_FILE_PREFIX = "guild-";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final GuildStorage INSTANCE = new GuildStorage();

    private final Set<Guild> guilds;
    private final Set<Guild> modifiedGuilds;

    public GuildStorage() {
        this.guilds = new HashOrderedTreeSet<>();
        this.modifiedGuilds = new HashOrderedTreeSet<>();
    }

    public Stream<Guild> guildsStream() {
        return guilds.stream();
    }

    @Nullable
    public Guild getGuildByName(String name) {
        for (Guild guild : guilds) {
            if (guild.getName().equals(name)) {
                return guild;
            }
        }
        return null;
    }

    @Nullable
    public Guild getGuildOfPlayer(ServerPlayerEntity player) {
        return ((GuildPlayer) player).getGuild();
    }

    @Nullable
    public Guild findPlayerGuild(ServerPlayerEntity player) {
        for (Guild guild : guilds) {
            if (guild.hasMember(player)) {
                return guild;
            }
        }
        return null;
    }

    public void markModified(Guild guild) {
        modifiedGuilds.add(guild);
    }

    public void read(MinecraftServer server) {
        File guildsDir = new File(
                String.valueOf(DimensionType.getSaveDirectory(World.OVERWORLD, server.getSavePath(WorldSavePath.ROOT))),
                "/data/guilds/");
        if (guildsDir.exists()) {
            try {
                File[] files = guildsDir.listFiles((dir, name) -> name.startsWith(GUILD_FILE_PREFIX));
                assert files != null;
                for (File file : files) {
                    FileReader reader = new FileReader(file);
                    JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                    reader.close();
                    Guild guild = Guild.fromJson(obj);
                    guilds.add(guild);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(MinecraftServer server) {
//        File dir = new File(
//                String.valueOf(DimensionType.getSaveDirectory(World.OVERWORLD, server.getSavePath(WorldSavePath.ROOT))),
//                "/data/guilds/");
//        if (!dir.exists() && !dir.mkdir()) {
//            Logger.getLogger("guilds").log(Level.WARNING, "failed to create guilds directory");
//        }
//
//        try {
//            for (Guild modifiedGuild : modifiedGuilds) {
//                String id = modifiedGuild.getID().toString();
//                File file = new File(dir, id + ".json");
//                if (!file.exists() && !file.createNewFile()) {
//                    Logger.getLogger("guilds").log(Level.WARNING, "failed to create a guild file");
//                }
//
//                JsonObject obj = modifiedGuild.toJson();
//                FileWriter writer = new FileWriter(file);
//                GSON.toJson(obj, writer);
//                writer.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}