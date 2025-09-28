package com.abfann.libunand.building;

import com.abfann.libunand.LibunandMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchematicLoader {

    /**
     * Carga una estructura desde un archivo .schematic o .schem
     */
    public static StructureData loadStructure(File file) {
        try {
            if (file.getName().endsWith(".schematic")) {
                return loadSchematicFile(file);
            } else if (file.getName().endsWith(".schem")) {
                return loadSchemFile(file);
            } else {
                LibunandMod.LOGGER.warn("Formato de archivo no soportado: {}", file.getName());
                return null;
            }
        } catch (Exception e) {
            LibunandMod.LOGGER.error("Error cargando estructura {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Carga archivo formato .schematic (MCEdit)
     */
    private static StructureData loadSchematicFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(fis);

            // Leer dimensiones
            int width = nbt.getShort("Width");
            int height = nbt.getShort("Height");
            int depth = nbt.getShort("Length");

            // Leer datos de bloques
            byte[] blocks = nbt.getByteArray("Blocks");
            byte[] blockData = nbt.getByteArray("Data");

            Map<Block, Integer> blockCounts = new HashMap<>();

            // Procesar bloques (formato legacy)
            for (int i = 0; i < blocks.length; i++) {
                int blockId = blocks[i] & 0xFF;
                int data = i < blockData.length ? (blockData[i] & 0xFF) : 0;

                Block block = convertLegacyBlock(blockId, data);
                if (block != null && block != Blocks.AIR) {
                    blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
                }
            }

            return new StructureData(width, height, depth, blockCounts);
        }
    }

    /**
     * Carga archivo formato .schem (WorldEdit)
     */
    private static StructureData loadSchemFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(fis);

            // Leer dimensiones
            int width = nbt.getShort("Width");
            int height = nbt.getShort("Height");
            int depth = nbt.getShort("Length");

            Map<Block, Integer> blockCounts = new HashMap<>();

            // Leer paleta de bloques
            CompoundNBT palette = nbt.getCompound("Palette");
            Map<Integer, Block> blockPalette = new HashMap<>();

            for (String blockName : palette.getAllKeys()) {
                int id = palette.getInt(blockName);
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                if (block != null) {
                    blockPalette.put(id, block);
                }
            }

            // Leer datos de bloques
            byte[] blockData = nbt.getByteArray("BlockData");

            // Decodificar VarInt array
            int[] blockIds = decodeVarIntArray(blockData);

            for (int blockId : blockIds) {
                Block block = blockPalette.get(blockId);
                if (block != null && block != Blocks.AIR) {
                    blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
                }
            }

            return new StructureData(width, height, depth, blockCounts);
        }
    }

    /**
     * Convierte IDs de bloques legacy a bloques modernos
     */
    private static Block convertLegacyBlock(int blockId, int data) {
        // Mapeo básico de IDs legacy más comunes
        switch (blockId) {
            case 0: return Blocks.AIR;
            case 1: return Blocks.STONE;
            case 2: return Blocks.GRASS_BLOCK;
            case 3: return Blocks.DIRT;
            case 4: return Blocks.COBBLESTONE;
            case 5: return convertWoodPlanks(data);
            case 6: return convertSapling(data);
            case 7: return Blocks.BEDROCK;
            case 8: case 9: return Blocks.WATER;
            case 10: case 11: return Blocks.LAVA;
            case 12: return Blocks.SAND;
            case 13: return Blocks.GRAVEL;
            case 14: return Blocks.GOLD_ORE;
            case 15: return Blocks.IRON_ORE;
            case 16: return Blocks.COAL_ORE;
            case 17: return convertWoodLog(data);
            case 18: return convertLeaves(data);
            case 19: return Blocks.SPONGE;
            case 20: return Blocks.GLASS;
            case 35: return convertWool(data);
            case 41: return Blocks.GOLD_BLOCK;
            case 42: return Blocks.IRON_BLOCK;
            case 45: return Blocks.BRICKS;
            case 46: return Blocks.TNT;
            case 47: return Blocks.BOOKSHELF;
            case 48: return Blocks.MOSSY_COBBLESTONE;
            case 49: return Blocks.OBSIDIAN;
            case 50: return Blocks.TORCH;
            case 53: return Blocks.OAK_STAIRS;
            case 54: return Blocks.CHEST;
            case 56: return Blocks.DIAMOND_ORE;
            case 57: return Blocks.DIAMOND_BLOCK;
            case 58: return Blocks.CRAFTING_TABLE;
            case 61: return Blocks.FURNACE;
            case 64: return Blocks.OAK_DOOR;
            case 65: return Blocks.LADDER;
            case 67: return Blocks.COBBLESTONE_STAIRS;
            case 78: return Blocks.SNOW;
            case 79: return Blocks.ICE;
            case 80: return Blocks.SNOW_BLOCK;
            case 82: return Blocks.CLAY;
            case 85: return Blocks.OAK_FENCE;
            case 89: return Blocks.GLOWSTONE;
            case 91: return Blocks.JACK_O_LANTERN;
            case 98: return convertStoneBrick(data);
            case 101: return Blocks.IRON_BARS;
            case 102: return Blocks.GLASS_PANE;
            case 103: return Blocks.MELON;
            case 108: return Blocks.BRICK_STAIRS;
            case 109: return Blocks.STONE_BRICK_STAIRS;
            case 112: return Blocks.NETHER_BRICKS;
            case 114: return Blocks.NETHER_BRICK_STAIRS;
            case 116: return Blocks.ENCHANTING_TABLE;
            case 121: return Blocks.END_STONE;
            case 123: return Blocks.REDSTONE_LAMP;
            case 124: return Blocks.REDSTONE_LAMP;
            case 133: return Blocks.EMERALD_BLOCK;
            case 152: return Blocks.REDSTONE_BLOCK;
            case 155: return Blocks.QUARTZ_BLOCK;
            case 156: return Blocks.QUARTZ_STAIRS;
            case 159: return convertTerracotta(data);
            case 168: return Blocks.PRISMARINE;
            case 169: return Blocks.SEA_LANTERN;
            case 172: return Blocks.TERRACOTTA;
            case 173: return Blocks.COAL_BLOCK;
            case 174: return Blocks.PACKED_ICE;
            default:
                LibunandMod.LOGGER.debug("ID de bloque legacy no mapeado: {} (data: {})", blockId, data);
                return Blocks.STONE; // Fallback seguro
        }
    }

    // Métodos auxiliares para conversión de metadata
    private static Block convertWoodPlanks(int data) {
        switch (data) {
            case 0: return Blocks.OAK_PLANKS;
            case 1: return Blocks.SPRUCE_PLANKS;
            case 2: return Blocks.BIRCH_PLANKS;
            case 3: return Blocks.JUNGLE_PLANKS;
            case 4: return Blocks.ACACIA_PLANKS;
            case 5: return Blocks.DARK_OAK_PLANKS;
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

    private static Block convertLeaves(int data) {
        switch (data & 3) {
            case 0: return Blocks.OAK_LEAVES;
            case 1: return Blocks.SPRUCE_LEAVES;
            case 2: return Blocks.BIRCH_LEAVES;
            case 3: return Blocks.JUNGLE_LEAVES;
            default: return Blocks.OAK_LEAVES;
        }
    }

    private static Block convertWool(int data) {
        switch (data) {
            case 0: return Blocks.WHITE_WOOL;
            case 1: return Blocks.ORANGE_WOOL;
            case 2: return Blocks.MAGENTA_WOOL;
            case 3: return Blocks.LIGHT_BLUE_WOOL;
            case 4: return Blocks.YELLOW_WOOL;
            case 5: return Blocks.LIME_WOOL;
            case 6: return Blocks.PINK_WOOL;
            case 7: return Blocks.GRAY_WOOL;
            case 8: return Blocks.LIGHT_GRAY_WOOL;
            case 9: return Blocks.CYAN_WOOL;
            case 10: return Blocks.PURPLE_WOOL;
            case 11: return Blocks.BLUE_WOOL;
            case 12: return Blocks.BROWN_WOOL;
            case 13: return Blocks.GREEN_WOOL;
            case 14: return Blocks.RED_WOOL;
            case 15: return Blocks.BLACK_WOOL;
            default: return Blocks.WHITE_WOOL;
        }
    }

    private static Block convertSapling(int data) {
        switch (data & 7) {
            case 0: return Blocks.OAK_SAPLING;
            case 1: return Blocks.SPRUCE_SAPLING;
            case 2: return Blocks.BIRCH_SAPLING;
            case 3: return Blocks.JUNGLE_SAPLING;
            case 4: return Blocks.ACACIA_SAPLING;
            case 5: return Blocks.DARK_OAK_SAPLING;
            default: return Blocks.OAK_SAPLING;
        }
    }

    private static Block convertStoneBrick(int data) {
        switch (data) {
            case 0: return Blocks.STONE_BRICKS;
            case 1: return Blocks.MOSSY_STONE_BRICKS;
            case 2: return Blocks.CRACKED_STONE_BRICKS;
            case 3: return Blocks.CHISELED_STONE_BRICKS;
            default: return Blocks.STONE_BRICKS;
        }
    }

    private static Block convertTerracotta(int data) {
        switch (data) {
            case 0: return Blocks.WHITE_TERRACOTTA;
            case 1: return Blocks.ORANGE_TERRACOTTA;
            case 2: return Blocks.MAGENTA_TERRACOTTA;
            case 3: return Blocks.LIGHT_BLUE_TERRACOTTA;
            case 4: return Blocks.YELLOW_TERRACOTTA;
            case 5: return Blocks.LIME_TERRACOTTA;
            case 6: return Blocks.PINK_TERRACOTTA;
            case 7: return Blocks.GRAY_TERRACOTTA;
            case 8: return Blocks.LIGHT_GRAY_TERRACOTTA;
            case 9: return Blocks.CYAN_TERRACOTTA;
            case 10: return Blocks.PURPLE_TERRACOTTA;
            case 11: return Blocks.BLUE_TERRACOTTA;
            case 12: return Blocks.BROWN_TERRACOTTA;
            case 13: return Blocks.GREEN_TERRACOTTA;
            case 14: return Blocks.RED_TERRACOTTA;
            case 15: return Blocks.BLACK_TERRACOTTA;
            default: return Blocks.TERRACOTTA;
        }
    }

    /**
     * Decodifica un array VarInt (formato .schem)
     */
    private static int[] decodeVarIntArray(byte[] data) {
        java.util.List<Integer> result = new java.util.ArrayList<>();
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
     * Clase interna para datos de estructura
     */
    public static class StructureData {
        public final int width, height, depth;
        public final Map<Block, Integer> blockCounts;

        public StructureData(int width, int height, int depth, Map<Block, Integer> blockCounts) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.blockCounts = blockCounts;
        }
    }
}