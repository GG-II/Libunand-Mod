package com.abfann.libunand.entities;

import com.abfann.libunand.LibunandMod;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, LibunandMod.MOD_ID);

    // Registro del Aldeano de Bienes Raíces
    public static final RegistryObject<EntityType<RealEstateVillagerEntity>> REAL_ESTATE_VILLAGER =
            ENTITIES.register("real_estate_villager", () -> EntityType.Builder
                    .<RealEstateVillagerEntity>of(RealEstateVillagerEntity::new, EntityClassification.CREATURE)
                    .sized(0.6F, 1.95F) // Mismo tamaño que villager normal
                    .clientTrackingRange(10)
                    .build("real_estate_villager"));
}