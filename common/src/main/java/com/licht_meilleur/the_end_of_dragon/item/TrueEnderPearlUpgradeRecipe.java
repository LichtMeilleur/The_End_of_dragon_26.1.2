package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.registry.ModRecipeSerializers;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class TrueEnderPearlUpgradeRecipe
        extends CustomRecipe {

    public TrueEnderPearlUpgradeRecipe() {
    }

    @Override
    public boolean matches(
            CraftingInput input,
            Level level
    ) {
        UpgradeInput upgradeInput =
                findUpgradeInput(input);

        if (upgradeInput == null) {
            return false;
        }

        int currentLevel =
                TrueEnderPearlLevel.get(
                        upgradeInput.pearl()
                );

        /*
         * 必ず1段階ずつ強化する。
         *
         * I + 応用書 -> II
         * II + 上級書 -> III
         * III + 熟達書 -> IV
         * IV + 極意書 -> V
         */
        return currentLevel
                == upgradeInput.targetLevel() - 1;
    }

    @Override
    public ItemStack assemble(
            CraftingInput input
    ) {
        UpgradeInput upgradeInput =
                findUpgradeInput(input);

        if (upgradeInput == null) {
            return ItemStack.EMPTY;
        }

        int currentLevel =
                TrueEnderPearlLevel.get(
                        upgradeInput.pearl()
                );

        if (currentLevel
                != upgradeInput.targetLevel() - 1) {
            return ItemStack.EMPTY;
        }

        ItemStack result =
                upgradeInput.pearl()
                        .copy();

        result.setCount(1);

        TrueEnderPearlLevel.set(
                result,
                upgradeInput.targetLevel()
        );

        return result;
    }


    @Override
    public RecipeSerializer<
            TrueEnderPearlUpgradeRecipe>
    getSerializer() {
        return ModRecipeSerializers
                .TRUE_ENDER_PEARL_UPGRADE;
    }

    private static UpgradeInput findUpgradeInput(
            CraftingInput input
    ) {
        ItemStack pearl =
                ItemStack.EMPTY;

        ItemStack book =
                ItemStack.EMPTY;

        int occupiedSlots =
                0;

        for (int slot = 0;
             slot < input.size();
             slot++) {

            ItemStack stack =
                    input.getItem(
                            slot
                    );

            if (stack.isEmpty()) {
                continue;
            }

            occupiedSlots++;

            /*
             * 真のエンダーパールは1個だけ。
             */
            if (stack.is(
                    ModItems.TRUE_ENDER_PEARL
            )) {
                if (!pearl.isEmpty()) {
                    return null;
                }

                pearl = stack;
                continue;
            }

            /*
             * 強化書は1冊だけ。
             */
            if (getTargetLevel(stack) > 0) {
                if (!book.isEmpty()) {
                    return null;
                }

                book = stack;
                continue;
            }

            /*
             * 無関係な材料が入っている。
             */
            return null;
        }

        if (occupiedSlots != 2
                || pearl.isEmpty()
                || book.isEmpty()) {
            return null;
        }

        int targetLevel =
                getTargetLevel(
                        book
                );

        if (targetLevel < 2
                || targetLevel
                > TrueEnderPearlLevel.MAX_LEVEL) {
            return null;
        }

        return new UpgradeInput(
                pearl,
                targetLevel
        );
    }

    private static int getTargetLevel(
            ItemStack book
    ) {
        if (book.is(
                ModItems
                        .ENDER_PEARL_APPLICATION_BOOK
        )) {
            return 2;
        }

        if (book.is(
                ModItems
                        .ENDER_PEARL_ADVANCED_BOOK
        )) {
            return 3;
        }

        if (book.is(
                ModItems
                        .ENDER_PEARL_MASTERY_BOOK
        )) {
            return 4;
        }

        if (book.is(
                ModItems
                        .ENDER_PEARL_SECRET_BOOK
        )) {
            return 5;
        }

        return -1;
    }

    private record UpgradeInput(
            ItemStack pearl,
            int targetLevel
    ) {
    }
}