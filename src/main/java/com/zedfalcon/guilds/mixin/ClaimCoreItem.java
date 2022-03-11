package com.zedfalcon.guilds.mixin;

import com.zedfalcon.guilds.Claim;
import com.zedfalcon.guilds.ClaimCoreEntity;
import com.zedfalcon.guilds.Guild;
import com.zedfalcon.guilds.GuildStorage;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalItem.class)
public class ClaimCoreItem {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        info.cancel();
        if(context.getSide() != Direction.UP) {
            info.setReturnValue(ActionResult.FAIL);
            return;
        }
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockPos blockPosUp = blockPos.up();
        if (!world.isAir(blockPosUp)) {
            info.setReturnValue(ActionResult.FAIL);
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        assert player != null;

        Guild guild = GuildStorage.INSTANCE.getGuildOfPlayer(player);
        if(guild == null) {
            player.getWorld().playSound(null, context.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1f, 2f);
            player.sendMessage(new LiteralText("§cYou need to be part of a guild to create a claim"), true);
            info.setReturnValue(ActionResult.success(true));
            return;
        }

        Claim claim = new Claim(blockPos, world, guild);
        guild.addClaim(claim);

        ClaimCoreEntity claimCore = ClaimCoreEntity.spawnAt(world, blockPos.up(), claim);
        world.spawnEntity(claimCore);

        context.getStack().decrement(1);
        info.setReturnValue(ActionResult.success(true));
    }
}
