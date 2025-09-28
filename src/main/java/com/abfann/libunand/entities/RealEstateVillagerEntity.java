package com.abfann.libunand.entities;

import com.abfann.libunand.LibunandMod;
import com.abfann.libunand.protection.Plot;
import com.abfann.libunand.protection.PlotManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Optional;

public class RealEstateVillagerEntity extends VillagerEntity {

    private String plotName;
    private BlockPos plotCenter;

    public RealEstateVillagerEntity(EntityType<? extends VillagerEntity> type, World world) {
        super(type, world);
        this.setCustomName(new StringTextComponent("Agente de Bienes Raices").withStyle(TextFormatting.GOLD));
        this.setCustomNameVisible(true);
    }

    public RealEstateVillagerEntity(World world, String plotName, BlockPos plotCenter) {
        this(ModEntities.REAL_ESTATE_VILLAGER.get(), world);
        this.plotName = plotName;
        this.plotCenter = plotCenter;
        this.setPos(plotCenter.getX() + 0.5, plotCenter.getY(), plotCenter.getZ() + 0.5);
    }

    @Override
    protected void registerGoals() {
        // AI Goals simplificados - solo mirar a jugadores y alrededor
        this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(2, new LookRandomlyGoal(this));
        // No agregar goals de movimiento para que se quede en su lugar
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!this.level.isClientSide && hand == Hand.MAIN_HAND) {
            if (plotName != null) {
                openPlotPurchaseGUI((ServerPlayerEntity) player);
                return ActionResultType.SUCCESS;
            } else {
                player.sendMessage(
                        new StringTextComponent("Error: Este agente no tiene un lote asignado!")
                                .withStyle(TextFormatting.RED),
                        player.getUUID()
                );
                return ActionResultType.FAIL;
            }
        }
        return ActionResultType.PASS;
    }

    private void openPlotPurchaseGUI(ServerPlayerEntity player) {
        Optional<Plot> plotOpt = PlotManager.getInstance().getPlotByName(plotName);
        if (!plotOpt.isPresent()) {
            player.sendMessage(
                    new StringTextComponent("Error: Lote no encontrado!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            return;
        }

        Plot plot = plotOpt.get();
        if (!plot.isForSale()) {
            player.sendMessage(
                    new StringTextComponent("Este lote ya no esta disponible para compra!")
                            .withStyle(TextFormatting.RED),
                    player.getUUID()
            );
            // El NPC debería desaparecer, pero por ahora solo informamos
            return;
        }

        // Por ahora mostrar información del lote en chat
        // En el futuro esto abrirá una GUI
        showPlotInfoInChat(player, plot);
    }

    private void showPlotInfoInChat(ServerPlayerEntity player, Plot plot) {
        player.sendMessage(
                new StringTextComponent("=== Informacion del Lote ===")
                        .withStyle(TextFormatting.GOLD),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Nombre: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(plot.getName()).withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Precio: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(plot.getPrice() + " JoJoCoins").withStyle(TextFormatting.GOLD)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Area: ")
                        .withStyle(TextFormatting.AQUA)
                        .append(new StringTextComponent(plot.getArea() + " bloques").withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        player.sendMessage(
                new StringTextComponent("Usa /plot buy " + plot.getName() + " para comprarlo!")
                        .withStyle(TextFormatting.GREEN),
                player.getUUID()
        );
    }

    @Override
    public void tick() {
        super.tick();

        // Mantener al NPC en su posición
        if (plotCenter != null) {
            double distance = this.distanceToSqr(plotCenter.getX() + 0.5, plotCenter.getY(), plotCenter.getZ() + 0.5);
            if (distance > 4.0) { // Si se aleja más de 2 bloques
                this.setPos(plotCenter.getX() + 0.5, plotCenter.getY(), plotCenter.getZ() + 0.5);
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        if (plotName != null) {
            compound.putString("PlotName", plotName);
        }
        if (plotCenter != null) {
            compound.putInt("PlotCenterX", plotCenter.getX());
            compound.putInt("PlotCenterY", plotCenter.getY());
            compound.putInt("PlotCenterZ", plotCenter.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("PlotName")) {
            this.plotName = compound.getString("PlotName");
        }
        if (compound.contains("PlotCenterX")) {
            this.plotCenter = new BlockPos(
                    compound.getInt("PlotCenterX"),
                    compound.getInt("PlotCenterY"),
                    compound.getInt("PlotCenterZ")
            );
        }
    }

    // Getters y setters
    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public BlockPos getPlotCenter() {
        return plotCenter;
    }

    public void setPlotCenter(BlockPos plotCenter) {
        this.plotCenter = plotCenter;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // No remover automáticamente por distancia
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        // Mantener persistencia para que no desaparezca
        return true;
    }
}