package com.zedfalcon.guilds.block;

import com.zedfalcon.guilds.Guilds;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GuildsBlocks {
    public static final ClaimPointBlock CLAIM_POINT = register("claim_point", new ClaimPointBlock(Block.Settings.create().nonOpaque().hardness(1)));
    public static final ExchangeTableBlock EXCHANGE_TABLE = register("exchange_table", new ExchangeTableBlock(Block.Settings.create().hardness(2).nonOpaque()));

    public static void register() {
    }

    public static <T extends Block> T register(String path, T item) {
        return Registry.register(Registries.BLOCK, new Identifier(Guilds.ID, path), item);
    }
}
