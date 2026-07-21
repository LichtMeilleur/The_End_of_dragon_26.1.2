package com.licht_meilleur.the_end_of_dragon.compat;

import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class RoarEquipmentRules {

    private RoarEquipmentRules() {
    }

    public static boolean isNonDamageableExcluded(
            ItemStack stack
    ) {
        if (stack == null
                || stack.isEmpty()) {
            return false;
        }

        /*
         * この除外設定は、
         * 耐久値を持たないアイテムだけに適用する。
         */
        if (stack.isDamageableItem()) {
            return false;
        }

        Identifier id =
                BuiltInRegistries.ITEM
                        .getKey(stack.getItem());

        if (id == null) {
            return false;
        }

        String itemId =
                id.toString();

        String namespaceWildcard =
                id.getNamespace() + ":*";

        if (TedConfig.values
                .roarNonDamageableExclusions == null) {
            return false;
        }

        return TedConfig.values
                .roarNonDamageableExclusions
                .stream()
                .filter(configuredId ->
                        configuredId != null
                )
                .map(String::valueOf)
                .anyMatch(configuredId ->
                        itemId.equals(configuredId)
                                || namespaceWildcard.equals(
                                configuredId
                        )
                );
    }
}