package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class TedVillageTradeMenu
        extends AbstractContainerMenu {


    private static final int PLAYER_INVENTORY_X =
            100;

    private static final int PLAYER_INVENTORY_Y =
            180;

    private static final int PLAYER_HOTBAR_Y =
            230;


    public static final int TRADE_SLOT_START =
            0;

    public static final int TRADE_SLOT_COUNT =
            2;

    public static final int WORK_BENCH_SLOT_START =
            TRADE_SLOT_START
                    + TRADE_SLOT_COUNT;

    public static final int WORK_BENCH_SLOT_COUNT =
            4;

    public static final int TOTAL_INPUT_SLOT_COUNT =
            TRADE_SLOT_COUNT
                    + WORK_BENCH_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_START =
            TOTAL_INPUT_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START
                    + 27;

    private static final int HOTBAR_START =
            PLAYER_INVENTORY_END;

    private static final int HOTBAR_END =
            HOTBAR_START
                    + 9;

    private static final int TRADE_BUTTON_BASE =
            1000;

    private final Container inputContainer =
            new SimpleContainer(
                    TOTAL_INPUT_SLOT_COUNT
            );

    private final DataSlot technicianEntityId =
            DataSlot.standalone();

    private final DataSlot selectedTradeIndex =
            DataSlot.standalone();

    public TedVillageTradeMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                -1
        );
    }

    public TedVillageTradeMenu(
            int containerId,
            Inventory playerInventory,
            int technicianEntityId
    ) {
        super(
                ModMenus.TED_VILLAGE_TRADE,
                containerId
        );

        this.technicianEntityId.set(
                technicianEntityId
        );

        this.selectedTradeIndex.set(
                -1
        );

        this.addDataSlot(
                this.technicianEntityId
        );

        this.addDataSlot(
                this.selectedTradeIndex
        );

        /*
         * 通常交換用・縦2スロット。
         */
        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        0,
                        159,
                        22,
                        TedVillageTradeType.NORMAL
                )
        );

        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        1,
                        159,
                        47,
                        TedVillageTradeType.NORMAL
                )
        );

        /*
         * 作業台用・4スロット。
         */
        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        2,
                        159,
                        18,
                        TedVillageTradeType.WORK_BENCH
                )
        );

        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        3,
                        159,
                        72,
                        TedVillageTradeType.WORK_BENCH
                )
        );

        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        4,
                        106,
                        110,
                        TedVillageTradeType.WORK_BENCH
                )
        );

        this.addSlot(
                new TradeInputSlot(
                        this.inputContainer,
                        5,
                        214,
                        110,
                        TedVillageTradeType.WORK_BENCH
                )
        );

        //プレイヤーインベントリ＆ホットバー
        this.addPlayerInventory(
                playerInventory
        );
    }

    public Container getInputContainer() {
        return this.inputContainer;
    }

    public int getTechnicianEntityId() {
        return this.technicianEntityId.get();
    }

    public int getSelectedTradeIndex() {
        return this.selectedTradeIndex.get();
    }

    public TedVillageTradeDefinition
    getSelectedTrade() {
        int index =
                this.getSelectedTradeIndex();

        if (index < 0) {
            return null;
        }

        return TedVillageTradeRegistry
                .getByIndex(
                        index
                );
    }

    public TedVillageTradeType
    getSelectedTradeType() {
        TedVillageTradeDefinition trade =
                this.getSelectedTrade();

        return trade == null
                ? null
                : trade.type();
    }

    public boolean isInputSlotActive(
            TedVillageTradeType type
    ) {
        return type != null
                && type
                == this.getSelectedTradeType();
    }

    /*
     * Screenで一覧をクリックした直後に、
     * クライアント側表示を即座に切り替える。
     */
    public void selectTradeClient(
            int tradeIndex
    ) {
        this.selectedTradeIndex.set(
                tradeIndex
        );
    }

    public static int getTradeButtonId(
            int tradeIndex
    ) {
        return TRADE_BUTTON_BASE
                + tradeIndex;
    }

    @Override
    public boolean clickMenuButton(
            Player player,
            int buttonId
    ) {
        int tradeIndex =
                buttonId
                        - TRADE_BUTTON_BASE;

        TedVillageTradeDefinition trade =
                TedVillageTradeRegistry
                        .getByIndex(
                                tradeIndex
                        );

        if (trade == null) {
            return false;
        }

        if (!player.level()
                .isClientSide()) {

            TedVillageTradeDefinition oldTrade =
                    this.getSelectedTrade();

            if (oldTrade != null
                    && oldTrade.type()
                    != trade.type()) {

                this.returnInputRange(
                        player,
                        oldTrade.type()
                );
            }
        }

        this.selectedTradeIndex.set(
                tradeIndex
        );

        this.broadcastChanges();

        return true;
    }

    private void returnInputRange(
            Player player,
            TedVillageTradeType type
    ) {
        int start =
                type
                        == TedVillageTradeType.NORMAL
                        ? TRADE_SLOT_START
                        : WORK_BENCH_SLOT_START;

        int count =
                type.getInputSlotCount();

        for (int index = start;
             index < start + count;
             index++) {

            ItemStack stack =
                    this.inputContainer
                            .removeItemNoUpdate(
                                    index
                            );

            if (stack.isEmpty()) {
                continue;
            }

            if (player
                    instanceof ServerPlayer serverPlayer) {

                serverPlayer
                        .getInventory()
                        .placeItemBackInInventory(
                                stack
                        );
            }
        }

        this.inputContainer
                .setChanged();
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        Slot slot =
                this.slots.get(
                        slotIndex
                );

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                slot.getItem();

        ItemStack originalStack =
                sourceStack.copy();

        if (slotIndex
                < TOTAL_INPUT_SLOT_COUNT) {

            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            TedVillageTradeDefinition trade =
                    this.getSelectedTrade();

            if (trade == null) {
                return ItemStack.EMPTY;
            }

            int targetStart =
                    trade.type()
                            == TedVillageTradeType.NORMAL
                            ? TRADE_SLOT_START
                            : WORK_BENCH_SLOT_START;

            int targetEnd =
                    targetStart
                            + trade.type()
                            .getInputSlotCount();

            if (!this.moveItemStackTo(
                    sourceStack,
                    targetStart,
                    targetEnd,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            slot.set(
                    ItemStack.EMPTY
            );
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount()
                == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                sourceStack
        );

        return originalStack;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        Entity entity =
                player.level()
                        .getEntity(
                                this.getTechnicianEntityId()
                        );

        return entity
                instanceof TedTechEndermanEntity technician
                && technician.isAlive()
                && player.distanceToSqr(
                technician
        ) <= 64.0D;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        if (!(player
                instanceof ServerPlayer serverPlayer)) {
            return;
        }

        /*
         * 取引スロットに残ったアイテムを返す。
         */
        this.clearContainer(
                serverPlayer,
                this.inputContainer
        );

        /*
         * DataSlotから実際のEntity IDを取得する。
         */
        Entity entity =
                serverPlayer.level()
                        .getEntity(
                                this.technicianEntityId.get()
                        );

        if (entity
                instanceof TedTechEndermanEntity technician) {

            technician.endMenuInteraction(
                    serverPlayer
            );
        }
    }

    private final class TradeInputSlot
            extends Slot {

        private final TedVillageTradeType type;

        private TradeInputSlot(
                Container container,
                int slot,
                int x,
                int y,
                TedVillageTradeType type
        ) {
            super(
                    container,
                    slot,
                    x,
                    y
            );

            this.type =
                    type;
        }

        @Override
        public boolean isActive() {
            return TedVillageTradeMenu.this
                    .isInputSlotActive(
                            this.type
                    );
        }

        @Override
        public boolean mayPlace(
                ItemStack stack
        ) {
            return this.isActive();
        }

        @Override
        public boolean mayPickup(
                Player player
        ) {
            return this.isActive();
        }
    }



    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        /*
         * プレイヤーインベントリ。
         */
        for (int row = 0;
             row < 3;
             row++) {

            for (int column = 0;
                 column < 9;
                 column++) {

                this.addSlot(
                        new Slot(
                                playerInventory,
                                column
                                        + row * 9
                                        + 9,
                                PLAYER_INVENTORY_X
                                        + column * 18,
                                PLAYER_INVENTORY_Y
                                        + row * 18
                        )
                );
            }
        }

        /*
         * ホットバー。
         */
        for (int column = 0;
             column < 9;
             column++) {

            this.addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            PLAYER_INVENTORY_X
                                    + column * 18,
                            PLAYER_HOTBAR_Y
                    )
            );
        }
    }
}