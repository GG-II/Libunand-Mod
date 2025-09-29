package com.abfann.libunand.shops;

import net.minecraft.util.math.BlockPos;
import java.util.UUID;

/**
 * Representa un cofre vinculado a las tiendas de un jugador
 */
public class LinkedChest {

    private final BlockPos position;
    private final UUID ownerUUID;
    private final String ownerName;
    private final BlockPos signPosition; // Posición del cartel [SHOP]

    public LinkedChest(BlockPos position, UUID ownerUUID, String ownerName, BlockPos signPosition) {
        this.position = position;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.signPosition = signPosition;
    }

    public BlockPos getPosition() { return position; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public BlockPos getSignPosition() { return signPosition; }

    public boolean isOwner(UUID playerUUID) {
        return this.ownerUUID.equals(playerUUID);
    }

    @Override
    public String toString() {
        return String.format("LinkedChest[%s] at %s", ownerName, position);
    }
}