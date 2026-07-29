package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class TedVillageTradeMenu
        extends AbstractContainerMenu {

    /*
     * 左側に表示する通常取引用スロット。
     */
    public static final int TRADE_SLOT_COUNT = 2;

    /*
     * 右側に表示する作業台取引用スロット。
     */
    public static final int WORK_BENCH_SLOT_COUNT = 4;

    public static final int TOTAL_INPUT_SLOT_COUNT =
            TRADE_SLOT_COUNT
                    + WORK_BENCH_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_START =
            TOTAL_INPUT_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;

    private static final int PLAYER_HOTBAR_START =
            PLAYER_INVENTORY_END;

    private static final int PLAYER_HOTBAR_END =
            PLAYER_HOTBAR_START + 9;

    private final Container inputContainer;

    private int technicianEntityId;

    /*
     * 選択されていない状態は空文字。
     */
    private String selectedTradeId = "";

    /*
     * サーバーからMenuを開く際に使用。
     */
    public TedVillageTradeMenu(
            int containerId,
            Inventory playerInventory,
            int technicianEntityId
    ) {
        this(
                containerId,
                playerInventory,
                technicianEntityId,
                new SimpleContainer(
                        TOTAL_INPUT_SLOT_COUNT
                )
        );
    }

    /*
     * FabricのMenuTypeがクライアント側で使用する
     * 追加データなしのコンストラクタ。
     *
     * 技術者Entity IDは、Menuの同期データとして
     * 後からサーバーから受け取る。
     */
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

    private TedVillageTradeMenu(
            int containerId,
            Inventory playerInventory,
            int technicianEntityId,
            Container inputContainer
    ) {
        super(
                ModMenus.TED_VILLAGE_TRADE,
                containerId
        );

        this.technicianEntityId =
                technicianEntityId;

        this.inputContainer =
                inputContainer;

        checkContainerSize(
                inputContainer,
                TOTAL_INPUT_SLOT_COUNT
        );

        this.addDataSlot(
                new DataSlot() {
                    @Override
                    public int get() {
                        return TedVillageTradeMenu.this
                                .technicianEntityId;
                    }

                    @Override
                    public void set(int value) {
                        TedVillageTradeMenu.this
                                .technicianEntityId = value;
                    }
                }
        );

        inputContainer.startOpen(
                playerInventory.player
        );

        /*
         * 通常取引用スロット2個。
         *
         * 座標は後ほど画面テクスチャに合わせて
         * TedVillageTradeScreenと一緒に調整する。
         */
        this.addSlot(
                new Slot(
                        inputContainer,
                        0,
                        35,
                        48
                )
        );

        this.addSlot(
                new Slot(
                        inputContainer,
                        1,
                        57,
                        48
                )
        );

        /*
         * 作業台用スロット4個。
         * 2×2配置。
         */
        this.addSlot(
                new Slot(
                        inputContainer,
                        2,
                        119,
                        39
                )
        );

        this.addSlot(
                new Slot(
                        inputContainer,
                        3,
                        141,
                        39
                )
        );

        this.addSlot(
                new Slot(
                        inputContainer,
                        4,
                        119,
                        61
                )
        );

        this.addSlot(
                new Slot(
                        inputContainer,
                        5,
                        141,
                        61
                )
        );

        addPlayerInventory(
                playerInventory
        );

        addPlayerHotbar(
                playerInventory
        );
    }


    private void addPlayerInventory(
            Inventory inventory
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0;
                 column < 9;
                 column++) {

                this.addSlot(
                        new Slot(
                                inventory,
                                column
                                        + row * 9
                                        + 9,
                                8 + column * 18,
                                103 + row * 18
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            Inventory inventory
    ) {
        for (int column = 0;
             column < 9;
             column++) {

            this.addSlot(
                    new Slot(
                            inventory,
                            column,
                            8 + column * 18,
                            161
                    )
            );
        }
    }

    public int getTechnicianEntityId() {
        return this.technicianEntityId;
    }

    public String getSelectedTradeId() {
        return this.selectedTradeId;
    }

    public void setSelectedTradeId(
            String selectedTradeId
    ) {
        if (selectedTradeId == null) {
            this.selectedTradeId = "";
            return;
        }

        this.selectedTradeId =
                selectedTradeId;
    }

    public Container getInputContainer() {
        return this.inputContainer;
    }

    public ItemStack getInputStack(
            int inputSlot
    ) {
        if (inputSlot < 0
                || inputSlot
                >= TOTAL_INPUT_SLOT_COUNT) {

            return ItemStack.EMPTY;
        }

        return this.inputContainer
                .getItem(inputSlot);
    }

    /*
     * 成立処理専用。
     *
     * 必要個数だけスロットから削除する。
     * 呼び出す前に必ず全素材を検証すること。
     */
    public void consumeInput(
            int inputSlot,
            int amount
    ) {
        if (inputSlot < 0
                || inputSlot
                >= TOTAL_INPUT_SLOT_COUNT
                || amount <= 0) {

            return;
        }

        this.inputContainer
                .removeItem(
                        inputSlot,
                        amount
                );

        this.inputContainer
                .setChanged();

        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        if (player.isRemoved()
                || !player.isAlive()) {

            return false;
        }

        /*
         * 技術者が消失した場合や、
         * 離れすぎた場合はMenuを閉じる。
         */
        var entity =
                player.level()
                        .getEntity(
                                this.technicianEntityId
                        );

        if (entity == null
                || entity.isRemoved()) {

            return false;
        }

        return player.distanceToSqr(entity)
                <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        if (slotIndex < 0
                || slotIndex
                >= this.slots.size()) {

            return ItemStack.EMPTY;
        }

        Slot slot =
                this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack =
                slot.getItem();

        ItemStack originalStack =
                slotStack.copy();

        /*
         * 取引入力欄
         * → プレイヤーインベントリ。
         */
        if (slotIndex
                < TOTAL_INPUT_SLOT_COUNT) {

            if (!this.moveItemStackTo(
                    slotStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            /*
             * プレイヤーインベントリ
             * → 取引入力欄。
             */
            if (!this.moveItemStackTo(
                    slotStack,
                    0,
                    TOTAL_INPUT_SLOT_COUNT,
                    false
            )) {
                /*
                 * メインインベントリと
                 * ホットバーの相互移動。
                 */
                if (slotIndex
                        < PLAYER_INVENTORY_END) {

                    if (!this.moveItemStackTo(
                            slotStack,
                            PLAYER_HOTBAR_START,
                            PLAYER_HOTBAR_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(
                        slotStack,
                        PLAYER_INVENTORY_START,
                        PLAYER_INVENTORY_END,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(
                    ItemStack.EMPTY
            );
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount()
                == originalStack.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                slotStack
        );

        return originalStack;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        this.inputContainer.stopOpen(
                player
        );

        /*
         * Menuを閉じた際、投入された素材を返却する。
         *
         * clearContainerはプレイヤーが生存中なら
         * インベントリへ戻し、入らなければ足元へ落とす。
         */
        if (!player.level().isClientSide()) {
            this.clearContainer(
                    player,
                    this.inputContainer
            );
        }
    }
}