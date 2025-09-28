package com.abfann.libunand.commands;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.data.IPlayerEconomy;
import com.abfann.libunand.data.PlayerDataHandler;
import com.abfann.libunand.items.SelectionTool;
import com.abfann.libunand.protection.*;
import com.abfann.libunand.protection.PlotBorderManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

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
                        .then(Commands.literal("trust")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(PlotCommands::trustPlayer)))
                        .then(Commands.literal("untrust")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(PlotCommands::untrustPlayer)))
                        .then(Commands.literal("addowner")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(PlotCommands::addCoOwner)))
                        .then(Commands.literal("removeowner")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(PlotCommands::removeCoOwner)))
                        .then(Commands.literal("members")
                                .executes(PlotCommands::showMembersHere)
                                .then(Commands.argument("plotname", StringArgumentType.word())
                                        .executes(PlotCommands::showMembers)))
                        .then(Commands.literal("updatesigns")
                                .executes(PlotCommands::updateSigns))
                        .then(Commands.literal("refreshsign")
                                .executes(PlotCommands::refreshSignHere))
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .executes(PlotCommands::debugHere))
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

// Procesar fusión automática
        // Crear bordes visuales para el lote comprado
        PlotBorderManager.createPlotBorders(plot, player.level);

// Procesar fusión automática
        boolean wasMerged = PlotMerger.processAutoMerge(plot, player.getUUID(), player.level);

        if (wasMerged) {
            player.sendMessage(
                    new StringTextComponent("Tu lote se fusiono automaticamente con lotes cercanos!")
                            .withStyle(TextFormatting.LIGHT_PURPLE),
                    player.getUUID()
            );

            // Actualizar bordes tras fusión
            PlotBorderManager.updateBordersAfterMerge(plot, player.level);

            // Actualizar carteles tras fusión
            SignEventHandler.updateAllSaleSignsInPlot(plot, player.level);
        } else {
            player.sendMessage(
                    new StringTextComponent("Bordes visuales del lote creados con ladrillos de piedra!")
                            .withStyle(TextFormatting.AQUA),
                    player.getUUID()
            );
        }

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

        // Comandos básicos para todos
        player.sendMessage(new StringTextComponent("--- Comandos Basicos ---").withStyle(TextFormatting.AQUA), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot list - Ver lotes disponibles para compra").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot info [nombre] - Ver informacion detallada de lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot buy <nombre> - Comprar lote con JoJoCoins").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot tool - Obtener herramienta de seleccion").withStyle(TextFormatting.YELLOW), player.getUUID());

        // Comandos de gestión de permisos
        player.sendMessage(new StringTextComponent("--- Gestion de Permisos ---").withStyle(TextFormatting.GREEN), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot trust <jugador> - Dar permisos de construccion").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot untrust <jugador> - Quitar permisos de construccion").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot addowner <jugador> - Agregar co-propietario").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot removeowner <jugador> - Remover co-propietario").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot members [nombre] - Ver miembros del lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        // REEMPLAZA la sección de carteles por:
        player.sendMessage(new StringTextComponent("--- Carteles de Venta ---").withStyle(TextFormatting.LIGHT_PURPLE), player.getUUID());
        player.sendMessage(new StringTextComponent("• Coloca un cartel en un lote en venta (OPs pueden hacerlo)").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• Escribe [VENTA] en la primera linea").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• Click derecho en el cartel para comprar").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot refreshsign - Actualizar cartel cercano").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /plot updatesigns - Actualizar todos los carteles").withStyle(TextFormatting.YELLOW), player.getUUID());

        // Comandos de administrador
        if (player.hasPermissions(2)) {
            player.sendMessage(new StringTextComponent("--- Comandos de Administrador ---").withStyle(TextFormatting.RED), player.getUUID());
            player.sendMessage(new StringTextComponent("• /plot create <nombre> <precio> - Crear nuevo lote").withStyle(TextFormatting.YELLOW), player.getUUID());
            player.sendMessage(new StringTextComponent("• /plot delete <nombre> - Eliminar lote existente").withStyle(TextFormatting.YELLOW), player.getUUID());
        }

        player.sendMessage(new StringTextComponent("--- Informacion ---").withStyle(TextFormatting.LIGHT_PURPLE), player.getUUID());
        player.sendMessage(new StringTextComponent("• Usa el hacha dorada para seleccionar areas").withStyle(TextFormatting.GRAY), player.getUUID());
        player.sendMessage(new StringTextComponent("• Click izquierdo = Posicion 1, Click derecho = Posicion 2").withStyle(TextFormatting.GRAY), player.getUUID());
        player.sendMessage(new StringTextComponent("• Los lotes te protegen contra griefing").withStyle(TextFormatting.GRAY), player.getUUID());

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

            // Mostrar información de miembros
            int totalMembers = plot.getCoOwners().size() + plot.getTrustedPlayers().size();
            if (totalMembers > 0) {
                player.sendMessage(
                        new StringTextComponent("Miembros: ")
                                .withStyle(TextFormatting.AQUA)
                                .append(new StringTextComponent(totalMembers + " jugador(es)").withStyle(TextFormatting.WHITE))
                                .append(new StringTextComponent(" (")
                                        .append(new StringTextComponent(plot.getCoOwners().size() + " co-propietarios").withStyle(TextFormatting.GREEN))
                                        .append(new StringTextComponent(", ").withStyle(TextFormatting.GRAY))
                                        .append(new StringTextComponent(plot.getTrustedPlayers().size() + " confianza").withStyle(TextFormatting.YELLOW))
                                        .append(new StringTextComponent(")").withStyle(TextFormatting.GRAY))),
                        player.getUUID()
                );
            }
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

        // Mostrar permisos del jugador si está en el lote
        if (!plot.isForSale()) {
            PlotRole playerRole = PermissionManager.getPlayerRole(plot, player.getUUID());
            player.sendMessage(
                    new StringTextComponent("Tu rol: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent(playerRole.getDisplayName()).withStyle(
                                    playerRole == PlotRole.OWNER ? TextFormatting.GOLD :
                                            playerRole == PlotRole.CO_OWNER ? TextFormatting.GREEN :
                                                    playerRole == PlotRole.TRUSTED ? TextFormatting.YELLOW :
                                                            TextFormatting.GRAY
                            )),
                    player.getUUID()
            );

            // Mostrar permisos específicos
            if (playerRole != PlotRole.VISITOR) {
                StringBuilder permissions = new StringBuilder();
                for (PlotPermission permission : playerRole.getPermissions()) {
                    if (permissions.length() > 0) permissions.append(", ");
                    permissions.append(permission.getDescription());
                }

                player.sendMessage(
                        new StringTextComponent("Permisos: ")
                                .withStyle(TextFormatting.AQUA)
                                .append(new StringTextComponent(permissions.toString()).withStyle(TextFormatting.WHITE)),
                        player.getUUID()
                );
            }
        }
    }
    // /plot trust <jugador>
    private static int trustPlayer(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String targetPlayerName = StringArgumentType.getString(context, "player");

        // Buscar el lote donde está el jugador
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        // Verificar que el jugador puede administrar este lote
        if (!PermissionManager.canAdministrate(plot, player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("No tienes permisos para administrar este lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // Buscar el UUID del jugador objetivo (simplificado para este ejemplo)
        ServerPlayerEntity targetPlayer = player.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(
                    new StringTextComponent("Jugador '" + targetPlayerName + "' no encontrado o no esta conectado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        UUID targetUUID = targetPlayer.getUUID();

        if (PermissionManager.addTrustedPlayer(plot, player.getUUID(), targetUUID)) {
            PlotManager.getInstance().savePlots();

            player.sendMessage(
                    new StringTextComponent("Jugador ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(targetPlayerName).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" agregado como confianza al lote '").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );

            // Notificar al jugador objetivo si está conectado
            targetPlayer.sendMessage(
                    new StringTextComponent("Has sido agregado como confianza al lote '")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("' por ").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(player.getName().getString()).withStyle(TextFormatting.WHITE)),
                    targetPlayer.getUUID()
            );

            return 1;
        } else {
            player.sendMessage(
                    new StringTextComponent("No se pudo agregar el jugador como confianza. Verifica que no sea ya propietario o co-propietario.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }
    }

    // /plot untrust <jugador>
    private static int untrustPlayer(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String targetPlayerName = StringArgumentType.getString(context, "player");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        if (!PermissionManager.canAdministrate(plot, player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("No tienes permisos para administrar este lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        ServerPlayerEntity targetPlayer = player.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(
                    new StringTextComponent("Jugador '" + targetPlayerName + "' no encontrado o no esta conectado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        UUID targetUUID = targetPlayer.getUUID();

        if (PermissionManager.removeTrustedPlayer(plot, player.getUUID(), targetUUID)) {
            PlotManager.getInstance().savePlots();

            player.sendMessage(
                    new StringTextComponent("Jugador ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(targetPlayerName).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" removido de confianza del lote '").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );

            // Notificar al jugador objetivo
            targetPlayer.sendMessage(
                    new StringTextComponent("Has sido removido de confianza del lote '")
                            .withStyle(TextFormatting.RED)
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.RED)),
                    targetPlayer.getUUID()
            );

            return 1;
        } else {
            player.sendMessage(
                    new StringTextComponent("No se pudo remover el jugador de confianza.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }
    }

    // /plot addowner <jugador>
    private static int addCoOwner(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String targetPlayerName = StringArgumentType.getString(context, "player");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        // Solo el dueño original puede añadir co-dueños
        if (plot.getOwner() == null || !plot.getOwner().equals(player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("Solo el propietario original puede agregar co-propietarios.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        ServerPlayerEntity targetPlayer = player.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(
                    new StringTextComponent("Jugador '" + targetPlayerName + "' no encontrado o no esta conectado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        UUID targetUUID = targetPlayer.getUUID();

        if (PermissionManager.addCoOwner(plot, player.getUUID(), targetUUID)) {
            PlotManager.getInstance().savePlots();

            player.sendMessage(
                    new StringTextComponent("Jugador ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(targetPlayerName).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" agregado como co-propietario del lote '").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );

            targetPlayer.sendMessage(
                    new StringTextComponent("Ahora eres co-propietario del lote '")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'!").withStyle(TextFormatting.GREEN)),
                    targetPlayer.getUUID()
            );

            return 1;
        } else {
            player.sendMessage(
                    new StringTextComponent("No se pudo agregar el co-propietario.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }
    }

    // /plot removeowner <jugador>
    private static int removeCoOwner(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String targetPlayerName = StringArgumentType.getString(context, "player");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        if (plot.getOwner() == null || !plot.getOwner().equals(player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("Solo el propietario original puede remover co-propietarios.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        ServerPlayerEntity targetPlayer = player.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(
                    new StringTextComponent("Jugador '" + targetPlayerName + "' no encontrado o no esta conectado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        UUID targetUUID = targetPlayer.getUUID();

        if (PermissionManager.removeCoOwner(plot, player.getUUID(), targetUUID)) {
            PlotManager.getInstance().savePlots();

            player.sendMessage(
                    new StringTextComponent("Jugador ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(targetPlayerName).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" removido como co-propietario del lote '").withStyle(TextFormatting.GREEN))
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );

            targetPlayer.sendMessage(
                    new StringTextComponent("Ya no eres co-propietario del lote '")
                            .withStyle(TextFormatting.RED)
                            .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent("'.").withStyle(TextFormatting.RED)),
                    targetPlayer.getUUID()
            );

            return 1;
        } else {
            player.sendMessage(
                    new StringTextComponent("No se pudo remover el co-propietario.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }
    }

    // /plot members
    private static int showMembersHere(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        showPlotMembers(player, plotOpt.get());
        return 1;
    }

    // /plot members <nombre>
    private static int showMembers(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String plotName = StringArgumentType.getString(context, "plotname");

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotByName(plotName);
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Lote '" + plotName + "' no encontrado.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        // Solo permitir ver miembros si tiene permisos o es admin
        if (!PermissionManager.canAdministrate(plot, player.getUUID()) && !player.hasPermissions(2)) {
            player.sendMessage(
                    new StringTextComponent("No tienes permisos para ver los miembros de este lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        showPlotMembers(player, plot);
        return 1;
    }

    /**
     * Muestra los miembros de un lote
     */
    private static void showPlotMembers(ServerPlayerEntity player, Plot plot) {
        player.sendMessage(
                new StringTextComponent("=== Miembros del Lote '")
                        .withStyle(TextFormatting.GOLD)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent("' ===").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        // Propietario
        if (plot.getOwner() != null) {
            player.sendMessage(
                    new StringTextComponent("Propietario: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent(plot.getOwnerName()).withStyle(TextFormatting.WHITE)),
                    player.getUUID()
            );
        }

        // Co-propietarios
        if (!plot.getCoOwners().isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("Co-propietarios (" + plot.getCoOwners().size() + "):")
                            .withStyle(TextFormatting.GREEN),
                    player.getUUID()
            );
            // Nota: Para mostrar nombres necesitaríamos un sistema de cache de nombres
            for (UUID coOwner : plot.getCoOwners()) {
                player.sendMessage(
                        new StringTextComponent("• " + coOwner.toString().substring(0, 8) + "...")
                                .withStyle(TextFormatting.WHITE),
                        player.getUUID()
                );
            }
        }

        // Jugadores de confianza
        if (!plot.getTrustedPlayers().isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("Jugadores de confianza (" + plot.getTrustedPlayers().size() + "):")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            for (UUID trusted : plot.getTrustedPlayers()) {
                player.sendMessage(
                        new StringTextComponent("• " + trusted.toString().substring(0, 8) + "...")
                                .withStyle(TextFormatting.WHITE),
                        player.getUUID()
                );
            }
        }

        if (plot.getCoOwners().isEmpty() && plot.getTrustedPlayers().isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay miembros adicionales en este lote.")
                            .withStyle(TextFormatting.GRAY),
                    player.getUUID()
            );
        }
    }

    // /plot updatesigns - Actualizar carteles en lote actual
    private static int updateSigns(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        // Verificar permisos
        if (!PermissionManager.canAdministrate(plot, player.getUUID()) && !player.hasPermissions(2)) {
            player.sendMessage(
                    new StringTextComponent("No tienes permisos para actualizar carteles en este lote.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        SignEventHandler.updateAllSaleSignsInPlot(plot, player.level);

        player.sendMessage(
                new StringTextComponent("Carteles actualizados en el lote '")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                player.getUUID()
        );

        return 1;
    }

    // /plot refreshsign - Actualizar cartel donde estás parado
    private static int refreshSignHere(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();

        // Buscar cartel cerca del jugador
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = player.level.getBlockState(checkPos);

                    if (state.getBlock() instanceof StandingSignBlock || state.getBlock() instanceof WallSignBlock) {
                        TileEntity tileEntity = player.level.getBlockEntity(checkPos);
                        if (tileEntity instanceof SignTileEntity) {
                            SignTileEntity signTile = (SignTileEntity) tileEntity;
                            String firstLine = signTile.getMessage(0).getString().trim();

                            if (firstLine.equalsIgnoreCase("[VENTA]")) {
                                Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, checkPos);
                                if (plotOpt.isPresent()) {
                                    Plot plot = plotOpt.get();
                                    if (plot.isForSale()) {
                                        SignEventHandler.updateSaleSign(signTile, plot, false);

                                        player.sendMessage(
                                                new StringTextComponent("Cartel actualizado con informacion del lote '")
                                                        .withStyle(TextFormatting.GREEN)
                                                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                                                        .append(new StringTextComponent("'.").withStyle(TextFormatting.GREEN)),
                                                player.getUUID()
                                        );
                                        return 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        player.sendMessage(
                new StringTextComponent("No se encontro ningun cartel de venta cerca.")
                        .withStyle(TextFormatting.RED),
                player.getUUID()
        );
        return 0;
    }
    // /plot debug - Información de debug sobre el lote actual
    private static int debugHere(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();

        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, pos);

        player.sendMessage(
                new StringTextComponent("=== Debug Info ===").withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Posicion: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                        .withStyle(TextFormatting.WHITE),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Es OP: " + player.hasPermissions(2))
                        .withStyle(TextFormatting.WHITE),
                player.getUUID()
        );

        if (plotOpt.isPresent()) {
            Plot plot = plotOpt.get();
            player.sendMessage(
                    new StringTextComponent("En lote: " + plot.getName())
                            .withStyle(TextFormatting.GREEN),
                    player.getUUID()
            );
            player.sendMessage(
                    new StringTextComponent("En venta: " + plot.isForSale())
                            .withStyle(TextFormatting.WHITE),
                    player.getUUID()
            );
            player.sendMessage(
                    new StringTextComponent("Puede construir: " + PermissionManager.canBuild(plot, player.getUUID()))
                            .withStyle(TextFormatting.WHITE),
                    player.getUUID()
            );
        } else {
            player.sendMessage(
                    new StringTextComponent("No estas en ningun lote")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
        }

        return 1;
    }
}