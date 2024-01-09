package com.zedfalcon.guilds;

import com.zedfalcon.guilds.blocks.ClaimPointBlock;
import com.zedfalcon.guilds.blocks.ExchangeTableBlock;
import com.zedfalcon.guilds.item.ClaimPointItem;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Guilds implements ModInitializer {
    private static final String modId = "guilds";

    public static final ClaimPointBlock CLAIM_POINT_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(modId, "claim_point"),
            new ClaimPointBlock(Block.Settings.create().hardness(2).nonOpaque())
    );
    public static final Item CLAIM_POINT_ITEM = Registry.register(
            Registries.ITEM,
            Registries.BLOCK.getId(CLAIM_POINT_BLOCK),
            new ClaimPointItem(CLAIM_POINT_BLOCK, new Item.Settings())
    );

    public static final ExchangeTableBlock EXCHANGE_TABLE_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(modId, "exchange_table"),
            new ExchangeTableBlock(Block.Settings.create().hardness(2).nonOpaque())
    );
    public static final Item EXCHANGE_TABLE_ITEM = Registry.register(
            Registries.ITEM,
            Registries.BLOCK.getId(EXCHANGE_TABLE_BLOCK),
            new PolymerBlockItem(EXCHANGE_TABLE_BLOCK, new Item.Settings(), Items.LODESTONE)
    );


    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        ServerTickEvents.END_WORLD_TICK.register((w) -> ClaimVisualization.INSTANCE.visualizeAllClaims());
    }
}
