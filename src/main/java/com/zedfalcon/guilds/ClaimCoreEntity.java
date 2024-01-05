package com.zedfalcon.guilds;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ClaimCoreEntity extends Entity implements PolymerEntity {
    private Claim claim;

    // TODO stupid java 8 ambiguity. fix later
    public static ClaimCoreEntity spawnAt(World world, Vec3d location, Claim claim) {
        ClaimCoreEntity claimCoreEntity = new ClaimCoreEntity(Guilds.CLAIM_CORE, world);
        claimCoreEntity.setPosition(location.getX(), location.getY(), location.getZ());
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

        if(claim != null) {
            claim.remove();
        }

        return true;
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
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

//    @Override
//    public Packet<?> createSpawnPacket() {
//        return new EntitySpawnS2CPacket(this);
//    }
}
