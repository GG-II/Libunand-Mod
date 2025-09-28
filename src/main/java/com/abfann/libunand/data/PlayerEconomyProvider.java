package com.abfann.libunand.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerEconomyProvider implements ICapabilitySerializable<CompoundNBT> {

    @CapabilityInject(IPlayerEconomy.class)
    public static final Capability<IPlayerEconomy> PLAYER_ECONOMY = null;

    private final PlayerEconomyData economyData = new PlayerEconomyData();
    private final LazyOptional<IPlayerEconomy> economyOptional = LazyOptional.of(() -> economyData);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_ECONOMY) {
            return economyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return economyData.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        economyData.deserializeNBT(nbt);
    }
}