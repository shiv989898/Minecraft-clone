package com.minecraft.render;

import com.minecraft.block.BlockType;
import com.minecraft.world.Chunk;
import com.minecraft.world.World;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class ChunkMesh {
    private int vao;
    private int vbo;
    private int vertexCount;
    private Matrix4f modelMatrix;
    private static boolean debugPrinted = false;
    private static int totalChunksBuilt = 0;
    private static final float EPSILON = 0.0008f;
    
    public ChunkMesh(Chunk chunk, World world) {
        this.modelMatrix = new Matrix4f().identity();
        this.vao = 0;
        this.vbo = 0;
        buildMesh(chunk, world);
    }
    
    private void buildMesh(Chunk chunk, World world) {
        List<Float> vertices = new ArrayList<>();
        
        int worldX = chunk.getChunkX() * Chunk.CHUNK_SIZE;
        int worldZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE;
        
        // Optimize: Only process blocks that might be visible
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    BlockType block = chunk.getBlock(x, y, z);
                    
                    if (block == BlockType.AIR || !block.isSolid()) {
                        continue;
                    }
                    
                    int wx = worldX + x;
                    int wz = worldZ + z;
                    
                    // Check each face and only render if exposed
                    if (shouldRenderFace(world, wx, y + 1, wz)) {
                        addTopFace(vertices, x, y, z, block, wx, y + 1, wz);
                    }
                    if (shouldRenderFace(world, wx, y - 1, wz)) {
                        addBottomFace(vertices, x, y, z, block, wx, y - 1, wz);
                    }
                    if (shouldRenderFace(world, wx, y, wz + 1)) {
                        addNorthFace(vertices, x, y, z, block, wx, y, wz + 1);
                    }
                    if (shouldRenderFace(world, wx, y, wz - 1)) {
                        addSouthFace(vertices, x, y, z, block, wx, y, wz - 1);
                    }
                    if (shouldRenderFace(world, wx + 1, y, wz)) {
                        addEastFace(vertices, x, y, z, block, wx + 1, y, wz);
                    }
                    if (shouldRenderFace(world, wx - 1, y, wz)) {
                        addWestFace(vertices, x, y, z, block, wx - 1, y, wz);
                    }
                }
            }
        }
        
        vertexCount = vertices.size() / 6; // 3 pos + 3 color
        
        totalChunksBuilt++;
        if (!debugPrinted || (totalChunksBuilt <= 5 && vertexCount > 0)) {
            System.out.println("Chunk (" + chunk.getChunkX() + ", " + chunk.getChunkZ() + ") built with " + vertexCount + " vertices");
            if (totalChunksBuilt >= 5) debugPrinted = true;
        }
        
        if (vertexCount == 0) {
            return;
        }
        
        // Convert to array
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }
        
        // Create VAO and VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        FloatBuffer buffer = memAllocFloat(vertexArray.length);
        buffer.put(vertexArray).flip();
        
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        memFree(buffer);
        
        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindVertexArray(0);
    }
    
    private boolean shouldRenderFace(World world, int x, int y, int z) {
        // Don't render faces adjacent to solid blocks
        BlockType block = world.getBlock(x, y, z);
        return block == BlockType.AIR || !block.isSolid();
    }
    
    private void addTopFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(0), worldX, worldY, worldZ);
        float shade = 1.0f;
        float yTop = y + 1.0f + EPSILON;
        float[] verts = {
            x,     yTop, z,     color[0]*shade, color[1]*shade, color[2]*shade,
            x,     yTop, z+1,   color[0]*shade, color[1]*shade, color[2]*shade,
            x+1,   yTop, z+1,   color[0]*shade, color[1]*shade, color[2]*shade,
            x,     yTop, z,     color[0]*shade, color[1]*shade, color[2]*shade,
            x+1,   yTop, z+1,   color[0]*shade, color[1]*shade, color[2]*shade,
            x+1,   yTop, z,     color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }
    
    private void addBottomFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(1), worldX, worldY, worldZ);
        float shade = 0.5f;
        float yBottom = y - EPSILON;
        float[] verts = {
            x,   yBottom, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, yBottom, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, yBottom, z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   yBottom, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, yBottom, z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   yBottom, z+1, color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }
    
    private void addNorthFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(2), worldX, worldY, worldZ);
        float shade = 0.85f;
        float zNorth = z + 1.0f + EPSILON;
        float[] verts = {
            x,   y,   zNorth, color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, y,   zNorth, color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, y+1, zNorth, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   y,   zNorth, color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, y+1, zNorth, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   y+1, zNorth, color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }
    
    private void addSouthFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(3), worldX, worldY, worldZ);
        float shade = 0.85f;
        float zSouth = z - EPSILON;
        float[] verts = {
            x+1, y,   zSouth, color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, y+1, zSouth, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   y+1, zSouth, color[0]*shade, color[1]*shade, color[2]*shade,
            x+1, y,   zSouth, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   y+1, zSouth, color[0]*shade, color[1]*shade, color[2]*shade,
            x,   y,   zSouth, color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }
    
    private void addEastFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(4), worldX, worldY, worldZ);
        float shade = 0.75f;
        float xEast = x + 1.0f + EPSILON;
        float[] verts = {
            xEast, y,   z,   color[0]*shade, color[1]*shade, color[2]*shade,
            xEast, y+1, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            xEast, y+1, z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            xEast, y,   z,   color[0]*shade, color[1]*shade, color[2]*shade,
            xEast, y+1, z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            xEast, y,   z+1, color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }
    
    private void addWestFace(List<Float> vertices, float x, float y, float z, BlockType block, int worldX, int worldY, int worldZ) {
        float[] color = applyTint(block.getFaceColor(5), worldX, worldY, worldZ);
        float shade = 0.75f;
        float xWest = x - EPSILON;
        float[] verts = {
            xWest, y,   z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            xWest, y+1, z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            xWest, y+1, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            xWest, y,   z+1, color[0]*shade, color[1]*shade, color[2]*shade,
            xWest, y+1, z,   color[0]*shade, color[1]*shade, color[2]*shade,
            xWest, y,   z,   color[0]*shade, color[1]*shade, color[2]*shade
        };
        for (float v : verts) vertices.add(v);
    }

    private float[] applyTint(float[] baseColor, int x, int y, int z) {
        // Generate small deterministic variation to avoid flat coloring
        int hash = x * 734287 + y * 912271 + z * 523287;
        hash = (hash ^ (hash >> 13)) & 0xFF;
        float variation = (hash / 255.0f - 0.5f) * 0.1f; // +/-5%
        return new float[] {
            clampColor(baseColor[0] + variation),
            clampColor(baseColor[1] + variation),
            clampColor(baseColor[2] + variation)
        };
    }

    private float clampColor(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
    
    public void render(ShaderProgram shader) {
        if (vertexCount == 0) {
            return;
        }
        
        shader.setMatrix4f("model", modelMatrix);
        
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }
    
    public void cleanup() {
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
    }
}
