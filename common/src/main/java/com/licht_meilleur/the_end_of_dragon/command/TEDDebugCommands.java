package com.licht_meilleur.the_end_of_dragon.command;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                                    try {
                                        ServerLevel level = context.getSource().getLevel();

                                        System.out.println("[TED] spawn command start");
                                        EndDragonSpawnHandler.spawn(level, context.getSource().getPosition());
                                        System.out.println("[TED] spawn command end");

                                        context.getSource().sendSuccess(
                                                () -> Component.literal("[TED] spawn command executed"),
                                                false
                                        );
                                        return 1;
                                    } catch (Throwable t) {
                                        System.err.println("[TED] spawn command failed");
                                        t.printStackTrace();

                                        context.getSource().sendFailure(
                                                Component.literal("[TED] spawn failed: " + t.getClass().getSimpleName())
                                        );
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("kill_ender_dragon")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();

                                    int count = 0;
                                    AABB box = new AABB(
                                            context.getSource().getPosition(),
                                            context.getSource().getPosition()
                                    ).inflate(512.0D);

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
                                    player.getInventory().add(new ItemStack(ModItems.TED_DEBUG_BOW));
                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("ted_anim")
                        .then(Commands.argument("animation", StringArgumentType.word())
                                .executes(context -> {
                                    String key = StringArgumentType.getString(context, "animation");

                                    DragonState state = switch (key) {
                                        case "idle" -> DragonState.IDLE;
                                        case "walk" -> DragonState.WALK;
                                        case "fly" -> DragonState.FLY;
                                        case "fly_start" -> DragonState.FLY_START;
                                        case "fly_left" -> DragonState.FLY_LEFT;
                                        case "fly_right" -> DragonState.FLY_RIGHT;
                                        case "fly_shot" -> DragonState.FLY_SHOT;
                                        case "fall" -> DragonState.FALL;
                                        case "landing" -> DragonState.LANDING;
                                        case "super_landing" -> DragonState.SUPER_LANDING;

                                        case "orb" -> DragonState.ORB_OF_ANNIHILATION;
                                        case "roar" -> DragonState.ROAR_OF_OBLITERATION;
                                        case "flames" -> DragonState.FLAMES_OF_RAGNAROK;
                                        case "light" -> DragonState.LIGHT_OF_DESTRUCTION;
                                        case "photon" -> DragonState.PHOTON_BLASTER;
                                        case "tackle" -> DragonState.BLASTER_TACKLE;

                                        default -> DragonState.IDLE;
                                    };

                                    var source = context.getSource();
                                    var player = source.getPlayerOrException();

                                    TheEndOfDragonCoreEntity nearest = null;
                                    double nearestDist = Double.MAX_VALUE;

                                    for (TheEndOfDragonCoreEntity dragon : player.level().getEntitiesOfClass(
                                            TheEndOfDragonCoreEntity.class,
                                            player.getBoundingBox().inflate(128.0D)
                                    )) {
                                        double dist = dragon.distanceToSqr(player);
                                        if (dist < nearestDist) {
                                            nearestDist = dist;
                                            nearest = dragon;
                                        }
                                    }

                                    if (nearest == null) {
                                        source.sendFailure(Component.literal("No The End Of Dragon Core nearby."));
                                        return 0;
                                    }

                                    nearest.setDragonState(state);


                                    var box = player.getBoundingBox().inflate(512.0D);

                                    var cores = player.level().getEntitiesOfClass(
                                            TheEndOfDragonCoreEntity.class,
                                            box
                                    );

                                    source.sendSuccess(
                                            () -> Component.literal("Core count: " + cores.size()),
                                            false
                                    );

                                    for (TheEndOfDragonCoreEntity dragon : cores) {
                                        double dist = dragon.distanceToSqr(player);

                                        source.sendSuccess(
                                                () -> Component.literal(
                                                        "Core id=" + dragon.getId()
                                                                + " dist=" + Math.sqrt(dist)
                                                                + " pos=" + dragon.position()
                                                                + " state=" + dragon.getDragonState()
                                                ),
                                                false
                                        );

                                        if (dist < nearestDist) {
                                            nearestDist = dist;
                                            nearest = dragon;
                                        }
                                    }

                                    if (nearest == null) {
                                        source.sendFailure(Component.literal("No The End Of Dragon Core nearby."));
                                        return 0;
                                    }

                                    nearest.setDragonState(state);

                                    return 1;
                                })
                        )
        );

    }

    private TEDDebugCommands() {
    }
}