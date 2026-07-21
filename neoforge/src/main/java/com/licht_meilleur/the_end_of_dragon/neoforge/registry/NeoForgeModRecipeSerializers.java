package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item
        .TrueEnderPearlUpgradeRecipe;
import com.licht_meilleur.the_end_of_dragon.registry
        .ModRecipeSerializers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModRecipeSerializers {

    public static final DeferredRegister<
            RecipeSerializer<?>>
            RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<
                    TrueEnderPearlUpgradeRecipe>>
            TRUE_ENDER_PEARL_UPGRADE =
            RECIPE_SERIALIZERS.register(
                    "true_ender_pearl_upgrade",
                    ModRecipeSerializers
                            ::createSerializer
            );

    public static void register(
            IEventBus modBus
    ) {
        RECIPE_SERIALIZERS.register(
                modBus
        );
    }

    public static void bindCommonReferences() {
        ModRecipeSerializers.bindNeoForge(
                TRUE_ENDER_PEARL_UPGRADE.get()
        );
    }

    private NeoForgeModRecipeSerializers() {
    }
}