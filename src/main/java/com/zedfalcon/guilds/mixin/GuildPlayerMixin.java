package com.zedfalcon.guilds.mixin;

import com.zedfalcon.guilds.Guild;
import com.zedfalcon.guilds.GuildPlayer;
import com.zedfalcon.guilds.GuildStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class GuildPlayerMixin implements GuildPlayer {
//    @Nullable
    @Unique
    private Guild guild;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        guild = GuildStorage.INSTANCE.findPlayerGuild((ServerPlayerEntity) (Object) this);
    }

    @Override
//    @Nullable
    public Guild getGuild() {
        return guild;
    }

    @Override
    public void setGuild(Guild guild) {
        this.guild = guild;
    }
}
