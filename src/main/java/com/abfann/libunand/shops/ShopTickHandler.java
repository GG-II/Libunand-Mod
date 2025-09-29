package com.abfann.libunand.shops;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import com.abfann.libunand.LibunandMod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class ShopTickHandler {

    private static int tickCounter = 0;
    private static final int PARTICLE_INTERVAL = 100; // Cada 5 segundos (20 ticks/seg * 5)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != LogicalSide.SERVER) return;

        tickCounter++;

        if (tickCounter >= PARTICLE_INTERVAL) {
            tickCounter = 0;

            // Obtener servidor
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                // Spawear partículas en todos los mundos
                for (ServerWorld world : server.getAllLevels()) {
                    ParticleSpawner.spawnAllLinkedChestParticles(world);
                }
            }
        }
    }
}