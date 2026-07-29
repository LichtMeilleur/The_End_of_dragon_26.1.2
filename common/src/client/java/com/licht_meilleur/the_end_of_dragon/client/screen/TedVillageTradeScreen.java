package com.licht_meilleur.the_end_of_dragon.client.screen;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client
        .TedVillageTradeClientHandler;
import com.licht_meilleur.the_end_of_dragon.network
        .TedExecuteTradePayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenTradeScreenPayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeEntryData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeIngredientData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedVillageTradeNetwork;
import com.licht_meilleur.the_end_of_dragon.world.village.trade
        .TedVillageTradeMenu;
import com.licht_meilleur.the_end_of_dragon.world.village.trade
        .TedVillageTradeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory
        .AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TedVillageTradeScreen
        extends AbstractContainerScreen<
        TedVillageTradeMenu> {

    private static final int TEXTURE_SIZE =
            256;

    private static final Identifier MENU_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "the_end_of_dragon",
                    "textures/gui/trade/trade_menu.png"
            );

    private static final Identifier HAND_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "the_end_of_dragon",
                    "textures/gui/trade/enderman_hand.png"
            );

    private static final Identifier TRADE_SLOT_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "the_end_of_dragon",
                    "textures/gui/trade/trade_slot.png"
            );

    private static final Identifier WORK_BENCH_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "the_end_of_dragon",
                    "textures/gui/trade/work_bench_slot.png"
            );

    /*
     * この画像は256×26。
     */
    private static final Identifier TRADE_CONTENT_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "the_end_of_dragon",
                    "textures/gui/trade/trade_content.png"
            );
    private static final Identifier ITEM_SLOT_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    TheEndOfDragon.MOD_ID,
                    "textures/gui/item_slot.png"
            );


    private static final int PLAYER_INVENTORY_X = 92;
    private static final int PLAYER_INVENTORY_Y = 174;

    private static final int PLAYER_HOTBAR_Y = 232;

    /*
     * trade_content.pngの実寸。
     */
    private static final int TRADE_CONTENT_WIDTH =
            109;

    private static final int ROW_HEIGHT =
            26;

    /*
     * GUI本体と左一覧の間隔。
     */
    private static final int LIST_GAP =
            4;

    private static final int LIST_Y =
            8;

    private static final int LIST_WIDTH =
            TRADE_CONTENT_WIDTH;

    private static final int VISIBLE_ROWS =
            3;

    private static final int LIST_HEIGHT =
            ROW_HEIGHT * VISIBLE_ROWS;

    /*
     * 一覧内の素材表示位置。
     * 一覧背景の左上を基準にする。
     */
    private static final int ROW_FIRST_INGREDIENT_X =
            13;

    private static final int ROW_INGREDIENT_Y =
            5;

    private static final int ROW_INGREDIENT_SPACING =
            18;

    /*
     * 一覧内の完成品表示位置。
     */
    private static final int ROW_RESULT_X =
            82;

    private static final int ROW_RESULT_Y =
            5;



    /*
     * 通常交換台：上、中央の2枠。
     */
    private static final int[] NORMAL_INGREDIENT_X = {
            159,
            159
    };

    private static final int[] NORMAL_INGREDIENT_Y = {
            22,
            47
    };

    /*
     * 作業台：上、黄色、ピンク、緑の4枠。
     */
    private static final int[] WORK_BENCH_INGREDIENT_X = {
            159, // 上
            159, // 黄色
            106, // ピンク
            214  // 緑
    };

    private static final int[] WORK_BENCH_INGREDIENT_Y = {
            18,   // 上
            72,  // 黄色
            110, // ピンク
            110  // 緑
    };

    /*
     * 黒い手の先：完成品。
     */
    private static final int HAND_RESULT_X =
            52;

    private static final int HAND_RESULT_Y =
            160;

    /*
     * 手の上の完成品を押せる範囲。
     */
    private static final int EXECUTE_AREA_X =
            HAND_RESULT_X - 4;

    private static final int EXECUTE_AREA_Y =
            HAND_RESULT_Y - 4;

    private static final int EXECUTE_AREA_WIDTH =
            24;

    private static final int EXECUTE_AREA_HEIGHT =
            24;

    /*
     * 信頼度表示位置。
     */
    private static final int TRUST_TEXT_X =
            119;

    private static final int TRUST_TEXT_Y =
            9;

    private List<TedTradeEntryData> trades =
            List.of();

    private int technicianEntityId =
            -1;

    private int trustPoints =
            0;

    private int trustCap =
            0;

    private int trustLevel =
            0;

    /*
     * 一覧の先頭に表示する取引番号。
     */
    private int scrollOffset =
            0;

    public TedVillageTradeScreen(
            TedVillageTradeMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        /*
         * 独自画像を使用するため、
         * バニラのタイトルとインベントリ名を画面外へ移動。
         */
        this.titleLabelX =
                10000;

        this.titleLabelY =
                10000;

        this.inventoryLabelX =
                10000;

        this.inventoryLabelY =
                10000;
    }

    @Override
    protected void init() {
        super.init();

        /*
         * Menu画面が開くより先にPayloadを受け取っていた場合、
         * 保留されているPayloadをここで反映する。
         */
        TedOpenTradeScreenPayload pending =
                TedVillageTradeClientHandler
                        .takePendingPayload(
                                this.menu
                                        .getTechnicianEntityId()
                        );

        if (pending != null) {
            this.applyPayload(
                    pending
            );
        }
    }

    /*
     * 初回表示と、取引成功後の再同期の両方で使用する。
     */
    public void applyPayload(
            TedOpenTradeScreenPayload payload
    ) {
        if (payload == null) {
            return;
        }

        int menuTechnicianId =
                this.menu
                        .getTechnicianEntityId();

        /*
         * 別の技術者用Payloadは受け付けない。
         */
        if (menuTechnicianId >= 0
                && menuTechnicianId
                != payload.technicianEntityId()) {
            return;
        }

        this.technicianEntityId =
                payload.technicianEntityId();

        this.trustPoints =
                payload.trustPoints();

        this.trustCap =
                payload.trustCap();

        this.trustLevel =
                payload.trustLevel();

        this.trades =
                payload.trades() == null
                        ? List.of()
                        : List.copyOf(
                        payload.trades()
                );

        int selectedIndex =
                this.menu
                        .getSelectedTradeIndex();

        /*
         * 現在の選択番号が使えない場合は、
         * 解禁済みの最初の取引を選択する。
         */
        if (selectedIndex < 0
                || selectedIndex
                >= this.trades.size()) {

            int initialIndex =
                    this.findInitialTradeIndex();

            if (initialIndex >= 0) {
                this.selectTrade(
                        initialIndex
                );
            }
        }

        this.clampScrollOffset();
        this.ensureSelectedTradeVisible();
    }

    private int getTradeListLeft() {
        /*
         * 原則としてGUI本体の左へ出す。
         *
         * 左側の空間が足りない小さい画面では、
         * 画面左端から2pxの位置までに制限する。
         */
        return Math.max(
                2,
                this.leftPos
                        - LIST_WIDTH
                        - LIST_GAP
        );
    }

    private int findInitialTradeIndex() {
        if (this.trades.isEmpty()) {
            return -1;
        }

        for (int index = 0;
             index < this.trades.size();
             index++) {

            if (this.isUnlocked(
                    this.trades.get(index)
            )) {
                return index;
            }
        }

        /*
         * 全取引が未解禁の場合も、
         * ロック状態を確認できるよう先頭を選ぶ。
         */
        return 0;
    }

    private void selectTrade(
            int tradeIndex
    ) {
        if (tradeIndex < 0
                || tradeIndex
                >= this.trades.size()) {
            return;
        }

        /*
         * クライアント側の表示を即座に切り替える。
         */
        this.menu.selectTradeClient(
                tradeIndex
        );

        this.ensureSelectedTradeVisible();

        /*
         * サーバー側Menuにも選択状態を通知する。
         */
        if (this.minecraft != null
                && this.minecraft.gameMode != null) {

            this.minecraft.gameMode
                    .handleInventoryButtonClick(
                            this.menu.containerId,
                            TedVillageTradeMenu
                                    .getTradeButtonId(
                                            tradeIndex
                                    )
                    );
        }
    }

    private TedTradeEntryData getSelectedTrade() {
        int selectedIndex =
                this.menu
                        .getSelectedTradeIndex();

        if (selectedIndex < 0
                || selectedIndex
                >= this.trades.size()) {
            return null;
        }

        return this.trades.get(
                selectedIndex
        );
    }

    private boolean isUnlocked(
            TedTradeEntryData trade
    ) {
        return trade != null
                && this.trustLevel
                >= trade.requiredTrustLevel();
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        int x =
                this.leftPos;

        int y =
                this.topPos;

        TedTradeEntryData selectedTrade =
                this.getSelectedTrade();

        /*
         * 共通背景。
         */
        this.blitFullTexture(
                graphics,
                MENU_TEXTURE,
                x,
                y
        );

        /*
         * 手は取引台より奥。
         */
        this.blitFullTexture(
                graphics,
                HAND_TEXTURE,
                x,
                y
        );

        /*
         * 取引台は手より手前。
         */
        if (selectedTrade != null) {
            if (selectedTrade.type()
                    == TedVillageTradeType.WORK_BENCH) {

                this.blitFullTexture(
                        graphics,
                        WORK_BENCH_TEXTURE,
                        x,
                        y
                );
            } else {
                this.blitFullTexture(
                        graphics,
                        TRADE_SLOT_TEXTURE,
                        x,
                        y
                );
            }
        }

        /*
         * プレイヤーインベントリの枠。
         * 実際のアイテムより先に描画する。
         */
        this.drawPlayerSlotBackgrounds(
                graphics
        );

        /*
         * 選択中の交換台・作業台の
         * 素材スロットと完成品スロット。
         */
        this.drawSelectedTradeDetails(
                graphics
        );

        /*
         * GUI本体左側の取引一覧。
         */
        this.drawTradeList(
                graphics,
                mouseX,
                mouseY
        );

        this.drawTrustText(
                graphics
        );
    }

    private void blitFullTexture(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0F,
                0.0F,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    private void drawTradeList(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (this.trades.isEmpty()) {
            return;
        }

        int selectedIndex =
                this.menu
                        .getSelectedTradeIndex();

        int listLeft =
                this.getTradeListLeft();

        int listTop =
                this.topPos
                        + LIST_Y;

        for (int visibleIndex = 0;
             visibleIndex < VISIBLE_ROWS;
             visibleIndex++) {

            int tradeIndex =
                    this.scrollOffset
                            + visibleIndex;

            if (tradeIndex
                    >= this.trades.size()) {
                break;
            }

            TedTradeEntryData trade =
                    this.trades.get(
                            tradeIndex
                    );

            int rowX =
                    listLeft;

            int rowY =
                    listTop
                            + visibleIndex
                            * ROW_HEIGHT;

            /*
             * trade_content.pngは109×26。
             */
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TRADE_CONTENT_TEXTURE,
                    rowX,
                    rowY,
                    0.0F,
                    0.0F,
                    TRADE_CONTENT_WIDTH,
                    ROW_HEIGHT,
                    TRADE_CONTENT_WIDTH,
                    ROW_HEIGHT
            );

            /*
             * 選択中の行。
             */
            if (tradeIndex
                    == selectedIndex) {

                graphics.fill(
                        rowX,
                        rowY,
                        rowX + LIST_WIDTH,
                        rowY + ROW_HEIGHT,
                        0x553F1F6F
                );
            } else if (isInside(
                    mouseX,
                    mouseY,
                    rowX,
                    rowY,
                    LIST_WIDTH,
                    ROW_HEIGHT
            )) {
                /*
                 * マウスが乗っている行。
                 */
                graphics.fill(
                        rowX,
                        rowY,
                        rowX + LIST_WIDTH,
                        rowY + ROW_HEIGHT,
                        0x334F3F5F
                );
            }

            this.drawTradeRowItems(
                    graphics,
                    trade,
                    rowX,
                    rowY
            );

            /*
             * 未解禁の取引。
             */
            if (!this.isUnlocked(
                    trade
            )) {
                graphics.fill(
                        rowX,
                        rowY,
                        rowX + LIST_WIDTH,
                        rowY + ROW_HEIGHT,
                        0x99000000
                );
            }
        }
    }

    private void drawTradeRowItems(
            GuiGraphicsExtractor graphics,
            TedTradeEntryData trade,
            int rowX,
            int rowY
    ) {
        List<TedTradeIngredientData> ingredients =
                trade.ingredients();

        int visibleIngredientCount =
                Math.min(
                        ingredients.size(),
                        3
                );

        for (int index = 0;
             index < visibleIngredientCount;
             index++) {

            ItemStack stack =
                    ingredients.get(index)
                            .stack();

            if (stack.isEmpty()) {
                continue;
            }

            int itemX =
                    rowX
                            + ROW_FIRST_INGREDIENT_X
                            + index
                            * ROW_INGREDIENT_SPACING;

            int itemY =
                    rowY
                            + ROW_INGREDIENT_Y;

            this.drawItemSlot(
                    graphics,
                    itemX,
                    itemY
            );

            graphics.item(
                    stack,
                    itemX,
                    itemY
            );

            graphics.itemDecorations(
                    this.font,
                    stack,
                    itemX,
                    itemY
            );
        }

        ItemStack result =
                trade.result();

        if (result.isEmpty()) {
            return;
        }

        int resultX =
                rowX
                        + ROW_RESULT_X;

        int resultY =
                rowY
                        + ROW_RESULT_Y;

        this.drawItemSlot(
                graphics,
                resultX,
                resultY
        );

        graphics.item(
                result,
                resultX,
                resultY
        );

        graphics.itemDecorations(
                this.font,
                result,
                resultX,
                resultY
        );
    }

    private void drawSelectedTradeDetails(
            GuiGraphicsExtractor graphics
    ) {
        TedTradeEntryData trade =
                this.getSelectedTrade();

        if (trade == null) {
            return;
        }

        boolean unlocked =
                this.isUnlocked(
                        trade
                );

        if (trade.type()
                == TedVillageTradeType.WORK_BENCH) {

            this.drawWorkBenchIngredients(
                    graphics,
                    trade,
                    unlocked
            );
        } else {
            this.drawNormalTradeIngredients(
                    graphics,
                    trade,
                    unlocked
            );
        }

        /*
         * 完成品スロットは、
         * 取引がロック中でも場所を確認できるよう表示する。
         */
        int resultX =
                this.leftPos
                        + HAND_RESULT_X;

        int resultY =
                this.topPos
                        + HAND_RESULT_Y;

        /*
         * 黒い手の先に完成品スロット。
         */
        this.drawItemSlot(
                graphics,
                resultX,
                resultY
        );

        if (unlocked) {
            ItemStack result =
                    trade.result();

            if (!result.isEmpty()) {
                graphics.item(
                        result,
                        resultX,
                        resultY
                );

                graphics.itemDecorations(
                        this.font,
                        result,
                        resultX,
                        resultY
                );
            }
        }
    }

    private void drawNormalTradeIngredients(
            GuiGraphicsExtractor graphics,
            TedTradeEntryData trade,
            boolean unlocked
    ) {
        List<TedTradeIngredientData> ingredients =
                trade.ingredients();

        for (int index = 0;
             index < NORMAL_INGREDIENT_X.length;
             index++) {

            int itemX =
                    this.leftPos
                            + NORMAL_INGREDIENT_X[index];

            int itemY =
                    this.topPos
                            + NORMAL_INGREDIENT_Y[index];

            /*
             * 空でも素材を置く場所として枠を表示。
             */
            this.drawItemSlot(
                    graphics,
                    itemX,
                    itemY
            );

            if (!unlocked
                    || index >= ingredients.size()) {
                continue;
            }

            ItemStack stack =
                    ingredients.get(index)
                            .stack();

            if (stack.isEmpty()) {
                continue;
            }

            graphics.item(
                    stack,
                    itemX,
                    itemY
            );

            graphics.itemDecorations(
                    this.font,
                    stack,
                    itemX,
                    itemY
            );
        }
    }

    private void drawWorkBenchIngredients(
            GuiGraphicsExtractor graphics,
            TedTradeEntryData trade,
            boolean unlocked
    ) {
        List<TedTradeIngredientData> ingredients =
                trade.ingredients();

        for (int index = 0;
             index < WORK_BENCH_INGREDIENT_X.length;
             index++) {

            int itemX =
                    this.leftPos
                            + WORK_BENCH_INGREDIENT_X[index];

            int itemY =
                    this.topPos
                            + WORK_BENCH_INGREDIENT_Y[index];

            /*
             * 素材が3種類しかない場合でも、
             * 作業台にある4枠自体はすべて表示。
             */
            this.drawItemSlot(
                    graphics,
                    itemX,
                    itemY
            );

            if (!unlocked
                    || index >= ingredients.size()) {
                continue;
            }

            ItemStack stack =
                    ingredients.get(index)
                            .stack();

            if (stack.isEmpty()) {
                continue;
            }

            graphics.item(
                    stack,
                    itemX,
                    itemY
            );

            graphics.itemDecorations(
                    this.font,
                    stack,
                    itemX,
                    itemY
            );
        }
    }

    private void drawTrustText(
            GuiGraphicsExtractor graphics
    ) {
        /*
         * 内部の信頼度が0始まりなら、
         * 表示上は1始まりにする。
         */
        int displayTrustLevel =
                this.trustLevel
                        + 1;

        Component text =
                Component.translatable(
                        "gui.the_end_of_dragon.trade.trust",
                        displayTrustLevel,
                        this.trustPoints,
                        this.trustCap
                );

        graphics.text(
                this.font,
                text,
                this.leftPos
                        + TRUST_TEXT_X,
                this.topPos
                        + TRUST_TEXT_Y,
                0xFFF2D47A,
                true
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        double mouseX =
                event.x();

        double mouseY =
                event.y();

        /*
         * 取引一覧の選択。
         */
        int clickedTradeIndex =
                this.getTradeIndexAt(
                        mouseX,
                        mouseY
                );

        if (clickedTradeIndex >= 0) {
            this.selectTrade(
                    clickedTradeIndex
            );

            return true;
        }

        /*
         * エンダーマンの手の上にある完成品を押すと
         * 現在選択中の取引を実行する。
         */
        if (isInside(
                mouseX,
                mouseY,
                this.leftPos
                        + EXECUTE_AREA_X,
                this.topPos
                        + EXECUTE_AREA_Y,
                EXECUTE_AREA_WIDTH,
                EXECUTE_AREA_HEIGHT
        )) {
            this.executeSelectedTrade();

            return true;
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private int getTradeIndexAt(
            double mouseX,
            double mouseY
    ) {
        int listLeft =
                this.getTradeListLeft();

        int listTop =
                this.topPos
                        + LIST_Y;

        if (!isInside(
                mouseX,
                mouseY,
                listLeft,
                listTop,
                LIST_WIDTH,
                LIST_HEIGHT
        )) {
            return -1;
        }

        int visibleRow =
                (int) (
                        mouseY
                                - listTop
                ) / ROW_HEIGHT;

        int tradeIndex =
                this.scrollOffset
                        + visibleRow;

        if (tradeIndex < 0
                || tradeIndex
                >= this.trades.size()) {
            return -1;
        }

        return tradeIndex;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        /*
         * 一覧外でのスクロールは親画面へ渡す。
         */
        if (!isInside(
                mouseX,
                mouseY,
                this.getTradeListLeft(),
                this.topPos + LIST_Y,
                LIST_WIDTH,
                LIST_HEIGHT
        )) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    horizontalAmount,
                    verticalAmount
            );
        }

        if (verticalAmount < 0.0D) {
            this.scrollOffset++;
        } else if (verticalAmount > 0.0D) {
            this.scrollOffset--;
        } else {
            return false;
        }

        this.clampScrollOffset();

        return true;
    }

    private void clampScrollOffset() {
        int maximumOffset =
                Math.max(
                        0,
                        this.trades.size()
                                - VISIBLE_ROWS
                );

        this.scrollOffset =
                Math.clamp(
                        this.scrollOffset,
                        0,
                        maximumOffset
                );
    }

    /*
     * 選択された取引が現在の表示範囲外なら、
     * 一覧を自動的に移動する。
     */
    private void ensureSelectedTradeVisible() {
        int selectedIndex =
                this.menu
                        .getSelectedTradeIndex();

        if (selectedIndex < 0) {
            return;
        }

        if (selectedIndex
                < this.scrollOffset) {
            this.scrollOffset =
                    selectedIndex;
        } else if (selectedIndex
                >= this.scrollOffset
                + VISIBLE_ROWS) {

            this.scrollOffset =
                    selectedIndex
                            - VISIBLE_ROWS
                            + 1;
        }

        this.clampScrollOffset();
    }

    private void executeSelectedTrade() {
        TedTradeEntryData trade =
                this.getSelectedTrade();

        if (trade == null
                || !this.isUnlocked(trade)) {
            return;
        }

        int entityId =
                this.menu
                        .getTechnicianEntityId();

        if (entityId < 0) {
            entityId =
                    this.technicianEntityId;
        }

        if (entityId < 0) {
            return;
        }

        TedVillageTradeNetwork
                .sendExecuteTrade(
                        new TedExecuteTradePayload(
                                entityId,
                                trade.tradeId()
                        )
                );
    }

    private static boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    private void drawItemSlot(
            GuiGraphicsExtractor graphics,
            int itemX,
            int itemY
    ) {
        /*
         * itemX・itemYは16×16アイテムの左上。
         * 18×18の枠なので、上下左右へ1px広げる。
         */
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ITEM_SLOT_TEXTURE,
                itemX - 1,
                itemY - 1,
                0.0F,
                0.0F,
                18,
                18,
                18,
                18
        );
    }

    private void drawPlayerSlotBackgrounds(
            GuiGraphicsExtractor graphics
    ) {
        /*
         * Menuの末尾36個がプレイヤーインベントリ。
         *
         * それ以前に登録されている取引用スロットは、
         * ここでは描画しない。
         */
        int playerSlotStart =
                Math.max(
                        0,
                        this.menu.slots.size() - 36
                );

        for (int index = playerSlotStart;
             index < this.menu.slots.size();
             index++) {

            Slot slot =
                    this.menu.slots.get(index);

            this.drawItemSlot(
                    graphics,
                    this.leftPos + slot.x,
                    this.topPos + slot.y
            );
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}