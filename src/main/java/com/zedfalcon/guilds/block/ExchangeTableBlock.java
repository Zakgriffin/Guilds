package com.zedfalcon.guilds.block;

import com.mojang.serialization.MapCodec;
import com.zedfalcon.guilds.ExchangeScreenHandler;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExchangeTableBlock extends Block implements PolymerBlock {
    public static final MapCodec<ExchangeTableBlock> CODEC = createCodec(ExchangeTableBlock::new);

    @Override
    public MapCodec<? extends ExchangeTableBlock> getCodec() {
        return CODEC;
    }

    public ExchangeTableBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.LODESTONE;
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.CONSUME;
    }

    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new ExchangeScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos));
        }, Text.literal("Confuse"));
    }
}
