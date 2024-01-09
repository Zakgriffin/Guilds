package com.zedfalcon.guilds.blocks;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class ClaimPointBlock extends Block implements PolymerBlock {
    public ClaimPointBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BEACON;
    }
}
