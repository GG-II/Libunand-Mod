package com.abfann.libunand.building;

import com.abfann.libunand.LibunandMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressiveBuilder {

    private final World world;
    private final BlockPos startPos;
    private final StructureBuilder.StructureData data;
    private final ServerPlayerEntity player;
    private final int blocksPerTick;
    private final List<StructureBuilder.StructureBlock> blocks;

    private int currentIndex = 0;
    private boolean isRunning = false;
    private ScheduledExecutorService scheduler;

    public ProgressiveBuilder(World world, BlockPos startPos, StructureBuilder.StructureData data,
                              ServerPlayerEntity player, int blocksPerTick) {
        this.world = world;
        this.startPos = startPos;
        this.data = data;
        this.player = player;
        this.blocksPerTick = blocksPerTick;
        this.blocks = data.blocks;
    }

    public void start() {
        if (isRunning) return;

        isRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        player.sendMessage(
                new StringTextComponent("Iniciando construccion progresiva... ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(blocks.size() + " bloques")
                                .withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        // Construir bloques cada 0.5 segundos
        scheduler.scheduleAtFixedRate(this::buildBatch, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void buildBatch() {
        if (!isRunning || currentIndex >= blocks.size()) {
            finish();
            return;
        }

        int blocksToPlace = Math.min(blocksPerTick, blocks.size() - currentIndex);
        int placed = 0;

        for (int i = 0; i < blocksToPlace && currentIndex < blocks.size(); i++) {
            StructureBuilder.StructureBlock block = blocks.get(currentIndex);
            BlockPos pos = startPos.offset(block.x, block.y, block.z);

            try {
                world.setBlock(pos, block.blockState, 3);
                placed++;
            } catch (Exception e) {
                LibunandMod.LOGGER.warn("Error colocando bloque en {}: {}", pos, e.getMessage());
            }

            currentIndex++;
        }

        // Mostrar progreso cada 50 bloques
        if (currentIndex % 50 == 0 || currentIndex >= blocks.size()) {
            float progress = (float) currentIndex / blocks.size() * 100;
            player.sendMessage(
                    new StringTextComponent("Progreso: ")
                            .withStyle(TextFormatting.AQUA)
                            .append(new StringTextComponent(String.format("%.1f%% (%d/%d)",
                                    progress, currentIndex, blocks.size()))
                                    .withStyle(TextFormatting.WHITE)),
                    player.getUUID()
            );
        }
    }

    private void finish() {
        if (!isRunning) return;

        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }

        player.sendMessage(
                new StringTextComponent("Construccion completada! ")
                        .withStyle(TextFormatting.GREEN)
                        .append(new StringTextComponent(currentIndex + " bloques colocados")
                                .withStyle(TextFormatting.WHITE)),
                player.getUUID()
        );

        LibunandMod.LOGGER.info("Construccion progresiva completada para {}: {} bloques",
                player.getName().getString(), currentIndex);
    }

    public void cancel() {
        if (!isRunning) return;

        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }

        player.sendMessage(
                new StringTextComponent("Construccion cancelada en bloque " + currentIndex)
                        .withStyle(TextFormatting.YELLOW),
                player.getUUID()
        );
    }

    public boolean isRunning() {
        return isRunning;
    }

    public float getProgress() {
        return blocks.size() > 0 ? (float) currentIndex / blocks.size() : 0;
    }
}