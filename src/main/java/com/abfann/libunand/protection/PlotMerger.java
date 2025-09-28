package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.config.ConfigManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotMerger {

    /**
     * Busca lotes cercanos que pertenezcan al mismo jugador para fusionar
     */
    public static List<Plot> findNearbyPlotsForMerging(Plot newPlot, UUID ownerUUID) {
        List<Plot> nearbyPlots = new ArrayList<>();
        List<Plot> allPlots = PlotManager.getInstance().getAllPlots();

        for (Plot existingPlot : allPlots) {
            if (existingPlot.equals(newPlot)) continue;
            if (existingPlot.isForSale()) continue; // No fusionar lotes en venta
            if (existingPlot.getOwner() == null || !existingPlot.getOwner().equals(ownerUUID)) continue;

            // Verificar si están en el mismo mundo
            if (!existingPlot.getWorldUUID().equals(newPlot.getWorldUUID())) continue;

            // Verificar distancia
            if (isWithinMergingDistance(newPlot, existingPlot)) {
                nearbyPlots.add(existingPlot);
            }
        }

        return nearbyPlots;
    }

    /**
     * Verifica si dos lotes están dentro de la distancia de fusión
     */
    private static boolean isWithinMergingDistance(Plot plot1, Plot plot2) {
        int mergeDistance = ConfigManager.PLOT_MERGE_DISTANCE.get();

        // Obtener los bordes de ambos lotes
        int minX1 = Math.min(plot1.getCorner1().getX(), plot1.getCorner2().getX());
        int maxX1 = Math.max(plot1.getCorner1().getX(), plot1.getCorner2().getX());
        int minZ1 = Math.min(plot1.getCorner1().getZ(), plot1.getCorner2().getZ());
        int maxZ1 = Math.max(plot1.getCorner1().getZ(), plot1.getCorner2().getZ());

        int minX2 = Math.min(plot2.getCorner1().getX(), plot2.getCorner2().getX());
        int maxX2 = Math.max(plot2.getCorner1().getX(), plot2.getCorner2().getX());
        int minZ2 = Math.min(plot2.getCorner1().getZ(), plot2.getCorner2().getZ());
        int maxZ2 = Math.max(plot2.getCorner1().getZ(), plot2.getCorner2().getZ());

        // Calcular distancia mínima entre bordes
        int distanceX = 0;
        if (maxX1 < minX2) {
            distanceX = minX2 - maxX1;
        } else if (maxX2 < minX1) {
            distanceX = minX1 - maxX2;
        }

        int distanceZ = 0;
        if (maxZ1 < minZ2) {
            distanceZ = minZ2 - maxZ1;
        } else if (maxZ2 < minZ1) {
            distanceZ = minZ1 - maxZ2;
        }

        // Distancia euclidiana entre bordes
        double distance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);

        return distance <= mergeDistance;
    }

    /**
     * Fusiona múltiples lotes en uno solo
     */
    public static Plot mergePlots(Plot mainPlot, List<Plot> plotsToMerge, World world) {
        if (plotsToMerge.isEmpty()) {
            return mainPlot;
        }

        LibunandMod.LOGGER.info("Fusionando {} lotes con el lote principal '{}'",
                plotsToMerge.size(), mainPlot.getName());

        // Calcular el área combinada (rectángulo mínimo que contenga todos)
        int minX = Math.min(mainPlot.getCorner1().getX(), mainPlot.getCorner2().getX());
        int maxX = Math.max(mainPlot.getCorner1().getX(), mainPlot.getCorner2().getX());
        int minZ = Math.min(mainPlot.getCorner1().getZ(), mainPlot.getCorner2().getZ());
        int maxZ = Math.max(mainPlot.getCorner1().getZ(), mainPlot.getCorner2().getZ());
        int minY = Math.min(mainPlot.getCorner1().getY(), mainPlot.getCorner2().getY());
        int maxY = Math.max(mainPlot.getCorner1().getY(), mainPlot.getCorner2().getY());

        for (Plot plot : plotsToMerge) {
            minX = Math.min(minX, Math.min(plot.getCorner1().getX(), plot.getCorner2().getX()));
            maxX = Math.max(maxX, Math.max(plot.getCorner1().getX(), plot.getCorner2().getX()));
            minZ = Math.min(minZ, Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ()));
            maxZ = Math.max(maxZ, Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ()));
            minY = Math.min(minY, Math.min(plot.getCorner1().getY(), plot.getCorner2().getY()));
            maxY = Math.max(maxY, Math.max(plot.getCorner1().getY(), plot.getCorner2().getY()));
        }

        // Crear el nuevo lote fusionado
        BlockPos newCorner1 = new BlockPos(minX, minY, minZ);
        BlockPos newCorner2 = new BlockPos(maxX, maxY, maxZ);

        // Actualizar el lote principal
        mainPlot.setCorner1(newCorner1);
        mainPlot.setCorner2(newCorner2);

        // Fusionar permisos de todos los lotes
        for (Plot plot : plotsToMerge) {
            // Añadir trusted players
            for (UUID trustedPlayer : plot.getTrustedPlayers()) {
                mainPlot.addTrustedPlayer(trustedPlayer);
            }

            // Añadir co-owners
            for (UUID coOwner : plot.getCoOwners()) {
                mainPlot.addCoOwner(coOwner);
            }
        }

        // Eliminar los lotes fusionados
        for (Plot plot : plotsToMerge) {
            PlotManager.getInstance().deletePlot(plot.getName());
        }

        // Actualizar nombre del lote fusionado
        String oldName = mainPlot.getName();
        String newName = oldName + "_fusionado";
        mainPlot.setName(newName);

        LibunandMod.LOGGER.info("Lote '{}' fusionado exitosamente. Nueva area: {} bloques",
                newName, mainPlot.getArea());

        return mainPlot;
    }

    /**
     * Procesa la fusión automática tras comprar un lote
     */
    public static boolean processAutoMerge(Plot purchasedPlot, UUID buyerUUID, World world) {
        List<Plot> nearbyPlots = findNearbyPlotsForMerging(purchasedPlot, buyerUUID);

        if (!nearbyPlots.isEmpty()) {
            Plot mergedPlot = mergePlots(purchasedPlot, nearbyPlots, world);
            PlotManager.getInstance().savePlots();

            LibunandMod.LOGGER.info("Auto-fusion completada para jugador {} en lote '{}'",
                    buyerUUID, mergedPlot.getName());
            return true;
        }

        return false;
    }

    /**
     * Calcula el área total que cubriría la fusión
     */
    public static int calculateMergedArea(Plot mainPlot, List<Plot> plotsToMerge) {
        if (plotsToMerge.isEmpty()) {
            return mainPlot.getArea();
        }

        int minX = Math.min(mainPlot.getCorner1().getX(), mainPlot.getCorner2().getX());
        int maxX = Math.max(mainPlot.getCorner1().getX(), mainPlot.getCorner2().getX());
        int minZ = Math.min(mainPlot.getCorner1().getZ(), mainPlot.getCorner2().getZ());
        int maxZ = Math.max(mainPlot.getCorner1().getZ(), mainPlot.getCorner2().getZ());

        for (Plot plot : plotsToMerge) {
            minX = Math.min(minX, Math.min(plot.getCorner1().getX(), plot.getCorner2().getX()));
            maxX = Math.max(maxX, Math.max(plot.getCorner1().getX(), plot.getCorner2().getX()));
            minZ = Math.min(minZ, Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ()));
            maxZ = Math.max(maxZ, Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ()));
        }

        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;
        return width * length;
    }
}