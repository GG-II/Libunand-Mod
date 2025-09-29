package com.abfann.libunand.building;

import com.abfann.libunand.LibunandMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureBuilder {

    /**
     * Construye una estructura completa de forma instantánea
     */
    public static boolean buildStructureInstant(World world, BlockPos startPos, String structureName, ServerPlayerEntity player) {
        try {
            // Cargar datos de la estructura
            StructureData data = loadStructureData(structureName);
            if (data == null) {
                player.sendMessage(
                        new StringTextComponent("Error: No se pudo cargar la estructura")
                                .withStyle(TextFormatting.RED),
                        player.getUUID()
                );
                return false;
            }

            // Construir instantáneamente
            int blocksPlaced = 0;
            for (StructureBlock block : data.blocks) {
                BlockPos pos = startPos.offset(block.x, block.y, block.z);

                if (block.blockState != null) {
                    world.setBlock(pos, block.blockState, 3);
                    blocksPlaced++;
                }
            }

            player.sendMessage(
                    new StringTextComponent("Estructura construida! ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent(blocksPlaced + " bloques colocados")
                                    .withStyle(TextFormatting.WHITE)),
                    player.getUUID()
            );

            LibunandMod.LOGGER.info("Estructura '{}' construida en {} por {}",
                    structureName, startPos, player.getName().getString());

            return true;

        } catch (Exception e) {
            LibunandMod.LOGGER.error("Error construyendo estructura {}: {}", structureName, e.getMessage());
            player.sendMessage(
                    new StringTextComponent("Error construyendo la estructura: " + e.getMessage())
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return false;
        }
    }

    /**
     * Construye una estructura de forma progresiva
     */
    public static void buildStructureProgressive(World world, BlockPos startPos, String structureName,
                                                 ServerPlayerEntity player, int blocksPerTick) {
        try {
            StructureData data = loadStructureData(structureName);
            if (data == null) {
                player.sendMessage(
                        new StringTextComponent("Error: No se pudo cargar la estructura")
                                .withStyle(TextFormatting.RED),
                        player.getUUID()
                );
                return;
            }

            // Crear tarea de construcción progresiva
            ProgressiveBuilder builder = new ProgressiveBuilder(world, startPos, data, player, blocksPerTick);
            builder.start();

        } catch (Exception e) {
            LibunandMod.LOGGER.error("Error iniciando construcción progresiva {}: {}", structureName, e.getMessage());
        }
    }

    /**
     * Carga los datos completos de una estructura incluyendo posiciones de bloques
     */
    private static StructureData loadStructureData(String structureName) {
        try {
            // Encontrar el archivo
            File structuresDir = StructureCatalog.getInstance().getStructuresDirectory();
            File structureFile = findStructureFile(structuresDir, structureName);

            if (structureFile == null) {
                LibunandMod.LOGGER.warn("Archivo de estructura no encontrado: {}", structureName);
                return null;
            }

            if (structureFile.getName().endsWith(".schem")) {
                return loadSchemData(structureFile);
            } else if (structureFile.getName().endsWith(".schematic")) {
                return loadSchematicData(structureFile);
            }

        } catch (Exception e) {
            LibunandMod.LOGGER.error("Error cargando datos de estructura {}: {}", structureName, e.getMessage());
        }

        return null;
    }

    /**
     * Busca recursivamente un archivo de estructura
     */
    private static File findStructureFile(File dir, String structureName) {
        LibunandMod.LOGGER.info("Buscando estructura '{}' en directorio: {}", structureName, dir.getAbsolutePath());

        if (!dir.exists() || !dir.isDirectory()) {
            LibunandMod.LOGGER.warn("Directorio no existe: {}", dir.getAbsolutePath());
            return null;
        }

        File[] files = dir.listFiles();
        if (files == null) return null;

        for (File file : files) {
            LibunandMod.LOGGER.debug("Revisando archivo: {}", file.getName());

            if (file.isDirectory()) {
                File found = findStructureFile(file, structureName);
                if (found != null) return found;
            } else if (file.getName().endsWith(".schem") || file.getName().endsWith(".schematic")) {
                String fileName = file.getName().replaceAll("\\.(schem|schematic)$", "");
                LibunandMod.LOGGER.debug("Comparando '{}' con '{}'", fileName, structureName);

                if (fileName.equalsIgnoreCase(structureName)) {
                    LibunandMod.LOGGER.info("Estructura encontrada: {}", file.getAbsolutePath());
                    return file;
                }
            }
        }

        LibunandMod.LOGGER.warn("Estructura '{}' no encontrada en {}", structureName, dir.getAbsolutePath());
        return null;
    }

    /**
     * Carga datos de archivo .schem (WorldEdit)
     */
    private static StructureData loadSchemData(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(fis);

            int width = nbt.getShort("Width");
            int height = nbt.getShort("Height");
            int depth = nbt.getShort("Length");

            // Leer paleta
            CompoundNBT palette = nbt.getCompound("Palette");
            Map<Integer, BlockState> blockPalette = new HashMap<>();

            for (String blockName : palette.getAllKeys()) {
                int id = palette.getInt(blockName);
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                if (block != null) {
                    blockPalette.put(id, block.defaultBlockState());
                }
            }

            // Leer datos de bloques
            byte[] blockData = nbt.getByteArray("BlockData");
            int[] blockIds = decodeVarIntArray(blockData);

            List<StructureBlock> blocks = new ArrayList<>();
            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    for (int x = 0; x < width; x++) {
                        if (index < blockIds.length) {
                            BlockState state = blockPalette.get(blockIds[index]);
                            if (state != null && state.getBlock() != Blocks.AIR) {
                                blocks.add(new StructureBlock(x, y, z, state));
                            }
                        }
                        index++;
                    }
                }
            }

            return new StructureData(width, height, depth, blocks);
        }
    }

    /**
     * Carga datos de archivo .schematic (MCEdit)
     */
    private static StructureData loadSchematicData(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(fis);

            int width = nbt.getShort("Width");
            int height = nbt.getShort("Height");
            int depth = nbt.getShort("Length");

            byte[] blocks = nbt.getByteArray("Blocks");
            byte[] blockData = nbt.getByteArray("Data");

            List<StructureBlock> structureBlocks = new ArrayList<>();
            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    for (int x = 0; x < width; x++) {
                        if (index < blocks.length) {
                            int blockId = blocks[index] & 0xFF;
                            int data = index < blockData.length ? (blockData[index] & 0xFF) : 0;

                            Block block = convertLegacyBlock(blockId, data);
                            if (block != null && block != Blocks.AIR) {
                                structureBlocks.add(new StructureBlock(x, y, z, block.defaultBlockState()));
                            }
                        }
                        index++;
                    }
                }
            }

            return new StructureData(width, height, depth, structureBlocks);
        }
    }

    // Métodos auxiliares de conversión (igual que en SchematicLoader)
    private static Block convertLegacyBlock(int blockId, int data) {
        // Mapeo básico igual que en SchematicLoader
        switch (blockId) {
            case 0: return Blocks.AIR;
            case 1: return Blocks.STONE;
            case 2: return Blocks.GRASS_BLOCK;
            case 3: return Blocks.DIRT;
            case 4: return Blocks.COBBLESTONE;
            case 5: return convertWoodPlanks(data);
            case 17: return convertWoodLog(data);
            case 20: return Blocks.GLASS;
            case 35: return convertWool(data);
            case 45: return Blocks.BRICKS;
            case 50: return Blocks.TORCH;
            case 53: return Blocks.OAK_STAIRS;
            case 54: return Blocks.CHEST;
            case 64: return Blocks.OAK_DOOR;
            case 65: return Blocks.LADDER;
            default: return Blocks.STONE;
        }
    }

    private static Block convertWoodPlanks(int data) {
        switch (data) {
            case 0: return Blocks.OAK_PLANKS;
            case 1: return Blocks.SPRUCE_PLANKS;
            case 2: return Blocks.BIRCH_PLANKS;
            case 3: return Blocks.JUNGLE_PLANKS;
            default: return Blocks.OAK_PLANKS;
        }
    }

    private static Block convertWoodLog(int data) {
        switch (data & 3) {
            case 0: return Blocks.OAK_LOG;
            case 1: return Blocks.SPRUCE_LOG;
            case 2: return Blocks.BIRCH_LOG;
            case 3: return Blocks.JUNGLE_LOG;
            default: return Blocks.OAK_LOG;
        }
    }

    private static Block convertWool(int data) {
        switch (data) {
            case 0: return Blocks.WHITE_WOOL;
            case 1: return Blocks.ORANGE_WOOL;
            case 14: return Blocks.RED_WOOL;
            case 15: return Blocks.BLACK_WOOL;
            default: return Blocks.WHITE_WOOL;
        }
    }

    private static int[] decodeVarIntArray(byte[] data) {
        List<Integer> result = new ArrayList<>();
        int index = 0;

        while (index < data.length) {
            int value = 0;
            int position = 0;
            byte currentByte;

            do {
                currentByte = data[index++];
                value |= (currentByte & 0x7F) << position;
                position += 7;
            } while ((currentByte & 0x80) != 0 && index < data.length);

            result.add(value);
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Clase para datos completos de estructura
     */
    public static class StructureData {
        public final int width, height, depth;
        public final List<StructureBlock> blocks;

        public StructureData(int width, int height, int depth, List<StructureBlock> blocks) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.blocks = blocks;
        }
    }

    /**
     * Clase para un bloque individual en la estructura
     */
    public static class StructureBlock {
        public final int x, y, z;
        public final BlockState blockState;

        public StructureBlock(int x, int y, int z, BlockState blockState) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockState = blockState;
        }
    }
}