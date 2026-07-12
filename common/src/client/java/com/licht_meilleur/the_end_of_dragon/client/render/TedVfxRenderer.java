package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.model.TedVfxModel;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;


import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TedVfxRenderer<R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TedVfxEntity, R> {

    private static int debugRenderLogCounter = 0;

    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        TedVfxType type = renderPassInfo.renderState()
                .getGeckolibData(TedVfxRenderTickets.VFX_TYPE);

        /*
         * TED_LASER_BEAMはsubmit()内で、
         * forwardとlengthから直接ポリゴンを描画する。
         *
         * ここで回転すると二重回転になるため、JETだけ従来方式を残す。
         */
        if (type != TedVfxType.TED_JET) {
            return;
        }

        Vec3 forward = renderPassInfo.renderState()
                .getGeckolibData(TedVfxRenderTickets.VFX_FORWARD);

        Vec3 up = renderPassInfo.renderState()
                .getGeckolibData(TedVfxRenderTickets.VFX_UP);

        Quaternionf q = rotationFromBasis(forward, up);
        renderPassInfo.poseStack().mulPose(q);
    }

    @Override
    public RenderType getRenderType(
            R renderState,
            Identifier texture
    ) {
        return RenderTypes.entityTranslucent(texture);
    }

    public TedVfxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TedVfxModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void addRenderData(TedVfxEntity animatable, Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(TedVfxRenderTickets.VFX_TYPE, animatable.getVfxType());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_SCALE, animatable.getVfxScale());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_LENGTH, animatable.getVfxLength());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_FORWARD, animatable.getForward());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_UP, animatable.getUp());
        //renderState.addGeckolibData(TedVfxRenderTickets.VFX_DIRECTION, animatable.getVfxDirection());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_AGE, animatable.tickCount);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        TedVfxType type = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_TYPE);
        if (type == null) {
            type = TedVfxType.LIGHT_PROJECTILE;
        }

        Float scaleData = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_SCALE);
        Float lengthData = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_LENGTH);

        float scale = scaleData != null ? scaleData : 1.0F;
        float length = lengthData != null ? lengthData : 1.0F;
