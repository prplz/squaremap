package xyz.jpenilla.squaremap.common.util.chunksnapshot;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.squaremap.common.util.ChunkMapAccess;

@DefaultQualifier(NonNull.class)
record VanillaChunkSnapshotProvider(ServerLevel level) implements ChunkSnapshotProvider {
    private static final ResourceLocation FULL = BuiltInRegistries.CHUNK_STATUS.getKey(ChunkStatus.FULL);

    @Override
    public CompletableFuture<@Nullable ChunkSnapshot> asyncSnapshot(final int x, final int z) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable ChunkAccess chunk = chunkIfGenerated(this.level, x, z);
            if (chunk == null) {
                return ChunkSnapshot.biomeSnapshot(this.level, x, z);
            }
            return ChunkSnapshot.snapshot(this.level, chunk, false);
        }, this.level.getServer());
    }

    private static @Nullable ChunkAccess chunkIfGenerated(final ServerLevel level, final int x, final int z) {
        final ChunkPos chunkPos = new ChunkPos(x, z);
        final ChunkMapAccess chunkMap = (ChunkMapAccess) level.getChunkSource().chunkMap;

        final ChunkHolder visibleChunk = chunkMap.squaremap$getVisibleChunkIfPresent(chunkPos.toLong());
        if (visibleChunk != null) {
            final @Nullable ChunkAccess chunk = fullIfPresent(visibleChunk);
            if (chunk != null) {
                return chunk;
            }
        }

        final ChunkHolder unloadingChunk = chunkMap.squaremap$pendingUnloads().get(chunkPos.toLong());
        if (unloadingChunk != null) {
            final @Nullable ChunkAccess chunk = fullIfPresent(unloadingChunk);
            if (chunk != null) {
                return chunk;
            }
        }

        final @Nullable CompoundTag chunkTag = chunkMap.squaremap$readChunk(chunkPos).join().orElse(null);
        if (chunkTag != null && chunkTag.contains("Status", Tag.TAG_STRING)) {
            if (isFullStatus(chunkTag) || preHeightChangeFullChunk(chunkTag)) {
                final @Nullable ChunkAccess chunk = level.getChunkSource()
                    .getChunkFuture(x, z, ChunkStatus.EMPTY, true)
                    .join()
                    .orElse(null);
                return unwrap(chunk);
            }
        }

        return null;
    }

    private static boolean isFullStatus(final CompoundTag chunkTag) {
        return FULL.equals(ResourceLocation.tryParse(chunkTag.getString("Status")));
    }

    private static @Nullable ChunkAccess fullIfPresent(final ChunkHolder chunkHolder) {
        return unwrap(chunkHolder.getLastAvailable());
    }

    private static @Nullable ChunkAccess unwrap(@Nullable ChunkAccess chunk) {
        if (chunk == null) {
            return null;
        }
        if (chunk instanceof ImposterProtoChunk imposter) {
            chunk = imposter.getWrapped();
        }
        if (!chunk.getStatus().isOrAfter(ChunkStatus.FULL) && !preHeightChangeFullChunk(chunk)) {
            return null;
        }
        return chunk;
    }

    private static boolean preHeightChangeFullChunk(final ChunkAccess chunk) {
        return chunk.getBelowZeroRetrogen() != null && chunk.getBelowZeroRetrogen().targetStatus().isOrAfter(ChunkStatus.SPAWN);
    }

    private static boolean preHeightChangeFullChunk(final CompoundTag chunkTag) {
        final CompoundTag belowZeroRetrogen = chunkTag.getCompound("below_zero_retrogen");
        if (belowZeroRetrogen.isEmpty()) {
            return false;
        }
        final String targetStatusStr = belowZeroRetrogen.getString("target_status");
        if (targetStatusStr.isEmpty()) {
            return false;
        }
        final @Nullable ResourceLocation targetStatus = ResourceLocation.tryParse(targetStatusStr);
        if (targetStatus == null) {
            return false;
        }
        return BuiltInRegistries.CHUNK_STATUS.get(targetStatus).isOrAfter(ChunkStatus.SPAWN);
    }
}
