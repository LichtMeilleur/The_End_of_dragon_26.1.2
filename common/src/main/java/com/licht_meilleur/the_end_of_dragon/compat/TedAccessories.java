package com.licht_meilleur.the_end_of_dragon.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TedAccessories {
    private static TedAccessoryBridge bridge = TedAccessoryBridge.EMPTY;

    private TedAccessories() {
    }

    public static void setBridge(TedAccessoryBridge newBridge) {
        bridge = newBridge != null ? newBridge : TedAccessoryBridge.EMPTY;
    }

    public static List<ItemStack> getAccessories(LivingEntity entity) {
        return bridge.getAccessories(entity);
    }
}