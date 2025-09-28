package com.abfann.libunand.building;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class BlockPriceCalculator {

    private static final Map<Block, Integer> BLOCK_PRICES = new HashMap<>();

    static {
        initializePrices();
    }

    /**
     * Inicializa los precios base de los bloques (50% más baratos que trades de aldeanos)
     */
    private static void initializePrices() {
        // Bloques básicos naturales (0.5-1 JoJoCoin)
        BLOCK_PRICES.put(Blocks.DIRT, 1);
        BLOCK_PRICES.put(Blocks.GRASS_BLOCK, 1);
        BLOCK_PRICES.put(Blocks.STONE, 1);
        BLOCK_PRICES.put(Blocks.COBBLESTONE, 1);
        BLOCK_PRICES.put(Blocks.SAND, 1);
        BLOCK_PRICES.put(Blocks.GRAVEL, 1);

        // Materiales de construcción básicos (1-2 JoJoCoins)
        BLOCK_PRICES.put(Blocks.OAK_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.SPRUCE_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.BIRCH_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.JUNGLE_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.ACACIA_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.DARK_OAK_PLANKS, 1);
        BLOCK_PRICES.put(Blocks.STONE_BRICKS, 2);
        BLOCK_PRICES.put(Blocks.SMOOTH_STONE, 2);

        // Bloques procesados (1.5-3 JoJoCoins)
        BLOCK_PRICES.put(Blocks.BRICKS, 2);
        BLOCK_PRICES.put(Blocks.GLASS, 2);
        BLOCK_PRICES.put(Blocks.GLASS_PANE, 1);
        BLOCK_PRICES.put(Blocks.OAK_STAIRS, 2);
        BLOCK_PRICES.put(Blocks.STONE_STAIRS, 2);
        BLOCK_PRICES.put(Blocks.BRICK_STAIRS, 3);
        BLOCK_PRICES.put(Blocks.OAK_SLAB, 1);
        BLOCK_PRICES.put(Blocks.STONE_SLAB, 1);
        BLOCK_PRICES.put(Blocks.BRICK_SLAB, 2);

        // Lana y bloques decorativos (1-2 JoJoCoins)
        BLOCK_PRICES.put(Blocks.WHITE_WOOL, 1);
        BLOCK_PRICES.put(Blocks.RED_WOOL, 1);
        BLOCK_PRICES.put(Blocks.BLUE_WOOL, 1);
        BLOCK_PRICES.put(Blocks.GREEN_WOOL, 1);
        BLOCK_PRICES.put(Blocks.YELLOW_WOOL, 1);
        BLOCK_PRICES.put(Blocks.BLACK_WOOL, 1);
        BLOCK_PRICES.put(Blocks.WHITE_CARPET, 1);
        BLOCK_PRICES.put(Blocks.RED_CARPET, 1);

        // Metales comunes (2-5 JoJoCoins)
        BLOCK_PRICES.put(Blocks.IRON_BLOCK, 23); // 9 lingotes × 2.5 (50% descuento)
        BLOCK_PRICES.put(Blocks.GOLD_BLOCK, 45); // 9 lingotes × 5
        BLOCK_PRICES.put(Blocks.IRON_BARS, 2);
        BLOCK_PRICES.put(Blocks.IRON_DOOR, 5);
        BLOCK_PRICES.put(Blocks.IRON_TRAPDOOR, 3);

        // Bloques raros y valiosos (25-100 JoJoCoins)
        BLOCK_PRICES.put(Blocks.DIAMOND_BLOCK, 225); // 9 diamantes × 25 (50% descuento)
        BLOCK_PRICES.put(Blocks.EMERALD_BLOCK, 450); // 9 esmeraldas × 50
        BLOCK_PRICES.put(Blocks.BEACON, 1000); // Muy raro (50% descuento)
        BLOCK_PRICES.put(Blocks.ENCHANTING_TABLE, 100);
        BLOCK_PRICES.put(Blocks.ANVIL, 50);

        // Bloques funcionales (3-15 JoJoCoins)
        BLOCK_PRICES.put(Blocks.CRAFTING_TABLE, 3);
        BLOCK_PRICES.put(Blocks.FURNACE, 4);
        BLOCK_PRICES.put(Blocks.CHEST, 4);
        BLOCK_PRICES.put(Blocks.BARREL, 4);
        BLOCK_PRICES.put(Blocks.BOOKSHELF, 6);
        BLOCK_PRICES.put(Blocks.LECTERN, 8);

        // Redstone (2-10 JoJoCoins)
        BLOCK_PRICES.put(Blocks.REDSTONE_BLOCK, 5);
        BLOCK_PRICES.put(Blocks.REDSTONE_TORCH, 2);
        BLOCK_PRICES.put(Blocks.LEVER, 2);
        BLOCK_PRICES.put(Blocks.STONE_BUTTON, 1);
        BLOCK_PRICES.put(Blocks.STONE_PRESSURE_PLATE, 2);

        // Plantas y decoración natural (0.5-2 JoJoCoins)
        BLOCK_PRICES.put(Blocks.OAK_LEAVES, 1);
        BLOCK_PRICES.put(Blocks.GRASS, 1);
        BLOCK_PRICES.put(Blocks.FERN, 1);
        BLOCK_PRICES.put(Blocks.POPPY, 1);
        BLOCK_PRICES.put(Blocks.DANDELION, 1);
        BLOCK_PRICES.put(Blocks.ROSE_BUSH, 2);

        // Bloques de luz (2-5 JoJoCoins)
        BLOCK_PRICES.put(Blocks.TORCH, 1);
        BLOCK_PRICES.put(Blocks.LANTERN, 3);
        BLOCK_PRICES.put(Blocks.GLOWSTONE, 4);
        BLOCK_PRICES.put(Blocks.SEA_LANTERN, 5);
    }

    /**
     * Obtiene el precio base de un bloque
     */
    public static int getBlockPrice(Block block) {
        return BLOCK_PRICES.getOrDefault(block, getDefaultPrice(block));
    }

    /**
     * Precio por defecto para bloques no listados
     */
    private static int getDefaultPrice(Block block) {
        String blockName = block.getRegistryName().toString();

        // Heurísticas para bloques no listados
        if (blockName.contains("diamond")) return 25;
        if (blockName.contains("emerald")) return 50;
        if (blockName.contains("gold")) return 5;
        if (blockName.contains("iron")) return 3;
        if (blockName.contains("glass")) return 2;
        if (blockName.contains("wool") || blockName.contains("carpet")) return 1;
        if (blockName.contains("stone")) return 1;
        if (blockName.contains("wood") || blockName.contains("planks")) return 1;
        if (blockName.contains("stairs")) return 2;
        if (blockName.contains("slab")) return 1;

        // Precio por defecto
        return 2;
    }

    /**
     * Calcula el precio total de una estructura con factores adicionales
     */
    public static int calculateStructurePrice(Map<Block, Integer> blockCounts) {
        int basePrice = 0;
        int uniqueBlocks = blockCounts.size();
        int totalBlocks = 0;

        // Calcular precio base
        for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
            Block block = entry.getKey();
            int count = entry.getValue();
            int blockPrice = getBlockPrice(block);

            basePrice += blockPrice * count;
            totalBlocks += count;
        }

        // Factor de complejidad (más tipos de bloque = más caro)
        double complexityFactor = 1.0 + (uniqueBlocks * 0.02); // +2% por cada tipo único

        // Factor de tamaño (estructuras grandes tienen economía de escala)
        double sizeFactor = 1.0;
        if (totalBlocks > 500) sizeFactor = 0.9;      // -10% para estructuras grandes
        if (totalBlocks > 1000) sizeFactor = 0.8;     // -20% para estructuras muy grandes
        if (totalBlocks < 50) sizeFactor = 1.2;       // +20% para estructuras pequeñas

        // Aplicar factores
        double finalPrice = basePrice * complexityFactor * sizeFactor;

        // Aplicar descuento del 90% (precio final = 10% del original)
        finalPrice = finalPrice * 0.10;

        return Math.max(1, (int) Math.round(finalPrice));
    }

    /**
     * Obtiene información detallada del cálculo de precios
     */
    public static String getPriceBreakdown(Map<Block, Integer> blockCounts) {
        int basePrice = 0;
        int uniqueBlocks = blockCounts.size();
        int totalBlocks = 0;

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("=== Desglose de Precio ===\n");

        // Mostrar algunos bloques más caros
        blockCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(
                        getBlockPrice(b.getKey()) * b.getValue(),
                        getBlockPrice(a.getKey()) * a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    Block block = entry.getKey();
                    int count = entry.getValue();
                    int price = getBlockPrice(block);
                    breakdown.append(String.format("%s x%d = %d JoJoCoins\n",
                            block.getName().getString(), count, price * count));
                });

        for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
            basePrice += getBlockPrice(entry.getKey()) * entry.getValue();
            totalBlocks += entry.getValue();
        }

        breakdown.append(String.format("Precio base: %d JoJoCoins\n", basePrice));
        breakdown.append(String.format("Bloques unicos: %d (+%.0f%%)\n", uniqueBlocks, uniqueBlocks * 2.0));
        breakdown.append(String.format("Total de bloques: %d\n", totalBlocks));

        int finalPrice = calculateStructurePrice(blockCounts);
        breakdown.append(String.format("Precio final: %d JoJoCoins", finalPrice));

        return breakdown.toString();
    }
}