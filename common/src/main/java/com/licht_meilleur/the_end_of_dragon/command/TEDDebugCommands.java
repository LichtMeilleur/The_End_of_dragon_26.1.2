package com.licht_meilleur.the_end_of_dragon.command;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public final class TEDDebugCommands {

    private static boolean canUseTedDebug(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();

        // コンソール等は許可
        if (player == null) {
            return true;
        }

        return source.getServer()
                .getPlayerList()
                .isOp(player.nameAndId());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ted")
                        .requires(TEDDebugCommands::canUseTedDebug)
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

                        .then(Commands.literal("test_unbreakable_gear")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                                    giveRoarTestItems(player);
                                    giveUnbreakableGear(player);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Granted unbreakable test gear."),
                                            false
                                    );

                                    return 1;
                                })
                        )
                        .then(Commands.literal("kill")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ServerLevel level = ctx.getSource().getLevel();

                                    TheEndOfDragonCoreEntity dragon =
                                            level.getEntitiesOfClass(
                                                            TheEndOfDragonCoreEntity.class,
                                                            player.getBoundingBox().inflate(256.0D),
                                                            e -> e.isAlive()
                                                    )
                                                    .stream()
                                                    .min(Comparator.comparingDouble(player::distanceToSqr))
                                                    .orElse(null);

                                    if (dragon == null) {
                                        ctx.getSource().sendFailure(Component.literal("Dragon not found"));
                                        return 0;
                                    }

                                    dragon.setHealth(0.0F);
                                    dragon.die(level.damageSources().generic());

                                    return 1;
                                }))


        );

        dispatcher.register(
                Commands.literal("ted_anim")
                        .requires(TEDDebugCommands::canUseTedDebug)
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
                                        case "photon_blaster" -> DragonState.PHOTON_BLASTER;
                                        case "tackle" -> DragonState.BLASTER_TACKLE;
                                        case "photon_buster" -> DragonState.PHOTON_BUSTER;
                                        case "judgment_ray" -> DragonState.JUDGMENT_RAY;

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

        dispatcher.register(
                Commands.literal("ted_debug")
                        .requires(TEDDebugCommands::canUseTedDebug)
                        .then(Commands.literal("yaw")
                                .then(Commands.argument("yaw", com.mojang.brigadier.arguments.FloatArgumentType.floatArg())
                                        .executes(ctx -> {
                                            float yaw = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "yaw");

                                            var source = ctx.getSource();
                                            var player = source.getPlayerOrException();
                                            var level = player.level();

                                            var dragons = level.getEntitiesOfClass(
                                                    TheEndOfDragonCoreEntity.class,
                                                    player.getBoundingBox().inflate(128.0D)
                                            );

                                            for (TheEndOfDragonCoreEntity dragon : dragons) {
                                                dragon.snapTo(
                                                        dragon.getX(),
                                                        dragon.getY(),
                                                        dragon.getZ(),
                                                        yaw,
                                                        dragon.getXRot()
                                                );

                                                dragon.setYBodyRot(yaw);
                                                dragon.setYHeadRot(yaw);
                                                dragon.yRotO = yaw;
                                                dragon.yBodyRotO = yaw;
                                                dragon.yHeadRotO = yaw;
                                            }

                                            source.sendSuccess(
                                                    () -> Component.literal("[TED] yaw set: " + yaw + " count=" + dragons.size()),
                                                    false
                                            );

                                            return dragons.size();
                                        })
                                )
                        )

                        .then(Commands.literal("tp_here")
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    var player = source.getPlayerOrException();
                                    var level = player.level();

                                    var dragons = level.getEntitiesOfClass(
                                            TheEndOfDragonCoreEntity.class,
                                            player.getBoundingBox().inflate(128.0D)
                                    );

                                    for (TheEndOfDragonCoreEntity dragon : dragons) {
                                        dragon.snapTo(
                                                player.getX(),
                                                player.getY(),
                                                player.getZ(),
                                                dragon.getYRot(),
                                                dragon.getXRot()
                                        );
                                    }

                                    source.sendSuccess(
                                            () -> Component.literal("[TED] teleported count=" + dragons.size()),
                                            false
                                    );

                                    return dragons.size();
                                })
                        )

                        .then(Commands.literal("vfx")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(context -> {
                                            var source = context.getSource();
                                            var player = source.getPlayerOrException();
                                            var level = player.level();

                                            String key = StringArgumentType.getString(context, "type");

                                            TedVfxType type = switch (key) {
                                                case "orb" -> TedVfxType.ORB_OF_ANIHILATION;
                                                case "laser" -> TedVfxType.TED_LASER_BEAM;
                                                case "jet" -> TedVfxType.TED_JET;
                                                case "light" -> TedVfxType.LIGHT_OF_DESTRUCTION;
                                                default -> TedVfxType.LIGHT_PROJECTILE;
                                            };

                                            TedVfxEntity vfx = new TedVfxEntity(
                                                    ModEntities.TED_VFX,
                                                    level
                                            );

                                            vfx.setup(type, 2.0F, 8.0F, 200);

                                            vfx.snapTo(
                                                    player.getX(),
                                                    player.getY() + 1.5D,
                                                    player.getZ(),
                                                    player.getYRot(),
                                                    player.getXRot()
                                            );

                                            level.addFreshEntity(vfx);

                                            source.sendSuccess(
                                                    () -> Component.literal("[TED] spawned vfx: " + type.id),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("rotate")
                                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    ServerLevel level = (ServerLevel) player.level();

                                                    float yaw = FloatArgumentType.getFloat(ctx, "yaw");
                                                    float pitch = FloatArgumentType.getFloat(ctx, "pitch");

                                                    TheEndOfDragonCoreEntity dragon =
                                                            level.getEntitiesOfClass(
                                                                    TheEndOfDragonCoreEntity.class,
                                                                    player.getBoundingBox().inflate(128.0D)
                                                            ).stream().findFirst().orElse(null);

                                                    if (dragon == null) {
                                                        ctx.getSource().sendFailure(Component.literal("No The End Of Dragon nearby"));
                                                        return 0;
                                                    }

                                                    dragon.setVisualRotation(yaw, pitch);

                                                    System.out.println("===== TED ROTATE =====");
                                                    System.out.println("YRot      = " + dragon.getYRot());
                                                    System.out.println("Pitch     = " + dragon.getVisualPitch());
                                                    System.out.println("======================");

                                                    System.out.println(
                                                            "[TED rotate] core=" + dragon.getId()
                                                                    + " visualPitch=" + dragon.getVisualPitch()
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("spawn_frozen")
                                .executes(ctx -> {

                                    CommandSourceStack source = ctx.getSource();

                                    if (!(source.getLevel() instanceof ServerLevel level)) {
                                        return 0;
                                    }

                                    TheEndOfDragonCoreEntity dragon =
                                            ModEntities.THE_END_OF_DRAGON.create(
                                                    level,
                                                    EntitySpawnReason.COMMAND
                                            );

                                    if (dragon == null) {
                                        return 0;
                                    }

                                    Vec3 pos = source.getPosition();

                                    dragon.snapTo(
                                            pos.x,
                                            pos.y,
                                            pos.z,
                                            source.getRotation().y,
                                            0.0F
                                    );

                                    dragon.setDebugFrozen(true);
                                    dragon.setCombatStarted(true);
                                    dragon.setDragonState(DragonState.IDLE);

                                    level.addFreshEntity(dragon);

                                    return 1;
                                })
                        )


        );
        dispatcher.register(
                Commands.literal("ted_air")
                        .requires(TEDDebugCommands::canUseTedDebug)
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.literal("ragnarok")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ServerLevel level = ctx.getSource().getLevel();

                                    TheEndOfDragonCoreEntity dragon =
                                            level.getEntitiesOfClass(
                                                            TheEndOfDragonCoreEntity.class,
                                                            player.getBoundingBox().inflate(256.0D),
                                                            e -> e.isAlive()
                                                    )
                                                    .stream()
                                                    .min(Comparator.comparingDouble(player::distanceToSqr))
                                                    .orElse(null);

                                    if (dragon == null) {
                                        ctx.getSource().sendFailure(Component.literal("Dragon not found"));
                                        return 0;
                                    }

                                    dragon.debugStartRagnarok();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Start Ragnarok test"), true);
                                    return 1;
                                }))
                        .then(Commands.literal("figure")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ServerLevel level = ctx.getSource().getLevel();

                                    TheEndOfDragonCoreEntity dragon =
                                            level.getEntitiesOfClass(
                                                            TheEndOfDragonCoreEntity.class,
                                                            player.getBoundingBox().inflate(256.0D),
                                                            e -> e.isAlive()
                                                    )
                                                    .stream()
                                                    .min(Comparator.comparingDouble(player::distanceToSqr))
                                                    .orElse(null);

                                    if (dragon == null) {
                                        ctx.getSource().sendFailure(Component.literal("Dragon not found"));
                                        return 0;
                                    }

                                    dragon.debugStartFigureEight();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Start Figure Eight test"), true);
                                    return 1;
                                }))
        );



    }

    private TEDDebugCommands() {
    }

    private static void giveUnbreakableGear(ServerPlayer player) {
        player.level().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "item replace entity @s armor.head with minecraft:netherite_helmet[unbreakable={show_in_tooltip:false}]"
        );
        player.level().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "item replace entity @s armor.chest with minecraft:netherite_chestplate[unbreakable={show_in_tooltip:false}]"
        );
        player.level().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "item replace entity @s armor.legs with minecraft:netherite_leggings[unbreakable={show_in_tooltip:false}]"
        );
        player.level().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "item replace entity @s armor.feet with minecraft:netherite_boots[unbreakable={show_in_tooltip:false}]"
        );






    }
    private static void giveRoarTestItems(ServerPlayer player) {
        var server = player.level().getServer();

        server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "give @s minecraft:stick 1"
        );

        server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "give @s minecraft:netherite_sword[unbreakable={show_in_tooltip:false}] 1"
        );

        server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "give @s minecraft:iron_sword[max_damage=2000,damage=0] 1"
        );
    }

}