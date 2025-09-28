package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.SelectionTool;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.abfann.libunand.protection.PermissionManager;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class ProtectionEventHandler {

    /**
     * Maneja la protección contra destrucción de bloques
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return; // Solo en servidor

        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();

        // Verificar si la posición está en un lote protegido
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(world, pos);
        if (plotOpt.isPresent()) {
            Plot plot = plotOpt.get();

            // Si el lote está en venta, nadie puede modificarlo
            if (plot.isForSale()) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No puedes romper bloques en el lote '")
                                    .withStyle(TextFormatting.RED)
                                    .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                                    .append(new StringTextComponent("' (en venta)").withStyle(TextFormatting.RED)),
                            player.getUUID()
                    );
                }
                return;
            }

            // Verificar permisos del jugador
            if (!PermissionManager.canBuild(plot, player.getUUID())) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No tienes permisos para romper bloques en el lote '")
                                    .withStyle(TextFormatting.RED)
                                    .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                                    .append(new StringTextComponent("' de ").withStyle(TextFormatting.RED))
                                    .append(new StringTextComponent(plot.getOwnerName()).withStyle(TextFormatting.WHITE)),
                            player.getUUID()
                    );
                }

                LibunandMod.LOGGER.debug("Jugador {} intento romper bloque en lote protegido '{}' en {}",
                        player.getName().getString(), plot.getName(), pos);
            }
        }
    }

    /**
     * Maneja la protección contra colocación de bloques
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isClientSide()) return; // Solo en servidor
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        PlayerEntity player = (PlayerEntity) event.getEntity();

        // Verificar si la posición está en un lote protegido
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotAt(world, pos);
        if (plotOpt.isPresent()) {
            Plot plot = plotOpt.get();

            // Si el lote está en venta, nadie puede modificarlo
            if (plot.isForSale()) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No puedes colocar bloques en el lote '")
                                    .withStyle(TextFormatting.RED)
                                    .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                                    .append(new StringTextComponent("' (en venta)").withStyle(TextFormatting.RED)),
                            player.getUUID()
                    );
                }
                return;
            }

            // Verificar permisos del jugador
            if (!PermissionManager.canBuild(plot, player.getUUID())) {
                event.setCanceled(true);
                if (player instanceof ServerPlayerEntity) {
                    player.sendMessage(
                            new StringTextComponent("No tienes permisos para colocar bloques en el lote '")
                                    .withStyle(TextFormatting.RED)
                                    .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.YELLOW))
                                    .append(new StringTextComponent("' de ").withStyle(TextFormatting.RED))
                                    .append(new StringTextComponent(plot.getOwnerName()).withStyle(TextFormatting.WHITE)),
                            player.getUUID()
                    );
                }

                LibunandMod.LOGGER.debug("Jugador {} intento colocar bloque en lote protegido '{}' en {}",
                        player.getName().getString(), plot.getName(), pos);
            }
        }
    }

    /**
     * Maneja la selección de área con hacha dorada
     */
    @SubscribeEvent
    public static void onBlockClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isClientSide()) return; // Solo en servidor

        PlayerEntity player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        // Verificar si está usando la herramienta de selección
        if (SelectionTool.isSelectionTool(heldItem)) {
            event.setCanceled(true); // Evitar romper el bloque
            event.setCancellationResult(ActionResultType.SUCCESS);

            BlockPos pos = event.getPos();
            SelectionTool.setPosition1(player, pos);
        }
    }

    /**
     * Maneja la selección de área con hacha dorada (click derecho)
     */
    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return; // Solo en servidor

        PlayerEntity player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        // Verificar si está usando la herramienta de selección
        if (SelectionTool.isSelectionTool(heldItem)) {
            event.setCanceled(true); // Evitar interacciones normales
            event.setCancellationResult(ActionResultType.SUCCESS);

            BlockPos pos = event.getPos();
            SelectionTool.setPosition2(player, pos);
        }
    }
}