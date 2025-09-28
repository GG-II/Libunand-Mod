package com.abfann.libunand.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigManager {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Configuraciones económicas
    public static final ForgeConfigSpec.IntValue STARTING_BALANCE;
    public static final ForgeConfigSpec.IntValue MAX_BALANCE;
    public static final ForgeConfigSpec.IntValue PLOT_MERGE_DISTANCE;

    static {
        BUILDER.push("Economy");
        STARTING_BALANCE = BUILDER
                .comment("Balance inicial de JoJoCoins para nuevos jugadores")
                .defineInRange("startingBalance", 100, 0, Integer.MAX_VALUE);

        MAX_BALANCE = BUILDER
                .comment("Balance máximo de JoJoCoins por jugador")
                .defineInRange("maxBalance", 1000000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Plots");
        PLOT_MERGE_DISTANCE = BUILDER
                .comment("Distancia en bloques para fusión automática de lotes")
                .defineInRange("plotMergeDistance", 10, 1, 50);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}