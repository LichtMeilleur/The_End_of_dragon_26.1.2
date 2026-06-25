package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.licht_meilleur.the_end_of_dragon.client.DragonRenderTickets;
import com.licht_meilleur.the_end_of_dragon.client.model.TheEndOfDragonModel;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.client.hitbox.DragonLocatorSnapshot;
import com.licht_meilleur.the_end_of_dragon.client.hitbox.GeckoLocatorUtil;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TheEndOfDragonRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TheEndOfDragonEntity, R> {

    private int dragonDebugTick;

    public TheEndOfDragonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TheEndOfDragonModel());
        this.shadowRadius = 4.0F;
    }

    private static final List<String> DEBUG_LOCATORS = List.of(
            DragonLocators.UPPER_BODY_ROOT,
            DragonLocators.UPPER_BODY_TIP,
            DragonLocators.HEAD_ROOT,
            DragonLocators.HEAD_TIP,
            DragonLocators.MOUTH,
            DragonLocators.CHEST_CRYSTAL,

            DragonLocators.FRONT_LEFT_ARM_ROOT,
            DragonLocators.FRONT_LEFT_ARM_TIP,
            DragonLocators.FRONT_LEFT_FORE_ARM_ROOT,
            DragonLocators.FRONT_LEFT_FORE_ARM_TIP,
            DragonLocators.FRONT_LEFT_HAND_ROOT,
            DragonLocators.FRONT_LEFT_HAND_TIP,
            DragonLocators.FRONT_LEFT_JET,

            DragonLocators.FRONT_RIGHT_ARM_ROOT,
            DragonLocators.FRONT_RIGHT_ARM_TIP,
            DragonLocators.FRONT_RIGHT_FORE_ARM_ROOT,
            DragonLocators.FRONT_RIGHT_FORE_ARM_TIP,
            DragonLocators.FRONT_RIGHT_HAND_ROOT,
            DragonLocators.FRONT_RIGHT_HAND_TIP,
            DragonLocators.FRONT_RIGHT_JET,

            DragonLocators.BACK_LEFT_ARM_ROOT,
            DragonLocators.BACK_LEFT_ARM_TIP,
            DragonLocators.BACK_LEFT_FORE_ARM_ROOT,
            DragonLocators.BACK_LEFT_FORE_ARM_TIP,
            DragonLocators.BACK_LEFT_HAND_ROOT,
            DragonLocators.BACK_LEFT_HAND_TIP,
            DragonLocators.BACK_LEFT_JET,

            DragonLocators.BACK_RIGHT_ARM_ROOT,
            DragonLocators.BACK_RIGHT_ARM_TIP,
            DragonLocators.BACK_RIGHT_FORE_ARM_ROOT,
            DragonLocators.BACK_RIGHT_FORE_ARM_TIP,
            DragonLocators.BACK_RIGHT_HAND_ROOT,
            DragonLocators.BACK_RIGHT_HAND_TIP,
            DragonLocators.BACK_RIGHT_JET,

            DragonLocators.LEFT_LEG_ROOT,
            DragonLocators.LEFT_LEG_TIP,
            DragonLocators.LEFT_LOWER_LEG_ROOT,
            DragonLocators.LEFT_LOWER_LEG_TIP,

            DragonLocators.RIGHT_LEG_ROOT,
            DragonLocators.RIGHT_LEG_TIP,
            DragonLocators.RIGHT_LOWER_LEG_ROOT,
            DragonLocators.RIGHT_LOWER_LEG_TIP,

            DragonLocators.TAIL_ROOT,
            DragonLocators.TAIL_MIDDLE,
            DragonLocators.TAIL_TIP
    );

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        var uuid = renderPassInfo.getGeckolibData(DragonRenderTickets.DRAGON_UUID);
        if (uuid == null) {
            return;
        }

        DragonLocatorSnapshot.begin(uuid);

        for (String locatorName : DEBUG_LOCATORS) {
            renderPassInfo.addLocatorPositionListener(locatorName, (localPos, modelPos, worldPos) -> {
                Vec3 pos = worldPos != null ? worldPos : modelPos;
                if (pos != null) {
                    DragonLocatorSnapshot.put(uuid, locatorName, pos);
                }
            });
        }
    }

    @Override
    public void addRenderData(TheEndOfDragonEntity animatable, Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(DragonRenderTickets.DRAGON_UUID, animatable.getUUID());
    }
}