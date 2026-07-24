package com.licht_meilleur.the_end_of_dragon.client.quest;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.network
        .TedQuestClientNetwork;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedQuestItemRequirement;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedQuestItemReward;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuest;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TedVillageQuestLetterScreen
        extends Screen {

    private static final Identifier BACKGROUND_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/quest_letter.png"
            );

    private static final Identifier UNKNOWN_REQUIREMENT_TEXTURE =
            TheEndOfDragon.id(
                    "textures/gui/quest/unknown_requirement.png"
            );

    private static final int TEXTURE_SIZE =
            256;

    private static final int GUI_WIDTH =
            256;

    private static final int GUI_HEIGHT =
            256;

    private final TedVillageQuest quest;
    private final boolean completable;

    private int leftPos;
    private int topPos;

    public TedVillageQuestLetterScreen(
            TedVillageQuest quest,
            boolean completable
    ) {
        super(
                quest == null
                        ? Component.empty()
                        : quest.title()
        );

        this.quest =
                quest;

        this.completable =
                completable;
    }

    @Override
    protected void init() {
        this.leftPos =
                (this.width - GUI_WIDTH)
                        / 2;

        this.topPos =
                (this.height - GUI_HEIGHT)
                        / 2;

        Component buttonText =
                this.completable
                        ? Component.translatable(
                        "gui.the_end_of_dragon.quest.submit"
                )
                        : Component.translatable(
                        "gui.the_end_of_dragon.quest.check"
                );

        this.addRenderableWidget(
                Button.builder(
                                buttonText,
                                button -> {
                                    if (this.quest == null) {
                                        return;
                                    }

                                    TedQuestClientNetwork.submitQuest(
                                            this.quest.id()
                                                    .getSerializedName()
                                    );
                                }
                        )
                        .bounds(
                                this.leftPos + 91,
                                this.topPos + 224,
                                74,
                                18
                        )
                        .build()
        );
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

        if (this.quest == null) {
            return;
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                GUI_WIDTH,
                GUI_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        /*
         * クエストタイトル。
         */
        drawTitle(
                graphics,
                this.quest.title(),
                this.leftPos + 39,
                this.topPos + 50,
                82
        );

        /*
         * 説明文。
         */
        drawWrappedText(
                graphics,
                this.quest.description(),
                this.leftPos + 39,
                this.topPos + 90,
                82,
                0xFF2A1A2C
        );

        /*
         * 右上イラスト。
         */
        if (this.quest.illustrationTexture()
                != null) {

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.quest.illustrationTexture(),
                    this.leftPos + 125,
                    this.topPos + 61,
                    0,
                    0,
                    123,
                    98,
                    123,
                    98
            );
        }

        /*
         * 必要素材。
         */
        drawRequirements(
                graphics,
                mouseX,
                mouseY
        );

        /*
         * 報酬。
         */
        drawRewards(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void drawRequirements(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        List<TedQuestItemRequirement> requirements =
                this.quest.requirements();

        int startX =
                this.leftPos + 18;

        int startY =
                this.topPos + 178;

        for (int index = 0;
             index < requirements.size();
             index++) {

            TedQuestItemRequirement requirement =
                    requirements.get(index);

            int x =
                    startX + index * 28;

            ItemStack stack =
                    new ItemStack(
                            requirement.item(),
                            requirement.count()
                    );

            if (requirement.hidden()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        UNKNOWN_REQUIREMENT_TEXTURE,
                        x,
                        startY,
                        0,
                        0,
                        16,
                        16,
                        16,
                        16
                );

                String countText =
                        String.valueOf(
                                requirement.count()
                        );

                graphics.text(
                        this.font,
                        countText,
                        x + 18
                                - this.font.width(
                                countText
                        ),
                        startY + 9,
                        0xFFFFFFFF,
                        true
                );
            } else {
                graphics.item(
                        stack,
                        x,
                        startY
                );

                graphics.itemDecorations(
                        this.font,
                        stack,
                        x,
                        startY,
                        String.valueOf(
                                requirement.count()
                        )
                );
            }
        }
    }

    private void drawRewards(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        List<TedQuestItemReward> rewards =
                this.quest.rewards();

        int startX =
                this.leftPos + 135;

        int startY =
                this.topPos + 178;

        for (int index = 0;
             index < rewards.size();
             index++) {

            TedQuestItemReward reward =
                    rewards.get(index);

            int x =
                    startX + index * 28;

            ItemStack stack =
                    new ItemStack(
                            reward.item(),
                            reward.count()
                    );

            graphics.item(
                    stack,
                    x,
                    startY
            );

            graphics.itemDecorations(
                    this.font,
                    stack,
                    x,
                    startY,
                    reward.count() > 1
                            ? String.valueOf(
                            reward.count()
                    )
                            : null
            );
        }
    }

    private void drawWrappedText(
            GuiGraphicsExtractor graphics,
            Component text,
            int x,
            int y,
            int width,
            int color
    ) {
        if (text == null) {
            return;
        }

        float scale =
                0.9F;

        int scaledWidth =
                Math.round(
                        width / scale
                );

        graphics.pose()
                .pushMatrix();

        graphics.pose()
                .translate(
                        x,
                        y
                );

        graphics.pose()
                .scale(
                        scale,
                        scale
                );

        int lineY =
                0;

        String[] paragraphs =
                text.getString()
                        .split(
                                "\\n",
                                -1
                        );

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lineY +=
                        this.font.lineHeight + 1;

                continue;
            }

            List<net.minecraft.util.FormattedCharSequence>
                    lines =
                    this.font.split(
                            Component.literal(
                                    paragraph
                            ),
                            scaledWidth
                    );

            for (net.minecraft.util.FormattedCharSequence line
                    : lines) {

                graphics.text(
                        this.font,
                        line,
                        0,
                        lineY,
                        color,
                        false
                );

                lineY +=
                        this.font.lineHeight;
            }
        }

        graphics.pose()
                .popMatrix();
    }

    private void drawTitle(
            GuiGraphicsExtractor graphics,
            Component title,
            int x,
            int y,
            int maximumWidth
    ) {
        if (title == null) {
            return;
        }

        int titleWidth =
                this.font.width(title);

        if (titleWidth <= maximumWidth) {
            graphics.text(
                    this.font,
                    title,
                    x,
                    y,
                    0xFF231326,
                    false
            );

            return;
        }

        /*
         * 長いタイトルだけ縮小する。
         * 短いタイトルは通常サイズのまま。
         */
        float scale =
                Math.max(
                        0.72F,
                        (float) maximumWidth
                                / (float) titleWidth
                );

        graphics.pose()
                .pushMatrix();

        graphics.pose()
                .translate(
                        x,
                        y
                );

        graphics.pose()
                .scale(
                        scale,
                        scale
                );

        graphics.text(
                this.font,
                title,
                0,
                0,
                0xFF231326,
                false
        );

        graphics.pose()
                .popMatrix();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}