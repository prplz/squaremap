package xyz.jpenilla.squaremap.common.data;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public final class BiomeBlocks {
    private BiomeBlocks() {
    }

    @Nullable
    private static Reference2ObjectMap<Biome, BlockState> biomeBlocks = null;

    public static BlockState get(Biome biome) {
        return Objects.requireNonNull(biomeBlocks).get(biome);
    }

    public static void init(RegistryAccess registryAccess) {
        if (biomeBlocks == null) {
            biomeBlocks = new Reference2ObjectOpenHashMap<>();
            biomeBlocks.defaultReturnValue(Blocks.GRASS_BLOCK.defaultBlockState());
            Registry<Biome> registry = registryAccess.registryOrThrow(Registries.BIOME);
            registry.holders().forEach(biome -> biomeBlocks.computeIfAbsent(biome.value(), $ -> blockForBiome(biome)));
            // registry.holders().forEach(biome -> logger.info("{}: {}", biome.key(), blockForBiome(biome)));
        }
    }

    private static BlockState blockForBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(Biomes.RIVER)) {
            return Blocks.WATER.defaultBlockState();
        } else if (biome.is(Biomes.BEACH) || biome.is(Biomes.DESERT)) {
            return Blocks.SAND.defaultBlockState();
        } else if (biome.is(Biomes.JAGGED_PEAKS)
            || biome.is(Biomes.FROZEN_PEAKS)
            || biome.is(Biomes.GROVE)
            || biome.is(Biomes.SNOWY_SLOPES)
            || biome.is(Biomes.SNOWY_TAIGA)
            || biome.is(Biomes.SNOWY_BEACH)
            || biome.is(Biomes.SNOWY_PLAINS)
            || biome.is(Biomes.ICE_SPIKES)
        ) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        } else if (biome.is(Biomes.FROZEN_RIVER)) {
            return Blocks.ICE.defaultBlockState();
        } else if (biome.is(BiomeTags.IS_BADLANDS)) {
            return Blocks.RED_SAND.defaultBlockState();
        } else if (biome.is(Biomes.MUSHROOM_FIELDS)) {
            return Blocks.MYCELIUM.defaultBlockState();
        } else if (biome.is(Biomes.STONY_SHORE) || biome.is(Biomes.STONY_PEAKS)) {
            return Blocks.STONE.defaultBlockState();
        } else if (biome.is(Biomes.WINDSWEPT_GRAVELLY_HILLS)){
            return Blocks.GRAVEL.defaultBlockState();
        } else if (biome.is(Biomes.WINDSWEPT_SAVANNA)) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        } else if (biome.is(Biomes.JUNGLE)) {
            return Blocks.JUNGLE_LEAVES.defaultBlockState();
        } else {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
    }
}
