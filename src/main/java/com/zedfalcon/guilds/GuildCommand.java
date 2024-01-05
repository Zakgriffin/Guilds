package com.zedfalcon.guilds;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GuildCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("guild")
                .then(CommandManager.literal("create").then(
                        CommandManager.argument("name", StringArgumentType.string()).executes(GuildCommand::create)))
                .then(CommandManager.literal("info").executes(GuildCommand::info))
                .then(CommandManager.literal("leave").executes(GuildCommand::leave))
                .then(CommandManager.literal("list").executes(GuildCommand::list))
                .then(CommandManager.literal("join").then(
                        CommandManager.argument("name", StringArgumentType.string()).executes(GuildCommand::join)))
                .then(CommandManager.literal("showClaim").executes(GuildCommand::showClaim))
        );
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (((GuildPlayer) player).getGuild() != null) {
            player.sendMessage(Text.literal("You are already a member of a guild"), true);
            return Command.SINGLE_SUCCESS;
        }
        String guildName = StringArgumentType.getString(context, "name");
        if (guildName == null) {
            player.sendMessage(Text.literal("You need to enter a guild name"), true);
            return Command.SINGLE_SUCCESS;
        }
        if (GuildStorage.INSTANCE.getGuildByName(guildName) != null) {
            player.sendMessage(Text.literal("A guild by that name already exists"), true);
            return Command.SINGLE_SUCCESS;
        }
        Guild guild = new Guild(guildName);
        guild.addMember(player);
        player.sendMessage(Text.literal("Guild '" + guildName + "' created"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if (guild == null) {
            player.sendMessage(Text.literal("You are not part of a guild"), false);
        } else {
            player.sendMessage(Text.literal(guild.toJson().toString()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int leave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if (guild == null) {
            player.sendMessage(Text.literal("You are not part of a guild"), true);
        } else {
            guild.removeMember(player);
            player.sendMessage(Text.literal("You have left the guild " + guild.getName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        StringBuilder guildsListStr = new StringBuilder();
        GuildStorage.INSTANCE.guildsStream()
                .forEach(guild -> guildsListStr.append(guild.getName()).append("\n"));
        player.sendMessage(Text.literal(guildsListStr.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int join(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (((GuildPlayer) player).getGuild() != null) {
            player.sendMessage(Text.literal("You are already in a guild"), true);
            return Command.SINGLE_SUCCESS;
        }
        String name = StringArgumentType.getString(context, "name");
        if (name == null) {
            return 0;
        }
        Guild guild = GuildStorage.INSTANCE.getGuildByName(name);
        if (guild == null) {
            player.sendMessage(Text.literal("That is not a valid guild name"), true);
        } else {
            guild.addMember(player);
            player.sendMessage(Text.literal("You have joined the guild " + guild.getName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int showClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (ClaimVisualization.INSTANCE.isShowingForPlayer(player)) {
            ClaimVisualization.INSTANCE.removePlayerVisualizationForClaim(player);
            player.sendMessage(Text.literal("No longer showing claim"), false);
            return Command.SINGLE_SUCCESS;
        }

        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if (guild == null) {
            player.sendMessage(Text.literal("You are not part of a guild"), false);
            return Command.SINGLE_SUCCESS;
        }
        Claim claim = ClaimStorage.INSTANCE.getClaimAt(player.getBlockPos());
        if (claim == null || !guild.ownsClaim(claim)) {
            player.sendMessage(Text.literal("Your guild does not own a claim here"), false);
            return Command.SINGLE_SUCCESS;
        }

        ClaimVisualization.INSTANCE.addPlayerVisualizationForClaim(player, claim);
        player.sendMessage(Text.literal("Showing claim"), false);

        return Command.SINGLE_SUCCESS;
    }
}
