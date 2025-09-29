package com.abfann.libunand.shops;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Representa una tienda en un cartel
 */
public class ShopSign {

    public enum ShopType {
        SELL,  // Jugador vende items
        BUY    // Jugador compra items
    }

    private final BlockPos position;
    private final UUID ownerUUID;
    private final String ownerName;
    private final ShopType type;
    private final String itemId;
    private final int quantity;
    private final int price;

    public ShopSign(BlockPos position, UUID ownerUUID, String ownerName,
                    ShopType type, String itemId, int quantity, int price) {
        this.position = position;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.type = type;
        this.itemId = normalizeItemId(itemId);
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Normaliza el ID del item para aceptar varios formatos
     * Ejemplos: "DIAMOND" -> "minecraft:diamond"
     *           "diamond" -> "minecraft:diamond"
     *           "minecraft:diamond" -> "minecraft:diamond"
     *           "modid:item" -> "modid:item"
     */
    private String normalizeItemId(String input) {
        input = input.trim();

        // Si ya tiene namespace, devolverlo
        if (input.contains(":")) {
            return input.toLowerCase();
        }

        // Si no tiene namespace, asumir minecraft:
        return "minecraft:" + input.toLowerCase();
    }

    /**
     * Obtiene el item de Minecraft correspondiente
     */
    public Item getItem() {
        ResourceLocation itemLocation = new ResourceLocation(itemId);
        return ForgeRegistries.ITEMS.getValue(itemLocation);
    }

    /**
     * Verifica si el item ID es válido
     */
    public boolean isValidItem() {
        return getItem() != null && getItem() != net.minecraft.item.Items.AIR;
    }

    /**
     * Crea un ItemStack del item de la tienda
     */
    public ItemStack createItemStack() {
        return new ItemStack(getItem(), quantity);
    }

    // Getters
    public BlockPos getPosition() { return position; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public ShopType getType() { return type; }
    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }

    public boolean isOwner(UUID playerUUID) {
        return this.ownerUUID.equals(playerUUID);
    }

    @Override
    public String toString() {
        return String.format("ShopSign[%s] %s x%d = %d JoJoCoins (Owner: %s)",
                type, itemId, quantity, price, ownerName);
    }
}