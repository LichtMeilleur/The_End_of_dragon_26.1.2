package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.crop
        .RechorusIrrigationHelper;
import com.licht_meilleur.the_end_of_dragon.world.crop
        .RechorusIrrigationType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class HoeItemRechorusFarmlandMixin {

    @Inject(
            method = "useOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$createRechorusFarmland(
            UseOnContext context,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        Level level =
                context.getLevel();

        BlockPos position =
                context.getClickedPos();

        BlockState state =
                level.getBlockState(
                        position
                );

        System.out.println(
                "[TED] Hoe mixin called: "
                        + state
        );

        System.out.println(
                "[TED] irrigation: "
                        + RechorusIrrigationHelper.findIrrigation(
                        level,
                        position
                )
        );

        System.out.println(
                "[TED] farmland registered: "
                        + ModBlocks.RECHORUS_FARMLAND
        );

        /*
         * バニラで通常農地へ耕せる基本ブロック。
         */
        boolean tillable =
                state.is(
                        Blocks.DIRT
                )
                        || state.is(
                        Blocks.GRASS_BLOCK
                )
                        || state.is(
                        Blocks.DIRT_PATH
                );

        if (!tillable) {
            return;
        }

        /*
         * 上が塞がっている場合は耕さない。
         */
        if (!level.getBlockState(
                position.above()
        ).isAir()) {
            return;
        }

        RechorusIrrigationType irrigation =
                RechorusIrrigationHelper
                        .findIrrigation(
                                level,
                                position
                        );

        /*
         * 果汁水がない場合はMixinで処理せず、
         * バニラのHoeItemへ任せる。
         */
        if (irrigation
                != RechorusIrrigationType
                .RECHORUS_JUICE) {
            return;
        }

        Player player =
                context.getPlayer();

        level.playSound(
                player,
                position,
                SoundEvents.HOE_TILL,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        if (!level.isClientSide()) {
            level.setBlock(
                    position,
                    ModBlocks.RECHORUS_FARMLAND
                            .defaultBlockState(),
                    11
            );

            if (player != null) {
                EquipmentSlot slot =
                        context.getHand()
                                == net.minecraft.world
                                .InteractionHand.MAIN_HAND
                                ? EquipmentSlot.MAINHAND
                                : EquipmentSlot.OFFHAND;

                context.getItemInHand()
                        .hurtAndBreak(
                                1,
                                player,
                                slot
                        );
            }
        }

        callback.setReturnValue(
                InteractionResult.SUCCESS
        );
    }
}