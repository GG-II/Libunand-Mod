package com.abfann.libunand.data;

import com.abfann.libunand.LibunandMod;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LibunandMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModLootModifiers {

    @SubscribeEvent
    public static void registerModifierSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        event.getRegistry().register(
                new JojoCoinLootModifier.Serializer().setRegistryName(
                        LibunandMod.MOD_ID, "jojocoin_loot"
                )
        );

        LibunandMod.LOGGER.info("Loot Modifier de JoJoCoin registrado!");
    }
}