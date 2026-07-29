package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.village.trust
        .TedVillageTrustStage;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/*
 * エンダーマン村の実際の取引内容。
 *
 * 信頼度段階ごとに登録処理を分け、
 * 後から取引を追加しやすくしている。
 */
public final class TedVillageTradeCatalog {

    private TedVillageTradeCatalog() {
    }

    public static void registerAll() {
        registerLevel1Trades();
        registerLevel2Trades();
        registerLevel3Trades();
        registerLevel4Trades();
        registerLevel5Trades();
        registerLevel6Trades();
    }

    /*
     * 信頼度1。
     */
    private static void registerLevel1Trades() {

        /*
         * 花全般8個
         * ＝ エンダーパール1個
         */
        registerNormal(
                "flowers_for_ender_pearl",
                TedVillageTrustStage.LEVEL_1,
                List.of(
                        TedVillageTradeIngredient.ofTag(
                                TedVillageTradeTags.FLOWERS,
                                8,
                                new ItemStack(
                                        Items.POPPY
                                )
                        )
                ),
                new ItemStack(
                        Items.ENDER_PEARL,
                        1
                )
        );

        /*
         * 作物全般16個
         * ＝ エンダーパール1個
         */
        registerNormal(
                "crops_for_ender_pearl",
                TedVillageTrustStage.LEVEL_1,
                List.of(
                        TedVillageTradeIngredient.ofTag(
                                TedVillageTradeTags.CROPS,
                                16,
                                new ItemStack(
                                        Items.WHEAT
                                )
                        )
                ),
                new ItemStack(
                        Items.ENDER_PEARL,
                        1
                )
        );

        /*
         * 草ブロックまたはポドソル1個
         * ＝ エンダーパール1個
         */
        registerNormal(
                "soil_for_ender_pearl",
                TedVillageTrustStage.LEVEL_1,
                List.of(
                        TedVillageTradeIngredient.ofTag(
                                TedVillageTradeTags.SOIL,
                                1,
                                new ItemStack(
                                        Items.GRASS_BLOCK
                                )
                        )
                ),
                new ItemStack(
                        Items.ENDER_PEARL,
                        1
                )
        );

        /*
         * 作物全般1個
         * ＝ コーラスフルーツ1個
         */
        registerNormal(
                "crop_for_chorus_fruit",
                TedVillageTrustStage.LEVEL_1,
                List.of(
                        TedVillageTradeIngredient.ofTag(
                                TedVillageTradeTags.CROPS,
                                1,
                                new ItemStack(
                                        Items.WHEAT
                                )
                        )
                ),
                new ItemStack(
                        Items.CHORUS_FRUIT,
                        1
                )
        );
    }

    /*
     * 信頼度2。
     */
    private static void registerLevel2Trades() {

        /*
         * 本1個＋木炭1個
         * ＝ エンダーパール指南書1個
         */
        registerNormal(
                "ender_pearl_guide_book",
                TedVillageTrustStage.LEVEL_2,
                bookIngredients(),
                new ItemStack(
                        ModItems.ENDER_PEARL_GUIDE_BOOK,
                        1
                )
        );

        /*
         * エンダーパール5個
         * ラピスラズリ4個
         * アメジスト4個
         * レッドストーン4個
         * ＝ 水転送装置A
         */
        registerWorkBench(
                "water_transfer_machine_a",
                TedVillageTrustStage.LEVEL_2,
                waterTransferMachineIngredients(),
                new ItemStack(
                        ModItems.WATER_TRANSFER_MACHINE_A,
                        1
                )
        );

        /*
         * 同じ素材
         * ＝ 水転送装置B
         */
        registerWorkBench(
                "water_transfer_machine_b",
                TedVillageTrustStage.LEVEL_2,
                waterTransferMachineIngredients(),
                new ItemStack(
                        ModItems.WATER_TRANSFER_MACHINE_B,
                        1
                )
        );
    }

    /*
     * 信頼度3。
     */
    private static void registerLevel3Trades() {

        /*
         * 本1個＋木炭1個
         * ＝ エンダーパール応用書1個
         */
        registerNormal(
                "ender_pearl_application_book",
                TedVillageTrustStage.LEVEL_3,
                bookIngredients(),
                new ItemStack(
                        ModItems.ENDER_PEARL_APPLICATION_BOOK,
                        1
                )
        );

        /*
         * スイカの種16個
         * コーラスフルーツ16個
         * ＝ 試作コーラススイカの種8個
         */
        registerWorkBench(
                "rechorus_melon_seed_prototype",
                TedVillageTrustStage.LEVEL_3,
                List.of(
                        ingredient(
                                Items.MELON_SEEDS,
                                16
                        ),
                        ingredient(
                                Items.CHORUS_FRUIT,
                                16
                        )
                ),
                new ItemStack(
                        ModItems.RECHORUS_MELON_SEED_PROTOTYPE,
                        8
                )
        );
    }

