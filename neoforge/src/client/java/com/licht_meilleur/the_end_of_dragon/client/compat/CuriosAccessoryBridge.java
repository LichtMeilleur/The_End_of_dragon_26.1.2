package com.licht_meilleur.the_end_of_dragon.client.compat;

import com.licht_meilleur.the_end_of_dragon.compat.TedAccessories;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;

public final class CuriosAccessoryBridge {
    private CuriosAccessoryBridge() {}

    public static void register() {
        TedAccessories.setBridge(entity -> {
            List<ItemStack> result = new ArrayList<>();

            CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            result.add(stack);
                        }
                    }
                }
            });

            return result;
        });
    }
}