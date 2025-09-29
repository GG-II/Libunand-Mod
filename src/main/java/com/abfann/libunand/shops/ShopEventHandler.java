package com.abfann.libunand.shops;

import com.abfann.libunand.LibunandMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class ShopEventHandler {

    /**
     * Detecta cuando un jugador EDITA un cartel (para crear tiendas)
     */
    @SubscribeEvent
    public static void onSignEdit(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        // Solo procesar si es un cartel SIN registrar (nuevo)
        if (!(state.getBlock() instanceof StandingSignBlock || state.getBlock() instanceof WallSignBlock)) {
            return;
        }

        // Si ya es una tienda registrada, no procesar (se maneja en onShopClick)
        if (ShopManager.isShop(pos) || ShopManager.isLinkedChest(pos.below())) {
            return;
        }

        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof SignTileEntity)) {
            return;
        }

        // Esperar un tick para que el texto se actualice
        world.getServer().execute(() -> {
            SignTileEntity sign = (SignTileEntity) world.getBlockEntity(pos);
            if (sign != null) {
                processNewSign(world, pos, sign, event.getPlayer());
            }
        });
    }

    /**
     * Procesa un cartel NUEVO para detectar [SELL], [BUY] o [SHOP]
     */
    private static void processNewSign(World world, BlockPos pos, SignTileEntity sign, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        String line1 = sign.getMessage(0).getString().trim();
        String line2 = sign.getMessage(1).getString().trim();
        String line3 = sign.getMessage(2).getString().trim();
        String line4 = sign.getMessage(3).getString().trim();

        // Si está vacío, ignorar
        if (line1.isEmpty()) {
            return;
        }

        // Detectar [SHOP] - Cofre vinculado
        if (line1.equalsIgnoreCase("[SHOP]")) {
            handleShopStorageSign(world, pos, sign, serverPlayer);
            return;
        }

        // Detectar [SELL] - Tienda de venta
        if (line1.equalsIgnoreCase("[SELL]")) {
            handleSellShopSign(world, pos, sign, serverPlayer, line2, line3, line4);
            return;
        }

        // Detectar [BUY] - Tienda de compra
        if (line1.equalsIgnoreCase("[BUY]")) {
            handleBuyShopSign(world, pos, sign, serverPlayer, line2, line3, line4);
            return;
        }
    }

    /**
     * Detecta clicks en carteles de tiendas YA REGISTRADAS
     */
    @SubscribeEvent
    public static void onShopClick(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isClientSide) return;

        BlockPos pos = event.getPos();

        // Verificar si es una tienda registrada
        if (!ShopManager.isShop(pos)) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        // IMPORTANTE: Cancelar el evento para que no se abra la GUI de edición
        event.setCanceled(true);

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        ShopSign shop = ShopManager.getShop(pos);

        if (shop == null) {
            return;
        }

        // Procesar según el tipo de tienda
        if (shop.getType() == ShopSign.ShopType.SELL) {
            ShopTransaction.processSellShop(world, shop, serverPlayer);
        } else if (shop.getType() == ShopSign.ShopType.BUY) {
            ShopTransaction.processBuyShop(world, shop, serverPlayer);
        }
    }

    /**
     * Maneja la creación de un cartel [SHOP] para vincular cofre
     */
    private static void handleShopStorageSign(World world, BlockPos signPos, SignTileEntity sign,
                                              ServerPlayerEntity player) {
        // Buscar cofre debajo del cartel
        BlockPos chestPos = signPos.below();
        BlockState chestState = world.getBlockState(chestPos);

        if (!(chestState.getBlock() instanceof ChestBlock)) {
            player.sendMessage(
                    new StringTextComponent("Debes colocar el cartel [SHOP] encima de un cofre!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(signPos, true);
            return;
        }

        // Verificar si ya está vinculado
        if (ShopManager.isLinkedChest(chestPos)) {
            player.sendMessage(
                    new StringTextComponent("Este cofre ya está vinculado!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(signPos, true);
            return;
        }

        // Crear cofre vinculado
        LinkedChest linkedChest = new LinkedChest(
                chestPos,
                player.getUUID(),
                player.getName().getString(),
                signPos
        );

        ShopManager.registerLinkedChest(linkedChest);

        // Actualizar cartel con nombre del jugador
        sign.setMessage(0, new StringTextComponent("[SHOP]").withStyle(TextFormatting.GREEN));
        sign.setMessage(1, new StringTextComponent(player.getName().getString()).withStyle(TextFormatting.DARK_GREEN));
        sign.setMessage(2, new StringTextComponent("Vinculado").withStyle(TextFormatting.GREEN));
        sign.setChanged();
        world.sendBlockUpdated(signPos, world.getBlockState(signPos), world.getBlockState(signPos), 3);

        player.sendMessage(
                new StringTextComponent("Cofre vinculado exitosamente a tus tiendas!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );
    }

    /**
     * Maneja la creación de un cartel [SELL]
     */
    private static void handleSellShopSign(World world, BlockPos pos, SignTileEntity sign,
                                           ServerPlayerEntity player,
                                           String itemId, String quantityStr, String priceStr) {
        // Validar formato
        if (itemId.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("Formato incorrecto! Usa: [SELL] / item / cantidad / precio")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        // Parsear cantidad y precio
        int quantity, price;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            player.sendMessage(
                    new StringTextComponent("Cantidad y precio deben ser numeros!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        // Validar valores
        if (quantity <= 0 || price <= 0) {
            player.sendMessage(
                    new StringTextComponent("Cantidad y precio deben ser mayores a 0!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        // Crear tienda
        ShopSign shop = new ShopSign(
                pos,
                player.getUUID(),
                player.getName().getString(),
                ShopSign.ShopType.SELL,
                itemId,
                quantity,
                price
        );

        // Validar que el item existe
        if (!shop.isValidItem()) {
            player.sendMessage(
                    new StringTextComponent("Item no valido: " + itemId)
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        // Registrar tienda
        ShopManager.registerShop(shop);

        // Actualizar cartel con colores
        sign.setMessage(0, new StringTextComponent("[SELL]").withStyle(TextFormatting.BLUE));
        sign.setMessage(1, shop.createItemStack().getHoverName().copy().withStyle(TextFormatting.YELLOW));
        sign.setMessage(2, new StringTextComponent("x" + quantity).withStyle(TextFormatting.WHITE));
        sign.setMessage(3, new StringTextComponent(price + " JC").withStyle(TextFormatting.GOLD));
        sign.setChanged();
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);

        player.sendMessage(
                new StringTextComponent("Tienda de venta creada exitosamente!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );
    }

    /**
     * Maneja la creación de un cartel [BUY]
     */
    private static void handleBuyShopSign(World world, BlockPos pos, SignTileEntity sign,
                                          ServerPlayerEntity player,
                                          String itemId, String quantityStr, String priceStr) {
        // Validar formato (igual que SELL)
        if (itemId.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            player.sendMessage(
                    new StringTextComponent("Formato incorrecto! Usa: [BUY] / item / cantidad / precio")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        int quantity, price;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            player.sendMessage(
                    new StringTextComponent("Cantidad y precio deben ser numeros!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        if (quantity <= 0 || price <= 0) {
            player.sendMessage(
                    new StringTextComponent("Cantidad y precio deben ser mayores a 0!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        // Crear tienda
        ShopSign shop = new ShopSign(
                pos,
                player.getUUID(),
                player.getName().getString(),
                ShopSign.ShopType.BUY,
                itemId,
                quantity,
                price
        );

        if (!shop.isValidItem()) {
            player.sendMessage(
                    new StringTextComponent("Item no valido: " + itemId)
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            world.destroyBlock(pos, true);
            return;
        }

        ShopManager.registerShop(shop);

        // Actualizar cartel
        sign.setMessage(0, new StringTextComponent("[BUY]").withStyle(TextFormatting.DARK_GREEN));
        sign.setMessage(1, shop.createItemStack().getHoverName().copy().withStyle(TextFormatting.YELLOW));
        sign.setMessage(2, new StringTextComponent("x" + quantity).withStyle(TextFormatting.WHITE));
        sign.setMessage(3, new StringTextComponent(price + " JC").withStyle(TextFormatting.GOLD));
        sign.setChanged();
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);

        player.sendMessage(
                new StringTextComponent("Tienda de compra creada exitosamente!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );
    }

    /**
     * Protege contra romper carteles de tienda y cofres vinculados
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();

        // Proteger carteles de tienda
        if (ShopManager.isShop(pos)) {
            ShopSign shop = ShopManager.getShop(pos);
            if (shop != null && !shop.isOwner(player.getUUID())) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No puedes romper la tienda de otro jugador!")
                                    .withStyle(TextFormatting.RED),
                            player.getUUID()
                    );
                }
                return;
            }
            // Si es el dueño, remover del sistema
            ShopManager.removeShop(pos);
        }

        // Proteger carteles [SHOP] de cofres vinculados
        BlockPos chestBelow = pos.below();
        if (ShopManager.isLinkedChest(chestBelow)) {
            LinkedChest chest = ShopManager.getLinkedChest(chestBelow);
            if (chest != null && chest.getSignPosition().equals(pos)) {
                if (!chest.isOwner(player.getUUID())) {
                    event.setCanceled(true);
                    if (player instanceof ServerPlayerEntity) {
                        player.sendMessage(
                                new StringTextComponent("No puedes romper el cartel [SHOP] de otro jugador!")
                                        .withStyle(TextFormatting.RED),
                                player.getUUID()
                        );
                    }
                    return;
                }
                // Si es el dueño, desvincular el cofre
                ShopManager.removeLinkedChest(chestBelow);
            }
        }

        // Proteger cofres vinculados
        if (ShopManager.isLinkedChest(pos)) {
            LinkedChest chest = ShopManager.getLinkedChest(pos);
            if (chest != null && !chest.isOwner(player.getUUID())) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No puedes romper el cofre de otro jugador!")
                                    .withStyle(TextFormatting.RED),
                            player.getUUID()
                    );
                }
                return;
            }
            // Si es el dueño, verificar si hay cartel encima
            BlockPos signPos = chest.getSignPosition();
            if (event.getWorld().getBlockState(signPos).getBlock() instanceof StandingSignBlock ||
                    event.getWorld().getBlockState(signPos).getBlock() instanceof WallSignBlock) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("Primero rompe el cartel [SHOP] encima del cofre!")
                                    .withStyle(TextFormatting.RED),
                            player.getUUID()
                    );
                }
                return;
            }
            // Si no hay cartel, permitir romper y desvincular
            ShopManager.removeLinkedChest(pos);
        }
    }

    /**
     * Detecta cuando un jugador hace click derecho en un cartel de tienda
     */
    @SubscribeEvent
    public static void onSignClick(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        // Verificar si es un cartel
        if (!(state.getBlock() instanceof StandingSignBlock || state.getBlock() instanceof WallSignBlock)) {
            return;
        }

        // Verificar si es una tienda registrada
        if (!ShopManager.isShop(pos)) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        ShopSign shop = ShopManager.getShop(pos);

        if (shop == null) {
            return;
        }

        // Cancelar el evento para que no abra la GUI de edición del cartel
        event.setCanceled(true);

        // Procesar según el tipo de tienda
        if (shop.getType() == ShopSign.ShopType.SELL) {
            // Tienda de venta - el jugador compra
            ShopTransaction.processSellShop(world, shop, serverPlayer);
        } else if (shop.getType() == ShopSign.ShopType.BUY) {
            // Tienda de compra - el jugador vende
            ShopTransaction.processBuyShop(world, shop, serverPlayer);
        }
    }

    /**
     * Protege contra abrir cofres vinculados de otros jugadores
     */
    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        // Verificar si es un cofre
        if (!(state.getBlock() instanceof ChestBlock)) {
            return;
        }

        // Verificar si es un cofre vinculado
        if (!ShopManager.isLinkedChest(pos)) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        LinkedChest chest = ShopManager.getLinkedChest(pos);

        if (chest != null && !chest.isOwner(player.getUUID())) {
            event.setCanceled(true);
            if (player instanceof ServerPlayerEntity) {
                player.sendMessage(
                        new StringTextComponent("Este cofre esta vinculado a las tiendas de " + chest.getOwnerName() + "!")
                                .withStyle(TextFormatting.RED),
                        player.getUUID()
                );
            }
        }
    }
}