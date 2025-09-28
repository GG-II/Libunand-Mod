package com.abfann.libunand.commands;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.data.PlayerDataHandler;
import com.abfann.libunand.items.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import com.abfann.libunand.data.IPlayerEconomy;

public class EconomyCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("jojocoins")
                        .then(Commands.literal("balance")
                                .executes(EconomyCommands::getBalance))
                        .then(Commands.literal("pay")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(EconomyCommands::payPlayer))))
                        .then(Commands.literal("give")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(EconomyCommands::giveCoins))))
                        .then(Commands.literal("withdraw")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::withdrawCoins)))
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::depositCoins)))
                        .then(Commands.literal("help")
                                .executes(EconomyCommands::showHelp))
        );
    }

    // /jojocoins balance
    private static int getBalance(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);

        player.sendMessage(
                new StringTextComponent("Tu balance actual: ")
                        .withStyle(TextFormatting.GOLD)
                        .append(new StringTextComponent(economy.getBalance() + " JoJoCoins")
                                .withStyle(TextFormatting.YELLOW)),
                player.getUUID()
        );

        return 1;
    }

    // /jojocoins pay <jugador> <cantidad>
    private static int payPlayer(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = context.getSource().getPlayerOrException();
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if (sender.equals(target)) {
            sender.sendMessage(
                    new StringTextComponent("No puedes transferirte dinero a ti mismo!")
                            .withStyle(TextFormatting.RED),
                    sender.getUUID()
            );
            return 0;
        }

        IPlayerEconomy senderEconomy = PlayerDataHandler.getPlayerEconomy(sender);
        IPlayerEconomy targetEconomy = PlayerDataHandler.getPlayerEconomy(target);

        if (!senderEconomy.hasBalance(amount)) {
            sender.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Tienes: " + senderEconomy.getBalance())
                            .withStyle(TextFormatting.RED),
                    sender.getUUID()
            );
            return 0;
        }

        if (senderEconomy.transferTo(targetEconomy, amount)) {
            // Mensaje al emisor
            sender.sendMessage(
                    new StringTextComponent("Transferiste ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(amount + " JoJoCoins")
                                    .withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" a " + target.getName().getString())
                                    .withStyle(TextFormatting.GREEN)),
                    sender.getUUID()
            );

            // Mensaje al receptor
            target.sendMessage(
                    new StringTextComponent("Recibiste ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(amount + " JoJoCoins")
                                    .withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" de " + sender.getName().getString())
                                    .withStyle(TextFormatting.GREEN)),
                    target.getUUID()
            );

            LibunandMod.LOGGER.info("Transferencia: {} -> {} ({} JoJoCoins)",
                    sender.getName().getString(), target.getName().getString(), amount);

            return 1;
        }

        return 0;
    }

    // /jojocoins give <jugador> <cantidad> (Solo OPs)
    private static int giveCoins(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(target);
        economy.addBalance(amount);

        context.getSource().sendSuccess(
                new StringTextComponent("Diste ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(amount + " JoJoCoins")
                                .withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent(" a " + target.getName().getString())
                                .withStyle(TextFormatting.GREEN)),
                true
        );

        target.sendMessage(
                new StringTextComponent("Recibiste ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(amount + " JoJoCoins")
                                .withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent(" del servidor")
                                .withStyle(TextFormatting.GREEN)),
                target.getUUID()
        );

        return 1;
    }

    // /jojocoins withdraw <cantidad>
    private static int withdrawCoins(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);

        if (!economy.hasBalance(amount)) {
            player.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Tienes: " + economy.getBalance())
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        if (economy.removeBalance(amount)) {
            // Dar items físicos al jugador
            ItemStack coinStack = new ItemStack(ModItems.JOJOCOIN.get(), amount);

            if (!player.inventory.add(coinStack)) {
                // Si el inventario está lleno, dropear en el suelo
                player.drop(coinStack, false);
            }

            player.sendMessage(
                    new StringTextComponent("Retiraste ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(amount + " JoJoCoins")
                                    .withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" a tu inventario")
                                    .withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );

            return 1;
        }

        return 0;
    }

    // /jojocoins deposit <cantidad>
    private static int depositCoins(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        // Contar JoJoCoins en el inventario
        int coinsInInventory = 0;
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() == ModItems.JOJOCOIN.get()) {
                coinsInInventory += stack.getCount();
            }
        }

        if (coinsInInventory < amount) {
            player.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins en tu inventario! Tienes: " + coinsInInventory)
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // Remover JoJoCoins del inventario
        int remaining = amount;
        for (int i = 0; i < player.inventory.items.size() && remaining > 0; i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.getItem() == ModItems.JOJOCOIN.get()) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }

        // Añadir al balance virtual
        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);
        economy.addBalance(amount);

        player.sendMessage(
                new StringTextComponent("Depositaste ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(amount + " JoJoCoins")
                                .withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent(" a tu cuenta")
                                .withStyle(TextFormatting.GREEN)),
                player.getUUID()
        );

        return 1;
    }

    // /jojocoins help
    private static int showHelp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        player.sendMessage(new StringTextComponent("=== Comandos de JoJoCoins ===").withStyle(TextFormatting.GOLD), player.getUUID());
        player.sendMessage(new StringTextComponent("• /jojocoins balance - Ver tu saldo").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /jojocoins pay <jugador> <cantidad> - Transferir dinero").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /jojocoins withdraw <cantidad> - Retirar a inventario").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /jojocoins deposit <cantidad> - Depositar del inventario").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /jojocoins help - Mostrar esta ayuda").withStyle(TextFormatting.YELLOW), player.getUUID());

        return 1;
    }
}