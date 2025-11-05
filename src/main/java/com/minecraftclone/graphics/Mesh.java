package com.minecraftclone.graphics;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public final class Mesh {
    private int vaoId;
    private int vboId;
    private int vertexCount;

    public Mesh() {
        this.vaoId = 0;
        this.vboId = 0;
        this.vertexCount = 0;
    }

    public void upload(float[] vertices) {
        if (vaoId == 0) {
            vaoId = glGenVertexArrays();
        }
        if (vboId == 0) {
            vboId = glGenBuffers();
        }
        vertexCount = vertices.length / 9;

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);

        int stride = 9 * Float.BYTES;

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6L * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        if (vaoId == 0 || vertexCount == 0) {
            return;
        }
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        if (vaoId != 0) {
            glDisableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDeleteBuffers(vboId);
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
            vboId = 0;
            vertexCount = 0;
        }
    }
}
