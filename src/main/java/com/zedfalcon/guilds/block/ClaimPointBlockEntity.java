package com.zedfalcon.guilds.block;

import com.zedfalcon.guilds.Guilds;
import com.zedfalcon.guilds.claim.Claim;
import com.zedfalcon.guilds.claim.ClaimStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClaimPointBlockEntity extends BlockEntity {
    private Claim claim;

    public ClaimPointBlockEntity(BlockPos pos, BlockState state) {
        super(GuildsBlockEntities.CLAIM_POINT, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(Guilds.CLAIM_NBT_KEY)) {
            this.claim = ClaimStorage.INSTANCE.getClaimFromUuid(nbt.getUuid(Guilds.CLAIM_NBT_KEY));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (claim != null) {
            nbt.putUuid(Guilds.CLAIM_NBT_KEY, claim.getUuid());
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ClaimPointBlockEntity blockEntity) {
        if(blockEntity.claim == null) return;

        blockEntity.claim.getBlastShieldReaches().tickBetter();
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }
}
