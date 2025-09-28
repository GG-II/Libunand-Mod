package com.abfann.libunand.commands;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.building.BlockPriceCalculator;
import com.abfann.libunand.building.StructureCatalog;
import com.abfann.libunand.building.StructureInfo;
import com.abfann.libunand.data.IPlayerEconomy;
import com.abfann.libunand.data.PlayerDataHandler;
import com.abfann.libunand.protection.PermissionManager;
import com.abfann.libunand.protection.Plot;
import com.abfann.libunand.protection.PlotManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConstructCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("construct")
                        .then(Commands.literal("list")
                                .executes(ConstructCommands::listStructures)
                                .then(Commands.argument("category", StringArgumentType.word())
                                        .executes(ConstructCommands::listByCategory)))
                        .then(Commands.literal("info")
                                .then(Commands.argument("structure", StringArgumentType.word())
                                        .executes(ConstructCommands::structureInfo)))
                        .then(Commands.literal("build")
                                .then(Commands.argument("structure", StringArgumentType.word())
                                        .executes(ConstructCommands::buildStructure)))
                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2)) // Solo OPs
                                .executes(ConstructCommands::reloadCatalog))
                        .then(Commands.literal("categories")
                                .executes(ConstructCommands::listCategories))
                        .then(Commands.literal("help")
                                .executes(ConstructCommands::showHelp))
        );
    }

    // /construct list [categoria]
    private static int listStructures(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        if (StructureCatalog.getInstance().isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay estructuras disponibles. Los administradores deben colocar archivos .schem en el directorio structures/")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            return 0;
        }

        List<StructureInfo> structures = StructureCatalog.getInstance().getAllStructures();

        player.sendMessage(
                new StringTextComponent("=== Catalogo de Estructuras ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        // Agrupar por categoría
        Set<String> categories = StructureCatalog.getInstance().getCategories();
        for (String category : categories) {
            List<StructureInfo> categoryStructures = StructureCatalog.getInstance().getStructuresByCategory(category);

            player.sendMessage(
                    new StringTextComponent("--- " + category.toUpperCase() + " ---")
                            .withStyle(TextFormatting.AQUA),
                    player.getUUID()
            );

            for (StructureInfo structure : categoryStructures) {
                player.sendMessage(
                        new StringTextComponent("• ")
                                .withStyle(TextFormatting.YELLOW)
                                .append(new StringTextComponent(structure.getName()).withStyle(TextFormatting.WHITE))
                                .append(new StringTextComponent(" - ").withStyle(TextFormatting.GRAY))
                                .append(new StringTextComponent(structure.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD))
                                .append(new StringTextComponent(" (" + structure.getSizeInfo() + ")").withStyle(TextFormatting.GRAY)),
                        player.getUUID()
                );
            }
        }

        player.sendMessage(
                new StringTextComponent("Usa '/construct info <estructura>' para ver detalles")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );

        return 1;
    }

    // /construct list <categoria>
    private static int listByCategory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String category = StringArgumentType.getString(context, "category");

        List<StructureInfo> structures = StructureCatalog.getInstance().getStructuresByCategory(category);

        if (structures.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay estructuras en la categoria '" + category + "'")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        player.sendMessage(
                new StringTextComponent("=== Estructuras - " + category.toUpperCase() + " ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        for (StructureInfo structure : structures) {
            player.sendMessage(
                    new StringTextComponent("• ")
                            .withStyle(TextFormatting.YELLOW)
                            .append(new StringTextComponent(structure.getName()).withStyle(TextFormatting.WHITE))
                            .append(new StringTextComponent(" - ").withStyle(TextFormatting.GRAY))
                            .append(new StringTextComponent(structure.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD))
                            .append(new StringTextComponent(" (" + structure.getSizeInfo() + ")").withStyle(TextFormatting.GRAY)),
                    player.getUUID()
            );
        }

        return 1;
    }

    // /construct categories
    private static int listCategories(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Set<String> categories = StructureCatalog.getInstance().getCategories();

        if (categories.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("No hay categorias disponibles")
                            .withStyle(TextFormatting.YELLOW),
                    player.getUUID()
            );
            return 0;
        }

        player.sendMessage(
                new StringTextComponent("=== Categorias Disponibles ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        for (String category : categories) {
            int count = StructureCatalog.getInstance().getStructuresByCategory(category).size();
            player.sendMessage(
                    new StringTextComponent("• ")
                            .withStyle(TextFormatting.YELLOW)
                            .append(new StringTextComponent(category).withStyle(TextFormatting.WHITE))
                            .append(new StringTextComponent(" (" + count + " estructuras)").withStyle(TextFormatting.GRAY)),
                    player.getUUID()
            );
        }

        return 1;
    }

    // /construct info <estructura>
    private static int structureInfo(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String structureName = StringArgumentType.getString(context, "structure");

        Optional<StructureInfo> structureOpt = StructureCatalog.getInstance().getStructure(structureName);
        if (!structureOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Estructura '" + structureName + "' no encontrada")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        StructureInfo structure = structureOpt.get();

        player.sendMessage(
                new StringTextComponent("=== Informacion de Estructura ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Nombre: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(structure.getName()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Categoria: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(structure.getCategory()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Precio: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(structure.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Dimensiones: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(structure.getSizeInfo()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Descripcion: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(structure.getDescription()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        // Mostrar desglose de precio
        String breakdown = BlockPriceCalculator.getPriceBreakdown(structure.getBlockCounts());
        String[] lines = breakdown.split("\n");

        player.sendMessage(
                new StringTextComponent("=== Desglose de Precio ===")
                        .withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        for (String line : lines) {
            player.sendMessage(
                    new StringTextComponent(line).withStyle(TextFormatting.WHITE),
                    player.getUUID()
            );
        }

        // Verificar si el jugador puede permitírsela
        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);
        if (economy.hasBalance(structure.getPrice())) {
            player.sendMessage(
                    new StringTextComponent("Puedes permitirte esta estructura!")
                            .withStyle(TextFormatting.GREEN),
                    player.getUUID()
            );
        } else {
            int needed = structure.getPrice() - economy.getBalance();
            player.sendMessage(
                    new StringTextComponent("Necesitas " + needed + " JoJoCoins mas para esta estructura")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
        }

        return 1;
    }

    // /construct build <estructura>
    private static int buildStructure(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String structureName = StringArgumentType.getString(context, "structure");

        // Verificar que la estructura existe
        Optional<StructureInfo> structureOpt = StructureCatalog.getInstance().getStructure(structureName);
        if (!structureOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Estructura '" + structureName + "' no encontrada")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        StructureInfo structure = structureOpt.get();

        // Verificar que está en un lote
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(player.level, player.blockPosition());
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Debes estar dentro de un lote para construir estructuras")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        Plot plot = plotOpt.get();

        // Verificar permisos en el lote
        if (!PermissionManager.canBuild(plot, player.getUUID())) {
            player.sendMessage(
                    new StringTextComponent("No tienes permisos para construir en este lote")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return 0;
        }

        // Verificar que el jugador tenga suficientes JoJoCoins
        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);
        if (!economy.hasBalance(structure.getPrice())) {
            int needed = structure.getPrice() - economy.getBalance();
            player.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Necesitas ")
                            .withStyle(TextFormatting.RED)
                            .append(new StringTextComponent(needed + " JoJoCoins mas").withStyle(TextFormatting.GOLD)),
                    player.getUUID()
            );
            return 0;
        }

        // TODO: Verificar que hay espacio suficiente en el lote
        // TODO: Implementar construcción real

        // Por ahora, solo simulamos la compra
        economy.removeBalance(structure.getPrice());

        player.sendMessage(
                new StringTextComponent("Construccion iniciada! Compraste '")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(structure.getName()).withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent("' por ").withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(structure.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("NOTA: La construccion automatica sera implementada pronto. Por ahora solo se dedujo el costo.")
                        .withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );

        LibunandMod.LOGGER.info("Jugador {} 'compro' estructura '{}' por {} JoJoCoins",
                player.getName().getString(), structure.getName(), structure.getPrice());

        return 1;
    }

    // /construct reload
    private static int reloadCatalog(CommandContext<CommandSource> context) throws CommandSyntaxException {
        StructureCatalog.getInstance().loadAllStructures();

        context.getSource().sendSuccess(
                new StringTextComponent("Catalogo de estructuras recargado. ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(StructureCatalog.getInstance().getStructureCount() + " estructuras cargadas")
                                .withStyle(TextFormatting.WHITE)),
                true
        );

        return 1;
    }

    // /construct help
    private static int showHelp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        player.sendMessage(new StringTextComponent("=== Comandos de Construccion ===").withStyle(TextFormatting.GOLD), player.getUUID());
        player.sendMessage(new StringTextComponent("• /construct list [categoria] - Ver estructuras disponibles").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /construct categories - Ver todas las categorias").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /construct info <estructura> - Ver detalles y precio").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /construct build <estructura> - Construir en tu lote").withStyle(TextFormatting.YELLOW), player.getUUID());
        player.sendMessage(new StringTextComponent("• /construct help - Mostrar esta ayuda").withStyle(TextFormatting.YELLOW), player.getUUID());

        if (player.hasPermissions(2)) {
            player.sendMessage(new StringTextComponent("--- Comandos de Admin ---").withStyle(TextFormatting.RED), player.getUUID());
            player.sendMessage(new StringTextComponent("• /construct reload - Recargar catalogo de estructuras").withStyle(TextFormatting.YELLOW), player.getUUID());
        }

        player.sendMessage(new StringTextComponent("--- Informacion ---").withStyle(TextFormatting.LIGHT_PURPLE), player.getUUID());
        player.sendMessage(new StringTextComponent("• Debes estar en un lote donde tengas permisos de construccion").withStyle(TextFormatting.GRAY), player.getUUID());
        player.sendMessage(new StringTextComponent("• Los precios se calculan automaticamente segun los bloques").withStyle(TextFormatting.GRAY), player.getUUID());
        player.sendMessage(new StringTextComponent("• Los admins pueden agregar archivos .schem en structures/").withStyle(TextFormatting.GRAY), player.getUUID());

        return 1;
    }
}