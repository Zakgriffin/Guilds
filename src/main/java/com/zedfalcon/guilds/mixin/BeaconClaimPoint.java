package com.zedfalcon.guilds.mixin;

import com.zedfalcon.guilds.Claim;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlock.class)
public class BeaconClaimPoint  {
    private static String CLAIM_BEACON_NAME = "§6Claim Beacon";

    @Unique
//    @Nullable
    private Claim claim;

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            if(itemStack.getName().getString().equals(CLAIM_BEACON_NAME)) {
            }
        }
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BeaconBlockEntity) {
            if(((BeaconBlockEntity) blockEntity).getDisplayName().getString().equals(CLAIM_BEACON_NAME)) {

            }
        }
    }
//
//    @Inject(method = "<init>*", at = @At("RETURN"))
//    private void initData(CallbackInfo info) {
//        guild = GuildStorage.INSTANCE.findPlayerGuild((ServerPlayerEntity) (Object) this);
//    }
}
