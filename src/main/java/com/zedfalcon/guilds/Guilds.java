package com.zedfalcon.guilds;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Guilds implements ModInitializer {
    public static final EntityType<ClaimCoreEntity> CLAIM_CORE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("guilds", "claim-core"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ClaimCoreEntity::new).build()
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(GuildCommand::register);
        PolymerEntityUtils.registerType(CLAIM_CORE);
    }
}
