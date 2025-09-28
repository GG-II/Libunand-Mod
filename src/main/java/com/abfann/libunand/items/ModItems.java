package com.abfann.libunand.items;

import com.abfann.libunand.LibunandMod;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    // DeferredRegister para registro automático de items
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LibunandMod.MOD_ID);

    // Registro del JoJoCoin
    public static final RegistryObject<Item> JOJOCOIN =
            ITEMS.register("jojocoin", JoJoCoinItem::new);
}