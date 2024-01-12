package com.zedfalcon.guilds.item;

import com.zedfalcon.guilds.Guilds;
import com.zedfalcon.guilds.block.GuildsBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GuildsItems {
    public static final Item CLAIM_POINT = register(GuildsBlocks.CLAIM_POINT, new ClaimPointItem(new Item.Settings()));
    public static final Item EXCHANGE_TABLE = register(GuildsBlocks.EXCHANGE_TABLE);

    public static void register() {
    }

    public static <T extends Item> T register(String path, T item) {
        Registry.register(Registries.ITEM, new Identifier(Guilds.ID, path), item);
        return item;
    }

    public static <E extends Block & PolymerBlock> BlockItem register(E block) {
        var id = Registries.BLOCK.getId(block);
        return Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new Item.Settings(), block.asItem()));
    }

    public static <E extends Block & PolymerBlock, T extends Item> T register(E block, T item) {
        var id = Registries.BLOCK.getId(block);
        return Registry.register(Registries.ITEM, id, item);
    }
}
