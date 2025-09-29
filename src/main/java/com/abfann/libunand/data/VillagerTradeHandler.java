package com.abfann.libunand.data;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.ModItems;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)  // <- CAMBIAR ESTA LÍNEA
public class VillagerTradeHandler {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        LibunandMod.LOGGER.info("===== EVENTO DE TRADES DETECTADO =====");
        LibunandMod.LOGGER.info("Profesión: {}", event.getType().getRegistryName());

        // Para cada nivel de trades (1-5)
        for (int level = 1; level <= 5; level++) {
            List<VillagerTrades.ITrade> trades = event.getTrades().get(level);

            if (trades != null && !trades.isEmpty()) {
                LibunandMod.LOGGER.info("Nivel {}: {} trades encontradas", level, trades.size());

                // Crear nueva lista de trades modificadas
                List<VillagerTrades.ITrade> modifiedTrades = new ArrayList<>();

                for (VillagerTrades.ITrade trade : trades) {
                    modifiedTrades.add(new EmeraldToJojoCoinTrade(trade));
                }

                // Reemplazar las trades
                trades.clear();
                trades.addAll(modifiedTrades);

                LibunandMod.LOGGER.info("Nivel {}: Trades modificadas exitosamente", level);
            }
        }

        LibunandMod.LOGGER.info("Trades de {} modificadas para usar JoJoCoins",
                event.getType().getRegistryName());
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        LibunandMod.LOGGER.info("===== EVENTO DE COMERCIANTE ERRANTE DETECTADO =====");

        modifyTradeList(event.getGenericTrades());
        modifyTradeList(event.getRareTrades());

        LibunandMod.LOGGER.info("Trades de comerciante errante modificadas para usar JoJoCoins");
    }

    private static void modifyTradeList(List<VillagerTrades.ITrade> trades) {
        if (trades != null && !trades.isEmpty()) {
            List<VillagerTrades.ITrade> modifiedTrades = new ArrayList<>();

            for (VillagerTrades.ITrade trade : trades) {
                modifiedTrades.add(new EmeraldToJojoCoinTrade(trade));
            }

            trades.clear();
            trades.addAll(modifiedTrades);
        }
    }

    private static class EmeraldToJojoCoinTrade implements VillagerTrades.ITrade {
        private final VillagerTrades.ITrade originalTrade;

        public EmeraldToJojoCoinTrade(VillagerTrades.ITrade originalTrade) {
            this.originalTrade = originalTrade;
        }

        @Override
        public MerchantOffer getOffer(net.minecraft.entity.Entity trader, java.util.Random rand) {
            MerchantOffer originalOffer = originalTrade.getOffer(trader, rand);

            if (originalOffer == null) {
                return null;
            }

            // Obtener los items de la oferta original
            ItemStack costA = originalOffer.getCostA().copy();
            ItemStack costB = originalOffer.getCostB().copy();
            ItemStack result = originalOffer.getResult().copy();

            // Reemplazar esmeraldas por JoJoCoins en los costos
            if (costA.getItem() == Items.EMERALD) {
                costA = new ItemStack(ModItems.JOJOCOIN.get(), costA.getCount());
                LibunandMod.LOGGER.debug("Reemplazada esmeralda en costA");
            }

            if (!costB.isEmpty() && costB.getItem() == Items.EMERALD) {
                costB = new ItemStack(ModItems.JOJOCOIN.get(), costB.getCount());
                LibunandMod.LOGGER.debug("Reemplazada esmeralda en costB");
            }

            // Reemplazar esmeraldas por JoJoCoins en el resultado
            if (result.getItem() == Items.EMERALD) {
                result = new ItemStack(ModItems.JOJOCOIN.get(), result.getCount());
                LibunandMod.LOGGER.debug("Reemplazada esmeralda en resultado");
            }

            // Crear nueva oferta con JoJoCoins
            return new MerchantOffer(
                    costA,
                    costB,
                    result,
                    originalOffer.getUses(),
                    originalOffer.getMaxUses(),
                    originalOffer.getXp(),
                    originalOffer.getPriceMultiplier(),
                    originalOffer.getDemand()
            );
        }
    }
}