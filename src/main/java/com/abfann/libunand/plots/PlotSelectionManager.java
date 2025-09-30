package com.abfann.libunand.plots;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maneja las selecciones temporales de los jugadores
 */
public class PlotSelectionManager {

    private static final Map<UUID, BlockPos> position1 = new HashMap<>();
    private static final Map<UUID, BlockPos> position2 = new HashMap<>();

    public static void setPosition1(UUID playerUUID, BlockPos pos) {
        position1.put(playerUUID, pos);
    }

    public static void setPosition2(UUID playerUUID, BlockPos pos) {
        position2.put(playerUUID, pos);
    }

    public static BlockPos getPosition1(UUID playerUUID) {
        return position1.get(playerUUID);
    }

    public static BlockPos getPosition2(UUID playerUUID) {
        return position2.get(playerUUID);
    }

    public static boolean hasCompleteSelection(UUID playerUUID) {
        return position1.containsKey(playerUUID) && position2.containsKey(playerUUID);
    }

    public static void clearSelection(UUID playerUUID) {
        position1.remove(playerUUID);
        position2.remove(playerUUID);
    }

    /**
     * Calcula el volumen del area seleccionada
     */
    public static int calculateVolume(BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;

        return width * length;
    }

    /**
     * Obtiene las coordenadas normalizadas (min/max)
     */
    public static PlotArea getPlotArea(UUID playerUUID) {
        if (!hasCompleteSelection(playerUUID)) {
            return null;
        }

        BlockPos pos1 = position1.get(playerUUID);
        BlockPos pos2 = position2.get(playerUUID);

        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Y siempre desde bedrock hasta altura maxima
        return new PlotArea(minX, 0, minZ, maxX, 256, maxZ);
    }

    /**
     * Clase para representar un area de lote
     */
    public static class PlotArea {
        public final int minX, minY, minZ;
        public final int maxX, maxY, maxZ;

        public PlotArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public boolean contains(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX &&
                    pos.getY() >= minY && pos.getY() <= maxY &&
                    pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d,%d) to (%d,%d,%d)",
                    minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}