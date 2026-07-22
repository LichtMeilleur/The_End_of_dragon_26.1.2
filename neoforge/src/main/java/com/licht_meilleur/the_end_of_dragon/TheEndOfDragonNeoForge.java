package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.command.TEDDebugCommands;
import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.neoforge.client.TheEndOfDragonNeoForgeClient;
import com.licht_meilleur.the_end_of_dragon.neoforge.network.TedNeoForgeNetwork;
import com.licht_meilleur.the_end_of_dragon.neoforge.registry.*;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleController;
import com.licht_meilleur.the_end_of_dragon.world.TedEndermanBattleHandler;
import com.licht_meilleur.the_end_of_dragon.world.enderman.TedEndermanFriendship;
import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageGatewayManager;
import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageResidentManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@Mod(TheEndOfDragon.MOD_ID)
public final class TheEndOfDragonNeoForge {

    public TheEndOfDragonNeoForge(IEventBus modBus) {
        NeoForgeModDataComponents.register(modBus);

        NeoForgeModBlocks.register(modBus);
        NeoForgeModBlockEntities.register(modBus);

        NeoForgeModEntities.register(modBus);
        NeoForgeModSounds.register(modBus);
        NeoForgeModItems.register(modBus);
        NeoForgeModRecipeSerializers.register(
                modBus
        );
        NeoForgeCreativeTabs.register(modBus);

        TheEndOfDragon.init();

        TedConfig.load(FMLPaths.CONFIGDIR.get());

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerAttributes);

        modBus.addListener(
                TheEndOfDragonNeoForgeClient::registerEntityRenderers
        );

        modBus.addListener(
                TheEndOfDragonNeoForgeClient::registerGuiLayers
        );

        TedNeoForgeNetwork.initSender();

        modBus.addListener(
                TedNeoForgeNetwork::registerPayloads
        );

        modBus.addListener(
                TheEndOfDragonNeoForgeClient::registerClientPayloads
        );

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            /*
             * DeferredRegister完了後にCommon側へ値を渡す。
             */

            NeoForgeModDataComponents
                    .bindCommonReferences();
            NeoForgeModBlocks.bindCommonReferences();
            NeoForgeModBlockEntities.bindCommonReferences();
            NeoForgeModEntities.bindCommonReferences();
            NeoForgeModSounds.bindCommonReferences();
            NeoForgeModItems.bindCommonReferences();
            NeoForgeModRecipeSerializers
                    .bindCommonReferences();
            NeoForgeCreativeTabs.bindCommon();

            TheEndOfDragon.LOGGER.info(
                    "The End Of Dragon NeoForge common setup completed"
            );
        });
    }

    private void registerAttributes(
            EntityAttributeCreationEvent event
    ) {
        /*
         * このイベントではCommonの可変フィールドではなく、
         * NeoForge側Holderを直接使う。
         */
        event.put(
                NeoForgeModEntities.THE_END_OF_DRAGON.get(),
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        event.put(
                NeoForgeModEntities.THE_END_OF_DRAGON_DISPLAY.get(),
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        event.put(
                NeoForgeModEntities.THE_END_OF_DRAGON_COLLISION.get(),
                TheEndOfDragonCoreEntity.createAttributes().build()
        );

        event.put(
                NeoForgeModEntities.TED_ALLY_ENDERMAN.get(),
                TedAllyEndermanEntity.createAttributes().build()
        );

        event.put(
                NeoForgeModEntities.TED_ELDER_ENDERMAN.get(),
                TedElderEndermanEntity.createAttributes().build()
        );

        event.put(
                NeoForgeModEntities.TED_TECH_ENDERMAN.get(),
                TedTechEndermanEntity.createAttributes().build()
        );
    }

    @SubscribeEvent
    public void onServerTick(
            net.neoforged.neoforge.event.tick
                    .ServerTickEvent.Post event
    ) {
        for (ServerLevel level :
                event.getServer().getAllLevels()) {

            /*
             * 村ディメンション内の
             * 帰還門Bを監視・復元する。
             */
            TedVillageGatewayManager.tick(
                    level
            );

            TedVillageResidentManager.tick(
                    level
            );

            /*
             * 一般エンダーマンの友好化処理。
             */
            TedEndermanFriendship.tick(
                    level
            );

            /*
             * 以下はEndディメンション専用。
             */
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

    @SubscribeEvent
    public void onRegisterCommands(
            RegisterCommandsEvent event
    ) {
        TEDDebugCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(
            EntityJoinLevelEvent event
    ) {
        if (!(event.getLevel()
                instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity()
                instanceof EnderMan enderman)) {
            return;
        }

        if (TedEndermanBattleHandler
                .shouldBlockNormalEndermanSpawn(
                        level,
                        enderman.position()
                )) {

            event.setCanceled(true);
        }
    }
}