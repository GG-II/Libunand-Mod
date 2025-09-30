package com.abfann.libunand.plots;

import com.abfann.libunand.LibunandMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Herramienta para seleccionar areas de lotes
 */
public class PlotSelectionTool extends Item {

    public PlotSelectionTool() {
        super(new Properties()
                .tab(ItemGroup.TAB_TOOLS)
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
        );
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getLevel().isClientSide) return ActionResultType.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResultType.FAIL;

        BlockPos pos = context.getClickedPos();

        // Click izquierdo = Posición 1, Click derecho = Posición 2
        // Como ItemUseContext solo detecta click derecho, usaremos NBT para alternar
        ItemStack stack = context.getItemInHand();
        boolean isPos1 = !stack.getOrCreateTag().getBoolean("lastWasPos1");

        if (isPos1) {
            PlotSelectionManager.setPosition1(player.getUUID(), pos);
            stack.getOrCreateTag().putBoolean("lastWasPos1", true);

            player.sendMessage(
                    new StringTextComponent("Posicion 1 establecida: " + formatPos(pos))
                            .withStyle(TextFormatting.AQUA),
                    player.getUUID()
            );
        } else {
            PlotSelectionManager.setPosition2(player.getUUID(), pos);
            stack.getOrCreateTag().putBoolean("lastWasPos1", false);

            player.sendMessage(
                    new StringTextComponent("Posicion 2 establecida: " + formatPos(pos))
                            .withStyle(TextFormatting.AQUA),
                    player.getUUID()
            );

            // Mostrar volumen seleccionado
            BlockPos pos1 = PlotSelectionManager.getPosition1(player.getUUID());
            if (pos1 != null) {
                int volume = PlotSelectionManager.calculateVolume(pos1, pos);
                player.sendMessage(
                        new StringTextComponent("Area seleccionada: " + volume + " bloques")
                                .withStyle(TextFormatting.GREEN),
                        player.getUUID()
                );
            }
        }

        return ActionResultType.SUCCESS;
    }

    private String formatPos(BlockPos pos) {
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }
}