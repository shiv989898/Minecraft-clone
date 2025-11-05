package com.minecraftclone.world;

import com.minecraftclone.graphics.ShaderProgram;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public final class World {
    private static final int VIEW_DISTANCE_CHUNKS = 4;
    private static final float MAX_RENDER_DISTANCE_SQUARED = (VIEW_DISTANCE_CHUNKS * Chunk.SIZE * 1.3f) * (VIEW_DISTANCE_CHUNKS * Chunk.SIZE * 1.3f);

    private final Map<Long, Chunk> chunks;

    public World() {
        this.chunks = new HashMap<>();
    }

    public void update(Vector3f playerPosition) {
        int chunkX = Math.floorDiv((int) Math.floor(playerPosition.x), Chunk.SIZE);
        int chunkZ = Math.floorDiv((int) Math.floor(playerPosition.z), Chunk.SIZE);
        ensureChunksAround(chunkX, chunkZ, VIEW_DISTANCE_CHUNKS);
        rebuildDirtyChunks(chunkX, chunkZ, VIEW_DISTANCE_CHUNKS + 1);
    }

    public void render(ShaderProgram shader, Vector3f cameraPosition) {
        int cameraChunkX = Math.floorDiv((int) Math.floor(cameraPosition.x), Chunk.SIZE);
        int cameraChunkZ = Math.floorDiv((int) Math.floor(cameraPosition.z), Chunk.SIZE);
        for (Chunk chunk : chunks.values()) {
            float dx = (chunk.chunkX() - cameraChunkX) * Chunk.SIZE;
            float dz = (chunk.chunkZ() - cameraChunkZ) * Chunk.SIZE;
            float distanceSquared = dx * dx + dz * dz;
            if (distanceSquared > MAX_RENDER_DISTANCE_SQUARED) {
                continue;
            }
            chunk.render(shader);
        }
    }

    public BlockType getBlock(int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.HEIGHT) {
            return worldY < 0 ? BlockType.BEDROCK : BlockType.AIR;
        }
        Chunk chunk = getChunkFor(worldX, worldZ);
        if (chunk == null) {
            return BlockType.AIR;
        }
        int localX = Math.floorMod(worldX, Chunk.SIZE);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE);
        return chunk.getBlock(localX, worldY, localZ);
    }

    public boolean isSolidBlock(int worldX, int worldY, int worldZ) {
        return getBlock(worldX, worldY, worldZ).isSolid();
    }

    public void setBlock(int worldX, int worldY, int worldZ, BlockType type) {
        if (worldY < 0 || worldY >= Chunk.HEIGHT) {
            return;
        }
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE);
        Chunk chunk = getOrCreateChunk(chunkX, chunkZ);
        int localX = Math.floorMod(worldX, Chunk.SIZE);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE);
        chunk.setBlock(localX, worldY, localZ, type);

        if (localX == 0) {
            markDirty(chunkX - 1, chunkZ);
        } else if (localX == Chunk.SIZE - 1) {
            markDirty(chunkX + 1, chunkZ);
        }
        if (localZ == 0) {
            markDirty(chunkX, chunkZ - 1);
        } else if (localZ == Chunk.SIZE - 1) {
            markDirty(chunkX, chunkZ + 1);
        }
    }

    public void cleanup() {
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
    }

    private void ensureChunksAround(int centerChunkX, int centerChunkZ, int radius) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int targetChunkX = centerChunkX + dx;
                int targetChunkZ = centerChunkZ + dz;
                getOrCreateChunk(targetChunkX, targetChunkZ);
            }
        }
    }

    private void rebuildDirtyChunks(int centerChunkX, int centerChunkZ, int radius) {
        int radiusSquared = radius * radius;
        for (Chunk chunk : chunks.values()) {
            int dx = chunk.chunkX() - centerChunkX;
            int dz = chunk.chunkZ() - centerChunkZ;
            if (dx * dx + dz * dz > radiusSquared) {
                continue;
            }
            if (chunk.isDirty()) {
                chunk.rebuildMesh(this);
            }
        }
    }

    private Chunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = packChunkKey(chunkX, chunkZ);
        return chunks.computeIfAbsent(key, k -> {
            Chunk chunk = new Chunk(chunkX, chunkZ);
            populateChunk(chunk);
            chunk.rebuildMesh(this);
            return chunk;
        });
    }

    private Chunk getChunkFor(int worldX, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE);
        long key = packChunkKey(chunkX, chunkZ);
        return chunks.get(key);
    }

    private void markDirty(int chunkX, int chunkZ) {
        long key = packChunkKey(chunkX, chunkZ);
        Chunk chunk = chunks.get(key);
        if (chunk != null) {
            chunk.markDirty();
        }
    }

    private static long packChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }

    private void populateChunk(Chunk chunk) {
        int baseX = chunk.chunkX() * Chunk.SIZE;
        int baseZ = chunk.chunkZ() * Chunk.SIZE;
        for (int localX = 0; localX < Chunk.SIZE; localX++) {
            for (int localZ = 0; localZ < Chunk.SIZE; localZ++) {
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

        double noise = Math.sin(worldX * 0.08) * 2.2
            + Math.cos(worldZ * 0.08) * 2.2
            + Math.sin(worldX * 0.017) * Math.cos(worldZ * 0.017) * 6.0;
        int surfaceHeight = clampToInt(Math.round(40 + noise), 4, Chunk.HEIGHT - 2);

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    BlockType type = BlockType.AIR;
                    if (y == 0) {
                        type = BlockType.BEDROCK;
                    } else if (y < surfaceHeight - 4) {
                        type = BlockType.STONE;
                    } else if (y < surfaceHeight - 1) {
                        type = BlockType.DIRT;
                    } else if (y == surfaceHeight - 1) {
                        type = BlockType.GRASS;
                    }
                    chunk.setBlock(localX, y, localZ, type);
                }
            }
        }
    }

    private static int clampToInt(long value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return (int) value;
    }
}
