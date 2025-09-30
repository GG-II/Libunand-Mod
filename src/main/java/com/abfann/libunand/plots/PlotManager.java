package com.abfann.libunand.plots;

import com.abfann.libunand.LibunandMod;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona todos los lotes del servidor
 */
public class PlotManager {

    private static final Map<String, Plot> plots = new ConcurrentHashMap<>();
    private static final Map<BlockPos, String> signToPlot = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> playerPlots = new ConcurrentHashMap<>();

    /**
     * Registra un nuevo lote
     */
    public static void registerPlot(Plot plot) {
        plots.put(plot.getName().toLowerCase(), plot);
        signToPlot.put(plot.getSignPos(), plot.getName().toLowerCase());

        LibunandMod.LOGGER.info("Lote registrado: {}", plot);
    }

    /**
     * Obtiene un lote por nombre
     */
    public static Plot getPlot(String name) {
        return plots.get(name.toLowerCase());
    }

    /**
     * Obtiene un lote por posicion del cartel
     */
    public static Plot getPlotBySign(BlockPos signPos) {
        String plotName = signToPlot.get(signPos);
        return plotName != null ? plots.get(plotName) : null;
    }

    /**
     * Obtiene el lote que contiene una posicion
     */
    public static Plot getPlotAt(BlockPos pos) {
        for (Plot plot : plots.values()) {
            if (plot.contains(pos)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Verifica si existe un lote con ese nombre
     */
    public static boolean plotExists(String name) {
        return plots.containsKey(name.toLowerCase());
    }

    /**
     * Verifica si un area se superpone con lotes existentes
     */
    public static boolean hasOverlap(PlotSelectionManager.PlotArea area) {
        for (Plot plot : plots.values()) {
            if (areasOverlap(area, plot.getArea())) {
                return true;
            }
        }
        return false;
    }

    private static boolean areasOverlap(PlotSelectionManager.PlotArea a1, PlotSelectionManager.PlotArea a2) {
        return !(a1.maxX < a2.minX || a1.minX > a2.maxX ||
                a1.maxZ < a2.minZ || a1.minZ > a2.maxZ);
    }

    /**
     * Asigna un lote a un jugador
     */
    public static void assignPlotToPlayer(Plot plot, UUID playerUUID) {
        playerPlots.computeIfAbsent(playerUUID, k -> new HashSet<>())
                .add(plot.getName().toLowerCase());
    }

    /**
     * Obtiene todos los lotes de un jugador
     */
    public static List<Plot> getPlayerPlots(UUID playerUUID) {
        Set<String> plotNames = playerPlots.get(playerUUID);
        if (plotNames == null) return Collections.emptyList();

        List<Plot> result = new ArrayList<>();
        for (String name : plotNames) {
            Plot plot = plots.get(name);
            if (plot != null) {
                result.add(plot);
            }
        }
        return result;
    }

    /**
     * Obtiene todos los lotes disponibles para compra
     */
    public static List<Plot> getAvailablePlots() {
        List<Plot> available = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.isForSale()) {
                available.add(plot);
            }
        }
        return available;
    }

    /**
     * Remueve un lote
     */
    public static void removePlot(String name) {
        Plot plot = plots.remove(name.toLowerCase());
        if (plot != null) {
            signToPlot.remove(plot.getSignPos());
            LibunandMod.LOGGER.info("Lote removido: {}", plot);
        }
    }

    /**
     * Limpia todos los datos
     */
    public static void clear() {
        plots.clear();
        signToPlot.clear();
        playerPlots.clear();
    }
}