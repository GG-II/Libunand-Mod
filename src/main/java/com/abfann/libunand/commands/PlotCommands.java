package com.abfann.libunand.commands;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.data.IPlayerEconomy;
import com.abfann.libunand.data.PlayerDataHandler;
import com.abfann.libunand.items.SelectionTool;
import com.abfann.libunand.protection.Plot;
import com.abfann.libunand.protection.PlotManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

public class PlotCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("plot")
                        .then(Commands.literal("create")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("price", IntegerArgumentType.integer(1))
                                                .executes(PlotCommands::createPlot))))
                        .then(Commands.literal("list")
                                .executes(PlotCommands::listPlots))
                        .then(Commands.literal("info")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(PlotCommands::plotInfo))
                                .executes(PlotCommands::plotInfoHere))
                        .then(Commands.literal("delete")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(PlotCommands::deletePlot)))
                        .then(Commands.literal("buy")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(PlotCommands::buyPlot)))
                        .then(Commands.literal("help")
                                .executes(PlotCommands::showHelp))
                        .then(Commands.literal("tool")
                                .executes(PlotCommands::giveTool))
        );
    }

    // /plot create <nombre> <precio>
    private static int createPlot(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "name");
        int price = IntegerArgumentType.getInteger(context, "price");

        // Buscar hacha dorada en inventario
        ItemStack axe = null;
        for (ItemStack stack : player.inventory.items) {
            if (SelectionTool.isSelectionTool(stack)) {
                axe = stack;
                break;
            }
        }

        if (axe == null) {
            player.sendMessage(
                    new StringTextComponent("Necesitas un hacha dorada para seleccionar el area!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        if (!SelectionTool.hasBothPositions(axe)) {
            player.sendMessage(
                    new StringTextComponent("Necesitas seleccionar ambas posiciones con el hacha dorada!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        BlockPos pos1 = SelectionTool.getPosition1(axe);
        BlockPos pos2 = SelectionTool.getPosition2(axe);

        if (PlotManager.getInstance().createPlot(name, player.level, pos1, pos2, price)) {
            player.sendMessage(
                    new StringTextComponent("Lote '")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(name).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("' creado exitosamente por ").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(price + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                    player.getUUID()
            );

            // Limpiar selección
            SelectionTool.clearPositions(player);

            return 1;
        } else {
            player.sendMessage(
                    new StringTextComponent("No se pudo crear el lote. Verifica que el nombre sea unico y no haya solapamiento.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }
    }

    // /plot list
    private static int listPlots(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        List<Plot> availablePlots = PlotManager.getInstance().getAvailablePlots();

        if (availablePlots.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay lotes disponibles para compra.")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            return 1;
        }

        player.sendMessage(
                new StringTextComponent("=== Lotes Disponibles ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        for (Plot plot : availablePlots) {
            player.sendMessage(
                    new StringTextComponent("• ")
                            .withStyle(TextFormatting.YELLOW)
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.WHITE))
                            .append(new StringTextComponent(" - ").withStyle(TextFormatting.GRAY))
                            .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD))
                            .append(new StringTextComponent(" (" + plot.getArea() + " bloques)").withStyle(TextFormatting.GRAY)),
                    player.getUUID()
            );
        }

        return 1;
    }

    // /plot info <nombre>
    private static int plotInfo(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "name");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotByName(name);
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Lote '" + name + "' no encontrado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        showPlotInfo(player, plotOpt.get());
        return 1;
    }

    // /plot info (lote actual)
    private static int plotInfoHere(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            return 0;
        }

        showPlotInfo(player, plotOpt.get());
        return 1;
    }

    // /plot delete <nombre>
    private static int deletePlot(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");

        if (PlotManager.getInstance().deletePlot(name)) {
            context.getSource().sendSuccess(
                    new StringTextComponent("Lote '")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(name).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("' eliminado exitosamente.").withStyle(TextFormatting.GREEN)),
                    true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                    new StringTextComponent("Lote '" + name + "' no encontrado.")
                            .withStyle(TextFormatting.RED)
            );
            return 0;
        }
    }

    // /plot buy <nombre>
    private static int buyPlot(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "name");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotByName(name);
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Lote '" + name + "' no encontrado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        if (!plot.isForSale()) {
            player.sendMessage(
                    new StringTextComponent("Este lote no esta disponible para compra.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);

        if (!economy.hasBalance(plot.getPrice())) {
            player.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Necesitas: ")
                            .withStyle(TextFormatting.RED)
                            .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD))
                            .append(new StringTextComponent(", tienes: ").withStyle(TextFormatting.RED))
                            .append(new StringTextComponent(economy.getBalance() + " JoJoCoins").withStyle(TextFormatting.YELLOW)),
                    player.getUUID()
            );
            return 0;
        }

        // Realizar compra
        economy.removeBalance(plot.getPrice());
        plot.purchaseBy(player.getUUID(), player.getName().getString());
        PlotManager.getInstance().savePlots();

        player.sendMessage(
                new StringTextComponent("Felicidades! Compraste el lote '")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent("' por ").withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        LibunandMod.LOGGER.info("Jugador {} compro el lote '{}' por {} JoJoCoins",
                player.getName().getString(), plot.getName(), plot.getPrice());

        return 1;
    }

    // /plot tool
    private static int giveTool(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        ItemStack goldenAxe = new ItemStack(Items.GOLDEN_AXE);

        if (!player.inventory.add(goldenAxe)) {
            player.drop(goldenAxe, false);
        }

        player.sendMessage(
                new StringTextComponent("Herramienta de seleccion entregada! Usa click izquierdo y derecho para seleccionar area.")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        return 1;
    }

    // /plot help
    private static int showHelp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        player.sendMessage(new StringTextComponent("=== Comandos de Lotes ===").withStyle(TextFormatting.GOLD), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot list - Ver lotes disponibles").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot info [nombre] - Ver informacion de lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot buy <nombre> - Comprar lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot tool - Obtener herramienta de seleccion").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot help - Mostrar esta ayuda").withStyle(TextFormatting.YELLOW), player.getUUID());

        if (player.hasPermissions(2)) {
            player.sendMessage(new StringTextComponent("=== Comandos de Admin ===").withStyle(TextFormatting.RED), player.getUUID());
            player.sendMessage(new StringTextComponent("• /plot create <nombre> <precio> - Crear lote").withStyle(TextFormatting.YELLOW), player.getUUID());
            player.sendMessage(new StringTextComponent("• /plot delete <nombre> - Eliminar lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        }

        return 1;
    }

    /**
     * Muestra información detallada de un lote
     */
    private static void showPlotInfo(ServerPlayerEntity player, Plot plot) {
        player.sendMessage(
                new StringTextComponent("=== Informacion del Lote ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Nombre: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        if (plot.isForSale()) {
            player.sendMessage(
                    new StringTextComponent("Precio: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                    player.getUUID()
            );
            player.sendMessage(
                    new StringTextComponent("Estado: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent("Disponible para compra").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );
        } else {
            player.sendMessage(
                    new StringTextComponent("Propietario: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent(plot.getOwnerName()).withStyle(TextFormatting.WHITE)),
                    player.getUUID()
            );
            player.sendMessage(
                    new StringTextComponent("Estado: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent("Ocupado").withStyle(TextFormatting.RED)),
                    player.getUUID()
            );
        }

        player.sendMessage(
                new StringTextComponent("Area: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(plot.getArea() + " bloques").withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        BlockPos center = plot.getCenter();
        player.sendMessage(
                new StringTextComponent("Centro: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent("(" + center.getX() + ", " + center.getY() + ", " + center.getZ() + ")").withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );
    }
}