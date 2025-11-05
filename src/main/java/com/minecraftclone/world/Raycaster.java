package com.minecraftclone.world;

import org.joml.Vector3f;

public final class Raycaster {
    private static final float STEP_LENGTH = 0.05f;

    private Raycaster() {
    }

    public static RayCastResult raycast(World world, Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f dir = new Vector3f(direction);
        if (dir.lengthSquared() == 0.0f) {
            return RayCastResult.miss();
        }
        dir.normalize();

        Vector3f currentPos = new Vector3f(origin);

        int previousX = (int) Math.floor(currentPos.x);
        int previousY = (int) Math.floor(currentPos.y);
        int previousZ = (int) Math.floor(currentPos.z);

        int steps = Math.max(1, (int) (maxDistance / STEP_LENGTH));
        for (int i = 0; i < steps; i++) {
            currentPos.fma(STEP_LENGTH, dir);
            int blockX = (int) Math.floor(currentPos.x);
            int blockY = (int) Math.floor(currentPos.y);
            int blockZ = (int) Math.floor(currentPos.z);

            if (blockX == previousX && blockY == previousY && blockZ == previousZ) {
                continue;
            }

            if (world.isSolidBlock(blockX, blockY, blockZ)) {
                return new RayCastResult(true, blockX, blockY, blockZ, previousX, previousY, previousZ);
            }

            previousX = blockX;
            previousY = blockY;
            previousZ = blockZ;
        }

        return RayCastResult.miss();
    }
}
