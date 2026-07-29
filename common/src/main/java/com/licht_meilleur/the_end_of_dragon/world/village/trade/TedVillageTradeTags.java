package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TedVillageTradeTags {

    public static final TagKey<Item>
            FLOWERS =
            TagKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "village_trade_flowers"
                    )
            );

    public static final TagKey<Item>
            CROPS =
            TagKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "village_trade_crops"
                    )
            );

    public static final TagKey<Item>
            SOIL =
            TagKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "village_trade_soil"
                    )
            );

    private TedVillageTradeTags() {
    }
}