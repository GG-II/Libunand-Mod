package com.abfann.libunand.data;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.items.ModItems;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JojoCoinLootModifier extends LootModifier {

    public JojoCoinLootModifier(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        // Crear nueva lista para no modificar la original
        List<ItemStack> modifiedLoot = new ArrayList<>(generatedLoot);

        // Buscar y reemplazar esmeraldas por JoJoCoins
        for (int i = 0; i < modifiedLoot.size(); i++) {
            ItemStack stack = modifiedLoot.get(i);

            if (stack.getItem() == Items.EMERALD) {
                // Reemplazar esmeraldas por JoJoCoins (misma cantidad)
                ItemStack jojoCoins = new ItemStack(ModItems.JOJOCOIN.get(), stack.getCount());
                modifiedLoot.set(i, jojoCoins);

                LibunandMod.LOGGER.debug("Reemplazadas {} esmeraldas por JoJoCoins en loot", stack.getCount());
            }
        }

        // Agregar medallones segun la estructura
        addMedallionsToLoot(modifiedLoot, context);

        return modifiedLoot;
    }

    /**
     * Agrega medallones segun el tipo de estructura
     */
    private void addMedallionsToLoot(List<ItemStack> loot, LootContext context) {
        Random random = context.getRandom();

        // 15% de probabilidad de aparecer un medallon
        if (random.nextFloat() > 0.15F) {
            return;
        }

        // Obtener la tabla de loot para determinar estructura
        ResourceLocation lootTable = context.getQueriedLootTableId();
        if (lootTable == null) return;

        String path = lootTable.getPath();
        ItemStack medallion = null;

        // Determinar medallon segun estructura
        if (path.contains("stronghold")) {
            // Fortalezas: Guerrero
            medallion = new ItemStack(ModItems.MEDALLION_GUERRERO.get());
        } else if (path.contains("woodland_mansion")) {
            // Mansiones: Berserker
            medallion = new ItemStack(ModItems.MEDALLION_BERSERKER.get());
        } else if (path.contains("simple_dungeon")) {
            // Templos selva: Cazador
            medallion = new ItemStack(ModItems.MEDALLION_CAZADOR.get());
        } else if (path.contains("end_city")) {
            // End City: Corredor
            medallion = new ItemStack(ModItems.MEDALLION_CORREDOR.get());
        } else if (path.contains("desert_pyramid")) {
            // Templo desierto: Sombras
            medallion = new ItemStack(ModItems.MEDALLION_SOMBRAS.get());
        } else if (path.contains("bastion")) {
            // Bastion: Titan
            medallion = new ItemStack(ModItems.MEDALLION_TITAN.get());
        } else if (path.contains("woodland_mansion")) {
            // Mazmorras: Vampirico
            medallion = new ItemStack(ModItems.MEDALLION_VAMPIRICO.get());
        } else if (path.contains("nether_bridge")) {
            // Fortaleza Nether: Igneo
            medallion = new ItemStack(ModItems.MEDALLION_IGNEO.get());
        } else if (path.contains("abandoned_mineshaft")) {
            // Minas: Ingeniero
            medallion = new ItemStack(ModItems.MEDALLION_INGENIERO.get());
        } else if (path.contains("underwater_ruin")) {
            // Ruinas oceanicas: Caparazon
            medallion = new ItemStack(ModItems.MEDALLION_CAPARAZON.get());
        } else if (path.contains("shipwreck")) {
            // Naufragios: Triton
            medallion = new ItemStack(ModItems.MEDALLION_TRITON.get());
        }

        if (medallion != null) {
            loot.add(medallion);
            LibunandMod.LOGGER.info("Agregado medallon a loot: {}", medallion.getHoverName().getString());
        }
    }

    public static class Serializer extends GlobalLootModifierSerializer<JojoCoinLootModifier> {
        @Override
        public JojoCoinLootModifier read(ResourceLocation location, JsonObject object,
                                         ILootCondition[] ailootcondition) {
            return new JojoCoinLootModifier(ailootcondition);
        }

        @Override
        public JsonObject write(JojoCoinLootModifier instance) {
            return makeConditions(instance.conditions);
        }
    }
}