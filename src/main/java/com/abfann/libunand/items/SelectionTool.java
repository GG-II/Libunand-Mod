package com.abfann.libunand.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SelectionTool {

    private static final String POS1_KEY = "libunand_pos1_x";
    private static final String POS1_Y_KEY = "libunand_pos1_y";
    private static final String POS1_Z_KEY = "libunand_pos1_z";
    private static final String POS2_KEY = "libunand_pos2_x";
    private static final String POS2_Y_KEY = "libunand_pos2_y";
    private static final String POS2_Z_KEY = "libunand_pos2_z";

    /**
     * Verifica si un item es una herramienta de selección válida
     */
    public static boolean isSelectionTool(ItemStack stack) {
        return stack.getItem() == Items.GOLDEN_AXE;
    }

    /**
     * Establece la primera posición
     */
    public static void setPosition1(PlayerEntity player, BlockPos pos) {
        ItemStack axe = findGoldenAxe(player);
        if (axe != null) {
            CompoundNBT nbt = axe.getOrCreateTag();
            nbt.putInt(POS1_KEY, pos.getX());
            nbt.putInt(POS1_Y_KEY, pos.getY());
            nbt.putInt(POS1_Z_KEY, pos.getZ());

            player.sendMessage(
                    new StringTextComponent("Posicion 1 establecida: ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                    .withStyle(TextFormatting.YELLOW)),
                    player.getUUID()
            );
        }
    }

    /**
     * Establece la segunda posición
     */
    public static void setPosition2(PlayerEntity player, BlockPos pos) {
        ItemStack axe = findGoldenAxe(player);
        if (axe != null) {
            CompoundNBT nbt = axe.getOrCreateTag();
            nbt.putInt(POS2_KEY, pos.getX());
            nbt.putInt(POS2_Y_KEY, pos.getY());
            nbt.putInt(POS2_Z_KEY, pos.getZ());

            player.sendMessage(
                    new StringTextComponent("Posicion 2 establecida: ")
                            .withStyle(TextFormatting.GREEN)
                            .append(new StringTextComponent("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                    .withStyle(TextFormatting.YELLOW)),
                    player.getUUID()
            );

            // Mostrar información del área seleccionada si ambas posiciones están establecidas
            if (hasPosition1(axe) && hasPosition2(axe)) {
                BlockPos pos1 = getPosition1(axe);
                BlockPos pos2 = getPosition2(axe);
                int area = calculateArea(pos1, pos2);

                player.sendMessage(
                        new StringTextComponent("Area seleccionada: ")
                                .withStyle(TextFormatting.AQUA)
                                .append(new StringTextComponent(area + " bloques")
                                        .withStyle(TextFormatting.WHITE)),
                        player.getUUID()
                );
            }
        }
    }

    /**
     * Obtiene la primera posición
     */
    public static BlockPos getPosition1(ItemStack axe) {
        if (!hasPosition1(axe)) return null;
        CompoundNBT nbt = axe.getTag();
        return new BlockPos(
                nbt.getInt(POS1_KEY),
                nbt.getInt(POS1_Y_KEY),
                nbt.getInt(POS1_Z_KEY)
        );
    }

    /**
     * Obtiene la segunda posición
     */
    public static BlockPos getPosition2(ItemStack axe) {
        if (!hasPosition2(axe)) return null;
        CompoundNBT nbt = axe.getTag();
        return new BlockPos(
                nbt.getInt(POS2_KEY),
                nbt.getInt(POS2_Y_KEY),
                nbt.getInt(POS2_Z_KEY)
        );
    }

    /**
     * Verifica si tiene la primera posición establecida
     */
    public static boolean hasPosition1(ItemStack axe) {
        CompoundNBT nbt = axe.getTag();
        return nbt != null && nbt.contains(POS1_KEY);
    }

    /**
     * Verifica si tiene la segunda posición establecida
     */
    public static boolean hasPosition2(ItemStack axe) {
        CompoundNBT nbt = axe.getTag();
        return nbt != null && nbt.contains(POS2_KEY);
    }

    /**
     * Verifica si ambas posiciones están establecidas
     */
    public static boolean hasBothPositions(ItemStack axe) {
        return hasPosition1(axe) && hasPosition2(axe);
    }

    /**
     * Busca un hacha dorada en el inventario del jugador
     */
    private static ItemStack findGoldenAxe(PlayerEntity player) {
        // Verificar item en mano principal
        ItemStack mainHand = player.getMainHandItem();
        if (isSelectionTool(mainHand)) {
            return mainHand;
        }

        // Verificar item en mano secundaria
        ItemStack offHand = player.getOffhandItem();
        if (isSelectionTool(offHand)) {
            return offHand;
        }

        // Buscar en inventario
        for (ItemStack stack : player.inventory.items) {
            if (isSelectionTool(stack)) {
                return stack;
            }
        }

        return null;
    }

    /**
     * Calcula el área entre dos posiciones
     */
    private static int calculateArea(BlockPos pos1, BlockPos pos2) {
        int width = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int length = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return width * length;
    }

    /**
     * Limpia las posiciones almacenadas
     */
    public static void clearPositions(PlayerEntity player) {
        ItemStack axe = findGoldenAxe(player);
        if (axe != null && axe.hasTag()) {
            CompoundNBT nbt = axe.getTag();
            nbt.remove(POS1_KEY);
            nbt.remove(POS1_Y_KEY);
            nbt.remove(POS1_Z_KEY);
            nbt.remove(POS2_KEY);
            nbt.remove(POS2_Y_KEY);
            nbt.remove(POS2_Z_KEY);

            player.sendMessage(
                    new StringTextComponent("Seleccion limpiada")
                            .withStyle(TextFormatting.GRAY),
                    player.getUUID()
            );
        }
    }
}