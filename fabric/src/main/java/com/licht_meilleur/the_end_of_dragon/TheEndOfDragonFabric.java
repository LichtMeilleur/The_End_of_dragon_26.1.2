package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.command.TEDDebugCommands;
import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.fabric.network.TedFabricNetwork;
import com.licht_meilleur.the_end_of_dragon.registry.*;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleController;
import com.licht_meilleur.the_end_of_dragon.world.TedEndermanBattleHandler;
import com.licht_meilleur.the_end_of_dragon.world.block.entity.EndermanVillageGatewayBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.enderman.TedEndermanFriendship;
import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageGatewayManager;
import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageResidentManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class TheEndOfDragonFabric implements ModInitializer {

    private static BlockEntityType<
                EndermanVillageGatewayBlockEntity>
            endermanVillageGatewayBlockEntityType;

    @Override
    public void onInitialize() {
        /*
         * FabricではCommon側の直接登録処理を使用する。
         *
         * Spawn EggがEntityTypeを参照するため、
         * Entity → Sound → Itemの順で登録する。
         */

        ModBlocks.registerFabric();
        ModDataComponents.registerFabric();
        /*
         * Fabricでのみ必要なBlockEntityType生成。
         */
        endermanVillageGatewayBlockEntityType =
                Registry.register(
                        BuiltInRegistries.BLOCK_ENTITY_TYPE,
                        ModBlockEntities
                                .ENDERMAN_VILLAGE_GATEWAY_KEY
                                .identifier(),
                        FabricBlockEntityTypeBuilder.create(
                                EndermanVillageGatewayBlockEntity::new,
                                ModBlocks.ENDERMAN_VILLAGE_GATEWAY,
                                ModBlocks.ENDERMAN_VILLAGE_RETURN_GATEWAY
                        ).build()
                );

        ModBlockEntities.bindFabric(
                endermanVillageGatewayBlockEntityType
        );

        ModEntities.registerFabric();
        ModSounds.registerFabric();
        ModItems.registerFabric();
        ModRecipeSerializers.registerFabric();
        ModCreativeTabs.registerFabric();


        /*
         * Registry以外のCommon初期化。
         */
        TheEndOfDragon.init();

        TedFabricNetwork.init();

        TedConfig.load(
                FabricLoader.getInstance().getConfigDir()
        );

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    for (ServerLevel level :
                            server.getAllLevels()) {

                        TedVillageGatewayManager.tick(
                                level
                        );

                        TedVillageResidentManager.tick(
                                level
                        );

                        TedEndermanFriendship.tick(
                                level
                        );

                        if (!level.dimension()
                                .equals(Level.END)) {
                            continue;
                        }

                        EndDragonSpawnHandler.tick(
                                level
                        );

                        TedBattleController.tick(
                                level
                        );
                    }
                }
        );

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

        FabricDefaultAttributeRegistry.register(
                ModEntities.TED_ELDER_ENDERMAN,
                TedElderEndermanEntity.createAttributes().build()
        );

        FabricDefaultAttributeRegistry.register(
                ModEntities.TED_TECH_ENDERMAN,
                TedTechEndermanEntity.createAttributes().build()
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