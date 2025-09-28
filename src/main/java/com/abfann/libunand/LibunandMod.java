package com.abfann.libunand;

import com.abfann.libunand.building.StructureCatalog;
import com.abfann.libunand.commands.ConstructCommands;
import com.abfann.libunand.commands.EconomyCommands;
import com.abfann.libunand.commands.PlotCommands;
import com.abfann.libunand.config.ConfigManager;
import com.abfann.libunand.data.PlayerDataHandler;
import com.abfann.libunand.items.ModItems;
import com.abfann.libunand.protection.PlotManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod("libunand")
public class LibunandMod {

    public static final String MOD_ID = "libunand";
    public static final Logger LOGGER = LogManager.getLogger();

    public LibunandMod() {
        // Registrar items
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

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

        // Registrar capabilities de economía
        PlayerDataHandler.registerCapabilities();

        LOGGER.info("Balance inicial configurado: " + ConfigManager.STARTING_BALANCE.get());
        LOGGER.info("JoJoCoin registrado exitosamente!");
        LOGGER.info("Sistema de datos del jugador configurado!");
        LOGGER.info("Sistema de lotes configurado!");
        LOGGER.info("Comandos de economía registrados!");
        LOGGER.info("Sistema económico JoJoCoins cargado!");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Cliente de Libunand configurado!");
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        PlotManager.getInstance().initialize(event.getServer());
        LOGGER.info("PlotManager inicializado con el servidor!");

        StructureCatalog.getInstance().initialize(event.getServer());
        LOGGER.info("StructureCatalog inicializado con el servidor!");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        EconomyCommands.register(event.getDispatcher());
        PlotCommands.register(event.getDispatcher());
        ConstructCommands.register(event.getDispatcher());
        LOGGER.info("Comandos de JoJoCoins registrados exitosamente!");
    }
}