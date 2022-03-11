package com.zedfalcon.guilds;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guilds implements ModInitializer {
    public static final EntityType<ClaimCoreEntity> CLAIM_CORE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("guilds", "claim-core"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ClaimCoreEntity::new).build()
    );

    public static Map<ServerPlayerEntity, Claim> visibleClaims = new HashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        PolymerEntityUtils.registerType(CLAIM_CORE);

        ServerTickEvents.END_WORLD_TICK.register((serverWorld) -> {
            for(var a : visibleClaims.entrySet()) {
                ServerPlayerEntity player = a.getKey();
                Claim claim = a.getValue();
                claim.showClaimOutlineTo(player);
            }
        });
    }
}
