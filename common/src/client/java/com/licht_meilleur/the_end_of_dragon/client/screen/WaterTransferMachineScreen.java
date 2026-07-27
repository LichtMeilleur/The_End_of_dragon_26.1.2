package com.licht_meilleur.the_end_of_dragon.client.screen;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.network
        .TedSetWaterTransferChannelPayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedWaterTransferNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class WaterTransferMachineScreen
        extends Screen {

    private static final Identifier BACKGROUND_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/water_transfer_machine_gui.png"
            );

    private static final Identifier A_NAME_TAG_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/water_transfer_machine_a_name_tag.png"
            );

    private static final Identifier B_NAME_TAG_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/water_transfer_machine_b_name_tag.png"
            );

    private static final Identifier SAVE_BUTTON_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/save_button.png"
            );

    private static final int GUI_WIDTH =
            256;

    private static final int GUI_HEIGHT =
            256;

    private static final int NAME_TAG_WIDTH =
            187;

    private static final int NAME_TAG_HEIGHT =
            29;

    private static final int SAVE_BUTTON_WIDTH =
            77;

    private static final int SAVE_BUTTON_HEIGHT =
            29;

    private final BlockPos machinePosition;
    private final boolean machineA;
    private final String initialChannelName;
    private final long storedWater;

    private EditBox channelField;

    private int leftPosition;
    private int topPosition;

    private int nameTagX;
    private int nameTagY;

    private int saveButtonX;
    private int saveButtonY;

    private int savedMessageTicks;

    private String savedChannelName =
            "";

    public WaterTransferMachineScreen(
            BlockPos machinePosition,
            boolean machineA,
            String channelName,
            long storedWater
    ) {
        super(
                Component.literal(
                        machineA
                                ? "Water Transfer Machine A"
                                : "Water Transfer Machine B"
                )
        );

        this.machinePosition =
                machinePosition != null
                        ? machinePosition.immutable()
                        : BlockPos.ZERO;

        this.machineA =
                machineA;

        this.initialChannelName =
                channelName == null
                        || channelName.isBlank()
                        ? "default"
                        : channelName;

        this.storedWater =
                Math.max(
                        0L,
                        storedWater
                );
    }

    @Override
    protected void init() {
        this.leftPosition =
                (this.width - GUI_WIDTH) / 2;

        this.topPosition =
                (this.height - GUI_HEIGHT) / 2;

        this.nameTagX =
                this.leftPosition
                        + (GUI_WIDTH - NAME_TAG_WIDTH) / 2;

        this.nameTagY =
                this.topPosition + 84;

        this.saveButtonX =
                this.leftPosition
                        + (GUI_WIDTH - SAVE_BUTTON_WIDTH) / 2;

        this.saveButtonY =
                this.topPosition + 132;

        this.channelField =
                new EditBox(
                        this.font,
                        this.nameTagX + 17,
                        this.nameTagY + 6,
                        NAME_TAG_WIDTH - 34,
                        17,
                        Component.literal(
                                "Channel"
                        )
                );

        this.channelField.setMaxLength(
                32
        );

        this.channelField.setValue(
                this.initialChannelName
        );

        this.channelField.setBordered(
                false
        );

        this.channelField.setTextColor(
                0xFFFFFFFF
        );

        this.channelField.setTextColorUneditable(
                0xFFAAAAAA
        );

        this.addRenderableWidget(
                this.channelField
        );

        /*
         * 26.1.2の入力イベント差異を避けるため、
         * 保存処理は標準Buttonへ任せる。
         */
        Button saveButton =
                Button.builder(
                                Component.literal(
                                        "SAVE"
                                ),
                                button ->
                                        saveChannel()
                        )
                        .bounds(
                                this.saveButtonX,
                                this.saveButtonY,
                                SAVE_BUTTON_WIDTH,
                                SAVE_BUTTON_HEIGHT
                        )
                        .build();

        this.addRenderableWidget(
                saveButton
        );

        this.setInitialFocus(
                this.channelField
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.savedMessageTicks > 0) {
            this.savedMessageTicks--;
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        /*
         * 先に背景・装飾画像・説明文字を描画する。
         */
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                this.leftPosition,
                this.topPosition,
                0,
                0,
                GUI_WIDTH,
                GUI_HEIGHT,
                GUI_WIDTH,
                GUI_HEIGHT
        );

        Identifier nameTagTexture =
                this.machineA
                        ? A_NAME_TAG_TEXTURE
                        : B_NAME_TAG_TEXTURE;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                nameTagTexture,
                this.nameTagX,
                this.nameTagY,
                0,
                0,
                NAME_TAG_WIDTH,
                NAME_TAG_HEIGHT,
                NAME_TAG_WIDTH,
                NAME_TAG_HEIGHT
        );

        /*
         * 保存ボタンの下地画像。
         *
         * この後superを呼ぶので、
         * 標準Buttonの文字が画像より上へ表示される。
         */
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                SAVE_BUTTON_TEXTURE,
                this.saveButtonX,
                this.saveButtonY,
                0,
                0,
                SAVE_BUTTON_WIDTH,
                SAVE_BUTTON_HEIGHT,
                SAVE_BUTTON_WIDTH,
                SAVE_BUTTON_HEIGHT
        );

        drawCenteredText(
                graphics,
                this.machineA
                        ? "WATER TRANSFER A"
                        : "WATER TRANSFER B",
                this.leftPosition
                        + GUI_WIDTH / 2,
                this.topPosition + 53,
                0xFFFFFFFF,
                true
        );

        drawCenteredText(
                graphics,
                "Stored Fluid: "
                        + this.storedWater
                        + " mB",
                this.leftPosition
                        + GUI_WIDTH / 2,
                this.topPosition + 177,
                0xFFFFFFFF,
                true
        );

        if (this.savedMessageTicks > 0) {
            drawCenteredText(
                    graphics,
                    "Saved: "
                            + this.savedChannelName,
                    this.leftPosition
                            + GUI_WIDTH / 2,
                    this.topPosition + 196,
                    0xFF80FF80,
                    true
            );
        }

        /*
         * EditBoxとButtonを最後に描画する。
         *
         * これを最後にしないと、
         * 入力文字とSAVE文字が画像の下へ隠れる。
         */
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
    }

    private void drawCenteredText(
            GuiGraphicsExtractor graphics,
            String text,
            int centerX,
            int y,
            int color,
            boolean shadow
    ) {
        int textWidth =
                this.font.width(
                        text
                );

        graphics.text(
                this.font,
                text,
                centerX - textWidth / 2,
                y,
                color,
                shadow
        );
    }

    private void saveChannel() {
        if (this.channelField == null) {
            return;
        }

        String channelName =
                this.channelField
                        .getValue()
                        .trim();


        if (channelName.length() > 32) {
            channelName =
                    channelName.substring(
                            0,
                            32
                    );

            this.channelField.setValue(
                    channelName
            );
        }

        TedWaterTransferNetwork
                .sendSetChannel(
                        new TedSetWaterTransferChannelPayload(
                                this.machinePosition,
                                channelName
                        )
                );

        this.savedChannelName =
                channelName;

        this.savedMessageTicks =
                60;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}