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

        return modifiedLoot;
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