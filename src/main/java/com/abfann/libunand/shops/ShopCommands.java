package com.abfann.libunand.shops;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ShopCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("shop")
                        .then(Commands.literal("itemname")
                                .executes(ShopCommands::getItemName))
                        .then(Commands.literal("help")
                                .executes(ShopCommands::showHelp))
        );
    }

    // /shop itemname
    private static int getItemName(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("Debes sostener un item en tu mano derecha!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        String itemId = heldItem.getItem().getRegistryName().toString();

        player.sendMessage(
                new StringTextComponent("=== Item en Mano ===").withStyle(TextFormatting.GOLD),
                player.getUUID()
        );
        player.sendMessage(
                new StringTextComponent("Nombre: " + heldItem.getHoverName().getString())
                        .withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );
        player.sendMessage(
                new StringTextComponent("ID: " + itemId)
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );
        player.sendMessage(
                new StringTextComponent("Para usar en cartel: " + itemId.split(":")[1].toUpperCase())
                        .withStyle(TextFormatting.AQUA),
                player.getUUID()
        );

        return 1;
    }

    // /shop help
    private static int showHelp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        player.sendMessage(new StringTextComponent("=== Sistema de Tiendas ===").withStyle(TextFormatting.GOLD), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());

        player.sendMessage(new StringTextComponent("COFRE VINCULADO:").withStyle(TextFormatting.GREEN), player.getUUID());
        player.sendMessage(new StringTextComponent("1. Coloca un cofre").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("2. Coloca cartel encima con [SHOP]").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("3. El cofre queda vinculado a tus tiendas").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());

        player.sendMessage(new StringTextComponent("TIENDA DE VENTA [SELL]:").withStyle(TextFormatting.BLUE), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 1: [SELL]").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 2: nombre_item (ej: diamond)").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 3: cantidad (ej: 1)").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 4: precio (ej: 10)").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());

        player.sendMessage(new StringTextComponent("TIENDA DE COMPRA [BUY]:").withStyle(TextFormatting.DARK_GREEN), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 1: [BUY]").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 2: nombre_item").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 3: cantidad").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("Linea 4: precio").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());

        player.sendMessage(new StringTextComponent("COMANDOS:").withStyle(TextFormatting.AQUA), player.getUUID());
        player.sendMessage(new StringTextComponent("/shop itemname - Ver ID del item en mano").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("/shop help - Mostrar esta ayuda").withStyle(TextFormatting.YELLOW), player.getUUID());

        return 1;
    }
}