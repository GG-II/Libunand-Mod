package com.abfann.libunand.protection;

import com.abfann.libunand.LibunandMod;
import com.google.gson.annotations.Expose;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.abfann.libunand.protection.PermissionManager;
import com.abfann.libunand.protection.PlotPermission;
import com.abfann.libunand.protection.PlotRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Plot {

    @Expose
    private String name;

    @Expose
    private UUID worldUUID;

    @Expose
    private String worldName;

    @Expose
    private BlockPos corner1;

    @Expose
    private BlockPos corner2;

    @Expose
    private UUID owner;

    @Expose
    private String ownerName;

    @Expose
    private int price;

    @Expose
    private boolean forSale;

    @Expose
    private long createdTime;

    @Expose
    private List<UUID> trustedPlayers;

    @Expose
    private List<UUID> coOwners;

    // Constructor para nuevos lotes
    public Plot(String name, World world, BlockPos corner1, BlockPos corner2, int price) {
        this.name = name;
        this.worldUUID = UUID.nameUUIDFromBytes(("libunand_" + world.dimension().location().toString()).getBytes());
        this.worldName = world.dimension().location().toString();
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.price = price;
        this.forSale = true;
        this.owner = null; // Sin dueño hasta que se compre
        this.ownerName = null;
        this.createdTime = System.currentTimeMillis();
        this.trustedPlayers = new ArrayList<>();
        this.coOwners = new ArrayList<>();
    }

    // Constructor vacío para Gson
    public Plot() {
        this.trustedPlayers = new ArrayList<>();
        this.coOwners = new ArrayList<>();
    }

    /**
     * Verifica si una posición está dentro del lote
     */
    public boolean contains(BlockPos pos) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());

        boolean inBounds = pos.getX() >= minX && pos.getX() <= maxX &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ;

        // Debug
        if (inBounds) {
            LibunandMod.LOGGER.debug("Posición {} está dentro del lote (X: {}-{}, Z: {}-{})",
                    pos, minX, maxX, minZ, maxZ);
        }

        return inBounds;
    }

    /**
     * Verifica si un jugador tiene permisos en este lote
     */
    public boolean hasPermission(UUID playerUUID) {
        if (owner == null) return false; // Lote sin dueño
        if (owner.equals(playerUUID)) return true; // Es el dueño
        if (coOwners.contains(playerUUID)) return true; // Es co-dueño
        if (trustedPlayers.contains(playerUUID)) return true; // Es trusted
        return false;
    }

    /**
     * Compra el lote para un jugador
     */
    public void purchaseBy(UUID playerUUID, String playerName) {
        this.owner = playerUUID;
        this.ownerName = playerName;
        this.forSale = false;
    }

    /**
     * Añade un jugador como trusted
     */
    public void addTrustedPlayer(UUID playerUUID) {
        if (!trustedPlayers.contains(playerUUID)) {
            trustedPlayers.add(playerUUID);
        }
    }

    /**
     * Remueve un jugador de trusted
     */
    public void removeTrustedPlayer(UUID playerUUID) {
        trustedPlayers.remove(playerUUID);
    }

    /**
     * Añade un co-dueño
     */
    public void addCoOwner(UUID playerUUID) {
        if (!coOwners.contains(playerUUID)) {
            coOwners.add(playerUUID);
        }
    }

    /**
     * Calcula el área del lote
     */
    public int getArea() {
        int width = Math.abs(corner2.getX() - corner1.getX()) + 1;
        int length = Math.abs(corner2.getZ() - corner1.getZ()) + 1;
        return width * length;
    }

    /**
     * Obtiene el centro del lote
     */
    public BlockPos getCenter() {
        int centerX = (corner1.getX() + corner2.getX()) / 2;
        int centerZ = (corner1.getZ() + corner2.getZ()) / 2;
        // Encontrar la superficie (Y más alta no-aire)
        int centerY = Math.max(corner1.getY(), corner2.getY());
        return new BlockPos(centerX, centerY, centerZ);
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getWorldUUID() { return worldUUID; }
    public String getWorldName() { return worldName; }

    public BlockPos getCorner1() { return corner1; }
    public void setCorner1(BlockPos corner1) { this.corner1 = corner1; }

    public BlockPos getCorner2() { return corner2; }
    public void setCorner2(BlockPos corner2) { this.corner2 = corner2; }

    public UUID getOwner() { return owner; }
    public String getOwnerName() { return ownerName; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isForSale() { return forSale; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }

    public long getCreatedTime() { return createdTime; }

    public List<UUID> getTrustedPlayers() { return new ArrayList<>(trustedPlayers); }
    public List<UUID> getCoOwners() { return new ArrayList<>(coOwners); }

    @Override
    public String toString() {
        return String.format("Plot{name='%s', owner='%s', price=%d, forSale=%s, area=%d}",
                name, ownerName, price, forSale, getArea());
    }

    // Agregar al final de la clase Plot, antes del último }

    /**
     * Remueve un co-dueño
     */
    public void removeCoOwner(UUID playerUUID) {
        coOwners.remove(playerUUID);
    }

    /**
     * Verifica si un jugador tiene permisos específicos usando el nuevo sistema
     */
    public boolean hasPermission(UUID playerUUID, PlotPermission permission) {
        return PermissionManager.hasPermission(this, playerUUID, permission);
    }

    /**
     * Obtiene el rol de un jugador
     */
    public PlotRole getPlayerRole(UUID playerUUID) {
        return PermissionManager.getPlayerRole(this, playerUUID);
    }

    /**
     * Obtiene todos los miembros del lote con sus roles
     */
    public String getMembersInfo() {
        StringBuilder info = new StringBuilder();

        if (owner != null) {
            info.append("Propietario: ").append(ownerName).append("\n");
        }

        if (!coOwners.isEmpty()) {
            info.append("Co-propietarios: ").append(coOwners.size()).append("\n");
        }

        if (!trustedPlayers.isEmpty()) {
            info.append("Jugadores de confianza: ").append(trustedPlayers.size()).append("\n");
        }

        return info.toString();
    }
}