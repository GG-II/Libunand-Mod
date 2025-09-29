package com.abfann.libunand.shops;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class ParticleSpawner {

    private static final Random RANDOM = new Random();

    /**
     * Spawea partículas alrededor de un cofre vinculado
     */
    public static void spawnLinkedChestParticles(ServerWorld world, BlockPos pos) {
        // Spawear 2-3 partículas alrededor del cofre
        int particleCount = 2 + RANDOM.nextInt(2);

        for (int i = 0; i < particleCount; i++) {
            double offsetX = pos.getX() + 0.2 + RANDOM.nextDouble() * 0.6;
            double offsetY = pos.getY() + 0.5 + RANDOM.nextDouble() * 0.5;
            double offsetZ = pos.getZ() + 0.2 + RANDOM.nextDouble() * 0.6;

            // Partículas verdes (happy villager)
            world.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    offsetX, offsetY, offsetZ,
                    1, // cantidad
                    0.0, 0.1, 0.0, // velocidad
                    0.02 // spread
            );
        }
    }

    /**
     * Spawea partículas para todos los cofres vinculados
     */
    public static void spawnAllLinkedChestParticles(ServerWorld world) {
        for (LinkedChest chest : ShopManager.getAllLinkedChests()) {
            // Solo spawear si el cofre está en un chunk cargado
            if (world.isLoaded(chest.getPosition())) {
                spawnLinkedChestParticles(world, chest.getPosition());
            }
        }
    }
}