package com.abfann.libunand.medallions;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.MedallionItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID)
public class MedallionEffectHandler {

    private static int tickCounter = 0;
    private static final int TICK_RATE = 20; // Cada segundo (20 ticks)

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level.isClientSide) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;

        tickCounter++;

        // Procesar cada segundo (20 ticks)
        if (tickCounter >= TICK_RATE) {
            tickCounter = 0;
            processMedallions(player);
        }

        // Aplicar efectos cada tick
        applyMedallionEffects(player);
    }

    /**
     * Procesa el desgaste de medallones
     */
    private static void processMedallions(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() instanceof MedallionItem) {
                MedallionItem medallion = (MedallionItem) stack.getItem();

                // Reducir durabilidad
                int remaining = medallion.getRemainingDuration(stack);
                medallion.setDuration(stack, remaining - 1);

                // Si se agoto, remover
                if (medallion.isDepleted(stack)) {
                    stack.shrink(1);
                    player.sendMessage(
                            new StringTextComponent("Tu medallon se ha agotado!")
                                    .withStyle(TextFormatting.RED),
                            player.getUUID()
                    );
                    LibunandMod.LOGGER.info("Medallon de {} se agoto", player.getName().getString());
                }
            }
        }
    }
    /**
     * Aplica efectos de los medallones activos
     */
    private static void applyMedallionEffects(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (stack.getItem() instanceof MedallionItem) {
                MedallionItem medallion = (MedallionItem) stack.getItem();
                applySpecificEffect(player, medallion.getEffectType());
                return; // Solo un medallon activo a la vez
            }
        }
    }

    /**
     * Aplica el efecto especifico segun el tipo de medallon
     */
    private static void applySpecificEffect(ServerPlayerEntity player, MedallionItem.MedallionEffect effect) {
        switch (effect) {
            // ATAQUE
            case GUERRERO:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.DAMAGE_BOOST, 40, 0, false, false));
                break;

            case BERSERKER:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.DAMAGE_BOOST, 40, 1, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.REGENERATION, 40, 1, false, false));
                break;

            case CAZADOR_NOCTURNO:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.DAMAGE_BOOST, 40, 0, false, false));
                // Municion infinita se maneja en evento de disparo
                break;

            // MOVIMIENTO
            case CORREDOR:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.MOVEMENT_SPEED, 40, 1, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.JUMP, 40, 1, false, false));
                break;

            case SOMBRAS:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.INVISIBILITY, 40, 0, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.MOVEMENT_SPEED, 40, 0, false, false));
                break;

            case TITAN:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.DAMAGE_RESISTANCE, 40, 2, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.MOVEMENT_SPEED, 40, 0, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.DAMAGE_RESISTANCE, 40, 0, false, false)); // Knockback resistance
                break;

            // CURACION
            case VAMPIRICO:
                // Absorcion de vida se maneja en evento de kill
                break;

            case IGNEO:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.FIRE_RESISTANCE, 40, 0, false, false));
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.REGENERATION, 40, 0, false, false));
                break;

            case INGENIERO:
                // Solo regenera hasta 75% de vida
                if (player.getHealth() < player.getMaxHealth() * 0.75f) {
                    player.addEffect(new EffectInstance(
                            net.minecraft.potion.Effects.REGENERATION, 40, 2, false, false));
                }
                break;

            // EXPLOSIVO
            case CAPARAZON:
                // Solo activo si HP < 50%
                if (player.getHealth() < player.getMaxHealth() * 0.5f) {
                    player.addEffect(new EffectInstance(
                            net.minecraft.potion.Effects.MOVEMENT_SPEED, 40, 2, false, false));
                    player.addEffect(new EffectInstance(
                            net.minecraft.potion.Effects.REGENERATION, 40, 0, false, false));
                }
                break;

            case SOBRECARGA:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.MOVEMENT_SPEED, 40, 1, false, false));
                // Dano en area al sprintar se maneja en evento
                break;

            case TRITON:
                player.addEffect(new EffectInstance(
                        net.minecraft.potion.Effects.JUMP, 40, 2, false, false));
                // Riptide permanente se maneja en evento
                break;
        }
    }
}