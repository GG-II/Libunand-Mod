package com.abfann.libunand.shops;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.data.IPlayerEconomy;
import com.abfann.libunand.data.PlayerDataHandler;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Maneja las transacciones de compra/venta en las tiendas
 */
public class ShopTransaction {

    /**
     * Procesa una compra en una tienda [SELL]
     * El comprador compra items del dueño de la tienda
     */
    public static boolean processSellShop(World world, ShopSign shop, ServerPlayerEntity buyer) {
        // Verificar que no es el dueño
        if (shop.isOwner(buyer.getUUID())) {
            buyer.sendMessage(
                    new StringTextComponent("No puedes comprar de tu propia tienda!")
                            .withStyle(TextFormatting.RED),
                    buyer.getUUID()
            );
            return false;
        }

        // Obtener economías
        IPlayerEconomy buyerEconomy = PlayerDataHandler.getPlayerEconomy(buyer);

        // Verificar que el comprador tiene suficiente dinero
        if (!buyerEconomy.hasBalance(shop.getPrice())) {
            buyer.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Necesitas: " + shop.getPrice())
                            .withStyle(TextFormatting.RED),
                    buyer.getUUID()
            );
            return false;
        }

        // Verificar que hay items en el storage del vendedor
        int available = StorageHelper.countItemsInStorage(world, shop.getOwnerUUID(), shop.getItem());
        if (available < shop.getQuantity()) {
            buyer.sendMessage(
                    new StringTextComponent("La tienda no tiene suficiente stock! (Tiene: " + available + ")")
                            .withStyle(TextFormatting.RED),
                    buyer.getUUID()
            );
            return false;
        }

        // Remover items del storage del vendedor
        if (!StorageHelper.removeItemsFromStorage(world, shop.getOwnerUUID(), shop.getItem(), shop.getQuantity())) {
            buyer.sendMessage(
                    new StringTextComponent("Error al obtener items del cofre!")
                            .withStyle(TextFormatting.RED),
                    buyer.getUUID()
            );
            return false;
        }

        // Realizar transacción económica
        buyerEconomy.removeBalance(shop.getPrice());

        // Dar dinero al vendedor (offline-safe)
        if (!player.level.isClientSide) {
            ServerPlayerEntity seller = buyer.getServer().getPlayerList().getPlayer(shop.getOwnerUUID());
            if (seller != null) {
                IPlayerEconomy sellerEconomy = PlayerDataHandler.getPlayerEconomy(seller);
                sellerEconomy.addBalance(shop.getPrice());
                seller.sendMessage(
                        new StringTextComponent("Has vendido " + shop.getQuantity() + "x " + shop.getItemId() +
                                " por " + shop.getPrice() + " JoJoCoins!")
                                .withStyle(TextFormatting.GREEN),
                        seller.getUUID()
                );
            }
        }

        // Dar items al comprador
        ItemStack purchasedItems = shop.createItemStack();
        if (!buyer.inventory.add(purchasedItems)) {
            // Si inventario lleno, dropear en el suelo
            ItemEntity itemEntity = new ItemEntity(world, buyer.getX(), buyer.getY(), buyer.getZ(), purchasedItems);
            world.addFreshEntity(itemEntity);
        }

        buyer.sendMessage(
                new StringTextComponent("Compraste " + shop.getQuantity() + "x " + shop.getItemId() +
                        " por " + shop.getPrice() + " JoJoCoins!")
                        .withStyle(TextFormatting.GREEN),
                buyer.getUUID()
        );

        LibunandMod.LOGGER.info("Transaccion SELL: {} compro {}x{} de {} por {} JC",
                buyer.getName().getString(), shop.getQuantity(), shop.getItemId(),
                shop.getOwnerName(), shop.getPrice());

        return true;
    }

    /**
     * Procesa una venta en una tienda [BUY]
     * El vendedor vende items al dueño de la tienda
     */
    public static boolean processBuyShop(World world, ShopSign shop, ServerPlayerEntity seller) {
        // Verificar que no es el dueño
        if (shop.isOwner(seller.getUUID())) {
            seller.sendMessage(
                    new StringTextComponent("No puedes vender a tu propia tienda!")
                            .withStyle(TextFormatting.RED),
                    seller.getUUID()
            );
            return false;
        }

        // Verificar que el vendedor tiene los items
        int sellerHas = countItemInInventory(seller, shop.getItem());
        if (sellerHas < shop.getQuantity()) {
            seller.sendMessage(
                    new StringTextComponent("No tienes suficientes items! Necesitas: " + shop.getQuantity() + ", tienes: " + sellerHas)
                            .withStyle(TextFormatting.RED),
                    seller.getUUID()
            );
            return false;
        }

        // Verificar que hay espacio en los cofres del comprador
        ItemStack itemsToStore = shop.createItemStack();
        if (!StorageHelper.hasSpaceInStorage(world, shop.getOwnerUUID(), itemsToStore)) {
            seller.sendMessage(
                    new StringTextComponent("Los cofres del comprador estan llenos!")
                            .withStyle(TextFormatting.RED),
                    seller.getUUID()
            );
            return false;
        }

        // Obtener economía del vendedor
        IPlayerEconomy sellerEconomy = PlayerDataHandler.getPlayerEconomy(seller);

        // Remover items del inventario del vendedor
        removeItemFromInventory(seller, shop.getItem(), shop.getQuantity());

        // Dar dinero al vendedor
        sellerEconomy.addBalance(shop.getPrice());

        // Añadir items a los cofres del comprador
        if (!StorageHelper.addItemsToStorage(world, shop.getOwnerUUID(), itemsToStore)) {
            // Si falla, devolver items al vendedor
            seller.inventory.add(itemsToStore);
            sellerEconomy.removeBalance(shop.getPrice());
            seller.sendMessage(
                    new StringTextComponent("Error al guardar items en el cofre!")
                            .withStyle(TextFormatting.RED),
                    seller.getUUID()
            );
            return false;
        }

        // Quitar dinero al comprador (offline-safe)
        if (!player.level.isClientSide) {
            ServerPlayerEntity buyerPlayer = seller.getServer().getPlayerList().getPlayer(shop.getOwnerUUID());
            if (buyerPlayer != null) {
                IPlayerEconomy buyerEconomy = PlayerDataHandler.getPlayerEconomy(buyerPlayer);
                buyerEconomy.removeBalance(shop.getPrice());
                buyerPlayer.sendMessage(
                        new StringTextComponent("Has comprado " + shop.getQuantity() + "x " + shop.getItemId() +
                                " por " + shop.getPrice() + " JoJoCoins!")
                                .withStyle(TextFormatting.GREEN),
                        buyerPlayer.getUUID()
                );
            }
        }

        seller.sendMessage(
                new StringTextComponent("Vendiste " + shop.getQuantity() + "x " + shop.getItemId() +
                        " por " + shop.getPrice() + " JoJoCoins!")
                        .withStyle(TextFormatting.GREEN),
                seller.getUUID()
        );

        LibunandMod.LOGGER.info("Transaccion BUY: {} vendio {}x{} a {} por {} JC",
                seller.getName().getString(), shop.getQuantity(), shop.getItemId(),
                shop.getOwnerName(), shop.getPrice());

        return true;
    }

    private static int countItemInInventory(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItemFromInventory(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.inventory.items.size() && remaining > 0; i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }
}