package com.licht_meilleur.the_end_of_dragon.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface TedAccessoryBridge {
    TedAccessoryBridge EMPTY = entity -> Collections.emptyList();

    List<ItemStack> getAccessories(LivingEntity entity);
}