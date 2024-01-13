package com.zedfalcon.guilds;

import com.zedfalcon.guilds.block.GuildsBlockEntities;
import com.zedfalcon.guilds.block.GuildsBlocks;
import com.zedfalcon.guilds.claim.ClaimStorage;
import com.zedfalcon.guilds.item.GuildsItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Guilds implements ModInitializer {
    public static final String ID = "guilds";
    public static final String CLAIM_NBT_KEY = "Claim";

    int i = 0;

    @Override
    public void onInitialize() {
        GuildsBlocks.register();
        GuildsBlockEntities.register();
        GuildsItems.register();

        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        ServerTickEvents.END_WORLD_TICK.register((w) -> ClaimVisualization.INSTANCE.visualizeAllClaims());

        ServerTickEvents.END_WORLD_TICK.register((w) -> {
            ClaimStorage.INSTANCE.tickAllClaims();

            i++;
            if (i > 10) {
                double tps =  (1_000_000_000 / (double)w.getServer().getAverageNanosPerTick());
                if(tps < 20) {
                    System.out.println("tps: " + tps);
                }
                i = 0;
            }
        });
    }
}
