package com.zedfalcon.guilds.item;

import com.zedfalcon.guilds.claim.Claim;
import com.zedfalcon.guilds.Guild;
import com.zedfalcon.guilds.GuildStorage;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public class ClaimPointItem extends BlockItem implements PolymerItem {
    public ClaimPointItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (player == null) return ActionResult.FAIL;

        ServerWorld world = (ServerWorld) player.getWorld();

        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if (guild == null) {
            world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1f, 2f);
            player.sendMessage(Text.literal("§cYou need to be part of a guild to create a claim"), true);
            return ActionResult.FAIL;
        }

        Claim claim = new Claim(world);
        guild.addClaim(claim);
        world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);

        return super.place(context);
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BEACON;
    }
}
