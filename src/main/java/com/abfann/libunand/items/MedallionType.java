package com.abfann.libunand.items;

import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;

/**
 * Define todos los tipos de medallones y sus efectos
 */
public enum MedallionType {
    // ATAQUE
    GUERRERO("Medallón del Guerrero", Effects.DAMAGE_BOOST, 0), // Fuerza I
    BERSERKER("Medallón del Berserker", Effects.DAMAGE_BOOST, 1), // Fuerza II + Regen
    CAZADOR_NOCTURNO("Medallón del Cazador Nocturno", Effects.DAMAGE_BOOST, 0), // Fuerza I + infinity

    // MOVIMIENTO
    CORREDOR("Medallón del Corredor", Effects.MOVEMENT_SPEED, 1), // Velocidad II + Salto II
    SOMBRAS("Medallón de las Sombras", Effects.INVISIBILITY, 0), // Invisibilidad + Velocidad I
    TITAN("Medallón del Titán", Effects.DAMAGE_RESISTANCE, 2), // Resistencia III + Velocidad I

    // CURACION
    VAMPIRICO("Medallón Vampírico", null, 0), // Absorción de vida (especial)
    IGNEO("Medallón Igneo", Effects.FIRE_RESISTANCE, 0), // Resistencia al fuego + Regen I
    INGENIERO("Medallón del Ingeniero", Effects.REGENERATION, 2), // Regeneración III

    // EXPLOSIVO
    CAPARAZON("Medallón del Caparazón", Effects.MOVEMENT_SPEED, 2), // Velocidad III cuando HP < 50%
    SOBRECARGA("Medallón de Sobrecarga", Effects.MOVEMENT_SPEED, 1), // Velocidad II + daño área
    TRITON("Medallón del Tritón", Effects.JUMP, 2); // Salto III + Riptide

    private final String displayName;
    private final Effect primaryEffect;
    private final int effectLevel;

    MedallionType(String displayName, Effect primaryEffect, int effectLevel) {
        this.displayName = displayName;
        this.primaryEffect = primaryEffect;
        this.effectLevel = effectLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Effect getPrimaryEffect() {
        return primaryEffect;
    }

    public int getEffectLevel() {
        return effectLevel;
    }
}