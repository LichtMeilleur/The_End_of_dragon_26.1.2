package com.licht_meilleur.the_end_of_dragon.client.screen;

import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class TedVillageTradeScreen
        extends AbstractContainerScreen<
        TedVillageTradeMenu> {

    private static final int PANEL_HEIGHT = 185;

    private static final int PANEL_COLOR =
            0xFF29243A;

    private static final int INNER_COLOR =
            0xFF171421;

    private static final int BORDER_COLOR =
            0xFFA785D8;

    private static final int SLOT_BACKGROUND_COLOR =
            0xFF100D18;

    private static final int SLOT_BORDER_COLOR =
            0xFF8F78AF;

    public TedVillageTradeScreen(
            TedVillageTradeMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title
        );

        /*
         * imageWidthとimageHeightはfinalなので
         * 代入しない。
         */
        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 91;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int left = this.leftPos;
        int top = this.topPos;

        /*
         * 画面全体。
         */
        graphics.fill(
                left,
                top,
                left + this.imageWidth,
                top + PANEL_HEIGHT,
                BORDER_COLOR
        );

        graphics.fill(
                left + 1,
                top + 1,
                left + this.imageWidth - 1,
                top + PANEL_HEIGHT - 1,
                PANEL_COLOR
        );

        /*
         * 通常取引欄。
         */
        graphics.fill(
                left + 8,
                top + 25,
                left + 88,
                top + 80,
                INNER_COLOR
        );

        /*
         * 作業台取引欄。
         */
        graphics.fill(
                left + 96,
                top + 25,
                left + 168,
                top + 80,
                INNER_COLOR
        );

        /*
         * プレイヤーインベントリ欄。
         */
        graphics.fill(
                left + 4,
                top + 88,
                left + 172,
                top + 181,
                INNER_COLOR
        );

        /*
         * 通常取引用の2スロット。
         */
        drawSlotBackground(
                graphics,
                left + 35,
                top + 48
        );

        drawSlotBackground(
                graphics,
                left + 57,
                top + 48
        );

        /*
         * 作業台取引用の4スロット。
         */
        drawSlotBackground(
                graphics,
                left + 119,
                top + 39
        );

        drawSlotBackground(
                graphics,
                left + 141,
                top + 39
        );

        drawSlotBackground(
                graphics,
                left + 119,
                top + 61
        );

        drawSlotBackground(
                graphics,
                left + 141,
                top + 61
        );

        /*
         * プレイヤーインベントリ。
         */
        for (int row = 0;
             row < 3;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                drawSlotBackground(
                        graphics,
                        left + 8 + column * 18,
                        top + 103 + row * 18
                );
            }
        }

        /*
         * ホットバー。
         */
        for (int column = 0;
             column < 9;
             column++) {

            drawSlotBackground(
                    graphics,
                    left + 8 + column * 18,
                    top + 161
            );
        }

        /*
         * 独自ラベル。
         */
        graphics.text(
                this.font,
                Component.translatable(
                        "gui.the_end_of_dragon.trade.input"
                ),
                left + 12,
                top + 29,
                0xFFD8C8EF,
                false
        );

        graphics.text(
                this.font,
                Component.translatable(
                        "gui.the_end_of_dragon.trade.work_bench"
                ),
                left + 100,
                top + 29,
                0xFFD8C8EF,
                false
        );
    }

    private static void drawSlotBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(
                x - 1,
                y - 1,
                x + 17,
                y + 17,
                SLOT_BORDER_COLOR
        );

        graphics.fill(
                x,
                y,
                x + 16,
                y + 16,
                SLOT_BACKGROUND_COLOR
        );
    }
}