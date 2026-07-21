package com.licht_meilleur.the_end_of_dragon.client.entity.enderman;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class TedInvitationItemLayer<
        T extends LivingEntity & GeoAnimatable,
        O,
        R extends GeoRenderState>
        extends ItemInHandGeoLayer<T, O, R> {

    private static final String INVITATION_BONE =
            "InvitationItem";

    /*
     * ここをゲーム内で調整する。
     *
     * Minecraft座標なので、
     * 1.0 = 約1ブロック。
     */
    private static final float OFFSET_X = 0.25F;
    private static final float OFFSET_Y = -0.25F;
    private static final float OFFSET_Z = -0.1F;

    private static final float ROTATION_X = 180.0F;
    private static final float ROTATION_Y = 45.0F;
    private static final float ROTATION_Z = 90.0F;

    private static final float ITEM_SCALE = 0.5F;

    public TedInvitationItemLayer(
            EntityRendererProvider.Context context,
            GeoRenderer<T, O, R> renderer,
            String rightHandBone,
            String invitationBone
    ) {
        super(
                context,
                renderer,
                rightHandBone,
                invitationBone
        );
    }

    @Override
    protected void submitItemStackRender(
            PoseStack poseStack,
            GeoBone bone,
            ItemStackRenderState stackState,
            ItemDisplayContext displayContext,
            R renderState,
            SubmitNodeCollector renderTasks,
            int packedLight
    ) {
        /*
         * 食料用RightHandItemは、
         * GeckoLib標準の変換をそのまま使う。
         */
        if (!INVITATION_BONE.equals(
                bone.name()
        )) {
            super.submitItemStackRender(
                    poseStack,
                    bone,
                    stackState,
                    displayContext,
                    renderState,
                    renderTasks,
                    packedLight
            );

            return;
        }

        poseStack.pushPose();

        /*
         * ItemInHandGeoLayerが左手用アイテムへ加える
         * 標準変換を事前に打ち消す。
         *
         * 標準処理：
         * X -90度
         * translate(0, 0.125, -0.0625)
         */
        if (displayContext
                == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {

            poseStack.translate(
                    0.0F,
                    -0.125F,
                    0.0625F
            );

            poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            90.0F
                    )
            );
        }

        /*
         * 門アイテム専用の調整。
         *
         * BlockbenchのInvitationItemボーン位置を
         * 基準に、微調整だけここで行う。
         */
        poseStack.translate(
                OFFSET_X,
                OFFSET_Y,
                OFFSET_Z
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(
                        ROTATION_X
                )
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        ROTATION_Y
                )
        );

        poseStack.mulPose(
                Axis.ZP.rotationDegrees(
                        ROTATION_Z
                )
        );

        poseStack.scale(
                ITEM_SCALE,
                ITEM_SCALE,
                ITEM_SCALE
        );

        /*
         * 実際の描画は標準Layerへ任せる。
         */
        super.submitItemStackRender(
                poseStack,
                bone,
                stackState,
                displayContext,
                renderState,
                renderTasks,
                packedLight
        );

        poseStack.popPose();
    }
}