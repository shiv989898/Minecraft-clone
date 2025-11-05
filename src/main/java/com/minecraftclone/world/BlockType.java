package com.minecraftclone.world;

import org.joml.Vector3f;

public enum BlockType {
    AIR(new Vector3f(0.0f)),
    GRASS(new Vector3f(0.45f, 0.72f, 0.26f)),
    DIRT(new Vector3f(0.54f, 0.35f, 0.22f)),
    STONE(new Vector3f(0.6f, 0.6f, 0.6f)),
    BEDROCK(new Vector3f(0.1f, 0.1f, 0.1f));

    private final Vector3f color;

    BlockType(Vector3f color) {
        this.color = color;
    }

    public boolean isSolid() {
        return this != AIR;
    }

    public Vector3f color() {
        return color;
    }
}
