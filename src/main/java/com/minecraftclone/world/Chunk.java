package com.minecraftclone.world;

import com.minecraftclone.graphics.Mesh;
import com.minecraftclone.graphics.ShaderProgram;

import java.util.Arrays;

public final class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 96;

    private static final int[][] FACE_NORMALS = {
            {-1, 0, 0},
            {1, 0, 0},
            {0, -1, 0},
            {0, 1, 0},
            {0, 0, -1},
            {0, 0, 1}
    };

    private static final float[][][] FACE_VERTICES = {
            {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 0.0f}}, // -X
            {{1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 1.0f}}, // +X
            {{0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 1.0f}}, // -Y
            {{0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 0.0f}}, // +Y
            {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}}, // -Z
            {{0.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 1.0f}}  // +Z
    };

    private static final int[] QUAD_INDICES = {0, 1, 2, 0, 2, 3};

    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks;
    private final Mesh mesh;
    private boolean dirty;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[SIZE][HEIGHT][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Arrays.fill(blocks[x][y], BlockType.AIR);
            }
        }
        this.mesh = new Mesh();
        this.dirty = true;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (y < 0 || y >= HEIGHT) {
            return;
        }
        blocks[x][y][z] = type;
        dirty = true;
    }

    public BlockType getBlock(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT) {
            return BlockType.AIR;
        }
        return blocks[x][y][z];
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    public void rebuildMesh(World world) {
        FloatArrayBuilder builder = new FloatArrayBuilder();

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockType block = blocks[x][y][z];
                    if (!block.isSolid()) {
                        continue;
                    }
                    for (int face = 0; face < FACE_NORMALS.length; face++) {
                        int nx = FACE_NORMALS[face][0];
                        int ny = FACE_NORMALS[face][1];
                        int nz = FACE_NORMALS[face][2];

                        int worldX = chunkX * SIZE + x;
                        int worldZ = chunkZ * SIZE + z;
                        if (world.isSolidBlock(worldX + nx, y + ny, worldZ + nz)) {
                            continue;
                        }

                        float[][] vertices = FACE_VERTICES[face];
                        for (int index : QUAD_INDICES) {
                            float[] v = vertices[index];
                            builder.add(
                                    x + v[0], y + v[1], z + v[2],
                                    nx, ny, nz,
                                    block.color().x, block.color().y, block.color().z
                            );
                        }
                    }
                }
            }
        }

        float[] vertexData = builder.toArray();
        mesh.upload(vertexData);
        dirty = false;
    }

    public void render(ShaderProgram shader) {
        shader.setUniform("chunkOffset", chunkX * SIZE, 0.0f, chunkZ * SIZE);
        mesh.render();
    }

    public void cleanup() {
        mesh.cleanup();
    }

    private static final class FloatArrayBuilder {
        private float[] data = new float[1024];
        private int size;

        void add(float... values) {
            ensureCapacity(size + values.length);
            for (float value : values) {
                data[size++] = value;
            }
        }

        float[] toArray() {
            return Arrays.copyOf(data, size);
        }

        private void ensureCapacity(int target) {
            if (target > data.length) {
                int newCapacity = Math.max(data.length * 2, target);
                data = Arrays.copyOf(data, newCapacity);
            }
        }
    }
}
