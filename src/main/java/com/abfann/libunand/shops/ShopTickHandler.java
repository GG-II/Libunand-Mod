package com.abfann.libunand.shops;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.abfann.libunand.LibunandMod;


@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class ShopTickHandler {

    private static int tickCounter = 0;
    private static final int PARTICLE_INTERVAL = 100; // Cada 5 segundos

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isClientSide()) return;
        if (!(event.world instanceof ServerWorld)) return;

        tickCounter++;

        if (tickCounter >= PARTICLE_INTERVAL) {
            tickCounter = 0;
            ParticleSpawner.spawnAllLinkedChestParticles((ServerWorld) event.world);
        }
    }
}