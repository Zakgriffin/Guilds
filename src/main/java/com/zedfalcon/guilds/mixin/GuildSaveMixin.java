package com.zedfalcon.guilds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zedfalcon.guilds.GuildStorage;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class GuildSaveMixin {
    @Inject(method = "createWorlds", at = @At("RETURN"))
    private void readGuildData(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        GuildStorage.INSTANCE.read(server);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void saveGuildData(CallbackInfoReturnable<Boolean> info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        GuildStorage.INSTANCE.save(server);
    }
}