package com.abfann.libunand;

import com.abfann.libunand.config.ConfigManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("libunand")
public class LibunandMod {

    public static final String MOD_ID = "libunand";
    public static final Logger LOGGER = LogManager.getLogger();

    public LibunandMod() {
        // Registrar configuración
        ConfigManager.register();

        // Registrar eventos del mod
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Registrar el mod para eventos del servidor
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Libunand está inicializando...");
        LOGGER.info("Balance inicial configurado: " + ConfigManager.STARTING_BALANCE.get());
        LOGGER.info("Sistema económico JoJoCoins cargado!");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Cliente de Libunand configurado!");
    }
}