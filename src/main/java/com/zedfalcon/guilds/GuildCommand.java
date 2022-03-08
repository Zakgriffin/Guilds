package com.zedfalcon.guilds;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class GuildCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("guild")
                .then(CommandManager.literal("create").then(
                        CommandManager.argument("name", StringArgumentType.string()).executes(GuildCommand::create)))
                .then(CommandManager.literal("info").executes(GuildCommand::info))
                .then(CommandManager.literal("leave").executes(GuildCommand::leave))
                .then(CommandManager.literal("list").executes(GuildCommand::list))
                .then(CommandManager.literal("join").then(
                        CommandManager.argument("name", StringArgumentType.string()).executes(GuildCommand::join))));
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (((GuildPlayer) player).getGuild() != null) {
            player.sendMessage(new LiteralText("Can't create guild if already part of one"), true);
            return Command.SINGLE_SUCCESS;
        }
        String name = StringArgumentType.getString(context, "name");
        if (name == null) {
            return 0;
        }
        Guild guild = new Guild(name);
        guild.addMember(player);
        player.sendMessage(new LiteralText("Created guild: " + guild.getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Guild guild = GuildStorage.INSTANCE.getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(new LiteralText("You are not part of a guild"), false);
        } else {
            player.sendMessage(new LiteralText(guild.toJson().toString()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int leave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Guild guild = GuildStorage.INSTANCE.getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(new LiteralText("You are not part of a guild"), true);
        } else {
            guild.removeMember(player);
            player.sendMessage(new LiteralText("You have left the guild " + guild.getName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        StringBuilder guildsListStr = new StringBuilder();
        GuildStorage.INSTANCE.guildsStream()
                .forEach(guild -> guildsListStr.append(guild.getName()).append("\n"));
        player.sendMessage(new LiteralText(guildsListStr.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int join(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (((GuildPlayer) player).getGuild() != null) {
            player.sendMessage(new LiteralText("You are already in a guild"), true);
            return Command.SINGLE_SUCCESS;
        }
        String name = StringArgumentType.getString(context, "name");
        if (name == null) {
            return 0;
        }
        Guild guild = GuildStorage.INSTANCE.getGuildByName(name);
        if (guild == null) {
            player.sendMessage(new LiteralText("That is not a valid guild name"), true);
        } else {
            guild.addMember(player);
            player.sendMessage(new LiteralText("You have joined the guild " + guild.getName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
