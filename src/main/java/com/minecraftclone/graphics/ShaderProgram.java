package com.minecraftclone.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram {
    private final int programId;
    private final Map<String, Integer> uniformLocations;

    private ShaderProgram(int vertexShaderId, int fragmentShaderId) {
        this.programId = glCreateProgram();
        if (programId == 0) {
            throw new IllegalStateException("Could not create shader program");
        }

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        int linkStatus = glGetProgrami(programId, GL_LINK_STATUS);
        if (linkStatus == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(programId);
            throw new IllegalStateException("Shader program linking failed: " + infoLog);
        }

        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);

        this.uniformLocations = new HashMap<>();
    }

    public static ShaderProgram createChunkShader() {
        String vertexShaderSource = "#version 330 core\n" +
                "layout(location = 0) in vec3 inPosition;\n" +
                "layout(location = 1) in vec3 inNormal;\n" +
                "layout(location = 2) in vec3 inColor;\n" +
                "uniform mat4 projection;\n" +
                "uniform mat4 view;\n" +
                "uniform vec3 chunkOffset;\n" +
                "out vec3 fragColor;\n" +
                "out vec3 fragNormal;\n" +
                "out vec3 fragPosition;\n" +
                "void main() {\n" +
                "    vec3 worldPos = chunkOffset + inPosition;\n" +
                "    fragColor = inColor;\n" +
                "    fragNormal = inNormal;\n" +
                "    fragPosition = worldPos;\n" +
                "    gl_Position = projection * view * vec4(worldPos, 1.0);\n" +
                "}";

        String fragmentShaderSource = "#version 330 core\n" +
                "in vec3 fragColor;\n" +
                "in vec3 fragNormal;\n" +
                "in vec3 fragPosition;\n" +
                "uniform vec3 cameraPosition;\n" +
                "out vec4 outColor;\n" +
                "void main() {\n" +
                "    vec3 lightDir = normalize(vec3(0.4, 1.0, 0.3));\n" +
                "    float diff = max(dot(normalize(fragNormal), lightDir), 0.2);\n" +
                "    vec3 baseColor = fragColor * diff;\n" +
                "    float distanceToCamera = length(fragPosition - cameraPosition);\n" +
                "    float fogFactor = clamp((distanceToCamera - 45.0) / 160.0, 0.0, 1.0);\n" +
                "    vec3 fogColor = vec3(0.53, 0.81, 0.92);\n" +
                "    vec3 finalColor = mix(baseColor, fogColor, fogFactor);\n" +
                "    outColor = vec4(finalColor, 1.0);\n" +
                "}";

        int vertexShaderId = compileShader(vertexShaderSource, GL_VERTEX_SHADER);
        int fragmentShaderId = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER);
        return new ShaderProgram(vertexShaderId, fragmentShaderId);
    }

    private static int compileShader(String source, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shaderId);
            throw new IllegalStateException("Shader compilation failed: " + infoLog);
        }
        return shaderId;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(String name, Matrix4f value) {
        int location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniform(String name, Vector3f value) {
        int location = getUniformLocation(name);
        glUniform3f(location, value.x, value.y, value.z);
    }

    public void setUniform(String name, float x, float y, float z) {
        int location = getUniformLocation(name);
        glUniform3f(location, x, y, z);
    }

    private int getUniformLocation(String name) {
        Integer cached = uniformLocations.get(name);
        if (cached != null) {
            return cached;
        }
        int location = glGetUniformLocation(programId, name);
        if (location < 0) {
            throw new IllegalArgumentException("Uniform not found: " + name);
        }
        uniformLocations.put(name, location);
        return location;
    }

    public void cleanup() {
        unbind();
        glDeleteProgram(programId);
    }
}
