package com.zedfalcon.guilds.block;

import com.zedfalcon.guilds.Guilds;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GuildsBlockEntities {
    public static final BlockEntityType<ClaimPointBlockEntity> CLAIM_POINT = register("claim_point",
            FabricBlockEntityTypeBuilder.create(ClaimPointBlockEntity::new).addBlock(GuildsBlocks.CLAIM_POINT));

    public static void register() {
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder<T> item) {
        var x = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Guilds.ID, path), item.build());
        PolymerBlockUtils.registerBlockEntity(x);
        return x;
    }
}
