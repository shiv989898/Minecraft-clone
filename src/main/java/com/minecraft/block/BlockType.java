package com.minecraft.block;

public enum BlockType {
    AIR(0, false, new float[0][]),
    GRASS(1, true, new float[][] {
        {0.2f, 0.8f, 0.2f},  // Top - green
        {0.6f, 0.4f, 0.2f},  // Bottom - dirt
        {0.45f, 0.6f, 0.3f}, // Sides - grass side
        {0.45f, 0.6f, 0.3f},
        {0.45f, 0.6f, 0.3f},
        {0.45f, 0.6f, 0.3f}
    }),
    DIRT(2, true, new float[][] {
        {0.6f, 0.4f, 0.2f},
        {0.6f, 0.4f, 0.2f},
        {0.6f, 0.4f, 0.2f},
        {0.6f, 0.4f, 0.2f},
        {0.6f, 0.4f, 0.2f},
        {0.6f, 0.4f, 0.2f}
    }),
    STONE(3, true, new float[][] {
        {0.5f, 0.5f, 0.5f},
        {0.5f, 0.5f, 0.5f},
        {0.5f, 0.5f, 0.5f},
        {0.5f, 0.5f, 0.5f},
        {0.5f, 0.5f, 0.5f},
        {0.5f, 0.5f, 0.5f}
    }),
    WOOD(4, true, new float[][] {
        {0.4f, 0.25f, 0.1f},
        {0.4f, 0.25f, 0.1f},
        {0.5f, 0.35f, 0.2f},
        {0.5f, 0.35f, 0.2f},
        {0.5f, 0.35f, 0.2f},
        {0.5f, 0.35f, 0.2f}
    }),
    LEAVES(5, true, new float[][] {
        {0.1f, 0.5f, 0.1f},
        {0.1f, 0.5f, 0.1f},
        {0.1f, 0.5f, 0.1f},
        {0.1f, 0.5f, 0.1f},
        {0.1f, 0.5f, 0.1f},
        {0.1f, 0.5f, 0.1f}
    }),
    SAND(6, true, new float[][] {
        {0.9f, 0.85f, 0.6f},
        {0.9f, 0.85f, 0.6f},
        {0.9f, 0.85f, 0.6f},
        {0.9f, 0.85f, 0.6f},
        {0.9f, 0.85f, 0.6f},
        {0.9f, 0.85f, 0.6f}
    }),
    COBBLESTONE(7, true, new float[][] {
        {0.4f, 0.4f, 0.4f},
        {0.4f, 0.4f, 0.4f},
        {0.4f, 0.4f, 0.4f},
        {0.4f, 0.4f, 0.4f},
        {0.4f, 0.4f, 0.4f},
        {0.4f, 0.4f, 0.4f}
    });

    private final int id;
    private final boolean isSolid;
    private final float[][] faceColors; // Top, Bottom, North, South, East, West

    BlockType(int id, boolean isSolid, float[][] faceColors) {
        this.id = id;
        this.isSolid = isSolid;
        this.faceColors = faceColors;
    }

    public int getId() {
        return id;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public float[] getFaceColor(int face) {
        if (faceColors.length == 0) return new float[]{1, 1, 1};
        return faceColors[face];
    }

    public static BlockType fromId(int id) {
        for (BlockType type : values()) {
            if (type.id == id) return type;
        }
        return AIR;
    }
}
