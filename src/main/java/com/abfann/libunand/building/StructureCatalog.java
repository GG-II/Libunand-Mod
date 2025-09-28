package com.abfann.libunand.building;

import com.abfann.libunand.LibunandMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;

import java.io.File;
import java.util.*;

public class StructureCatalog {

    private static StructureCatalog instance;
    private final Map<String, StructureInfo> structures = new HashMap<>();
    private final Map<String, List<StructureInfo>> categories = new HashMap<>();
    private File structuresDir;

    private StructureCatalog() {}

    public static StructureCatalog getInstance() {
        if (instance == null) {
            instance = new StructureCatalog();
        }
        return instance;
    }

    /**
     * Inicializa el catálogo con el servidor
     */
    public void initialize(MinecraftServer server) {
        File worldDir = server.getWorldPath(FolderName.ROOT).toFile();
        this.structuresDir = new File(worldDir, "structures");

        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
            LibunandMod.LOGGER.info("Creado directorio de estructuras: {}", structuresDir.getAbsolutePath());
            createExampleStructures();
        }

        loadAllStructures();
        LibunandMod.LOGGER.info("StructureCatalog inicializado. {} estructuras cargadas.", structures.size());
    }

    /**
     * Carga todas las estructuras del directorio
     */
    public void loadAllStructures() {
        structures.clear();
        categories.clear();

        if (!structuresDir.exists()) {
            return;
        }

        scanDirectory(structuresDir, "");

        LibunandMod.LOGGER.info("Cargadas {} estructuras en {} categorias",
                structures.size(), categories.size());
    }

    /**
     * Escanea un directorio recursivamente buscando estructuras
     */
    private void scanDirectory(File dir, String category) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String subCategory = category.isEmpty() ? file.getName() : category + "/" + file.getName();
                scanDirectory(file, subCategory);
            } else if (file.getName().endsWith(".schem") || file.getName().endsWith(".schematic")) {
                loadStructure(file, category);
            }
        }
    }

    /**
     * Carga una estructura específica
     */
    private void loadStructure(File file, String category) {
        try {
            SchematicLoader.StructureData data = SchematicLoader.loadStructure(file);
            if (data == null) {
                LibunandMod.LOGGER.warn("No se pudo cargar estructura: {}", file.getName());
                return;
            }

            String name = file.getName().replaceAll("\\.(schem|schematic)$", "");
            String displayCategory = category.isEmpty() ? "general" : category;
            String description = generateDescription(data);

            StructureInfo structureInfo = new StructureInfo(
                    name, displayCategory, file.getName(),
                    data.blockCounts, data.width, data.height, data.depth,
                    description
            );

            structures.put(name.toLowerCase(), structureInfo);
            categories.computeIfAbsent(displayCategory, k -> new ArrayList<>()).add(structureInfo);

            LibunandMod.LOGGER.debug("Cargada estructura '{}' ({}): {} JoJoCoins, {} bloques",
                    name, displayCategory, structureInfo.getPrice(), structureInfo.getTotalBlocks());

        } catch (Exception e) {
            LibunandMod.LOGGER.error("Error cargando estructura {}: {}", file.getName(), e.getMessage());
        }
    }

    /**
     * Genera una descripción automática basada en los bloques
     */
    private String generateDescription(SchematicLoader.StructureData data) {
        int totalBlocks = data.blockCounts.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueBlocks = data.blockCounts.size();

        String sizeDesc;
        if (totalBlocks < 100) sizeDesc = "Pequeña";
        else if (totalBlocks < 500) sizeDesc = "Mediana";
        else if (totalBlocks < 1000) sizeDesc = "Grande";
        else sizeDesc = "Muy grande";

        return String.format("%s estructura con %d tipos de bloques", sizeDesc, uniqueBlocks);
    }

    /**
     * Crea estructuras de ejemplo si el directorio está vacío
     */
    private void createExampleStructures() {
        try {
            // Crear subdirectorios de ejemplo
            new File(structuresDir, "houses").mkdirs();
            new File(structuresDir, "decorations").mkdirs();
            new File(structuresDir, "utilities").mkdirs();

            // Crear archivo README
            File readme = new File(structuresDir, "README.txt");
            java.nio.file.Files.write(readme.toPath(), Arrays.asList(
                    "=== Directorio de Estructuras de Libunand ===",
                    "",
                    "Coloca tus archivos .schem o .schematic aqui para que aparezcan en el catalogo.",
                    "",
                    "Estructura de directorios:",
                    "- houses/       - Casas y edificios residenciales",
                    "- decorations/  - Elementos decorativos",
                    "- utilities/    - Edificios funcionales",
                    "",
                    "El mod calculara automaticamente los precios basados en los bloques utilizados.",
                    "Usa '/construct reload' para recargar el catalogo tras agregar nuevas estructuras.",
                    "",
                    "Formatos soportados:",
                    "- .schem (WorldEdit moderno)",
                    "- .schematic (MCEdit legacy)",
                    "",
                    "Para mejores resultados, usa estructuras de tamaño razonable (menos de 2000 bloques)."
            ));

        } catch (Exception e) {
            LibunandMod.LOGGER.warn("No se pudo crear archivos de ejemplo: {}", e.getMessage());
        }
    }

    // Métodos de acceso público
    public Optional<StructureInfo> getStructure(String name) {
        return Optional.ofNullable(structures.get(name.toLowerCase()));
    }

    public List<StructureInfo> getAllStructures() {
        return new ArrayList<>(structures.values());
    }

    public List<StructureInfo> getStructuresByCategory(String category) {
        return categories.getOrDefault(category, new ArrayList<>());
    }

    public Set<String> getCategories() {
        return new HashSet<>(categories.keySet());
    }

    public boolean isEmpty() {
        return structures.isEmpty();
    }

    public int getStructureCount() {
        return structures.size();
    }
}