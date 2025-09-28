package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlotManager {

    private static PlotManager instance;
    private final List<Plot> plots;
    private final Gson gson;
    private File plotsFile;

    private PlotManager() {
        this.plots = new ArrayList<>();
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    public static PlotManager getInstance() {
        if (instance == null) {
            instance = new PlotManager();
        }
        return instance;
    }

    /**
     * Inicializa el PlotManager con el servidor
     */
    public void initialize(MinecraftServer server) {
        File worldDir = server.getWorldPath(FolderName.ROOT).toFile();
        File libunandDir = new File(worldDir, "data/libunand");

        if (!libunandDir.exists()) {
            libunandDir.mkdirs();
        }

        this.plotsFile = new File(libunandDir, "plots.json");
        loadPlots();

        LibunandMod.LOGGER.info("PlotManager inicializado. {} lotes cargados.", plots.size());
    }

    /**
     * Carga los lotes desde el archivo JSON
     */
    private void loadPlots() {
        if (!plotsFile.exists()) {
            LibunandMod.LOGGER.info("Archivo plots.json no existe, creando nuevo.");
            savePlots();
            return;
        }

        try (FileReader reader = new FileReader(plotsFile)) {
            Type listType = new TypeToken<List<Plot>>(){}.getType();
            List<Plot> loadedPlots = gson.fromJson(reader, listType);

            if (loadedPlots != null) {
                plots.clear();
                plots.addAll(loadedPlots);
                LibunandMod.LOGGER.info("Cargados {} lotes desde plots.json", plots.size());
            }

        } catch (IOException e) {
            LibunandMod.LOGGER.error("Error cargando plots.json: {}", e.getMessage());
        }
    }

    /**
     * Guarda los lotes en el archivo JSON
     */
    public void savePlots() {
        try (FileWriter writer = new FileWriter(plotsFile)) {
            gson.toJson(plots, writer);
            LibunandMod.LOGGER.debug("Lotes guardados en plots.json");
        } catch (IOException e) {
            LibunandMod.LOGGER.error("Error guardando plots.json: {}", e.getMessage());
        }
    }

    /**
     * Crea un nuevo lote
     */
    public boolean createPlot(String name, World world, BlockPos corner1, BlockPos corner2, int price) {
        // Verificar que no exista un lote con ese nombre
        if (getPlotByName(name).isPresent()) {
            return false;
        }

        // Verificar que no haya solapamiento con otros lotes
        if (hasOverlap(world, corner1, corner2)) {
            return false;
        }

        Plot newPlot = new Plot(name, world, corner1, corner2, price);
        plots.add(newPlot);
        savePlots();

        LibunandMod.LOGGER.info("Nuevo lote creado: {}", newPlot);
        return true;
    }

    /**
     * Verifica si hay solapamiento con otros lotes
     */
    private boolean hasOverlap(World world, BlockPos corner1, BlockPos corner2) {
        UUID worldUUID = UUID.nameUUIDFromBytes(world.dimension().location().toString().getBytes());

        for (Plot plot : plots) {
            if (!plot.getWorldUUID().equals(worldUUID)) continue;

            // Verificar solapamiento
            int minX1 = Math.min(corner1.getX(), corner2.getX());
            int maxX1 = Math.max(corner1.getX(), corner2.getX());
            int minZ1 = Math.min(corner1.getZ(), corner2.getZ());
            int maxZ1 = Math.max(corner1.getZ(), corner2.getZ());

            int minX2 = Math.min(plot.getCorner1().getX(), plot.getCorner2().getX());
            int maxX2 = Math.max(plot.getCorner1().getX(), plot.getCorner2().getX());
            int minZ2 = Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ());
            int maxZ2 = Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ());

            if (!(maxX1 < minX2 || minX1 > maxX2 || maxZ1 < minZ2 || minZ1 > maxZ2)) {
                return true; // Hay solapamiento
            }
        }

        return false;
    }

    /**
     * Busca un lote por nombre
     */
    public Optional<Plot> getPlotByName(String name) {
        return plots.stream()
                .filter(plot -> plot.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Busca el lote que contiene una posición específica
     */
    public Optional<Plot> getPlotAt(World world, BlockPos pos) {
        UUID worldUUID = UUID.nameUUIDFromBytes(world.dimension().location().toString().getBytes());

        return plots.stream()
                .filter(plot -> plot.getWorldUUID().equals(worldUUID))
                .filter(plot -> plot.contains(pos))
                .findFirst();
    }

    /**
     * Obtiene todos los lotes disponibles para compra
     */
    public List<Plot> getAvailablePlots() {
        return plots.stream()
                .filter(Plot::isForSale)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Obtiene todos los lotes de un jugador
     */
    public List<Plot> getPlayerPlots(UUID playerUUID) {
        return plots.stream()
                .filter(plot -> plot.getOwner() != null && plot.getOwner().equals(playerUUID))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Elimina un lote
     */
    public boolean deletePlot(String name) {
        Optional<Plot> plotOpt = getPlotByName(name);
        if (plotOpt.isPresent()) {
            plots.remove(plotOpt.get());
            savePlots();
            LibunandMod.LOGGER.info("Lote eliminado: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Obtiene todos los lotes
     */
    public List<Plot> getAllPlots() {
        return new ArrayList<>(plots);
    }
}