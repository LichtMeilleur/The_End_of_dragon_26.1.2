package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class TedVillageGatewayManager {

    public static void tick(
            ServerLevel level
    ) {
        /*
         * 村ディメンション以外では処理しない。
         */
        if (level.dimension()
                != TedDimensions.ENDERMAN_VILLAGE) {
            return;
        }

        /*
         * 1秒に1回だけ確認。
         */
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        level
                );

        if (!villageState
                .isVillageGenerated()) {
            return;
        }

        BlockPos returnGatewayPos =
                villageState
                        .getReturnGatewayPosition();

        /*
         * 座標を含むチャンクをロードして確認。
         */
        level.getChunk(
                returnGatewayPos
        );

        if (level.getBlockState(
                returnGatewayPos
        ).is(
                ModBlocks
                        .ENDERMAN_VILLAGE_RETURN_GATEWAY
        )) {
            return;
        }

        /*
         * 門Bが消失していた場合は復元する。
         */
        boolean restored =
                level.setBlock(
                        returnGatewayPos,
                        ModBlocks
                                .ENDERMAN_VILLAGE_RETURN_GATEWAY
                                .defaultBlockState(),
                        3
                );

        if (restored) {
            TheEndOfDragon.LOGGER.warn(
                    "Restored missing Enderman village return gateway B at {}",
                    returnGatewayPos
            );
        } else {
            TheEndOfDragon.LOGGER.error(
                    "Failed to restore Enderman village return gateway B at {}",
                    returnGatewayPos
            );
        }
    }

    private TedVillageGatewayManager() {
    }
}