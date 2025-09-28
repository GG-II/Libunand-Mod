package com.abfann.libunand.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class JoJoCoinItem extends Item {

    public JoJoCoinItem() {
        super(new Properties()
                .tab(ItemGroup.TAB_MISC)  // Aparece en pestaña "Miscellaneous"
                .stacksTo(64)             // Se puede stackear hasta 64
                .rarity(Rarity.UNCOMMON)  // Rareza verde (uncommon)
        );
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent("JoJoCoin").withStyle(TextFormatting.GOLD);
    }
}