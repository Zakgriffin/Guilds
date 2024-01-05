package com.zedfalcon.guilds;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Guilds implements ModInitializer {
    public static final EntityType<ClaimCoreEntity> CLAIM_CORE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("guilds", "claim-core"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ClaimCoreEntity::new).build()
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        PolymerEntityUtils.registerType(CLAIM_CORE);

        ServerTickEvents.END_WORLD_TICK.register((w) -> ClaimVisualization.INSTANCE.visualizeAllClaims());
    }
}
