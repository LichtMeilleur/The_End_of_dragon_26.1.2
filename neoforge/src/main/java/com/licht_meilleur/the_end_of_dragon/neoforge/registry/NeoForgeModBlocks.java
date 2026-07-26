package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.block.EndermanVillageGatewayBlock;
import com.licht_meilleur.the_end_of_dragon.world.block.EndermanVillageReturnGatewayBlock;
import com.licht_meilleur.the_end_of_dragon.world.block.RechorusFlowerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    Registries.BLOCK,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            Block,
            EndermanVillageGatewayBlock> ENDERMAN_VILLAGE_GATEWAY =
            BLOCKS.register(
                    "enderman_village_gateway",
                    () -> new EndermanVillageGatewayBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.ENDERMAN_VILLAGE_GATEWAY_KEY
                                    )
                                    .strength(
                                            3.0F,
                                            1200.0F
                                    )
                                    .sound(
                                            SoundType.AMETHYST
                                    )
                                    .lightLevel(
                                            state -> 10
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            EndermanVillageReturnGatewayBlock>
            ENDERMAN_VILLAGE_RETURN_GATEWAY =
            BLOCKS.register(
                    "enderman_village_return_gateway",
                    () -> new EndermanVillageReturnGatewayBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .ENDERMAN_VILLAGE_RETURN_GATEWAY_KEY
                                    )
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    .sound(
                                            SoundType.AMETHYST
                                    )
                                    .lightLevel(
                                            state -> 10
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_MELON =
            BLOCKS.register(
                    "rechorus_melon",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_MELON_KEY
                                    )
                                    .strength(1.0F)
                                    .sound(SoundType.WOOD)
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_PLANT_CORE =
            BLOCKS.register(
                    "rechorus_plant_core",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_PLANT_CORE_KEY
                                    )
                                    .strength(2.0F, 6.0F)
                                    .sound(SoundType.WOOD)
                                    .lightLevel(state -> 5)
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            WATER_TRANSFER_MACHINE_A =
            BLOCKS.register(
                    "water_transfer_machine_a",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_A_KEY
                                    )
                                    .strength(3.0F, 12.0F)
                                    .sound(SoundType.METAL)
                                    .lightLevel(state -> 4)
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            WATER_TRANSFER_MACHINE_B =
            BLOCKS.register(
                    "water_transfer_machine_b",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_B_KEY
                                    )
                                    .strength(3.0F, 12.0F)
                                    .sound(SoundType.METAL)
                                    .lightLevel(state -> 4)
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_PLANT_CORE_PREVIEW =
            BLOCKS.register(
                    "rechorus_plant_core_preview",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_PLANT_CORE_PREVIEW_KEY
                                    )
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    .noCollision()
                                    .noOcclusion()
                                    .replaceable()
                                    .lightLevel(
                                            state -> 3
                                    )
                    )
            );

    public static final DeferredHolder<Block, Block>
            WATER_TRANSFER_MACHINE_B_PREVIEW =
            BLOCKS.register(
                    "water_transfer_machine_b_preview",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_B_PREVIEW_KEY
                                    )
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    .noCollision()
                                    .noOcclusion()
                                    .replaceable()
                                    .lightLevel(
                                            state -> 3
                                    )
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_ROOT =
            BLOCKS.register(
                    "rechorus_root",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_ROOT_KEY
                                    )
                                    .strength(
                                            2.0F,
                                            6.0F
                                    )
                                    .sound(
                                            SoundType.WOOD
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_PLANT =
            BLOCKS.register(
                    "rechorus_plant",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_PLANT_KEY
                                    )
                                    .strength(
                                            2.0F,
                                            6.0F
                                    )
                                    .sound(
                                            SoundType.WOOD
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            RechorusFlowerBlock> RECHORUS_FLOWER =
            BLOCKS.register(
                    "rechorus_flower",
                    () -> new RechorusFlowerBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_FLOWER_KEY
                                    )
                                    .strength(
                                            0.5F
                                    )
                                    .sound(
                                            SoundType.AZALEA
                                    )
                                    .noCollision()
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_MELON_STEM =
            BLOCKS.register(
                    "rechorus_melon_stem",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_MELON_STEM_KEY
                                    )
                                    .strength(
                                            0.0F
                                    )
                                    .sound(
                                            SoundType.CROP
                                    )
                                    .noCollision()
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, LiquidBlock>
            RECHORUS_JUICE =
            BLOCKS.register(
                    "rechorus_juice",
                    () -> new LiquidBlock(
                            NeoForgeModFluids
                                    .RECHORUS_JUICE_SOURCE
                                    .get(),
                            BlockBehaviour.Properties
                                    .ofFullCopy(
                                            Blocks.WATER
                                    )
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_JUICE_KEY
                                    )
                                    .noLootTable()
                    )
            );

    public static final DeferredHolder<
            Block,
            DebugMarkerBlock> DEBUG_MARKER =
            BLOCKS.register(
                    "debug_marker",
                    () -> new DebugMarkerBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.DEBUG_MARKER_KEY
                                    )
                                    /*
                                     * サバイバルで破壊不可。
                                     */
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    /*
                                     * アイテムを落とさない。
                                     */
                                    .noLootTable()
                                    /*
                                     * 当たり判定なし。
                                     */
                                    .noCollision()
                                    /*
                                     * 周囲の面を隠さない。
                                     */
                                    .noOcclusion()
                                    /*
                                     * デバッグ位置を見つけやすくする。
                                     */
                                    .lightLevel(
                                            state -> 15
                                    )
                    )
            );

    public static final DeferredHolder<
            Block,
            LiquidBlock> RECHORUS_JUICE_GUIDE =
            BLOCKS.register(
                    "rechorus_juice_guide",
                    () -> new LiquidBlock(
                            NeoForgeModFluids
                                    .RECHORUS_JUICE_GUIDE_SOURCE
                                    .get(),
                            BlockBehaviour.Properties
                                    .ofFullCopy(
                                            Blocks.WATER
                                    )
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_JUICE_GUIDE_KEY
                                    )
                                    .noLootTable()
                    )
            );





    public static void register(
            IEventBus modBus
    ) {
        BLOCKS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModBlocks.bindNeoForge(
                ENDERMAN_VILLAGE_GATEWAY.get(),
                ENDERMAN_VILLAGE_RETURN_GATEWAY.get(),
                RECHORUS_MELON.get(),
                RECHORUS_PLANT_CORE.get(),
                WATER_TRANSFER_MACHINE_A.get(),
                WATER_TRANSFER_MACHINE_B.get(),
                RECHORUS_ROOT.get(),
                RECHORUS_PLANT.get(),
                RECHORUS_FLOWER.get(),
                RECHORUS_MELON_STEM.get(),
                RECHORUS_PLANT_CORE_PREVIEW.get(),
                WATER_TRANSFER_MACHINE_B_PREVIEW.get(),
                RECHORUS_JUICE.get(),
                RECHORUS_JUICE_GUIDE.get(),
                DEBUG_MARKER.get()


        );
    }

    private NeoForgeModBlocks() {
    }
}