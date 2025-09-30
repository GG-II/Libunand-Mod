package com.abfann.libunand.medallions;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.MedallionItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class MedallionRestrictionHandler {

    /**
     * Verifica si el jugador ya tiene un medallón
     */
    private static boolean hasMedallion(PlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() instanceof MedallionItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dropea el medallón que ya tenía el jugador
     */
    private static void dropCurrentMedallion(PlayerEntity player) {
        for (int i = 0; i < player.inventory.items.size(); i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.getItem() instanceof MedallionItem) {
                // Dropear el medallón actual
                ItemEntity itemEntity = new ItemEntity(
                        player.level,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        stack.copy()
                );
                player.level.addFreshEntity(itemEntity);

                // Remover del inventario
                player.inventory.items.set(i, ItemStack.EMPTY);

                player.sendMessage(
                        new StringTextComponent("Solo puedes llevar un medallón a la vez! El anterior fue dropeado.")
                                .withStyle(TextFormatting.YELLOW),
                        player.getUUID()
                );

                return;
            }
        }
    }

    /**
     * Impide recoger múltiples medallones
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        ItemStack pickupStack = event.getItem().getItem();

        if (pickupStack.getItem() instanceof MedallionItem) {
            PlayerEntity player = event.getPlayer();

            if (hasMedallion(player)) {
                // Ya tiene un medallón, dropear el actual y permitir recoger el nuevo
                dropCurrentMedallion(player);
            }
        }
    }
}