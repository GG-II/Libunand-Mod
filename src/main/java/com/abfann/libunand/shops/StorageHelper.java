package com.abfann.libunand.shops;

import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * Utilidades para manejar cofres vinculados
 */
public class StorageHelper {

    /**
     * Cuenta cuántos items de un tipo hay en los cofres vinculados de un jugador
     */
    public static int countItemsInStorage(World world, UUID playerUUID, Item item) {
        List<LinkedChest> chests = ShopManager.getPlayerChests(playerUUID);
        int total = 0;

        for (LinkedChest chest : chests) {
            IInventory inventory = getChestInventory(world, chest.getPosition());
            if (inventory != null) {
                total += countItemsInInventory(inventory, item);
            }
        }

        return total;
    }

    /**
     * Remueve items de los cofres vinculados del jugador
     * @return true si se pudieron remover todos los items
     */
    public static boolean removeItemsFromStorage(World world, UUID playerUUID, Item item, int amount) {
        // Primero verificar que hay suficientes
        if (countItemsInStorage(world, playerUUID, item) < amount) {
            return false;
        }

        List<LinkedChest> chests = ShopManager.getPlayerChests(playerUUID);
        int remaining = amount;

        for (LinkedChest chest : chests) {
            if (remaining <= 0) break;

            IInventory inventory = getChestInventory(world, chest.getPosition());
            if (inventory != null) {
                remaining -= removeItemsFromInventory(inventory, item, remaining);
            }
        }

        return remaining == 0;
    }

    /**
     * Añade items a los cofres vinculados del jugador
     * @return true si se pudieron añadir todos los items
     */
    public static boolean addItemsToStorage(World world, UUID playerUUID, ItemStack stack) {
        List<LinkedChest> chests = ShopManager.getPlayerChests(playerUUID);

        ItemStack remaining = stack.copy();

        for (LinkedChest chest : chests) {
            if (remaining.isEmpty()) break;

            IInventory inventory = getChestInventory(world, chest.getPosition());
            if (inventory != null) {
                remaining = addItemsToInventory(inventory, remaining);
            }
        }

        return remaining.isEmpty();
    }

    /**
     * Obtiene el inventario de un cofre en una posición
     */
    private static IInventory getChestInventory(World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ChestTileEntity) {
            return (ChestTileEntity) te;
        }
        return null;
    }

    /**
     * Cuenta items en un inventario
     */
    private static int countItemsInInventory(IInventory inventory, Item item) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Remueve items de un inventario
     * @return cantidad removida
     */
    private static int removeItemsFromInventory(IInventory inventory, Item item, int amount) {
        int removed = 0;

        for (int i = 0; i < inventory.getContainerSize() && removed < amount; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(amount - removed, stack.getCount());
                stack.shrink(toRemove);
                removed += toRemove;

                if (stack.isEmpty()) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        inventory.setChanged();
        return removed;
    }

    /**
     * Añade items a un inventario
     * @return ItemStack con los items que no cupieron
     */
    private static ItemStack addItemsToInventory(IInventory inventory, ItemStack stack) {
        ItemStack remaining = stack.copy();

        // Primero intentar stackear con items existentes
        for (int i = 0; i < inventory.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slotStack = inventory.getItem(i);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (ItemStack.isSame(slotStack, remaining) && ItemStack.tagMatches(slotStack, remaining)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, remaining.getCount());
                    slotStack.grow(toAdd);
                    remaining.shrink(toAdd);
                }
            }
        }

        // Luego buscar slots vacíos
        for (int i = 0; i < inventory.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slotStack = inventory.getItem(i);

            if (slotStack.isEmpty()) {
                int toAdd = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                ItemStack newStack = remaining.copy();
                newStack.setCount(toAdd);
                inventory.setItem(i, newStack);
                remaining.shrink(toAdd);
            }
        }

        inventory.setChanged();
        return remaining;
    }

    /**
     * Verifica si hay espacio en los cofres del jugador
     */
    public static boolean hasSpaceInStorage(World world, UUID playerUUID, ItemStack stack) {
        List<LinkedChest> chests = ShopManager.getPlayerChests(playerUUID);

        int requiredSpace = stack.getCount();
        int availableSpace = 0;

        for (LinkedChest chest : chests) {
            IInventory inventory = getChestInventory(world, chest.getPosition());
            if (inventory != null) {
                availableSpace += getAvailableSpace(inventory, stack);
                if (availableSpace >= requiredSpace) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Calcula el espacio disponible en un inventario para un item específico
     */
    private static int getAvailableSpace(IInventory inventory, ItemStack stack) {
        int space = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slotStack = inventory.getItem(i);

            if (slotStack.isEmpty()) {
                space += stack.getMaxStackSize();
            } else if (ItemStack.isSame(slotStack, stack) && ItemStack.tagMatches(slotStack, stack)) {
                space += slotStack.getMaxStackSize() - slotStack.getCount();
            }
        }

        return space;
    }
}