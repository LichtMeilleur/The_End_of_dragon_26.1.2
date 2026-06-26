package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCollisionEntity;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonDisplayEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {


    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(TheEndOfDragon.MOD_ID, "the_end_of_dragon")
            );

    public static final EntityType<TheEndOfDragonCoreEntity> THE_END_OF_DRAGON =
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



    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_DISPLAY_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("the_end_of_dragon_display")
            );
    public static final EntityType<TheEndOfDragonDisplayEntity> THE_END_OF_DRAGON_DISPLAY =
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

    public static final ResourceKey<EntityType<?>> THE_END_OF_DRAGON_COLLISION_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("the_end_of_dragon_collision")
            );
    public static final EntityType<TheEndOfDragonCollisionEntity> THE_END_OF_DRAGON_COLLISION =
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




    public static void init() {
    }

    private ModEntities() {
    }
}