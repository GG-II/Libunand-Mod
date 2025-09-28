package com.abfann.libunand.data;

import com.abfann.libunand.LibunandMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class PlayerDataHandler {

    public static final ResourceLocation PLAYER_ECONOMY_ID =
            new ResourceLocation(LibunandMod.MOD_ID, "player_economy");

    /**
     * Registra la Capability del sistema económico
     */
    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                IPlayerEconomy.class,
                new PlayerEconomyStorage(),
                PlayerEconomyData::new
        );
    }

    /**
     * Adjunta las capabilities a los jugadores cuando se crean
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(PLAYER_ECONOMY_ID, new PlayerEconomyProvider());
        }
    }

    /**
     * Maneja cuando un jugador se conecta por primera vez
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerEconomyProvider.PLAYER_ECONOMY).ifPresent(economy -> {
            LibunandMod.LOGGER.info("Jugador {} conectado con balance: {} JoJoCoins",
                    player.getName().getString(), economy.getBalance());
        });
    }

    /**
     * Copia datos cuando el jugador respawnea (evita pérdida de datos)
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        oldPlayer.getCapability(PlayerEconomyProvider.PLAYER_ECONOMY).ifPresent(oldEconomy -> {
            newPlayer.getCapability(PlayerEconomyProvider.PLAYER_ECONOMY).ifPresent(newEconomy -> {
                newEconomy.setBalance(oldEconomy.getBalance());
                LibunandMod.LOGGER.debug("Balance transferido tras muerte: {} JoJoCoins",
                        oldEconomy.getBalance());
            });
        });
    }

    /**
     * Obtiene la economía de un jugador de forma segura
     */
    public static IPlayerEconomy getPlayerEconomy(PlayerEntity player) {
        return player.getCapability(PlayerEconomyProvider.PLAYER_ECONOMY)
                .orElseThrow(() -> new IllegalStateException("Player economy capability not found"));
    }
}