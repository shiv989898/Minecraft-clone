package com.minecraftclone.player;

import com.minecraftclone.engine.Window;
import com.minecraftclone.graphics.Camera;
import com.minecraftclone.world.World;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public final class Player {
    private static final float MOVE_SPEED = 6.0f;
    private static final float SPRINT_MULTIPLIER = 1.6f;
    private static final float GRAVITY = 32.0f;
    private static final float JUMP_STRENGTH = 9.0f;
    private static final float MOUSE_SENSITIVITY = 0.08f;
    private static final float PLAYER_HEIGHT = 1.75f;
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float PLAYER_EYE_OFFSET = 1.55f;
    private static final float STEP_OFFSET = 0.5f;

    private final Vector3f position;
    private final Vector3f velocity;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public Player(Vector3f initialPosition) {
        this.position = new Vector3f(initialPosition);
        this.velocity = new Vector3f();
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }

    public void update(Window window, World world, float deltaTime, Camera camera) {
        handleMouse(window, camera);
        handleMovement(window, world, deltaTime);
        applyPhysics(world, deltaTime);
        syncCamera(camera);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getEyePosition() {
        return new Vector3f(position.x, position.y + PLAYER_EYE_OFFSET, position.z);
    }

    public Vector3f getViewDirection() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        return new Vector3f(
                (float) (Math.cos(pitchRad) * Math.cos(yawRad)),
                (float) Math.sin(pitchRad),
                (float) (Math.cos(pitchRad) * Math.sin(yawRad))
        ).normalize();
    }

    public boolean intersectsBlock(int blockX, int blockY, int blockZ) {
        float minX = blockX;
        float maxX = blockX + 1.0f;
        float minY = blockY;
        float maxY = blockY + 1.0f;
        float minZ = blockZ;
        float maxZ = blockZ + 1.0f;

        float playerMinX = position.x - PLAYER_WIDTH * 0.5f;
        float playerMaxX = position.x + PLAYER_WIDTH * 0.5f;
        float playerMinY = position.y;
        float playerMaxY = position.y + PLAYER_HEIGHT;
        float playerMinZ = position.z - PLAYER_WIDTH * 0.5f;
        float playerMaxZ = position.z + PLAYER_WIDTH * 0.5f;

        return playerMaxX > minX && playerMinX < maxX
                && playerMaxY > minY && playerMinY < maxY
                && playerMaxZ > minZ && playerMinZ < maxZ;
    }

    private void handleMouse(Window window, Camera camera) {
        if (!window.isCursorCaptured()) {
            return;
        }
        float deltaX = window.consumeMouseDeltaX();
        float deltaY = window.consumeMouseDeltaY();
        yaw = (yaw + deltaX * MOUSE_SENSITIVITY) % 360.0f;
        pitch -= deltaY * MOUSE_SENSITIVITY;
        pitch = clamp(pitch, -89.0f, 89.0f);
        camera.setRotation(yaw, pitch);
    }

    private void handleMovement(Window window, World world, float deltaTime) {
        Window.Input input = window.getInput();

        if (input.isKeyPressed(GLFW_KEY_GRAVE_ACCENT)) {
            window.requestClose();
        }

        Vector3f forward = getForwardVector();
        Vector3f right = new Vector3f(forward).cross(new Vector3f(0.0f, 1.0f, 0.0f)).normalize();

        Vector3f desiredVelocity = new Vector3f();
        if (input.isKeyDown(GLFW_KEY_W)) {
            desiredVelocity.add(forward);
        }
        if (input.isKeyDown(GLFW_KEY_S)) {
            desiredVelocity.sub(forward);
        }
        if (input.isKeyDown(GLFW_KEY_D)) {
            desiredVelocity.add(right);
        }
        if (input.isKeyDown(GLFW_KEY_A)) {
            desiredVelocity.sub(right);
        }

        float speed = MOVE_SPEED;
        if (input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            speed *= SPRINT_MULTIPLIER;
        }

        if (desiredVelocity.lengthSquared() > 0.0f) {
            desiredVelocity.normalize(speed);
        }

        velocity.x = desiredVelocity.x;
        velocity.z = desiredVelocity.z;

        if (onGround && input.isKeyPressed(GLFW_KEY_SPACE)) {
            velocity.y = JUMP_STRENGTH;
            onGround = false;
        }
    }

    private Vector3f getForwardVector() {
        float yawRad = (float) Math.toRadians(yaw);
        return new Vector3f((float) Math.cos(yawRad), 0.0f, (float) Math.sin(yawRad)).normalize();
    }

    private void applyPhysics(World world, float deltaTime) {
        velocity.y -= GRAVITY * deltaTime;
        if (velocity.y < -60.0f) {
            velocity.y = -60.0f;
        }

        Vector3f nextPosition = new Vector3f(position);
        onGround = false;
        nextPosition.x += velocity.x * deltaTime;
        moveAxis(world, nextPosition, Axis.X);

        nextPosition.y += velocity.y * deltaTime;
        moveAxis(world, nextPosition, Axis.Y);

        nextPosition.z += velocity.z * deltaTime;
        moveAxis(world, nextPosition, Axis.Z);

        position.set(nextPosition);
    }

    private void moveAxis(World world, Vector3f nextPosition, Axis axis) {
        float halfWidth = PLAYER_WIDTH * 0.5f;
        float minX = nextPosition.x - halfWidth;
        float maxX = nextPosition.x + halfWidth;
        float minY = nextPosition.y;
        float maxY = nextPosition.y + PLAYER_HEIGHT;
        float minZ = nextPosition.z - halfWidth;
    float maxZ = nextPosition.z + halfWidth;

        int startX = (int) Math.floor(minX - STEP_OFFSET);
        int endX = (int) Math.ceil(maxX + STEP_OFFSET);
        int startY = (int) Math.floor(minY - STEP_OFFSET);
        int endY = (int) Math.ceil(maxY + STEP_OFFSET);
        int startZ = (int) Math.floor(minZ - STEP_OFFSET);
        int endZ = (int) Math.ceil(maxZ + STEP_OFFSET);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                for (int z = startZ; z < endZ; z++) {
                    if (!world.isSolidBlock(x, y, z)) {
                        continue;
                    }
                    float blockMinX = x;
                    float blockMaxX = x + 1.0f;
                    float blockMinY = y;
                    float blockMaxY = y + 1.0f;
                    float blockMinZ = z;
                    float blockMaxZ = z + 1.0f;

                    if (maxX <= blockMinX || minX >= blockMaxX
                            || maxY <= blockMinY || minY >= blockMaxY
                            || maxZ <= blockMinZ || minZ >= blockMaxZ) {
                        continue;
                    }

                    if (axis == Axis.X) {
                        if (velocity.x > 0.0f) {
                            nextPosition.x = blockMinX - PLAYER_WIDTH * 0.5f;
                        } else if (velocity.x < 0.0f) {
                            nextPosition.x = blockMaxX + PLAYER_WIDTH * 0.5f;
                        }
                        velocity.x = 0.0f;
                    } else if (axis == Axis.Y) {
                        if (velocity.y > 0.0f) {
                            nextPosition.y = blockMinY - PLAYER_HEIGHT;
                        } else if (velocity.y < 0.0f) {
                            nextPosition.y = blockMaxY;
                            onGround = true;
                        }
                        velocity.y = 0.0f;
                    } else if (axis == Axis.Z) {
                        if (velocity.z > 0.0f) {
                            nextPosition.z = blockMinZ - PLAYER_WIDTH * 0.5f;
                        } else if (velocity.z < 0.0f) {
                            nextPosition.z = blockMaxZ + PLAYER_WIDTH * 0.5f;
                        }
                        velocity.z = 0.0f;
                    }
                }
            }
        }
    }

    private void syncCamera(Camera camera) {
        camera.setPosition(getEyePosition());
        camera.setRotation(yaw, pitch);
    }

    private static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private enum Axis {
        X, Y, Z
    }
}
