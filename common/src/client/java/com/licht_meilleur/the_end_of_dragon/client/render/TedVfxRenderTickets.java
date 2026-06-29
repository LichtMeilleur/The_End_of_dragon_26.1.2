package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import net.minecraft.world.phys.Vec3;

public final class TedVfxRenderTickets {
    public static final DataTicket<TedVfxType> VFX_TYPE =
            DataTicket.create("ted_vfx_type", TedVfxType.class);

    public static final DataTicket<Vec3> VFX_DIRECTION =
            DataTicket.create("ted_vfx_direction", Vec3.class);

    public static final DataTicket<Vec3> VFX_FORWARD =
            DataTicket.create("ted_vfx_forward", Vec3.class);

    public static final DataTicket<Vec3> VFX_UP =
            DataTicket.create("ted_vfx_up", Vec3.class);

    public static final DataTicket<Float> VFX_SCALE =
            DataTicket.create("ted_vfx_scale", Float.class);

    public static final DataTicket<Float> VFX_LENGTH =
            DataTicket.create("ted_vfx_length", Float.class);

    public static final DataTicket<org.joml.Quaternionf> VFX_ROTATION =
            DataTicket.create("ted_vfx_rotation", org.joml.Quaternionf.class);

    private TedVfxRenderTickets() {
    }
}