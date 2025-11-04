package com.minecraft.world;

import com.minecraft.block.BlockType;

import java.util.Random;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 128;
    
    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks;
    private boolean needsRebuild = true;
    
    public Chunk(int chunkX, int chunkZ, Random random) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
        generate(random);
    }
    
    private void generate(Random random) {
        int worldX = chunkX * CHUNK_SIZE;
        int worldZ = chunkZ * CHUNK_SIZE;
        
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int wx = worldX + x;
                int wz = worldZ + z;
                
                // Generate terrain height using improved noise
                double height = generateHeight(wx, wz);
                int terrainHeight = (int) (64 + height * 8);  // Flatter terrain (was 16)
                
                // Clamp height to reasonable values
                terrainHeight = Math.max(50, Math.min(terrainHeight, 80));
                
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    if (y > terrainHeight) {
                        blocks[x][y][z] = BlockType.AIR;
                    } else if (y == terrainHeight) {
                        // Surface block
                        if (terrainHeight < 62) {
                            blocks[x][y][z] = BlockType.SAND;  // Beach
                        } else if (terrainHeight > 75) {
                            blocks[x][y][z] = BlockType.STONE;  // Mountain
                        } else {
                            blocks[x][y][z] = BlockType.GRASS;  // Normal grass
                        }
                    } else if (y > terrainHeight - 3) {
                        // Sub-surface
                        if (terrainHeight < 62) {
                            blocks[x][y][z] = BlockType.SAND;
                        } else if (terrainHeight > 75) {
                            blocks[x][y][z] = BlockType.STONE;
                        } else {
                            blocks[x][y][z] = BlockType.DIRT;
                        }
                    } else {
                        // Deep underground
                        blocks[x][y][z] = BlockType.STONE;
                    }
                }
                
                // Add trees in grass biome (less frequent)
                if (terrainHeight >= 62 && terrainHeight <= 72 && random.nextDouble() < 0.01) {
                    generateTree(x, terrainHeight + 1, z, random);
                }
            }
        }
    }
    
    private double generateHeight(int x, int z) {
        double scale = 0.01;  // Larger scale for bigger features
        double value = 0;
        
        // Multiple octaves for more natural terrain (like Minecraft)
        value += noise(x * scale, z * scale) * 1.0;
        value += noise(x * scale * 2, z * scale * 2) * 0.5;
        value += noise(x * scale * 4, z * scale * 4) * 0.25;
        value += noise(x * scale * 8, z * scale * 8) * 0.125;
        
        return value / 1.875;
    }
    
    private double noise(double x, double z) {
        // Improved Perlin-like noise function
        int n = (int) (x * 1000) + (int) (z * 1000) * 57;
        n = (n << 13) ^ n;
        double noise = (1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
        
        // Smooth the noise
        return (noise + 1.0) / 2.0 - 0.5;
    }
    
    private void generateTree(int x, int y, int z, Random random) {
        int trunkHeight = 4 + random.nextInt(2);
        
        // Trunk
        for (int i = 0; i < trunkHeight; i++) {
            if (y + i < CHUNK_HEIGHT) {
                blocks[x][y + i][z] = BlockType.WOOD;
            }
        }
        
        // Leaves
        int leavesY = y + trunkHeight;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dy >= 0 && dz == 0) continue;
                    
                    int dist = Math.abs(dx) + Math.abs(dz);
                    if (dist <= 2 || (dist == 3 && random.nextBoolean())) {
                        int lx = x + dx;
                        int ly = leavesY + dy;
                        int lz = z + dz;
                        
                        if (lx >= 0 && lx < CHUNK_SIZE && 
                            ly >= 0 && ly < CHUNK_HEIGHT && 
                            lz >= 0 && lz < CHUNK_SIZE) {
                            blocks[lx][ly][lz] = BlockType.LEAVES;
                        }
                    }
                }
            }
        }
    }
    
    public BlockType getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) {
            return BlockType.AIR;
        }
        return blocks[x][y][z];
    }
    
    public void setBlock(int x, int y, int z, BlockType type) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) {
            return;
        }
        blocks[x][y][z] = type;
        needsRebuild = true;
    }
    
    public int getChunkX() {
        return chunkX;
    }
    
    public int getChunkZ() {
        return chunkZ;
    }
    
    public boolean needsRebuild() {
        return needsRebuild;
    }
    
    public void markForRebuild() {
        needsRebuild = true;
    }
    
    public void setRebuilt() {
        needsRebuild = false;
    }
}
