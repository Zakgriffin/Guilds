package com.zedfalcon.guilds.mixin;

import com.zedfalcon.guilds.ClaimVisualization;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Unique
    private boolean everyOtherWorkaroundScroll = false;
    private boolean everyOtherWorkaroundClick = false;

    @Inject(method = "onUpdateSelectedSlot", at = @At("HEAD"))
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet, CallbackInfo info) {
        everyOtherWorkaroundScroll = !everyOtherWorkaroundScroll;
        if(everyOtherWorkaroundScroll) return;

        if(player.getStackInHand(Hand.OFF_HAND).getItem() == Items.BLAZE_ROD && player.isSneaking()) {
            if(ClaimVisualization.INSTANCE.isShowingForPlayer(player)) {
                ClaimVisualization.INSTANCE.updatePlayerSelectedSlot(player, packet.getSelectedSlot());
            }
        }
    }


    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"))
    public void onButtonClick(PlayerInteractItemC2SPacket packet, CallbackInfo info) {
        everyOtherWorkaroundClick = !everyOtherWorkaroundClick;
        if(everyOtherWorkaroundClick) return;

        if(player.getStackInHand(Hand.OFF_HAND).getItem() == Items.BLAZE_ROD) {
            if(ClaimVisualization.INSTANCE.isShowingForPlayer(player)) {
                ClaimVisualization.INSTANCE.toggleResistanceBounds(player);
            }
        }
    }


//    @Inject(method = "onPlayerAction", at = @At("RETURN"))
//    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo info) {
//        if(packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
//            ItemStack itemStack = player.getStackInHand(Hand.OFF_HAND);
//            if(itemStack.getItem() == Items.BLAZE_ROD) {
//                ClaimVisualization.INSTANCE.get
//            } else {
//
//            }
//        }
//    }
}
