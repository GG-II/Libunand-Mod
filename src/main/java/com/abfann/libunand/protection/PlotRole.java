package com.abfann.libunand.protection;

import java.util.Arrays;
import java.util.List;

public enum PlotRole {

    /**
     * Dueño original del lote - todos los permisos
     */
    OWNER("owner", "Propietario", Arrays.asList(
            PlotPermission.BUILD,
            PlotPermission.INTERACT,
            PlotPermission.ADMIN,
            PlotPermission.INVITE
    )),

    /**
     * Co-dueño del lote - casi todos los permisos excepto eliminar el lote
     */
    CO_OWNER("co-owner", "Co-propietario", Arrays.asList(
            PlotPermission.BUILD,
            PlotPermission.INTERACT,
            PlotPermission.ADMIN,
            PlotPermission.INVITE
    )),

    /**
     * Jugador de confianza - puede construir e interactuar
     */
    TRUSTED("trusted", "Confianza", Arrays.asList(
            PlotPermission.BUILD,
            PlotPermission.INTERACT
    )),

    /**
     * Visitante - solo puede caminar
     */
    VISITOR("visitor", "Visitante", Arrays.asList());

    private final String name;
    private final String displayName;
    private final List<PlotPermission> permissions;

    PlotRole(String name, String displayName, List<PlotPermission> permissions) {
        this.name = name;
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<PlotPermission> getPermissions() {
        return permissions;
    }

    /**
     * Verifica si este rol tiene un permiso específico
     */
    public boolean hasPermission(PlotPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * Busca un rol por nombre
     */
    public static PlotRole fromName(String name) {
        for (PlotRole role : values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}