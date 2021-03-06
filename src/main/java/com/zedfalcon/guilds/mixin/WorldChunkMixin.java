package com.zedfalcon.guilds.mixin;

import com.zedfalcon.guilds.Claim;
import com.zedfalcon.guilds.ClaimStorage;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void setBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> info) {
//        WorldChunk worldChunk = (WorldChunk) (Object) this;
//        ChunkSection chunkSection = worldChunk.getSection(worldChunk.getSectionIndex(pos.getY()));
//        Claim claim = ClaimStorage.INSTANCE.getClaimAt(pos);
//        if(claim == null) return;
//

    }
}
