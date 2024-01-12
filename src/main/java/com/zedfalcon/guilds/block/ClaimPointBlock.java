package com.zedfalcon.guilds.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClaimPointBlock extends BlockWithEntity implements PolymerBlock {
    public static final MapCodec<ClaimPointBlock> CODEC = ClaimPointBlock.createCodec(ClaimPointBlock::new);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    public ClaimPointBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ClaimPointBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, GuildsBlockEntities.CLAIM_POINT, ClaimPointBlockEntity::serverTick);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BEACON;
    }
}
