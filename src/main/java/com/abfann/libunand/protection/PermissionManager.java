package com.abfann.libunand.protection;

import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class PermissionManager {

    /**
     * Verifica si un jugador tiene un permiso específico en un lote
     */
    public static boolean hasPermission(Plot plot, UUID playerUUID, PlotPermission permission) {
        if (plot == null || playerUUID == null) return false;

        PlotRole role = getPlayerRole(plot, playerUUID);
        return role.hasPermission(permission);
    }

    /**
     * Obtiene el rol de un jugador en un lote específico
     */
    public static PlotRole getPlayerRole(Plot plot, UUID playerUUID) {
        if (plot == null || playerUUID == null) {
            return PlotRole.VISITOR;
        }

        // Si el lote está en venta, nadie tiene permisos especiales
        if (plot.isForSale()) {
            return PlotRole.VISITOR;
        }

        // Verificar si es el dueño original
        if (plot.getOwner() != null && plot.getOwner().equals(playerUUID)) {
            return PlotRole.OWNER;
        }

        // Verificar si es co-dueño
        if (plot.getCoOwners().contains(playerUUID)) {
            return PlotRole.CO_OWNER;
        }

        // Verificar si es trusted
        if (plot.getTrustedPlayers().contains(playerUUID)) {
            return PlotRole.TRUSTED;
        }

        // Por defecto es visitante
        return PlotRole.VISITOR;
    }

    /**
     * Verifica si un jugador puede administrar un lote (dar/quitar permisos)
     */
    public static boolean canAdministrate(Plot plot, UUID playerUUID) {
        return hasPermission(plot, playerUUID, PlotPermission.ADMIN);
    }

    /**
     * Verifica si un jugador puede construir en un lote
     */
    public static boolean canBuild(Plot plot, UUID playerUUID) {
        return hasPermission(plot, playerUUID, PlotPermission.BUILD);
    }

    /**
     * Verifica si un jugador puede interactuar en un lote
     */
    public static boolean canInteract(Plot plot, UUID playerUUID) {
        return hasPermission(plot, playerUUID, PlotPermission.INTERACT);
    }

    /**
     * Verifica si un jugador puede invitar otros al lote
     */
    public static boolean canInvite(Plot plot, UUID playerUUID) {
        return hasPermission(plot, playerUUID, PlotPermission.INVITE);
    }

    /**
     * Añade un jugador como trusted a un lote
     */
    public static boolean addTrustedPlayer(Plot plot, UUID adminUUID, UUID targetUUID) {
        if (!canAdministrate(plot, adminUUID)) {
            return false;
        }

        // No se puede añadir al dueño o co-dueños como trusted
        if (plot.getOwner().equals(targetUUID) || plot.getCoOwners().contains(targetUUID)) {
            return false;
        }

        plot.addTrustedPlayer(targetUUID);
        return true;
    }

    /**
     * Remueve un jugador de trusted en un lote
     */
    public static boolean removeTrustedPlayer(Plot plot, UUID adminUUID, UUID targetUUID) {
        if (!canAdministrate(plot, adminUUID)) {
            return false;
        }

        plot.removeTrustedPlayer(targetUUID);
        return true;
    }

    /**
     * Añade un co-dueño a un lote
     */
    public static boolean addCoOwner(Plot plot, UUID ownerUUID, UUID targetUUID) {
        // Solo el dueño original puede añadir co-dueños
        if (plot.getOwner() == null || !plot.getOwner().equals(ownerUUID)) {
            return false;
        }

        // No se puede añadir a sí mismo
        if (ownerUUID.equals(targetUUID)) {
            return false;
        }

        // Remover de trusted si está ahí
        plot.removeTrustedPlayer(targetUUID);

        // Añadir como co-dueño
        plot.addCoOwner(targetUUID);
        return true;
    }

    /**
     * Remueve un co-dueño de un lote
     */
    public static boolean removeCoOwner(Plot plot, UUID ownerUUID, UUID targetUUID) {
        // Solo el dueño original puede remover co-dueños
        if (plot.getOwner() == null || !plot.getOwner().equals(ownerUUID)) {
            return false;
        }

        plot.removeCoOwner(targetUUID);
        return true;
    }

    /**
     * Obtiene una descripción del rol de un jugador
     */
    public static String getRoleDescription(Plot plot, UUID playerUUID) {
        PlotRole role = getPlayerRole(plot, playerUUID);

        if (role == PlotRole.OWNER) {
            return "Propietario";
        } else if (role == PlotRole.CO_OWNER) {
            return "Co-propietario";
        } else if (role == PlotRole.TRUSTED) {
            return "Confianza";
        } else {
            return "Visitante";
        }
    }

    /**
     * Obtiene información detallada de permisos de un jugador
     */
    public static String getPermissionInfo(Plot plot, UUID playerUUID) {
        PlotRole role = getPlayerRole(plot, playerUUID);
        StringBuilder info = new StringBuilder();

        info.append("Rol: ").append(role.getDisplayName()).append("\n");
        info.append("Permisos: ");

        if (role.getPermissions().isEmpty()) {
            info.append("Ninguno");
        } else {
            for (int i = 0; i < role.getPermissions().size(); i++) {
                if (i > 0) info.append(", ");
                info.append(role.getPermissions().get(i).getDescription());
            }
        }

        return info.toString();
    }
}