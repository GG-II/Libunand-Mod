package com.abfann.libunand.commands;

import com.abfann.libunand.plots.Plot;
import com.abfann.libunand.plots.PlotManager;
import com.abfann.libunand.plots.PlotSelectionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PlotCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("plot")
                        .then(Commands.literal("create")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .then(Commands.argument("nombre", StringArgumentType.word())
                                        .then(Commands.argument("precio", IntegerArgumentType.integer(1))
                                                .executes(PlotCommands::createPlot))))
                        .then(Commands.literal("list")
                                .executes(PlotCommands::listPlots))
                        .then(Commands.literal("info")
                                .executes(PlotCommands::plotInfo))
                        .then(Commands.literal("delete")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("nombre", StringArgumentType.word())
                                        .executes(PlotCommands::deletePlot)))
        );
    }

    // /plot create <nombre> <precio>
    private static int createPlot(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "nombre");
        int price = IntegerArgumentType.getInteger(context, "precio");

        // Verificar que tenga seleccion completa
        if (!PlotSelectionManager.hasCompleteSelection(player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("Debes seleccionar un area primero con la herramienta de seleccion!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // Verificar que no exista
        if (PlotManager.plotExists(name)) {
            player.sendMessage(
                    new StringTextComponent("Ya existe un lote con ese nombre!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // Obtener area
        PlotSelectionManager.PlotArea area = PlotSelectionManager.getPlotArea(player.getUUID());

        // Verificar superposicion
        if (PlotManager.hasOverlap(area)) {
            player.sendMessage(
                    new StringTextComponent("El area se superpone con otro lote existente!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // El cartel se coloca manualmente, por ahora usar posicion del jugador como referencia
        BlockPos signPos = player.blockPosition();

        // Crear lote
        Plot plot = new Plot(name, area, price, signPos);
        PlotManager.registerPlot(plot);

        // Limpiar seleccion
        PlotSelectionManager.clearSelection(player.getUUID());

        player.sendMessage(
                new StringTextComponent("Lote '" + name + "' creado exitosamente!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Coloca un cartel con [PLOT] para que los jugadores puedan comprarlo")
                        .withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        return 1;
    }

    // /plot list
    private static int listPlots(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        java.util.List<Plot> available = PlotManager.getAvailablePlots();

        if (available.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay lotes disponibles para compra")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            return 0;
        }

        player.sendMessage(
                new StringTextComponent("=== Lotes Disponibles ===").withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        for (Plot plot : available) {
            String owner = plot.getOwnerName() != null ? plot.getOwnerName() : "Sin dueno";
            player.sendMessage(
                    new StringTextComponent(String.format("- %s: %d JC (Dueno: %s)",
                            plot.getName(), plot.getResalePrice(), owner))
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
        }

        return 1;
    }

    // /plot info (del lote donde estas parado)
    private static int plotInfo(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Plot plot = PlotManager.getPlotAt(player.blockPosition());

        if (plot == null) {
            player.sendMessage(
                    new StringTextComponent("No estas en un lote")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        player.sendMessage(
                new StringTextComponent("=== Info del Lote ===").withStyle(TextFormatting.GOLD),
                player.getUUID()
        );
        player.sendMessage(
                new StringTextComponent("Nombre: " + plot.getName()).withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );
        player.sendMessage(
                new StringTextComponent("Precio: " + plot.getResalePrice() + " JC").withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        String owner = plot.getOwnerName() != null ? plot.getOwnerName() : "Sin dueno";
        player.sendMessage(
                new StringTextComponent("Dueno: " + owner).withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("En venta: " + (plot.isForSale() ? "Si" : "No")).withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        return 1;
    }

    // /plot delete <nombre>
    private static int deletePlot(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "nombre");

        if (!PlotManager.plotExists(name)) {
            player.sendMessage(
                    new StringTextComponent("No existe un lote con ese nombre")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        PlotManager.removePlot(name);

        player.sendMessage(
                new StringTextComponent("Lote '" + name + "' eliminado exitosamente")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        return 1;
    }
}