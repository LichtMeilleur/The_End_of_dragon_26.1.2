package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item
        .TrueEnderPearlUpgradeRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializers {

    public static final ResourceKey<
            RecipeSerializer<?>>
            TRUE_ENDER_PEARL_UPGRADE_KEY =
            ResourceKey.create(
                    Registries.RECIPE_SERIALIZER,
                    TheEndOfDragon.id(
                            "true_ender_pearl_upgrade"
                    )
            );

    public static RecipeSerializer<
            TrueEnderPearlUpgradeRecipe>
            TRUE_ENDER_PEARL_UPGRADE;

    private static boolean fabricRegistered =
            false;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        TRUE_ENDER_PEARL_UPGRADE =
                Registry.register(
                        BuiltInRegistries
                                .RECIPE_SERIALIZER,
                        TRUE_ENDER_PEARL_UPGRADE_KEY
                                .identifier(),
                        createSerializer()
                );

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon recipe serializers for Fabric"
        );
    }

    public static RecipeSerializer<
            TrueEnderPearlUpgradeRecipe>
    createSerializer() {
        return new RecipeSerializer<>(
                MapCodec.unit(
                        TrueEnderPearlUpgradeRecipe::new
                ),
                StreamCodec.unit(
                        new TrueEnderPearlUpgradeRecipe()
                )
        );
    }

    public static void bindNeoForge(
            RecipeSerializer<
                    TrueEnderPearlUpgradeRecipe>
                    serializer
    ) {
        TRUE_ENDER_PEARL_UPGRADE =
                serializer;
    }

    private ModRecipeSerializers() {
    }
}