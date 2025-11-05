package com.minecraftclone.world;

public record RayCastResult(boolean isHit, int blockX, int blockY, int blockZ, int adjacentX, int adjacentY, int adjacentZ) {
    public static RayCastResult miss() {
        return new RayCastResult(false, 0, 0, 0, 0, 0, 0);
    }
}
