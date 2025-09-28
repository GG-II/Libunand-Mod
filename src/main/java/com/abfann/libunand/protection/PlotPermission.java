package com.abfann.libunand.protection;

public enum PlotPermission {

    /**
     * Permiso para construir/destruir bloques
     */
    BUILD("build", "Construir y destruir bloques"),

    /**
     * Permiso para interactuar con bloques (botones, palancas, etc.)
     */
    INTERACT("interact", "Interactuar con bloques"),

    /**
     * Permiso para administrar el lote (dar permisos, cambiar configuraciones)
     */
    ADMIN("admin", "Administrar el lote"),

    /**
     * Permiso para invitar otros jugadores
     */
    INVITE("invite", "Invitar jugadores al lote");

    private final String name;
    private final String description;

    PlotPermission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Busca un permiso por nombre
     */
    public static PlotPermission fromName(String name) {
        for (PlotPermission permission : values()) {
            if (permission.name.equalsIgnoreCase(name)) {
                return permission;
            }
        }
        return null;
    }
}