package xyz.jpenilla.squaremap.common.util.chunksnapshot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.squaremap.common.data.BiomeBlocks;

@DefaultQualifier(NonNull.class)
record BiomeChunkSnapshot(
    LevelHeightAccessor heightAccessor,
    DimensionType dimensionType,
    ChunkPos pos,
    Holder<Biome>[] biomes
) implements ChunkSnapshot {
    @Override
    public BlockState getBlockState(BlockPos pos) {
        final Holder<Biome> biome = getBiome(pos.getX(), pos.getZ());
        return BiomeBlocks.get(biome.value());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight(Heightmap.Types type, int x, int z) {
        return 0;
    }

    @Override
    public boolean sectionEmpty(int sectionIndex) {
        return false;
    }

    @Override
    public int getHeight() {
        return this.heightAccessor.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return this.heightAccessor.getMinBuildHeight();
    }

    @Override
    public Holder<Biome> getNoiseBiome(final int quartX, final int quartY, final int quartZ) {
        return this.biomes[(quartX & 3) + (quartZ & 3) * 4];
    }

    private Holder<Biome> getBiome(final int blockX, final int blockZ) {
        return this.getNoiseBiome(QuartPos.fromBlock(blockX), 0, QuartPos.fromBlock(blockZ));
    }
}
