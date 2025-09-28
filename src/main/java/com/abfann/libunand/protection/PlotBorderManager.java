package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PlotBorderManager {

    /**
     * Crea bordes visuales para un lote usando ladrillos de piedra
     */
    public static void createPlotBorders(Plot plot, World world) {
        if (plot == null || world == null) return;

        int minX = Math.min(plot.getCorner1().getX(), plot.getCorner2().getX());
        int maxX = Math.max(plot.getCorner1().getX(), plot.getCorner2().getX());
        int minZ = Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ());
        int maxZ = Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ());

        List<BlockPos> borderPositions = new ArrayList<>();

        // Borde norte (minZ)
        for (int x = minX; x <= maxX; x++) {
            borderPositions.add(new BlockPos(x, 0, minZ));
        }

        // Borde sur (maxZ)
        for (int x = minX; x <= maxX; x++) {
            borderPositions.add(new BlockPos(x, 0, maxZ));
        }

        // Borde oeste (minX) - evitar esquinas duplicadas
        for (int z = minZ + 1; z < maxZ; z++) {
            borderPositions.add(new BlockPos(minX, 0, z));
        }

        // Borde este (maxX) - evitar esquinas duplicadas
        for (int z = minZ + 1; z < maxZ; z++) {
            borderPositions.add(new BlockPos(maxX, 0, z));
        }

        // Colocar ladrillos de piedra en las posiciones del borde
        int blocksPlaced = 0;
        for (BlockPos borderPos : borderPositions) {
            BlockPos surfacePos = findSurfacePosition(world, borderPos);
            if (surfacePos != null) {
                BlockState currentState = world.getBlockState(surfacePos);

                // Solo reemplazar si es aire o bloque reemplazable
                if (currentState.getBlock() instanceof AirBlock ||
                        currentState.getMaterial().isReplaceable()) {

                    world.setBlock(surfacePos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    blocksPlaced++;
                }
            }
        }

        LibunandMod.LOGGER.debug("Creados bordes visuales para lote '{}': {} bloques colocados",
                plot.getName(), blocksPlaced);
    }

    /**
     * Encuentra la posición de superficie más alta no-aire
     */
    private static BlockPos findSurfacePosition(World world, BlockPos basePos) {
        // En 1.16.5 usar límites fijos: Y=0 a Y=255
        for (int y = 255; y >= 0; y--) {
            BlockPos checkPos = new BlockPos(basePos.getX(), y, basePos.getZ());
            BlockState state = world.getBlockState(checkPos);

            if (!(state.getBlock() instanceof AirBlock)) {
                // Encontramos un bloque sólido, colocar el borde encima
                BlockPos abovePos = checkPos.above();
                if (world.getBlockState(abovePos).getBlock() instanceof AirBlock) {
                    return abovePos;
                }
                // Si no hay espacio encima, usar la posición del bloque sólido
                return checkPos;
            }
        }

        // Si no encontramos nada, usar y=64 (nivel del mar)
        return new BlockPos(basePos.getX(), 64, basePos.getZ());
    }

    /**
     * Remueve bordes visuales de un lote
     */
    public static void removePlotBorders(Plot plot, World world) {
        if (plot == null || world == null) return;

        int minX = Math.min(plot.getCorner1().getX(), plot.getCorner2().getX());
        int maxX = Math.max(plot.getCorner1().getX(), plot.getCorner2().getX());
        int minZ = Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ());
        int maxZ = Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ());

        List<BlockPos> borderPositions = new ArrayList<>();

        // Recopilar todas las posiciones del borde
        for (int x = minX; x <= maxX; x++) {
            borderPositions.add(new BlockPos(x, 0, minZ));
            borderPositions.add(new BlockPos(x, 0, maxZ));
        }

        for (int z = minZ + 1; z < maxZ; z++) {
            borderPositions.add(new BlockPos(minX, 0, z));
            borderPositions.add(new BlockPos(maxX, 0, z));
        }

        // Remover ladrillos de piedra
        int blocksRemoved = 0;
        for (BlockPos borderPos : borderPositions) {
            // Buscar en un rango vertical pequeño
            for (int y = borderPos.getY(); y <= borderPos.getY() + 10; y++) {
                BlockPos checkPos = new BlockPos(borderPos.getX(), y, borderPos.getZ());
                BlockState state = world.getBlockState(checkPos);

                if (state.getBlock() == Blocks.STONE_BRICKS) {
                    world.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                    blocksRemoved++;
                    break; // Solo remover uno por columna
                }
            }
        }

        LibunandMod.LOGGER.debug("Removidos bordes visuales del lote '{}': {} bloques",
                plot.getName(), blocksRemoved);
    }

    /**
     * Actualiza bordes tras fusión de lotes
     */
    public static void updateBordersAfterMerge(Plot mergedPlot, World world) {
        // Crear nuevos bordes para el lote fusionado
        createPlotBorders(mergedPlot, world);

        LibunandMod.LOGGER.info("Bordes actualizados tras fusión del lote '{}'", mergedPlot.getName());
    }

    /**
     * Crea bordes para todos los lotes existentes
     */
    public static void createAllPlotBorders(World world) {
        List<Plot> allPlots = PlotManager.getInstance().getAllPlots();
        int plotsProcessed = 0;

        for (Plot plot : allPlots) {
            if (!plot.isForSale()) { // Solo lotes ocupados
                createPlotBorders(plot, world);
                plotsProcessed++;
            }
        }

        LibunandMod.LOGGER.info("Creados bordes para {} lotes", plotsProcessed);
    }
}