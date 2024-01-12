package com.zedfalcon.guilds.item;

import com.zedfalcon.guilds.Guilds;
import com.zedfalcon.guilds.block.ClaimPointBlockEntity;
import com.zedfalcon.guilds.block.GuildsBlockEntities;
import com.zedfalcon.guilds.block.GuildsBlocks;
import com.zedfalcon.guilds.claim.Claim;
import com.zedfalcon.guilds.Guild;
import com.zedfalcon.guilds.GuildStorage;
import com.zedfalcon.guilds.claim.ClaimPoint;
import com.zedfalcon.guilds.claim.ClaimStorage;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimPointItem extends BlockItem implements PolymerItem {
    public ClaimPointItem(Settings settings) {
        super(GuildsBlocks.CLAIM_POINT, settings);
    }

//    public ActionResult useOnBlock(ItemUsageContext context) {
//    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer((ServerPlayerEntity) user);
        if (guild == null) {
            user.sendMessage(Text.literal("§cYou need to be part of a guild to assign a claim point"), true);
            return super.use(world, user, hand);
        }

        Claim claim = ClaimStorage.INSTANCE.getClaimAt(user.getBlockPos());
        user.sendMessage(Text.literal("" + (claim == null ? "No Claim" : claim.getUuid())));
        if (claim != null && !guild.ownsClaim(claim)) {
            user.sendMessage(Text.literal("§cYou do not own this claim, cannot assign claim point"), true);
            return super.use(world, user, hand);
        }

        ItemStack itemStack = user.getStackInHand(hand);
        if (claim == null) {
            itemStack.removeCustomName();

            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putUuid(Guilds.CLAIM_NBT_KEY, UUID.randomUUID());
            BlockItem.setBlockEntityNbt(itemStack, GuildsBlockEntities.CLAIM_POINT, nbtCompound);

        } else {
            itemStack.setCustomName(Text.literal("Claim Point [" + claim.getName() + "]"));

            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putUuid(Guilds.CLAIM_NBT_KEY, claim.getUuid());
            BlockItem.setBlockEntityNbt(itemStack, GuildsBlockEntities.CLAIM_POINT, nbtCompound);
        }

        return super.use(world, user, hand);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        if (!super.canPlace(context, state)) return false;

        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (player == null) return false;

        ItemStack itemStack = player.getStackInHand(context.getHand());
        if (!player.canPlaceOn(context.getBlockPos(), context.getSide(), itemStack)) return false;

        ServerWorld world = (ServerWorld) player.getWorld();

        BlockPos pos = context.getBlockPos();
        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if (guild == null) {
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1f, 2f);
            player.sendMessage(Text.literal("§cYou need to be part of a guild to create a claim"), true);
            return false;
        }

        // TODO should check corners (whole space)
        Claim existingClaim = ClaimStorage.INSTANCE.getClaimAt(pos);
        if (existingClaim != null && !guild.ownsClaim(existingClaim)) {
            player.sendMessage(Text.literal("§cThe claim'" + existingClaim.getName() + "' already exists here"), true);
            return false;
        }

        NbtCompound itemNbt = itemStack.getOrCreateNbt();

        Claim claim;
        if (itemNbt.containsUuid(Guilds.CLAIM_NBT_KEY)) {
            UUID claimUuid = itemNbt.getUuid(Guilds.CLAIM_NBT_KEY);
            claim = ClaimStorage.INSTANCE.getClaimFromUuid(claimUuid);
            if (claim == null) {
                player.sendMessage(Text.literal("§cThe claim for this claim point no longer exists"), true);
                return false;
            }
        }

        return true;
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        if (super.place(context) == ActionResult.FAIL) return ActionResult.FAIL;

        BlockEntity blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
        if (blockEntity instanceof ClaimPointBlockEntity claimCoreBlockEntity) {
            ServerWorld world = (ServerWorld) context.getWorld();

            Claim claim = claimCoreBlockEntity.getClaim();
            if (claim == null) {
                claim = new Claim(world);
                ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
                Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
                assert guild != null;
                guild.addClaim(claim);
                claimCoreBlockEntity.setClaim(claim);
            }


            BlockPos pos = context.getBlockPos();
            claim.addClaimPoint(new ClaimPoint(pos, 10));
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
        }

        return ActionResult.SUCCESS;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BEACON;
    }
}
