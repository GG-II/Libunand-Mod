package com.abfann.libunand.building;

import net.minecraft.block.Block;

import java.util.Map;

public class StructureInfo {

    private final String name;
    private final String category;
    private final String fileName;
    private final Map<Block, Integer> blockCounts;
    private final int width;
    private final int height;
    private final int depth;
    private final int price;
    private final String description;

    public StructureInfo(String name, String category, String fileName,
                         Map<Block, Integer> blockCounts,
                         int width, int height, int depth, String description) {
        this.name = name;
        this.category = category;
        this.fileName = fileName;
        this.blockCounts = blockCounts;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.description = description;
        this.price = BlockPriceCalculator.calculateStructurePrice(blockCounts);
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getFileName() { return fileName; }
    public Map<Block, Integer> getBlockCounts() { return blockCounts; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getTotalBlocks() {
        return blockCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getDisplayName() {
        return String.format("%s (%s)", name, category);
    }

    public String getSizeInfo() {
        return String.format("%dx%dx%d (%d bloques)", width, height, depth, getTotalBlocks());
    }

    @Override
    public String toString() {
        return String.format("Structure{name='%s', category='%s', price=%d, size=%s}",
                name, category, price, getSizeInfo());
    }
}