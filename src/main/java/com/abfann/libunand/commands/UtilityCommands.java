package com.abfann.libunand.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class UtilityCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        // Comando /feed
        dispatcher.register(
                Commands.literal("feed")
                        .requires(source -> source.hasPermission(2)) // Solo OPs
                        .executes(UtilityCommands::feedSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(UtilityCommands::feedOther))
        );

        // Comando /heal
        dispatcher.register(
                Commands.literal("heal")
                        .requires(source -> source.hasPermission(2)) // Solo OPs
                        .executes(UtilityCommands::healSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(UtilityCommands::healOther))
        );
    }

    // /feed (sin argumento)
    private static int feedSelf(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        feedPlayer(player);

        player.sendMessage(
                new StringTextComponent("Has sido alimentado completamente!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        return 1;
    }

    // /feed <jugador>
    private static int feedOther(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
        feedPlayer(target);

        context.getSource().sendSuccess(
                new StringTextComponent("Has alimentado a " + target.getName().getString())
                        .withStyle(TextFormatting.GREEN),
                true
        );

        target.sendMessage(
                new StringTextComponent("Has sido alimentado!")
                        .withStyle(TextFormatting.GREEN),
                target.getUUID()
        );

        return 1;
    }

    // /heal (sin argumento)
    private static int healSelf(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        healPlayer(player);

        player.sendMessage(
                new StringTextComponent("Has sido curado completamente!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        return 1;
    }

    // /heal <jugador>
    private static int healOther(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
        healPlayer(target);

        context.getSource().sendSuccess(
                new StringTextComponent("Has curado a " + target.getName().getString())
                        .withStyle(TextFormatting.GREEN),
                true
        );

        target.sendMessage(
                new StringTextComponent("Has sido curado!")
                        .withStyle(TextFormatting.GREEN),
                target.getUUID()
        );

        return 1;
    }

    /**
     * Alimenta completamente a un jugador
     */
    private static void feedPlayer(ServerPlayerEntity player) {
        FoodStats foodStats = player.getFoodData();
        foodStats.setFoodLevel(20); // Hambre completa
        foodStats.setSaturation(20.0F); // Saturacion completa
    }

    /**
     * Cura completamente a un jugador
     */
    private static void healPlayer(ServerPlayerEntity player) {
        player.setHealth(player.getMaxHealth()); // Vida completa
        player.removeAllEffects(); // Remover efectos negativos
        player.getFoodData().setFoodLevel(20); // Bonus: tambien alimenta
        player.getFoodData().setSaturation(20.0F);
        player.clearFire(); // Apagar fuego
    }
}