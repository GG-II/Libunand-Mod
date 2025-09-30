package com.abfann.libunand.medallions;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.MedallionItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class MedallionArrowHandler {

    /**
     * Verifica si el jugador tiene el medallon del cazador
     */
    private static boolean hasCazadorMedallion(PlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() instanceof MedallionItem) {
                MedallionItem medallion = (MedallionItem) stack.getItem();
                if (medallion.getEffectType() == MedallionItem.MedallionEffect.CAZADOR_NOCTURNO) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si el jugador tiene flechas
     */
    private static boolean hasArrows(PlayerEntity player) {
        if (player.abilities.instabuild) {
            return true;
        }

        for (ItemStack stack : player.inventory.items) {
            Item item = stack.getItem();
            if (item == Items.ARROW || item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW) {
                return true;
            }
        }
        return false;
    }

    /**
     * Da municion infinita al disparar con arco
     */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        PlayerEntity player = event.getPlayer();

        if (hasCazadorMedallion(player)) {
            // Si no tiene flechas, agregar una temporalmente
            if (!hasArrows(player)) {
                player.inventory.add(new ItemStack(Items.ARROW));
                LibunandMod.LOGGER.debug("Municion infinita: agregada flecha a {}", player.getName().getString());
            }
        }
    }
}