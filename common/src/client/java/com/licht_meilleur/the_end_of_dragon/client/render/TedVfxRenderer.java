package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.licht_meilleur.the_end_of_dragon.client.model.TedVfxModel;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TedVfxRenderer<R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TedVfxEntity, R> {

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

        Vec3 forward = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_FORWARD);
        Vec3 up = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_UP);

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

        // 2. parent_root = 親OBB回転継承じゃなくてレイキャスト継承に変更
        snapshots.ifPresent("parent_root", parent -> {
            if (finalType == TedVfxType.TED_LASER_BEAM || finalType == TedVfxType.TED_JET) {
                Vec3 rayDir = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_FORWARD);

                Quaternionf q = rotationFromRayDir(rayDir);
                Vector3f euler = q.getEulerAnglesXYZ(new Vector3f());

                parent.setRotX(euler.x);
                parent.setRotY(euler.y);
                parent.setRotZ(euler.z);
            } else {
                parent.setRotX(0.0F);
                parent.setRotY(0.0F);
                parent.setRotZ(0.0F);
            }
        });

        // 3. local_root = Blockbench側でY向き化しているので、基本コードでは触らない
        snapshots.ifPresent("local_root", local -> {
            local.setScaleX(1.0F);
            local.setScaleY(1.0F);
            local.setScaleZ(1.0F);
        });

        // 4. effect_root = エフェクト固有補正
        snapshots.ifPresent("effect_root", effect -> {
            if (finalType == TedVfxType.TED_LASER_BEAM) {
                effect.setScaleX(1.0F);     // 鏡写し補正
                effect.setScaleY(length);    // レーザー長
                effect.setScaleZ(1.0F);
            } else if (finalType == TedVfxType.TED_JET) {
                effect.setScaleX(1.0F);
                effect.setScaleY(1.0F);
                effect.setScaleZ(1.0F);
            } else {
                effect.setScaleX(1.0F);
                effect.setScaleY(1.0F);
                effect.setScaleZ(1.0F);
            }
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
        Vec3 forward = safeNormalize(forwardRaw, new Vec3(0, 1, 0));
        Vec3 up = safeNormalize(upRaw, new Vec3(0, 0, 1));

        Quaternionf q = new Quaternionf().rotationTo(
                0.0F, 1.0F, 0.0F,
                (float) forward.x,
                (float) forward.y,
                (float) forward.z
        );

        Vector3f modelUp = new Vector3f(0.0F, 0.0F, 1.0F);
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

        q.rotateAxis(
                (float) (angle * sign),
                (float) forward.x,
                (float) forward.y,
                (float) forward.z
        );

        return q;
    }

    private static Quaternionf rotationFromRayDir(Vec3 rayDirRaw) {
        Vec3 rayDir = safeNormalize(rayDirRaw, new Vec3(0, 1, 0));

        // モデルの +Y を rayDir に向ける
        return new Quaternionf().rotationTo(
                0.0F, 1.0F, 0.0F,
                (float) rayDir.x,
                (float) rayDir.y,
                (float) rayDir.z
        );
    }

    private static Vec3 safeNormalize(Vec3 v, Vec3 fallback) {
        if (v == null || v.lengthSqr() < 1.0E-6D) {
            return fallback;
        }
        return v.normalize();
    }

}