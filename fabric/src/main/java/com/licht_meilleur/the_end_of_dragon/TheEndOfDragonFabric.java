package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.command.TEDDebugCommands;
import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.fabric.network.TedFabricNetwork;
import com.licht_meilleur.the_end_of_dragon.registry.ModCreativeTabs;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.licht_meilleur.the_end_of_dragon.world.TedEndermanBattleHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.EnderMan;

public final class TheEndOfDragonFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        /*
         * FabricではCommon側の直接登録処理を使用する。
         *
         * Spawn EggがEntityTypeを参照するため、
         * Entity → Sound → Itemの順で登録する。
         */
        ModEntities.registerFabric();
        ModSounds.registerFabric();
        ModItems.registerFabric();
        ModCreativeTabs.registerFabric();

        /*
         * Registry以外のCommon初期化。
         */
        TheEndOfDragon.init();

        TedFabricNetwork.init();

        TedConfig.load(
                FabricLoader.getInstance().getConfigDir()
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                if (level.dimension()
                        == net.minecraft.world.level.Level.END) {
                    EndDragonSpawnHandler.tick(level);
                }
            }
        });

        FabricDefaultAttributeRegistry.register(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        FabricDefaultAttributeRegistry.register(
                ModEntities.THE_END_OF_DRAGON_DISPLAY,
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        FabricDefaultAttributeRegistry.register(
                ModEntities.THE_END_OF_DRAGON_COLLISION,
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        FabricDefaultAttributeRegistry.register(
                ModEntities.TED_ALLY_ENDERMAN,
                TedAllyEndermanEntity.createAttributes().build()
        );

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        TEDDebugCommands.register(dispatcher)
        );

        ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> {
                    if (!(entity instanceof EnderMan enderman)) {
                        return;
                    }

                    if (TedEndermanBattleHandler
                            .shouldBlockNormalEndermanSpawn(
                                    world,
                                    enderman.position()
                            )) {

                        enderman.discard();
                    }
                }
        );
    }
}