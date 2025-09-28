package com.abfann.libunand.data;

import com.abfann.libunand.config.ConfigManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerEconomyData implements IPlayerEconomy {

    private int balance;

    public PlayerEconomyData() {
        this.balance = ConfigManager.STARTING_BALANCE.get();
    }

    @Override
    public int getBalance() {
        return balance;
    }

    @Override
    public void setBalance(int balance) {
        this.balance = Math.max(0, Math.min(balance, ConfigManager.MAX_BALANCE.get()));
    }

    @Override
    public void addBalance(int amount) {
        if (amount > 0) {
            setBalance(this.balance + amount);
        }
    }

    @Override
    public boolean removeBalance(int amount) {
        if (amount <= 0) return false;
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasBalance(int amount) {
        return this.balance >= amount;
    }

    @Override
    public boolean transferTo(IPlayerEconomy target, int amount) {
        if (this.removeBalance(amount)) {
            target.addBalance(amount);
            return true;
        }
        return false;
    }

    /**
     * Guarda los datos en NBT
     */
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("balance", this.balance);
        return nbt;
    }

    /**
     * Carga los datos desde NBT
     */
    public void deserializeNBT(CompoundNBT nbt) {
        this.balance = nbt.getInt("balance");
    }
}