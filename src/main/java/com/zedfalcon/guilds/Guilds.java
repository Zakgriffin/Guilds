package com.zedfalcon.guilds;

import com.zedfalcon.guilds.block.GuildsBlockEntities;
import com.zedfalcon.guilds.block.GuildsBlocks;
import com.zedfalcon.guilds.item.GuildsItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Guilds implements ModInitializer {
    public static final String ID = "guilds";
    public static final String CLAIM_NBT_KEY = "Claim";

    @Override
    public void onInitialize() {
        GuildsBlocks.register();
        GuildsBlockEntities.register();
        GuildsItems.register();

        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        ServerTickEvents.END_WORLD_TICK.register((w) -> ClaimVisualization.INSTANCE.visualizeAllClaims());
    }
}
