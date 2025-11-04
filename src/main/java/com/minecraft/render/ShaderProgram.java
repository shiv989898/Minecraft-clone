package com.minecraft.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    public enum Type {
        BLOCK,
        LINE,
        UI
    }

    private final int programId;
    private final Type type;

    private static final String BLOCK_VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aColor;
            
            out vec3 vertexColor;
            out float visibility;
            
            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;
            
            const float density = 0.003;
            const float gradient = 2.0;
            
            void main() {
                vec4 worldPosition = model * vec4(aPos, 1.0);
                vec4 positionRelativeToCamera = view * worldPosition;
                gl_Position = projection * positionRelativeToCamera;
                vertexColor = aColor;
                
                float distance = length(positionRelativeToCamera.xyz);
                visibility = exp(-pow((distance * density), gradient));
                visibility = clamp(visibility, 0.0, 1.0);
            }
            """;

    private static final String BLOCK_FRAGMENT_SHADER = """
            #version 330 core
            in vec3 vertexColor;
            in float visibility;
            out vec4 FragColor;
            
            uniform vec3 skyColor;
            
            void main() {
                vec4 color = vec4(vertexColor, 1.0);
                FragColor = mix(vec4(skyColor, 1.0), color, visibility);
            }
            """;

    private static final String LINE_VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            
            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;
            
            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """;

    private static final String LINE_FRAGMENT_SHADER = """
            #version 330 core
            uniform vec4 color;
            out vec4 FragColor;
            
            void main() {
                FragColor = color;
            }
            """;

    private static final String UI_VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            
            void main() {
                gl_Position = vec4(aPos, 1.0);
            }
            """;

    private static final String UI_FRAGMENT_SHADER = """
            #version 330 core
            uniform vec4 color;
            out vec4 FragColor;
            
            void main() {
                FragColor = color;
            }
            """;

    public ShaderProgram(Type type) {
        this.type = type;
        this.programId = glCreateProgram();

        String vertexSource;
        String fragmentSource;

        switch (type) {
            case LINE -> {
                vertexSource = LINE_VERTEX_SHADER;
                fragmentSource = LINE_FRAGMENT_SHADER;
            }
            case UI -> {
                vertexSource = UI_VERTEX_SHADER;
                fragmentSource = UI_FRAGMENT_SHADER;
            }
            default -> {
                vertexSource = BLOCK_VERTEX_SHADER;
                fragmentSource = BLOCK_FRAGMENT_SHADER;
            }
        }

        int vertexShader = createShader(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentSource);

        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to link shader program: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int createShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setMatrix4f(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(glGetUniformLocation(programId, name), false, buffer);
        }
    }

    public void setVec3(String name, float x, float y, float z) {
        glUniform3f(glGetUniformLocation(programId, name), x, y, z);
    }

    public void setVec4(String name, float x, float y, float z, float w) {
        glUniform4f(glGetUniformLocation(programId, name), x, y, z, w);
    }

    public Type getType() {
        return type;
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}
