package com.minecraft.entity;

import org.joml.Vector3f;
import com.minecraft.world.World;
import com.minecraft.block.BlockType;

public class Player {
    private Vector3f position;
    private Vector3f velocity;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    
    private float yaw = -90.0f;
    private float pitch = -30.0f;  // Look down initially to see terrain
    
    private float movementSpeed = 4.317f;  // Minecraft walking speed (blocks/sec)
    private float sprintSpeed = 5.612f;    // Minecraft sprinting speed
    private float flyingSpeed = 10.89f;    // Minecraft flying speed
    private float mouseSensitivity = 0.1f;
    
    private static final float GRAVITY = -32.0f;  // Stronger gravity like Minecraft
    private static final float JUMP_VELOCITY = 10.0f;  // Jump velocity
    private static final float TERMINAL_VELOCITY = -78.4f;  // Max fall speed
    
    private boolean isOnGround = false;
    private boolean isFlying = false;  // Creative mode flying
    private boolean isSprinting = false;  // Sprint mode
    
    // Player dimensions (Minecraft sizes)
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float PLAYER_HEIGHT = 1.8f;
    private static final float PLAYER_EYE_HEIGHT = 1.62f;
    
    private World world;
    private static boolean collisionDebugPrinted = false;
    
    public Player(Vector3f position) {
        this.position = position;
        this.velocity = new Vector3f(0, 0, 0);
        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);
        this.right = new Vector3f();
        updateVectors();
    }
    
    public void setWorld(World world) {
        this.world = world;
    }
    
    public void processMovement(boolean forward, boolean backward, boolean left, 
                                boolean right, boolean jump, boolean sneak, float deltaTime) {
        // Auto-sprint when double-tapping forward (simplified: sprint when moving forward)
        isSprinting = forward && !isFlying && !sneak;
        
        float speed = movementSpeed;
        if (isFlying) {
            speed = flyingSpeed;  // Faster in creative
        } else if (isSprinting) {
            speed = sprintSpeed;  // Sprint speed
        }
        
        float moveAmount = speed * deltaTime;
        
        Vector3f horizontalFront = new Vector3f(front.x, 0, front.z).normalize();
        Vector3f horizontalRight = new Vector3f(this.right.x, 0, this.right.z).normalize();
        
        // Calculate desired movement
        Vector3f movement = new Vector3f(0, 0, 0);
        
        if (forward) {
            movement.add(horizontalFront.mul(moveAmount, new Vector3f()));
        }
        if (backward) {
            movement.sub(horizontalFront.mul(moveAmount, new Vector3f()));
        }
        if (left) {
            movement.sub(horizontalRight.mul(moveAmount, new Vector3f()));
        }
        if (right) {
            movement.add(horizontalRight.mul(moveAmount, new Vector3f()));
        }
        
        // Handle flying mode
        if (isFlying) {
            if (jump) {
                movement.y += moveAmount;
            }
            if (sneak) {
                movement.y -= moveAmount;
            }
            velocity.y = 0;  // No gravity in flying
        } else {
            // Handle jumping (only when on ground)
            if (jump && isOnGround) {
                velocity.y = JUMP_VELOCITY;
                isOnGround = false;
            }
            
            // Apply gravity
            velocity.y += GRAVITY * deltaTime;
            if (velocity.y < TERMINAL_VELOCITY) {
                velocity.y = TERMINAL_VELOCITY;
            }
            
            movement.y = velocity.y * deltaTime;
        }
        
        // Apply movement with collision detection
        if (world != null) {
            applyMovementWithCollision(movement);
        } else {
            position.add(movement);
        }
    }
    
    private void applyMovementWithCollision(Vector3f movement) {
        // Apply X movement
        position.x += movement.x;
        if (checkCollision()) {
            position.x -= movement.x;
            if (!collisionDebugPrinted) {
                System.out.println("Collision detected on X axis at " + position);
                collisionDebugPrinted = true;
            }
        }
        
        // Apply Y movement (vertical)
        position.y += movement.y;
        if (checkCollision()) {
            if (movement.y < 0) {
                isOnGround = true;
                if (!collisionDebugPrinted) {
                    System.out.println("Player landed on ground at " + position);
                    collisionDebugPrinted = true;
                }
            }
            position.y -= movement.y;
            velocity.y = 0;
        } else {
            // Check if still on ground by looking slightly below
            float oldY = position.y;
            position.y -= 0.1f;
            if (checkCollision()) {
                isOnGround = true;
                position.y = oldY;
            } else {
                isOnGround = false;
                position.y = oldY;
            }
        }
        
        // Apply Z movement
        position.z += movement.z;
        if (checkCollision()) {
            position.z -= movement.z;
            if (!collisionDebugPrinted) {
                System.out.println("Collision detected on Z axis at " + position);
                collisionDebugPrinted = true;
            }
        }
    }
    
    private boolean checkCollision() {
        if (world == null) return false;
        
        // Check collision with player bounding box
        float halfWidth = PLAYER_WIDTH / 2.0f;
        
        // Check multiple points around the player's bounding box
        float[][] checkPoints = {
            // Bottom corners
            {-halfWidth, 0.0f, -halfWidth},
            {halfWidth, 0.0f, -halfWidth},
            {-halfWidth, 0.0f, halfWidth},
            {halfWidth, 0.0f, halfWidth},
            // Middle corners
            {-halfWidth, PLAYER_HEIGHT/2, -halfWidth},
            {halfWidth, PLAYER_HEIGHT/2, -halfWidth},
            {-halfWidth, PLAYER_HEIGHT/2, halfWidth},
            {halfWidth, PLAYER_HEIGHT/2, halfWidth},
            // Top corners
            {-halfWidth, PLAYER_HEIGHT - 0.1f, -halfWidth},
            {halfWidth, PLAYER_HEIGHT - 0.1f, -halfWidth},
            {-halfWidth, PLAYER_HEIGHT - 0.1f, halfWidth},
            {halfWidth, PLAYER_HEIGHT - 0.1f, halfWidth},
            // Center points
            {0, 0, 0},
            {0, PLAYER_HEIGHT/2, 0},
            {0, PLAYER_HEIGHT - 0.1f, 0}
        };
        
        for (float[] point : checkPoints) {
            int blockX = (int) Math.floor(position.x + point[0]);
            int blockY = (int) Math.floor(position.y + point[1]);
            int blockZ = (int) Math.floor(position.z + point[2]);
            
            BlockType block = world.getBlock(blockX, blockY, blockZ);
            if (block != BlockType.AIR && block.isSolid()) {
                return true;
            }
        }
        
        return false;
    }
    
    public void toggleFlying() {
        isFlying = !isFlying;
        if (isFlying) {
            velocity.y = 0;
        }
    }
    
    public boolean isFlying() {
        return isFlying;
    }
    
    public boolean isOnGround() {
        return isOnGround;
    }
    
    public Vector3f getEyePosition() {
        return new Vector3f(position.x, position.y + PLAYER_EYE_HEIGHT, position.z);
    }
    
    public void processMouse(float xoffset, float yoffset) {
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;
        
        yaw += xoffset;
        pitch += yoffset;
        
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
        
        updateVectors();
    }
    
    private void updateVectors() {
        Vector3f newFront = new Vector3f();
        newFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newFront.y = (float) Math.sin(Math.toRadians(pitch));
        newFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        
        front = newFront.normalize();
        right = front.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();
        up = right.cross(front, new Vector3f()).normalize();
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public Vector3f getFront() {
        return front;
    }
    
    public Vector3f getUp() {
        return up;
    }
    
    public Vector3f getRight() {
        return right;
    }
}
