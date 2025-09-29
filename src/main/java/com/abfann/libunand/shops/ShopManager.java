package com.abfann.libunand.shops;

import com.abfann.libunand.LibunandMod;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona todas las tiendas y cofres vinculados del servidor
 */
public class ShopManager {

    private static final Map<BlockPos, ShopSign> shops = new ConcurrentHashMap<>();
    private static final Map<BlockPos, LinkedChest> linkedChests = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<BlockPos>> playerShops = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<BlockPos>> playerChests = new ConcurrentHashMap<>();

    /**
     * Registra una nueva tienda
     */
    public static void registerShop(ShopSign shop) {
        shops.put(shop.getPosition(), shop);

        playerShops.computeIfAbsent(shop.getOwnerUUID(), k -> new HashSet<>())
                .add(shop.getPosition());

        LibunandMod.LOGGER.info("Tienda registrada: {}", shop);
    }

    /**
     * Remueve una tienda
     */
    public static void removeShop(BlockPos position) {
        ShopSign shop = shops.remove(position);
        if (shop != null) {
            Set<BlockPos> playerShopSet = playerShops.get(shop.getOwnerUUID());
            if (playerShopSet != null) {
                playerShopSet.remove(position);
            }
            LibunandMod.LOGGER.info("Tienda removida: {}", shop);
        }
    }

    /**
     * Registra un cofre vinculado
     */
    public static void registerLinkedChest(LinkedChest chest) {
        linkedChests.put(chest.getPosition(), chest);

        playerChests.computeIfAbsent(chest.getOwnerUUID(), k -> new HashSet<>())
                .add(chest.getPosition());

        LibunandMod.LOGGER.info("Cofre vinculado: {}", chest);
    }

    /**
     * Remueve un cofre vinculado
     */
    public static void removeLinkedChest(BlockPos position) {
        LinkedChest chest = linkedChests.remove(position);
        if (chest != null) {
            Set<BlockPos> playerChestSet = playerChests.get(chest.getOwnerUUID());
            if (playerChestSet != null) {
                playerChestSet.remove(position);
            }
            LibunandMod.LOGGER.info("Cofre desvinculado: {}", chest);
        }
    }

    /**
     * Obtiene una tienda en una posición
     */
    public static ShopSign getShop(BlockPos position) {
        return shops.get(position);
    }

    /**
     * Obtiene un cofre vinculado en una posición
     */
    public static LinkedChest getLinkedChest(BlockPos position) {
        return linkedChests.get(position);
    }

    /**
     * Obtiene todos los cofres vinculados de un jugador
     */
    public static List<LinkedChest> getPlayerChests(UUID playerUUID) {
        Set<BlockPos> chestPositions = playerChests.get(playerUUID);
        if (chestPositions == null) {
            return Collections.emptyList();
        }

        List<LinkedChest> chests = new ArrayList<>();
        for (BlockPos pos : chestPositions) {
            LinkedChest chest = linkedChests.get(pos);
            if (chest != null) {
                chests.add(chest);
            }
        }
        return chests;
    }

    /**
     * Verifica si una posición es una tienda
     */
    public static boolean isShop(BlockPos position) {
        return shops.containsKey(position);
    }

    /**
     * Verifica si una posición es un cofre vinculado
     */
    public static boolean isLinkedChest(BlockPos position) {
        return linkedChests.containsKey(position);
    }

    /**
     * Limpia todos los datos (para reload)
     */
    public static void clear() {
        shops.clear();
        linkedChests.clear();
        playerShops.clear();
        playerChests.clear();
        LibunandMod.LOGGER.info("ShopManager limpiado");
    }

    /**
     * Obtiene todos los cofres vinculados
     */
    public static java.util.Collection<LinkedChest> getAllLinkedChests() {
        return linkedChests.values();
    }
}