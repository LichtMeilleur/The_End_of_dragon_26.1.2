package com.licht_meilleur.the_end_of_dragon.client;

import com.geckolib.constant.dataticket.DataTicket;

import java.util.UUID;

public final class DragonRenderTickets {
    public static final DataTicket<UUID> DRAGON_UUID =
            DataTicket.create("the_end_of_dragon_uuid", UUID.class);

    private DragonRenderTickets() {
    }
}