    /*
     * 信頼度4。
     */
    private static void registerLevel4Trades() {

        /*
         * 本1個＋木炭1個
         * ＝ エンダーパール上級書1個
         */
        registerNormal(
                "ender_pearl_advanced_book",
                TedVillageTrustStage.LEVEL_4,
                bookIngredients(),
                new ItemStack(
                        ModItems.ENDER_PEARL_ADVANCED_BOOK,
                        1
                )
        );
    }

    /*
     * 信頼度5。
     */
    private static void registerLevel5Trades() {

        /*
         * 本1個＋木炭1個
         * ＝ エンダーパール熟達書1個
         */
        registerNormal(
                "ender_pearl_mastery_book",
                TedVillageTrustStage.LEVEL_5,
                bookIngredients(),
                new ItemStack(
                        ModItems.ENDER_PEARL_MASTERY_BOOK,
                        1
                )
        );

        /*
         * マングローブの芽16個
         * コーラスフルーツ16個
         * ＝ 試作コーラスプラントの種1個
         */
        registerWorkBench(
                "rechorus_plant_seed",
                TedVillageTrustStage.LEVEL_5,
                List.of(
                        ingredient(
                                Items.MANGROVE_PROPAGULE,
                                16
                        ),
                        ingredient(
                                Items.CHORUS_FRUIT,
                                16
                        )
                ),
                new ItemStack(
                        ModItems.RECHORUS_PLANT_SEED,
                        1
                )
        );
    }

    /*
     * 信頼度6。
     */
    private static void registerLevel6Trades() {

        /*
         * 本1個＋木炭1個
         * ＝ エンダーパール極意書1個
         */
        registerNormal(
                "ender_pearl_secret_book",
                TedVillageTrustStage.LEVEL_6,
                bookIngredients(),
                new ItemStack(
                        ModItems.ENDER_PEARL_SECRET_BOOK,
                        1
                )
        );

        /*
         * エンダーパール1個
         * 果汁水バケツ3個
         * ＝ 位相パール1個
         *
         * バケツはスタックできないため、
         * 3つの素材枠へ1個ずつ入れる。
         */
        registerWorkBench(
                "different_phase_pearl",
                TedVillageTrustStage.LEVEL_6,
                List.of(

                        ingredient(
                                ModItems.RECHORUS_JUICE_BUCKET,
                                1
                        ),
                        ingredient(
                                Items.ENDER_PEARL,
                                1
                        ),

                        ingredient(
                                ModItems.RECHORUS_JUICE_BUCKET,
                                1
                        ),
                        ingredient(
                                ModItems.RECHORUS_JUICE_BUCKET,
                                1
                        )
                ),
                new ItemStack(
                        ModItems.DIFFERENT_PHASE_PEARL,
                        1
                )
        );
    }

    /*
     * 通常交換を登録する。
     */
    private static void registerNormal(
            String id,
            TedVillageTrustStage trustStage,
            List<TedVillageTradeIngredient> ingredients,
            ItemStack result
    ) {
        TedVillageTradeRegistry.register(
                new TedVillageTradeDefinition(
                        id,
                        TedVillageTradeType.NORMAL,
                        trustStage,
                        ingredients,
                        result
                )
        );
    }

    /*
     * 作業台取引を登録する。
     */
    private static void registerWorkBench(
            String id,
            TedVillageTrustStage trustStage,
            List<TedVillageTradeIngredient> ingredients,
            ItemStack result
    ) {
        TedVillageTradeRegistry.register(
                new TedVillageTradeDefinition(
                        id,
                        TedVillageTradeType.WORK_BENCH,
                        trustStage,
                        ingredients,
                        result
                )
        );
    }

    /*
     * 特定アイテム素材の短縮記法。
     */
    private static TedVillageTradeIngredient ingredient(
            Item item,
            int count
    ) {
        return TedVillageTradeIngredient.of(
                new ItemStack(
                        item,
                        count
                )
        );
    }

    /*
     * 指南書系に共通する素材。
     */
    private static List<TedVillageTradeIngredient>
    bookIngredients() {
        return List.of(
                ingredient(
                        Items.BOOK,
                        1
                ),
                ingredient(
                        Items.CHARCOAL,
                        1
                )
        );
    }

    /*
     * 水転送装置A・Bに共通する素材。
     */
    private static List<TedVillageTradeIngredient>
    waterTransferMachineIngredients() {
        return List.of(
                ingredient(
                        Items.ENDER_PEARL,
                        5
                ),
                ingredient(
                        Items.LAPIS_LAZULI,
                        4
                ),
                ingredient(
                        Items.AMETHYST_SHARD,
                        4
                ),
                ingredient(
                        Items.REDSTONE,
                        4
                )
        );
    }
}