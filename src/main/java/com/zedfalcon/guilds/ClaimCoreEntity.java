package com.zedfalcon.guilds;

import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClaimCoreEntity extends Entity implements PolymerEntity {
    private Claim claim;

    // TODO stupid java 8 ambiguity. fix later
    public static ClaimCoreEntity spawnAt(World world, BlockPos pos, Claim claim) {
        ClaimCoreEntity claimCoreEntity = new ClaimCoreEntity(Guilds.CLAIM_CORE, world);
        claimCoreEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        claimCoreEntity.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        claimCoreEntity.claim = claim;
        return claimCoreEntity;
    }

    public ClaimCoreEntity(EntityType<? extends Entity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        super.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 1f, 0.8f);
        super.remove(RemovalReason.KILLED);

        claim.remove();

        return true;
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.END_CRYSTAL;
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, EntityType.END_CRYSTAL, 0, this.getBlockPos());
    }
}
