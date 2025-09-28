package com.abfann.libunand.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerEconomyStorage implements Capability.IStorage<IPlayerEconomy> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IPlayerEconomy> capability, IPlayerEconomy instance, Direction side) {
        if (instance instanceof PlayerEconomyData) {
            return ((PlayerEconomyData) instance).serializeNBT();
        }
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<IPlayerEconomy> capability, IPlayerEconomy instance, Direction side, INBT nbt) {
        if (instance instanceof PlayerEconomyData && nbt instanceof CompoundNBT) {
            ((PlayerEconomyData) instance).deserializeNBT((CompoundNBT) nbt);
        }
    }
}