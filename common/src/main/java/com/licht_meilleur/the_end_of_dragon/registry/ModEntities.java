package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonHitBoxEntity;
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

    public static final EntityType<TheEndOfDragonEntity> THE_END_OF_DRAGON =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    THE_END_OF_DRAGON_KEY.identifier(),
                    EntityType.Builder.of(TheEndOfDragonEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 10.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(THE_END_OF_DRAGON_KEY)
            );


    public static final ResourceKey<EntityType<?>> DRAGON_HITBOX_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    TheEndOfDragon.id("dragon_hitbox")
            );

    public static final EntityType<DragonHitBoxEntity> DRAGON_HITBOX =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    DRAGON_HITBOX_KEY.identifier(),
                    EntityType.Builder.<DragonHitBoxEntity>of(DragonHitBoxEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .noSave()
                            .noSummon()
                            .build(DRAGON_HITBOX_KEY)
            );



    public static void init() {
    }

    private ModEntities() {
    }
}