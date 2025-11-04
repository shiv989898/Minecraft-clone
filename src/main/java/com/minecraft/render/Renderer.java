package com.minecraft.render;

import com.minecraft.entity.Player;
import com.minecraft.render.ShaderProgram.Type;
import com.minecraft.world.Chunk;
import com.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Renderer {
    private final ShaderProgram blockShader;
    private final ShaderProgram lineShader;
    private final ShaderProgram uiShader;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Map<Long, ChunkMesh> chunkMeshes;
    private boolean debugPrinted = false;
    private static final float SKY_COLOR_R = 0.53f;
    private static final float SKY_COLOR_G = 0.81f;
    private static final float SKY_COLOR_B = 0.92f;
    
    // Crosshair
    private int crosshairVAO;
    private int crosshairVBO;
    
    // Block outline
    private int outlineVAO;
    private int outlineVBO;
    private final Matrix4f outlineModel = new Matrix4f();
    
    public Renderer(long window) {
        this.blockShader = new ShaderProgram(Type.BLOCK);
        this.lineShader = new ShaderProgram(Type.LINE);
        this.uiShader = new ShaderProgram(Type.UI);
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.chunkMeshes = new HashMap<>();
        
        projectionMatrix.perspective((float) Math.toRadians(70.0f), 1280.0f / 720.0f, 0.1f, 1000.0f);
        
        setupCrosshair();
        setupBlockOutline();
    }
    
    public void render(World world, Player player, Vector3i targetBlockPos) {
        blockShader.use();
        
        // Set sky color for fog
        blockShader.setVec3("skyColor", SKY_COLOR_R, SKY_COLOR_G, SKY_COLOR_B);
        
        // Update view matrix - use eye position for camera
        Vector3f eyePos = player.getEyePosition();
        Vector3f center = new Vector3f(eyePos).add(player.getFront());
        viewMatrix.identity().lookAt(eyePos, center, player.getUp());
        
        blockShader.setMatrix4f("projection", projectionMatrix);
        blockShader.setMatrix4f("view", viewMatrix);
        
        // Debug print once
        if (!debugPrinted) {
            System.out.println("Rendering " + world.getChunks().size() + " chunks");
            System.out.println("Eye position: " + eyePos);
            System.out.println("Looking at: " + center);
            debugPrinted = true;
        }
        
        // Frustum culling - only render chunks near player
        int playerChunkX = (int) Math.floor(player.getPosition().x) >> 4;
        int playerChunkZ = (int) Math.floor(player.getPosition().z) >> 4;
        int renderDistance = 8;  // Match World render distance
        
        // Render chunks
        for (Map.Entry<Long, Chunk> entry : world.getChunks().entrySet()) {
            Chunk chunk = entry.getValue();
            long key = entry.getKey();
            
            // Simple frustum culling
            int dx = chunk.getChunkX() - playerChunkX;
            int dz = chunk.getChunkZ() - playerChunkZ;
            if (dx * dx + dz * dz > renderDistance * renderDistance) {
                continue;
            }
            
            ChunkMesh mesh = chunkMeshes.get(key);
            if (mesh == null || chunk.needsRebuild()) {
                if (mesh != null) {
                    mesh.cleanup();
                }
                mesh = new ChunkMesh(chunk, world);
                chunkMeshes.put(key, mesh);
                chunk.setRebuilt();
            }
            
            mesh.render(blockShader);
        }
        
        // Render crosshair (2D overlay)
        renderCrosshair();

        // Render block outline at targeted position
        renderBlockOutline(targetBlockPos);
    }
    
    private void setupCrosshair() {
        // Create crosshair (simple 2D lines in center of screen)
        float size = 0.02f;
        float[] vertices = {
            // Horizontal line
            -size, 0.0f, 0.0f,
            size, 0.0f, 0.0f,
            // Vertical line
            0.0f, -size, 0.0f,
            0.0f, size, 0.0f
        };
        
        crosshairVAO = glGenVertexArrays();
        crosshairVBO = glGenBuffers();
        
        glBindVertexArray(crosshairVAO);
        glBindBuffer(GL_ARRAY_BUFFER, crosshairVBO);
        
        FloatBuffer buffer = memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        memFree(buffer);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void setupBlockOutline() {
        // Create block outline wireframe
        float[] vertices = {
            // Bottom face
            0,0,0, 1,0,0,  1,0,0, 1,0,1,  1,0,1, 0,0,1,  0,0,1, 0,0,0,
            // Top face  
            0,1,0, 1,1,0,  1,1,0, 1,1,1,  1,1,1, 0,1,1,  0,1,1, 0,1,0,
            // Vertical edges
            0,0,0, 0,1,0,  1,0,0, 1,1,0,  1,0,1, 1,1,1,  0,0,1, 0,1,1
        };
        
        outlineVAO = glGenVertexArrays();
        outlineVBO = glGenBuffers();
        
        glBindVertexArray(outlineVAO);
        glBindBuffer(GL_ARRAY_BUFFER, outlineVBO);
        
        FloatBuffer buffer = memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        memFree(buffer);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void renderCrosshair() {
        // Disable depth test for 2D overlay
        glDisable(GL_DEPTH_TEST);

        uiShader.use();
        uiShader.setVec4("color", 1.0f, 1.0f, 1.0f, 1.0f);
        glBindVertexArray(crosshairVAO);
        glDrawArrays(GL_LINES, 0, 4);
        glBindVertexArray(0);
        
        // Re-enable depth test
        glEnable(GL_DEPTH_TEST);
    }

    private void renderBlockOutline(Vector3i blockPos) {
        if (blockPos == null) {
            return;
        }

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2.0f);

        lineShader.use();
        lineShader.setMatrix4f("projection", projectionMatrix);
        lineShader.setMatrix4f("view", viewMatrix);
        outlineModel.identity().translate(blockPos.x, blockPos.y, blockPos.z).scale(1.002f);
        lineShader.setMatrix4f("model", outlineModel);
        lineShader.setVec4("color", 1.0f, 1.0f, 1.0f, 0.9f);

        glBindVertexArray(outlineVAO);
        glDrawArrays(GL_LINES, 0, 24);
        glBindVertexArray(0);

        glDisable(GL_LINE_SMOOTH);
    }
    
    public void cleanup() {
        blockShader.cleanup();
        lineShader.cleanup();
        uiShader.cleanup();
        for (ChunkMesh mesh : chunkMeshes.values()) {
            mesh.cleanup();
        }
        glDeleteVertexArrays(crosshairVAO);
        glDeleteBuffers(crosshairVBO);
        glDeleteVertexArrays(outlineVAO);
        glDeleteBuffers(outlineVBO);
    }
}
