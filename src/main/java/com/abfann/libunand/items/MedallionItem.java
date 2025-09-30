package com.abfann.libunand.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Clase base para todos los medallones
 */
public class MedallionItem extends Item {

    private final String medallionName;
    private final int maxDuration;

    public enum MedallionEffect {
        GUERRERO,
        BERSERKER,
        CAZADOR_NOCTURNO,
        CORREDOR,
        SOMBRAS,
        TITAN,
        VAMPIRICO,
        IGNEO,
        INGENIERO,
        CAPARAZON,
        SOBRECARGA,
        TRITON
    }

    private final MedallionEffect effectType;

    public MedallionItem(String name, int durationMinutes, MedallionEffect effectType) {
        super(new Properties()
                .tab(ItemGroup.TAB_COMBAT)
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .durability(durationMinutes * 60)
        );
        this.medallionName = name;
        this.maxDuration = durationMinutes * 60;
        this.effectType = effectType;
    }

    public MedallionEffect getEffectType() {
        return effectType;
    }

    /**
     * Obtiene la duracion maxima del medallon
     */
    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Obtiene la duracion restante del medallon
     */
    public int getRemainingDuration(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.contains("Duration")) {
            nbt.putInt("Duration", maxDuration);
        }
        return nbt.getInt("Duration");
    }

    /**
     * Establece la duracion del medallon
     */
    public void setDuration(ItemStack stack, int duration) {
        stack.getOrCreateTag().putInt("Duration", Math.max(0, duration));
    }

    /**
     * Reduce la duracion del medallon
     */
    public void reduceDuration(ItemStack stack, int amount) {
        int current = getRemainingDuration(stack);
        setDuration(stack, current - amount);
    }

    /**
     * Verifica si el medallon esta agotado
     */
    public boolean isDepleted(ItemStack stack) {
        return getRemainingDuration(stack) <= 0;
    }

    /**
     * Formatea el tiempo restante para mostrar
     */
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        // Descripción del efecto según tipo
        tooltip.add(getEffectDescription());
        tooltip.add(new StringTextComponent(""));

        int remaining = getRemainingDuration(stack);
        String timeText = formatTime(remaining);

        tooltip.add(new StringTextComponent("Tiempo restante: " + timeText)
                .withStyle(remaining > 600 ? TextFormatting.GREEN :
                        remaining > 180 ? TextFormatting.YELLOW : TextFormatting.RED));

        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Se desgasta mientras esta en tu inventario")
                .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("Guardalo en un cofre para preservarlo")
                .withStyle(TextFormatting.GRAY));
    }

    /**
     * Obtiene la descripcion del efecto del medallon
     */
    private ITextComponent getEffectDescription() {
        switch (effectType) {
            case GUERRERO:
                return new StringTextComponent("Fuerza I permanente").withStyle(TextFormatting.RED);
            case BERSERKER:
                return new StringTextComponent("Fuerza II + Regeneracion II").withStyle(TextFormatting.DARK_RED);
            case CAZADOR_NOCTURNO:
                return new StringTextComponent("Fuerza I").withStyle(TextFormatting.RED);
            case CORREDOR:
                return new StringTextComponent("Velocidad II + Salto II").withStyle(TextFormatting.AQUA);
            case SOMBRAS:
                return new StringTextComponent("Invisibilidad + Velocidad I").withStyle(TextFormatting.DARK_GRAY);
            case TITAN:
                return new StringTextComponent("Resistencia III + Velocidad I").withStyle(TextFormatting.BLUE);
            case VAMPIRICO:
                return new StringTextComponent("Absorcion de vida (2 HP por kill)").withStyle(TextFormatting.DARK_RED);
            case IGNEO:
                return new StringTextComponent("Resistencia al fuego + Regeneracion I").withStyle(TextFormatting.GOLD);
            case INGENIERO:
                return new StringTextComponent("Regeneracion III (hasta 75% vida)").withStyle(TextFormatting.LIGHT_PURPLE);
            case CAPARAZON:
                return new StringTextComponent("Velocidad III + Regen si HP < 50%").withStyle(TextFormatting.GREEN);
            case SOBRECARGA:
                return new StringTextComponent("Velocidad II + Dano en area al correr").withStyle(TextFormatting.YELLOW);
            case TRITON:
                return new StringTextComponent("Salto III + Riptide permanente").withStyle(TextFormatting.DARK_AQUA);
            default:
                return new StringTextComponent("Efecto desconocido").withStyle(TextFormatting.GRAY);
        }
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent(medallionName).withStyle(TextFormatting.LIGHT_PURPLE);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Efecto de encantamiento
    }

    @Override
    public int getDamage(ItemStack stack) {
        int remaining = getRemainingDuration(stack);
        return maxDuration - remaining;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return maxDuration;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }


}