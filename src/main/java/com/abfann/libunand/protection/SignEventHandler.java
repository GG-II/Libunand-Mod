package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.data.IPlayerEconomy;
import com.abfann.libunand.data.PlayerDataHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class SignEventHandler {

    private static final String SALE_SIGN_TEXT = "[VENTA]";

    /**
     * Maneja cuando se coloca un cartel
     */
    @SubscribeEvent
    public static void onSignPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;

        BlockState state = event.getPlacedBlock();
        if (!(state.getBlock() instanceof StandingSignBlock) && !(state.getBlock() instanceof WallSignBlock)) {
            return;
        }

        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

        // Verificar si está en un lote
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(world, pos);
        if (plotOpt.isPresent()) {
            Plot plot = plotOpt.get();

            // Solo permitir carteles de venta en lotes que están en venta
            if (!plot.isForSale()) {
                player.sendMessage(
                        new StringTextComponent("Los carteles de venta solo se pueden colocar en lotes que estan en venta.")
                                .withStyle(TextFormatting.RED),
                        player.getUUID()
                );
                event.setCanceled(true);
                return;
            }

            // Informar al jugador sobre cómo crear cartel de venta
            player.sendMessage(
                    new StringTextComponent("Cartel colocado! Escribe ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent("[VENTA]").withStyle(TextFormatting.YELLOW))
                            .append(new StringTextComponent(" en la primera linea para crear un cartel de venta.").withStyle(TextFormatting.GREEN)),
                    player.getUUID()
            );
        }
    }

    /**
     * Maneja cuando se actualiza el texto de un cartel
     */
    @SubscribeEvent
    public static void onSignChange(net.minecraftforge.event.entity.player.PlayerSetSpawnEvent event) {
        // Nota: Este evento no existe directamente, necesitamos usar otro approach
        // Lo manejaremos en el interaction event
    }

    /**
     * Maneja cuando se hace click derecho en un cartel
     */
    @SubscribeEvent
    public static void onSignInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        if (event.getHand() != Hand.MAIN_HAND) return;

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof StandingSignBlock) && !(state.getBlock() instanceof WallSignBlock)) {
            return;
        }

        TileEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof SignTileEntity)) {
            return;
        }

        SignTileEntity signTile = (SignTileEntity) tileEntity;
        String firstLine = signTile.getMessage(0).getString().trim();

        // Verificar si es un cartel de venta
        if (!firstLine.equalsIgnoreCase(SALE_SIGN_TEXT)) {
            return;
        }

        // Buscar el lote
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(world, pos);
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Este cartel no esta en un lote valido.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return;
        }

        Plot plot = plotOpt.get();

        if (!plot.isForSale()) {
            player.sendMessage(
                    new StringTextComponent("Este lote ya no esta en venta.")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return;
        }

        // Verificar que el jugador pueda comprar
        IPlayerEconomy economy = PlayerDataHandler.getPlayerEconomy(player);

        if (!economy.hasBalance(plot.getPrice())) {
            player.sendMessage(
                    new StringTextComponent("No tienes suficientes JoJoCoins! Necesitas: ")
                            .withStyle(TextFormatting.RED)
                            .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD))
                            .append(new StringTextComponent(", tienes: ").withStyle(TextFormatting.RED))
                            .append(new StringTextComponent(economy.getBalance() + " JoJoCoins").withStyle(TextFormatting.YELLOW)),
                    player.getUUID()
            );
            return;
        }

        // Realizar compra
        economy.removeBalance(plot.getPrice());
        plot.purchaseBy(player.getUUID(), player.getName().getString());
        PlotManager.getInstance().savePlots();

        // Actualizar el cartel
        updateSaleSign(signTile, plot, true);

        player.sendMessage(
                new StringTextComponent("Felicidades! Compraste el lote '")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                        .append(new StringTextComponent("' por ").withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        LibunandMod.LOGGER.info("Jugador {} compro el lote '{}' por {} JoJoCoins via cartel",
                player.getName().getString(), plot.getName(), plot.getPrice());

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
    }

    /**
     * Actualiza un cartel de venta con información del lote
     */
    public static void updateSaleSign(SignTileEntity signTile, Plot plot, boolean justPurchased) {
        if (justPurchased) {
            // Cartel después de compra
            signTile.setMessage(0, new StringTextComponent("[VENDIDO]").withStyle(TextFormatting.GREEN));
            signTile.setMessage(1, new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW));
            signTile.setMessage(2, new StringTextComponent("Propietario:").withStyle(TextFormatting.GRAY));
            signTile.setMessage(3, new StringTextComponent(plot.getOwnerName()).withStyle(TextFormatting.WHITE));
        } else if (plot.isForSale()) {
            // Cartel de venta activo
            signTile.setMessage(0, new StringTextComponent(SALE_SIGN_TEXT).withStyle(TextFormatting.GOLD));
            signTile.setMessage(1, new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW));
            signTile.setMessage(2, new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GREEN));
            signTile.setMessage(3, new StringTextComponent(plot.getArea() + " bloques").withStyle(TextFormatting.GRAY));
        }

        signTile.setChanged();

        // Sincronizar con el cliente
        if (signTile.getLevel() != null) {
            signTile.getLevel().sendBlockUpdated(signTile.getBlockPos(),
                    signTile.getLevel().getBlockState(signTile.getBlockPos()),
                    signTile.getLevel().getBlockState(signTile.getBlockPos()), 3);
        }
    }

    /**
     * Busca y actualiza todos los carteles de venta en un lote
     */
    public static void updateAllSaleSignsInPlot(Plot plot, World world) {
        if (plot == null || world == null) return;

        int minX = Math.min(plot.getCorner1().getX(), plot.getCorner2().getX());
        int maxX = Math.max(plot.getCorner1().getX(), plot.getCorner2().getX());
        int minY = Math.min(plot.getCorner1().getY(), plot.getCorner2().getY());
        int maxY = Math.max(plot.getCorner1().getY(), plot.getCorner2().getY());
        int minZ = Math.min(plot.getCorner1().getZ(), plot.getCorner2().getZ());
        int maxZ = Math.max(plot.getCorner1().getZ(), plot.getCorner2().getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (state.getBlock() instanceof StandingSignBlock || state.getBlock() instanceof WallSignBlock) {
                        TileEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity instanceof SignTileEntity) {
                            SignTileEntity signTile = (SignTileEntity) tileEntity;
                            String firstLine = signTile.getMessage(0).getString().trim();

                            if (firstLine.equalsIgnoreCase(SALE_SIGN_TEXT) || firstLine.equalsIgnoreCase("[VENDIDO]")) {
                                updateSaleSign(signTile, plot, !plot.isForSale());
                            }
                        }
                    }
                }
            }
        }
    }
    // Agregar al final de la clase SignEventHandler, antes del último }

    /**
     * Detecta automáticamente cuando se escribe [VENTA] en un cartel y lo actualiza
     */
    public static void checkAndUpdateSaleSign(World world, BlockPos pos) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof SignTileEntity)) {
            return;
        }

        SignTileEntity signTile = (SignTileEntity) tileEntity;
        String firstLine = signTile.getMessage(0).getString().trim();

        if (firstLine.equalsIgnoreCase(SALE_SIGN_TEXT)) {
            Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(world, pos);
            if (plotOpt.isPresent()) {
                Plot plot = plotOpt.get();
                if (plot.isForSale()) {
                    updateSaleSign(signTile, plot, false);
                    LibunandMod.LOGGER.info("Auto-actualizado cartel de venta en lote '{}'", plot.getName());
                }
            }
        }
    }
}