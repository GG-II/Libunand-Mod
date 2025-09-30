package com.abfann.libunand.items;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.plots.PlotSelectionTool;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LibunandMod.MOD_ID);

    // JoJoCoin
    public static final RegistryObject<Item> JOJOCOIN =
            ITEMS.register("jojocoin", JoJoCoinItem::new);

    // MEDALLONES - ATAQUE
    public static final RegistryObject<Item> MEDALLION_GUERRERO =
            ITEMS.register("medallion_guerrero", () -> new MedallionItem("Medallon del Guerrero", 120, MedallionItem.MedallionEffect.GUERRERO));

    public static final RegistryObject<Item> MEDALLION_BERSERKER =
            ITEMS.register("medallion_berserker", () -> new MedallionItem("Medallon del Berserker", 90, MedallionItem.MedallionEffect.BERSERKER));

    public static final RegistryObject<Item> MEDALLION_CAZADOR =
            ITEMS.register("medallion_cazador", () -> new MedallionItem("Medallon del Cazador Nocturno", 120, MedallionItem.MedallionEffect.CAZADOR_NOCTURNO));

    // MEDALLONES - MOVIMIENTO
    public static final RegistryObject<Item> MEDALLION_CORREDOR =
            ITEMS.register("medallion_corredor", () -> new MedallionItem("Medallon del Corredor", 240, MedallionItem.MedallionEffect.CORREDOR));

    public static final RegistryObject<Item> MEDALLION_SOMBRAS =
            ITEMS.register("medallion_sombras", () -> new MedallionItem("Medallon de las Sombras", 180, MedallionItem.MedallionEffect.SOMBRAS));

    public static final RegistryObject<Item> MEDALLION_TITAN =
            ITEMS.register("medallion_titan", () -> new MedallionItem("Medallon del Titan", 240, MedallionItem.MedallionEffect.TITAN));

    // MEDALLONES - CURACION
    public static final RegistryObject<Item> MEDALLION_VAMPIRICO =
            ITEMS.register("medallion_vampirico", () -> new MedallionItem("Medallon Vampirico", 180, MedallionItem.MedallionEffect.VAMPIRICO));

    public static final RegistryObject<Item> MEDALLION_IGNEO =
            ITEMS.register("medallion_igneo", () -> new MedallionItem("Medallon Igneo", 180, MedallionItem.MedallionEffect.IGNEO));

    public static final RegistryObject<Item> MEDALLION_INGENIERO =
            ITEMS.register("medallion_ingeniero", () -> new MedallionItem("Medallon del Ingeniero", 180, MedallionItem.MedallionEffect.INGENIERO));

    // MEDALLONES - EXPLOSIVO
    public static final RegistryObject<Item> MEDALLION_CAPARAZON =
            ITEMS.register("medallion_caparazon", () -> new MedallionItem("Medallon del Caparazon", 150, MedallionItem.MedallionEffect.CAPARAZON));

    public static final RegistryObject<Item> MEDALLION_SOBRECARGA =
            ITEMS.register("medallion_sobrecarga", () -> new MedallionItem("Medallon de Sobrecarga", 150, MedallionItem.MedallionEffect.SOBRECARGA));

    public static final RegistryObject<Item> MEDALLION_TRITON =
            ITEMS.register("medallion_triton", () -> new MedallionItem("Medallon del Triton", 150, MedallionItem.MedallionEffect.TRITON));

    public static final RegistryObject<Item> PLOT_SELECTOR =
            ITEMS.register("plot_selector", PlotSelectionTool::new);
}