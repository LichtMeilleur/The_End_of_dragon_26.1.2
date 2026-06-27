package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public final class TedVfxRenderTickets {
    public static final DataTicket<TedVfxType> VFX_TYPE =
            DataTicket.create("ted_vfx_type", TedVfxType.class);

    private TedVfxRenderTickets() {
    }

    public static final DataTicket<Float> VFX_SCALE =
            DataTicket.create("ted_vfx_scale", Float.class);

    public static final DataTicket<Float> VFX_LENGTH =
            DataTicket.create("ted_vfx_length", Float.class);
}