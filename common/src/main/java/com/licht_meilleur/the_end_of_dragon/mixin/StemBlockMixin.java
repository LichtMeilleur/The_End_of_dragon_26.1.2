package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StemBlock.class)
public abstract class StemBlockMixin {

    @Inject(
            method = "mayPlaceOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void theEndOfDragon$allowRechorusFarmland(
            BlockState floorState,
            BlockGetter level,
            BlockPos floorPosition,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (floorState.is(
                ModBlocks.RECHORUS_FARMLAND
        )) {
            callback.setReturnValue(
                    true
            );
        }
    }
}