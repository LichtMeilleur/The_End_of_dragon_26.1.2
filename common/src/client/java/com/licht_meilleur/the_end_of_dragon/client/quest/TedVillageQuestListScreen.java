package com.licht_meilleur.the_end_of_dragon.client.quest;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.network
        .TedQuestClientNetwork;
import com.licht_meilleur.the_end_of_dragon.network
        .TedQuestListEntryData;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuest;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuestRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class TedVillageQuestListScreen
        extends Screen {

    private static final Identifier ENTRY_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/quest_list.png"
            );

    /*
     * PNGそのもののサイズ。
     */
    private static final int ENTRY_TEXTURE_WIDTH =
            115;

    private static final int ENTRY_TEXTURE_HEIGHT =
            30;

    /*
     * GUI上でも画像を原寸で表示する。
     *
     * MinecraftのGUIスケールが2なら、
     * 実画面上では約230×60ピクセルに見える。
     */
    private static final int ENTRY_WIDTH =
            115;

    private static final int ENTRY_HEIGHT =
            30;

    private static final int ENTRY_GAP =
            4;

    private static final int TITLE_MARGIN =
            10;

    private final List<TedQuestListEntryData> entries;

    private int leftPos;
    private int topPos;

    public TedVillageQuestListScreen(
            List<TedQuestListEntryData> entries
    ) {
        super(
                Component.translatable(
                        "gui.the_end_of_dragon.quest_list.title"
                )
        );

        this.entries =
                entries == null
                        ? List.of()
                        : List.copyOf(entries);
    }

    @Override
    protected void init() {
        int entryCount =
                Math.max(
                        1,
                        this.entries.size()
                );

        int totalHeight =
                entryCount * ENTRY_HEIGHT
                        + Math.max(
                        0,
                        entryCount - 1
                ) * ENTRY_GAP;

        this.leftPos =
                (this.width - ENTRY_WIDTH)
                        / 2;

        this.topPos =
                (this.height - totalHeight)
                        / 2;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                this.topPos - 16,
                0xFFFFFFFF
        );

        if (this.entries.isEmpty()) {
            graphics.centeredText(
                    this.font,
                    Component.translatable(
                            "gui.the_end_of_dragon.quest_list.empty"
                    ),
                    this.width / 2,
                    this.topPos + 10,
                    0xFFAAAAAA
            );

            return;
        }

        for (int index = 0;
             index < this.entries.size();
             index++) {

            TedQuestListEntryData entry =
                    this.entries.get(index);

            int y =
                    getEntryY(index);

            drawEntry(
                    graphics,
                    entry,
                    this.leftPos,
                    y,
                    mouseX,
                    mouseY
            );
        }
    }

    private void drawEntry(
            GuiGraphicsExtractor graphics,
            TedQuestListEntryData entry,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        TedVillageQuest quest =
                TedVillageQuestRegistry
                        .getBySerializedName(
                                entry.questId()
                        );

        if (quest == null) {
            return;
        }

        boolean hovered =
                isInsideEntry(
                        mouseX,
                        mouseY,
                        x,
                        y
                );

        /*
         * ホバー中だけ少し明るくする。
         */
        int textureColor =
                hovered
                        ? 0xFFFFFFFF
                        : 0xFFE8E8E8;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ENTRY_TEXTURE,
                x,
                y,
                0,
                0,
                ENTRY_WIDTH,
                ENTRY_HEIGHT,
                ENTRY_TEXTURE_WIDTH,
                ENTRY_TEXTURE_HEIGHT,
                textureColor
        );

        int titleY =
                y
                        + (ENTRY_HEIGHT
                        - this.font.lineHeight)
                        / 2
                        - 3;

        int titleColor;

        if (entry.completable()) {
            /*
             * 納品可能。
             */
            titleColor =
                    0xFF286218;
        } else if (hovered) {
            titleColor =
                    0xFF4E204E;
        } else {
            titleColor =
                    0xFF2B172B;
        }

        graphics.text(
                this.font,
                quest.title(),
                x + TITLE_MARGIN,
                titleY,
                titleColor,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        /*
         * 左クリック以外は親へ渡す。
         *
         * buttonInfo().button() ではなく、
         * この環境では input() が使える可能性があります。
         * まずは event.input() で左クリック判定します。
         */
        if (event.input() != 0) {
            return super.mouseClicked(
                    event,
                    doubleClick
            );
        }

        double mouseX =
                event.x();

        double mouseY =
                event.y();

        for (int index = 0;
             index < this.entries.size();
             index++) {

            int y =
                    getEntryY(index);

            if (!isInsideEntry(
                    mouseX,
                    mouseY,
                    this.leftPos,
                    y
            )) {
                continue;
            }

            TedQuestListEntryData entry =
                    this.entries.get(index);

            TedQuestClientNetwork.selectQuest(
                    entry.questId()
            );

            return true;
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private int getEntryY(
            int index
    ) {
        return this.topPos
                + index
                * (ENTRY_HEIGHT + ENTRY_GAP);
    }

    private static boolean isInsideEntry(
            double mouseX,
            double mouseY,
            int x,
            int y
    ) {
        return mouseX >= x
                && mouseX < x + ENTRY_WIDTH
                && mouseY >= y
                && mouseY < y + ENTRY_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}