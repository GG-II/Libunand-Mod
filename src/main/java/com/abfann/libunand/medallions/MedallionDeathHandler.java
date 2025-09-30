package com.abfann.libunand.medallions;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.MedallionItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class MedallionDeathHandler {

    /**
     * Elimina medallones al morir (incluso con keepInventory)
     */
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.level.isClientSide) return;

        int removed = 0;

        // Buscar y eliminar todos los medallones
        for (int i = 0; i < player.inventory.items.size(); i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.getItem() instanceof MedallionItem) {
                player.inventory.items.set(i, ItemStack.EMPTY);
                removed++;
            }
        }

        if (removed > 0) {
            LibunandMod.LOGGER.info("{} perdió {} medallón(es) al morir",
                    player.getName().getString(), removed);
        }
    }

    /**
     * Asegura que no se mantengan medallones con keepInventory
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        PlayerEntity player = event.getPlayer();

        // Eliminar medallones incluso después de respawn
        for (int i = 0; i < player.inventory.items.size(); i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.getItem() instanceof MedallionItem) {
                player.inventory.items.set(i, ItemStack.EMPTY);
            }
        }
    }
}