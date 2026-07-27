package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.*;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("the_end_of_dragon")
            );

    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_DISPLAY_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("the_end_of_dragon_display")
            );

    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_COLLISION_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("the_end_of_dragon_collision")
            );

    public static final ResourceKey<EntityType<?>> TED_VFX_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("ted_vfx")
            );

    public static final ResourceKey<EntityType<?>> TED_ALLY_ENDERMAN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("ted_ally_enderman")
            );
    public static final ResourceKey<EntityType<?>> TED_ELDER_ENDERMAN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("ted_elder_enderman")
            );
    public static final ResourceKey<EntityType<?>> TED_TECH_ENDERMAN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("ted_tech_enderman")
            );

    public static final ResourceKey<EntityType<?>>
            RECHORUS_JUICE_BLOB_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id(
                            "rechorus_juice_blob"
                    )
            );







    /*
     * 各ローダーの登録処理によって代入される。
     *
     * Fabric:
     * registerFabric() 内で登録して代入。
     *
     * NeoForge:
     * NeoForgeModEntitiesでDeferredRegisterした後、
     * bindNeoForge()から代入。
     */
    public static EntityType<TheEndOfDragonCoreEntity> THE_END_OF_DRAGON;
    public static EntityType<TheEndOfDragonDisplayEntity> THE_END_OF_DRAGON_DISPLAY;
    public static EntityType<TheEndOfDragonCollisionEntity> THE_END_OF_DRAGON_COLLISION;
    public static EntityType<TedVfxEntity> TED_VFX;
    public static EntityType<TedAllyEndermanEntity> TED_ALLY_ENDERMAN;
    public static EntityType<TedElderEndermanEntity> TED_ELDER_ENDERMAN;
    public static EntityType<TedTechEndermanEntity> TED_TECH_ENDERMAN;
    public static EntityType<RechorusJuiceBlobEntity>
            RECHORUS_JUICE_BLOB;


    private static boolean fabricRegistered = false;
    private static boolean bound = false;

    /**
     * Fabric専用の直接登録。
     *
     * NeoForgeからは絶対に呼ばないこと。
     */
    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        THE_END_OF_DRAGON =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        THE_END_OF_DRAGON_KEY.identifier(),
                        EntityType.Builder.of(
                                        TheEndOfDragonCoreEntity::new,
                                        MobCategory.MONSTER
                                )
                                .sized(8.0F, 10.0F)
                                .clientTrackingRange(16)
                                .updateInterval(1)
                                .build(THE_END_OF_DRAGON_KEY)
                );

        THE_END_OF_DRAGON_DISPLAY =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        THE_END_OF_DRAGON_DISPLAY_KEY.identifier(),
                        EntityType.Builder.of(
                                        TheEndOfDragonDisplayEntity::new,
                                        MobCategory.MONSTER
                                )
                                .sized(8.0F, 10.0F)
                                .clientTrackingRange(16)
                                .updateInterval(1)
                                .noSave()
                                .build(THE_END_OF_DRAGON_DISPLAY_KEY)
                );

        THE_END_OF_DRAGON_COLLISION =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        THE_END_OF_DRAGON_COLLISION_KEY.identifier(),
                        EntityType.Builder.of(
                                        TheEndOfDragonCollisionEntity::new,
                                        MobCategory.MONSTER
                                )
                                .sized(8.0F, 10.0F)
                                .clientTrackingRange(16)
                                .updateInterval(1)
                                .noSave()
                                .build(THE_END_OF_DRAGON_COLLISION_KEY)
                );

        TED_VFX =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        TED_VFX_KEY.identifier(),
                        EntityType.Builder.of(
                                        TedVfxEntity::new,
                                        MobCategory.MISC
                                )
                                .sized(1.0F, 1.0F)
                                .clientTrackingRange(128)
                                .updateInterval(1)
                                .noSave()
                                .build(TED_VFX_KEY)
                );

        TED_ALLY_ENDERMAN =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        TED_ALLY_ENDERMAN_KEY.identifier(),
                        EntityType.Builder.of(
                                        TedAllyEndermanEntity::new,
                                        MobCategory.CREATURE
                                )
                                .sized(
                                        0.6F,
                                        2.9F
                                )
                                .clientTrackingRange(10)
                                .updateInterval(2)
                                .build(TED_ALLY_ENDERMAN_KEY)
                );

        TED_ELDER_ENDERMAN =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        TED_ELDER_ENDERMAN_KEY.identifier(),
                        EntityType.Builder.of(
                                        TedElderEndermanEntity::new,
                                        MobCategory.CREATURE
                                )
                                .sized(
                                        0.6F,
                                        2.9F
                                )
                                .clientTrackingRange(10)
                                .updateInterval(2)
                                .build(TED_ELDER_ENDERMAN_KEY)
                );

        TED_TECH_ENDERMAN =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        TED_TECH_ENDERMAN_KEY.identifier(),
                        EntityType.Builder.of(
                                        TedTechEndermanEntity::new,
                                        MobCategory.CREATURE
                                )
                                .sized(
                                        0.6F,
                                        2.9F
                                )
                                .clientTrackingRange(10)
                                .updateInterval(2)
                                .build(TED_TECH_ENDERMAN_KEY)
                );

        RECHORUS_JUICE_BLOB =
                Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        RECHORUS_JUICE_BLOB_KEY.identifier(),
                        EntityType.Builder.of(
                                        RechorusJuiceBlobEntity::new,
                                        MobCategory.MISC
                                )
                                .sized(
                                        0.8F,
                                        0.8F
                                )
                                .clientTrackingRange(10)
                                .updateInterval(1)
                                .build(
                                        RECHORUS_JUICE_BLOB_KEY
                                )
                );





        bound = true;

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon entities for Fabric"
        );
    }



    /**
     * NeoForgeのDeferredRegisterで作成されたEntityTypeを
     * Common側へ渡す。
     */
    public static void bindNeoForge(
            EntityType<TheEndOfDragonCoreEntity> core,
            EntityType<TheEndOfDragonDisplayEntity> display,
            EntityType<TheEndOfDragonCollisionEntity> collision,
            EntityType<TedVfxEntity> vfx,
            EntityType<TedAllyEndermanEntity> allyEnderman,
            EntityType<TedElderEndermanEntity> elderEnderman,
            EntityType<TedTechEndermanEntity> techEnderman,
            EntityType<RechorusJuiceBlobEntity> rechorusJuiceBlob
    ) {
        THE_END_OF_DRAGON = core;
        THE_END_OF_DRAGON_DISPLAY = display;
        THE_END_OF_DRAGON_COLLISION = collision;
        TED_VFX = vfx;
        TED_ALLY_ENDERMAN = allyEnderman;
        TED_ELDER_ENDERMAN = elderEnderman;
        TED_TECH_ENDERMAN = techEnderman;
        RECHORUS_JUICE_BLOB = rechorusJuiceBlob;

        bound = true;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge entity types to common registry references"
        );
    }

    public static boolean isBound() {
        return bound;
    }

    private ModEntities() {
    }
}