package com.licht_meilleur.the_end_of_dragon.command;

import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class TEDDebugCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ted")
                        .then(Commands.literal("spawn")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    EndDragonSpawnHandler.spawn(level, context.getSource().getPosition());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("[TED] spawn command executed"),
                                            false
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("kill_ender_dragon")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();

                                    int count = 0;
                                    var box = new AABB(context.getSource().getPosition(), context.getSource().getPosition()).inflate(512.0D);

                                    for (EnderDragon dragon : level.getEntitiesOfClass(EnderDragon.class, box)) {
                                        dragon.hurtServer(level, level.damageSources().generic(), 1000000.0F);
                                        count++;
                                    }

                                    int finalCount = count;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("[TED] killed ender dragons: " + finalCount),
                                            false
                                    );
                                    return count;
                                })
                        )
                        .then(Commands.literal("bow")
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException();

                                    ItemStack stack = new ItemStack(ModItems.TED_DEBUG_BOW);
                                    player.getInventory().add(stack);

                                    return 1;
                                })
                        )
        );
    }

    private TEDDebugCommands() {
    }
}