/*
        Vec3 forward = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_FORWARD);
        Vec3 up = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_UP);


 */
        TedVfxType finalType = type;

        // 1. root = 全体スケールだけ
        snapshots.ifPresent("root", root -> {
            root.setScaleX(scale);
            root.setScaleY(scale);
            root.setScaleZ(scale);
            root.setRotX(0.0F);
            root.setRotY(0.0F);
            root.setRotZ(0.0F);
        });

        snapshots.ifPresent("parent_root", parent -> {
            parent.setRotX(0.0F);
            parent.setRotY(0.0F);
            parent.setRotZ(0.0F);
        });

        // 3. local_root = Blockbench側でY向き化しているので、基本コードでは触らない
        snapshots.ifPresent("local_root", local -> {
            local.setRotX(0.0F);
            local.setRotY(0.0F);
            local.setRotZ(0.0F);

            local.setScaleX(1.0F);
            local.setScaleY(1.0F);
            local.setScaleZ(1.0F);
        });

        // 4. effect_root = エフェクト固有補正
        snapshots.ifPresent("effect_root", effect -> {
            effect.setRotX(0.0F);
            effect.setRotY(0.0F);
            effect.setRotZ(0.0F);

            effect.setScaleX(1.0F);
            effect.setScaleY(1.0F);
            effect.setScaleZ(1.0F);
        });
    }




    /**
     * PART_BASIS用。
     * 今は「モデル側の基準方向 = Y+」として扱う。
     * 合わなければここだけ 0,0,1 や 0,0,-1 に変える。
     */


    @Override
    public boolean shouldRender(
            TedVfxEntity entity,
            net.minecraft.client.renderer.culling.Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {
        if (entity.getVfxType() == TedVfxType.TED_LASER_BEAM) {
            return true;
        }

        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }



    private static Quaternionf rotationFromBasis(Vec3 forwardRaw, Vec3 upRaw) {
        Vec3 forward = safeNormalize(forwardRaw, new Vec3(0, 0, 1));
        Vec3 up = safeNormalize(upRaw, new Vec3(0, 1, 0));

        // モデルの +Z（South）をBeamHitboxのdirectionへ向ける
        Quaternionf q = new Quaternionf().rotationTo(
                0.0F, 0.0F, 1.0F,
                (float) forward.x,
                (float) forward.y,
                (float) forward.z
        );

        // +Zをforwardに使ったので、roll確認用のモデル上方向は +Y
        Vector3f modelUp = new Vector3f(0.0F, 1.0F, 0.0F);
        q.transform(modelUp);

        Vec3 currentUp = new Vec3(modelUp.x, modelUp.y, modelUp.z);
        Vec3 targetUp = up.subtract(forward.scale(up.dot(forward)));

        if (currentUp.lengthSqr() < 1.0E-6D || targetUp.lengthSqr() < 1.0E-6D) {
            return q;
        }

        currentUp = currentUp.normalize();
        targetUp = targetUp.normalize();

        double dot = Math.max(-1.0D, Math.min(1.0D, currentUp.dot(targetUp)));
        double angle = Math.acos(dot);
        double sign = Math.signum(forward.dot(currentUp.cross(targetUp)));

        Quaternionf roll = new Quaternionf().rotateAxis(
                (float) (angle * sign),
                (float) forward.x,
                (float) forward.y,
                (float) forward.z
        );

        return roll.mul(q).normalize();
    }



    private static Vec3 safeNormalize(Vec3 v, Vec3 fallback) {
        if (v == null || v.lengthSqr() < 1.0E-6D) {
            return fallback;
        }
        return v.normalize();
    }

    private static final Identifier ROAR_TEXTURE =
            TheEndOfDragon.id("textures/vfx/roar_of_obliteration.png");

    private static final RenderType ROAR_RENDER_TYPE =
            RenderTypes.entityTranslucentCullItemTarget(ROAR_TEXTURE);

    private static final Identifier LASER_TEXTURE =
            TheEndOfDragon.id("textures/vfx/ted_laser_beam.png");

    private static final RenderType LASER_RENDER_TYPE =
            RenderTypes.entityTranslucent(LASER_TEXTURE);

    private static final int ROAR_LIFE = 34;

    private static final int[] ROAR_DELAYS = {0, 3, 6};
    private static final float[] ROAR_MAX_SIZE = {54.0F, 66.0F, 78.0F};
    private static final float[] ROAR_ROTATIONS = {0.0F, 18.0F, -13.0F};



    private void renderRoarOfObliteration(
            R renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        int age = (int) renderState.ageInTicks;

        for (int i = 0; i < ROAR_DELAYS.length; i++) {
            int localAge = age - ROAR_DELAYS[i];
            if (localAge < 0 || localAge > ROAR_LIFE) continue;

            float t = localAge / (float) ROAR_LIFE;
            float eased = easeOutExpo(t);

            float size = ROAR_MAX_SIZE[i] * eased;

            float fadeIn = Math.min(1.0F, localAge / 4.0F);
            float fadeOut = 1.0F - t;
            float alpha = fadeIn * fadeOut;

            float rotation = ROAR_ROTATIONS[i] + age * 1.2F;

            poseStack.pushPose();
            poseStack.mulPose(camera.orientation);
            poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(rotation)));
            poseStack.scale(size, size, size);

            int a = (int) (alpha * 220.0F);

            submitNodeCollector.submitCustomGeometry(
                    poseStack,
                    ROAR_RENDER_TYPE,
                    (pose, buffer) -> {
                        roarVertex(buffer, pose, -0.5F, -0.5F, 0.0F, 1.0F, a);
                        roarVertex(buffer, pose,  0.5F, -0.5F, 1.0F, 1.0F, a);
                        roarVertex(buffer, pose,  0.5F,  0.5F, 1.0F, 0.0F, a);
                        roarVertex(buffer, pose, -0.5F,  0.5F, 0.0F, 0.0F, a);
                    }
            );

            poseStack.popPose();
        }
    }

    private static void roarVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            int alpha
    ) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }



    private static float easeOutExpo(float t) {
        if (t >= 1.0F) return 1.0F;
        return 1.0F - (float) Math.pow(2.0D, -10.0D * t);
    }

    @Override
    public void submit(
            R renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        TedVfxType type = renderState.getGeckolibData(
                TedVfxRenderTickets.VFX_TYPE
        );

        if (type == TedVfxType.ROAR_OF_OBLITERATION) {
            renderRoarOfObliteration(
                    renderState,
                    poseStack,
                    submitNodeCollector,
                    camera
            );
            return;
        }

        /*
         * Photon Busterなどのレーザービームは、
         * Geckoモデルを描画せず、始点からforward方向へ直接描画する。
         */
        if (type == TedVfxType.TED_LASER_BEAM) {
            renderLaserBeam(
                    renderState,
                    poseStack,
                    submitNodeCollector
            );
            return;
        }

        super.submit(
                renderState,
                poseStack,
                submitNodeCollector,
                camera
        );
    }

    private void renderLaserBeam(
            R renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        Vec3 forwardRaw = renderState.getGeckolibData(
                TedVfxRenderTickets.VFX_FORWARD
        );

        Vec3 upRaw = renderState.getGeckolibData(
                TedVfxRenderTickets.VFX_UP
        );

        Float scaleData = renderState.getGeckolibData(
                TedVfxRenderTickets.VFX_SCALE
        );

        Float lengthData = renderState.getGeckolibData(
                TedVfxRenderTickets.VFX_LENGTH
        );

        Vec3 forward = safeNormalize(
                forwardRaw,
                new Vec3(0.0D, 0.0D, 1.0D)
        );

        Vec3 up = createBeamUp(forward, upRaw);

        float scale = scaleData != null
                ? Math.max(0.05F, scaleData)
                : 1.0F;

        float length = lengthData != null
                ? Math.max(0.01F, lengthData)
                : 1.0F;

        /*
         * modelScaleをそのまま半径にすると太すぎる場合があるため、
         * Photon Busterのscale=5.0を基準に調整。
         */
        float outerRadius = scale;
        float middleRadius = scale * 0.68F;
        float coreRadius = scale * 0.30F;

        poseStack.pushPose();

        /*
         * 外側の半透明光。
         */
        submitBeamLayer(
                poseStack,
                submitNodeCollector,
                forward,
                up,
                length,
                outerRadius,
                255,
                230,
                70,
                45
        );

        /*
         * 中間の黄色いレーザー。
         */
        submitBeamLayer(
                poseStack,
                submitNodeCollector,
                forward,
                up,
                length,
                middleRadius,
                255,
                245,
                120,
                125
        );

        /*
         * 中心の白い発光コア。
         */
        submitBeamLayer(
                poseStack,
                submitNodeCollector,
                forward,
                up,
                length,
                coreRadius,
                255,
                255,
                245,
                235
        );

        poseStack.popPose();
    }

    private Vec3 createBeamUp(Vec3 forward, Vec3 requestedUp) {
        Vec3 up = safeNormalize(
                requestedUp,
                new Vec3(0.0D, 1.0D, 0.0D)
        );

        /*
         * forward成分を取り除き、完全に直交させる。
         */
        up = up.subtract(
                forward.scale(up.dot(forward))
        );

        if (up.lengthSqr() < 1.0E-6D) {
            Vec3 helper = Math.abs(forward.y) < 0.95D
                    ? new Vec3(0.0D, 1.0D, 0.0D)
                    : new Vec3(1.0D, 0.0D, 0.0D);

            up = helper.subtract(
                    forward.scale(helper.dot(forward))
            );
        }

        if (up.lengthSqr() < 1.0E-6D) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }

        return up.normalize();
    }

    private void submitBeamLayer(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Vec3 forward,
            Vec3 up,
            float length,
            float radius,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        /*
         * right / up / forwardの直交座標を作成。
         */
        Vec3 right = up.cross(forward);

        if (right.lengthSqr() < 1.0E-6D) {
            return;
        }

        right = right.normalize();
        Vec3 correctedUp = forward.cross(right).normalize();

        /*
         * 八角柱。
         * 値を12や16へ増やせば、より丸い円柱になる。
         */
        int sides = 8;

        Vec3[] startRing = new Vec3[sides];
        Vec3[] endRing = new Vec3[sides];

        Vec3 endCenter = forward.scale(length);

        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2.0D * i / sides;

            Vec3 offset = right
                    .scale(Math.cos(angle) * radius)
                    .add(correctedUp.scale(Math.sin(angle) * radius));

            /*
             * VFX Entity自体がレイキャスト始点に配置されているため、
             * ローカル原点Vec3.ZEROがビーム始点になる。
             */
            startRing[i] = offset;
            endRing[i] = endCenter.add(offset);
        }

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                LASER_RENDER_TYPE,
                (pose, buffer) -> {
                    for (int i = 0; i < sides; i++) {
                        int next = (i + 1) % sides;

                        Vec3 a = startRing[i];
                        Vec3 b = endRing[i];
                        Vec3 c = endRing[next];
                        Vec3 d = startRing[next];

                        float u0 = i / (float) sides;
                        float u1 = (i + 1) / (float) sides;

                        Vec3 normal = a.add(d);

                        if (normal.lengthSqr() < 1.0E-6D) {
                            normal = correctedUp;
                        } else {
                            normal = normal.normalize();
                        }

                        /*
                         * 側面。
                         */
                        laserVertex(
                                buffer,
                                pose,
                                a,
                                u0,
                                1.0F,
                                red,
                                green,
                                blue,
                                alpha,
                                normal
                        );

                        laserVertex(
                                buffer,
                                pose,
                                b,
                                u0,
                                0.0F,
                                red,
                                green,
                                blue,
                                alpha,
                                normal
                        );

                        laserVertex(
                                buffer,
                                pose,
                                c,
                                u1,
                                0.0F,
                                red,
                                green,
                                blue,
                                alpha,
                                normal
                        );

                        laserVertex(
                                buffer,
                                pose,
                                d,
                                u1,
                                1.0F,
                                red,
                                green,
                                blue,
                                alpha,
                                normal
                        );
                    }
                }
        );
    }

    private static void laserVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            Vec3 position,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int alpha,
            Vec3 normal
    ) {
        buffer.addVertex(
                        pose,
                        (float) position.x,
                        (float) position.y,
                        (float) position.z
                )
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(
                        pose,
                        (float) normal.x,
                        (float) normal.y,
                        (float) normal.z
                );
    }



}