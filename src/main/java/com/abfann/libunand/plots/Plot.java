package com.abfann.libunand.plots;

import net.minecraft.util.math.BlockPos;
import java.util.*;

/**
 * Representa un lote vendible
 */
public class Plot {

    private final String name;
    private final PlotSelectionManager.PlotArea area;
    private final int price;
    private UUID ownerUUID;
    private String ownerName;
    private final BlockPos signPos;
    private final Set<UUID> members;
    private final Set<UUID> coOwners;
    private final Map<UUID, PlotPermission> permissions;
    private boolean forSale;
    private int resalePrice;

    public enum PlotPermission {
        INTERACT,  // Usar puertas, cofres, botones
        BUILD      // Colocar/romper bloques
    }

    public Plot(String name, PlotSelectionManager.PlotArea area, int price, BlockPos signPos) {
        this.name = name;
        this.area = area;
        this.price = price;
        this.signPos = signPos;
        this.ownerUUID = null;
        this.ownerName = null;
        this.members = new HashSet<>();
        this.coOwners = new HashSet<>();
        this.permissions = new HashMap<>();
        this.forSale = true;
        this.resalePrice = price;
    }

    public String getName() { return name; }
    public PlotSelectionManager.PlotArea getArea() { return area; }
    public int getPrice() { return price; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public BlockPos getSignPos() { return signPos; }
    public boolean isForSale() { return forSale; }
    public int getResalePrice() { return resalePrice; }

    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        this.forSale = false;
    }

    public void setForResale(int price) {
        this.forSale = true;
        this.resalePrice = price;
    }

    public void cancelResale() {
        this.forSale = false;
    }

    public boolean isOwner(UUID uuid) {
        return uuid.equals(ownerUUID);
    }

    public boolean isCoOwner(UUID uuid) {
        return coOwners.contains(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean hasPermission(UUID uuid, PlotPermission permission) {
        if (isOwner(uuid) || isCoOwner(uuid)) return true;

        PlotPermission userPerm = permissions.get(uuid);
        if (userPerm == null) return false;

        if (permission == PlotPermission.INTERACT) {
            return true; // INTERACT o BUILD permite interactuar
        } else if (permission == PlotPermission.BUILD) {
            return userPerm == PlotPermission.BUILD;
        }

        return false;
    }

    public void addMember(UUID uuid, PlotPermission permission) {
        members.add(uuid);
        permissions.put(uuid, permission);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        permissions.remove(uuid);
    }

    public void addCoOwner(UUID uuid) {
        coOwners.add(uuid);
        members.remove(uuid);
        permissions.remove(uuid);
    }

    public void removeCoOwner(UUID uuid) {
        coOwners.remove(uuid);
    }

    public boolean contains(BlockPos pos) {
        return area.contains(pos);
    }

    @Override
    public String toString() {
        return String.format("Plot[%s] %s - %d JC (Owner: %s)",
                name, area.toString(), resalePrice, ownerName != null ? ownerName : "None");
    }
}