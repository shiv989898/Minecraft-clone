package com.minecraft.world;

import com.minecraft.block.BlockType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class World {
    private Map<Long, Chunk> chunks;
    private Random random;
    private static final int RENDER_DISTANCE = 8;  // Reduced for better performance
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;
    
    public World() {
        this.chunks = new HashMap<>();
        this.random = new Random();
        generateWorld();
    }
    
    private void generateWorld() {
        for (int chunkX = -RENDER_DISTANCE; chunkX < RENDER_DISTANCE; chunkX++) {
            for (int chunkZ = -RENDER_DISTANCE; chunkZ < RENDER_DISTANCE; chunkZ++) {
                getOrCreateChunk(chunkX, chunkZ);
            }
        }
    }
    
    // Dynamic chunk loading based on player position
    public void updateChunks(float playerX, float playerZ) {
        int playerChunkX = (int) Math.floor(playerX) >> 4;
        int playerChunkZ = (int) Math.floor(playerZ) >> 4;
        
        // Only update if player moved to a new chunk
        if (playerChunkX == lastPlayerChunkX && playerChunkZ == lastPlayerChunkZ) {
            return;
        }
        
        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;
        
        // Load chunks around player
        for (int dx = -RENDER_DISTANCE; dx <= RENDER_DISTANCE; dx++) {
            for (int dz = -RENDER_DISTANCE; dz <= RENDER_DISTANCE; dz++) {
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                
                // Only load chunks within circular render distance
                if (dx * dx + dz * dz <= RENDER_DISTANCE * RENDER_DISTANCE) {
                    getOrCreateChunk(chunkX, chunkZ);
                }
            }
        }
        
        // Unload far chunks to save memory
        chunks.entrySet().removeIf(entry -> {
            Chunk chunk = entry.getValue();
            int dx = chunk.getChunkX() - playerChunkX;
            int dz = chunk.getChunkZ() - playerChunkZ;
            return dx * dx + dz * dz > (RENDER_DISTANCE + 2) * (RENDER_DISTANCE + 2);
        });
    }
    
    public Chunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = getChunkKey(chunkX, chunkZ);
        return chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkZ, random));
    }
    
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(getChunkKey(chunkX, chunkZ));
    }
    
    private long getChunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    public BlockType getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.CHUNK_HEIGHT) {
            return BlockType.AIR;
        }
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = getChunk(chunkX, chunkZ);
        
        if (chunk == null) {
            return BlockType.AIR;
        }
        
        int localX = x & 15;
        int localZ = z & 15;
        
        return chunk.getBlock(localX, y, localZ);
    }
    
    public void setBlock(int x, int y, int z, BlockType type) {
        if (y < 0 || y >= Chunk.CHUNK_HEIGHT) {
            return;
        }
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = getChunk(chunkX, chunkZ);
        
        if (chunk == null) {
            return;
        }
        
        int localX = x & 15;
        int localZ = z & 15;
        
        chunk.setBlock(localX, y, localZ, type);
    }
    
    public void markChunkForRebuild(int chunkX, int chunkZ) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            chunk.markForRebuild();
        }
    }
    
    public Map<Long, Chunk> getChunks() {
        return chunks;
    }
}
