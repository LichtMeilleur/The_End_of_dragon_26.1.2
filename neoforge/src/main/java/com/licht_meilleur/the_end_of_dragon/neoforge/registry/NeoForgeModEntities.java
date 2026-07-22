package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCollisionEntity;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonDisplayEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TheEndOfDragonCoreEntity>
            > THE_END_OF_DRAGON =
            ENTITY_TYPES.register(
                    "the_end_of_dragon",
                    () -> EntityType.Builder.of(
                                    TheEndOfDragonCoreEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(8.0F, 10.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(ModEntities.THE_END_OF_DRAGON_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TheEndOfDragonDisplayEntity>
            > THE_END_OF_DRAGON_DISPLAY =
            ENTITY_TYPES.register(
                    "the_end_of_dragon_display",
                    () -> EntityType.Builder.of(
                                    TheEndOfDragonDisplayEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(8.0F, 10.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .noSave()
                            .build(ModEntities.THE_END_OF_DRAGON_DISPLAY_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TheEndOfDragonCollisionEntity>
            > THE_END_OF_DRAGON_COLLISION =
            ENTITY_TYPES.register(
                    "the_end_of_dragon_collision",
                    () -> EntityType.Builder.of(
                                    TheEndOfDragonCollisionEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(8.0F, 10.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .noSave()
                            .build(ModEntities.THE_END_OF_DRAGON_COLLISION_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TedVfxEntity>
            > TED_VFX =
            ENTITY_TYPES.register(
                    "ted_vfx",
                    () -> EntityType.Builder.of(
                                    TedVfxEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .noSave()
                            .build(ModEntities.TED_VFX_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TedAllyEndermanEntity>
            > TED_ALLY_ENDERMAN =
            ENTITY_TYPES.register(
                    "ted_ally_enderman",
                    () -> EntityType.Builder.of(
                                    TedAllyEndermanEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(0.6F, 2.9F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(ModEntities.TED_ALLY_ENDERMAN_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TedElderEndermanEntity>
            > TED_ELDER_ENDERMAN =
            ENTITY_TYPES.register(
                    "ted_elder_enderman",
                    () -> EntityType.Builder.of(
                                    TedElderEndermanEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(0.6F, 2.9F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(ModEntities.TED_ELDER_ENDERMAN_KEY)
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<TedTechEndermanEntity>
            > TED_TECH_ENDERMAN =
            ENTITY_TYPES.register(
                    "ted_tech_enderman",
                    () -> EntityType.Builder.of(
                                    TedTechEndermanEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(0.6F, 2.9F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(ModEntities.TED_TECH_ENDERMAN_KEY)
            );

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    public static void bindCommonReferences() {
        ModEntities.bindNeoForge(
                THE_END_OF_DRAGON.get(),
                THE_END_OF_DRAGON_DISPLAY.get(),
                THE_END_OF_DRAGON_COLLISION.get(),
                TED_VFX.get(),
                TED_ALLY_ENDERMAN.get(),
                TED_ELDER_ENDERMAN.get(),
                TED_TECH_ENDERMAN.get()
        );
    }

    private NeoForgeModEntities() {
    }
}