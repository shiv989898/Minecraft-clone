package com.minecraft;

import com.minecraft.block.BlockType;
import com.minecraft.entity.Player;
import com.minecraft.render.Renderer;
import com.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private World world;
    private Player player;
    private Renderer renderer;
    private long window;
    private boolean firstMouse = true;
    private double lastX = 640, lastY = 360;
    
    private BlockType selectedBlock = BlockType.GRASS;
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;
    private double lastClickTime = 0;
    private Vector3i targetedBlock;
    private Vector3i placementPreview;

    public Game(long window) {
        this.window = window;
        this.world = new World();
        
        // Spawn player at a good height to see the world (higher up)
        this.player = new Player(new Vector3f(0, 100, 0));
        this.player.setWorld(world);  // Connect player to world for collision
        this.player.toggleFlying();  // Start in flying mode to avoid falling
        
        this.renderer = new Renderer(window);

        setupCallbacks();
        
        System.out.println("Game initialized!");
        System.out.println("Player spawned at: " + player.getPosition());
        System.out.println("World has " + world.getChunks().size() + " chunks loaded");
        System.out.println("Controls:");
        System.out.println("WASD - Move");
        System.out.println("Space - Jump / Ascend");
        System.out.println("Shift - Sneak/Descend (Flying)");
        System.out.println("F - Toggle Flying (Creative Mode)");
        System.out.println("Left Click - Break Block");
        System.out.println("Right Click - Place Block");
        System.out.println("1-7 - Select Block Type");
        System.out.println("ESC - Exit");
        System.out.println("\n*** Starting in FLYING MODE - Press F to toggle ***");
    }

    private void setupCallbacks() {
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_1 -> selectedBlock = BlockType.GRASS;
                    case GLFW_KEY_2 -> selectedBlock = BlockType.DIRT;
                    case GLFW_KEY_3 -> selectedBlock = BlockType.STONE;
                    case GLFW_KEY_4 -> selectedBlock = BlockType.WOOD;
                    case GLFW_KEY_5 -> selectedBlock = BlockType.LEAVES;
                    case GLFW_KEY_6 -> selectedBlock = BlockType.SAND;
                    case GLFW_KEY_7 -> selectedBlock = BlockType.COBBLESTONE;
                    case GLFW_KEY_F -> {
                        player.toggleFlying();
                        System.out.println("Flying mode: " + (player.isFlying() ? "ON" : "OFF"));
                    }
                }
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (firstMouse) {
                lastX = xpos;
                lastY = ypos;
                firstMouse = false;
            }

            double xoffset = xpos - lastX;
            double yoffset = lastY - ypos;
            lastX = xpos;
            lastY = ypos;

            player.processMouse((float) xoffset, (float) yoffset);
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                leftMousePressed = (action == GLFW_PRESS);
                if (action == GLFW_PRESS) {
                    breakBlock();  // Immediate break on click
                }
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                rightMousePressed = (action == GLFW_PRESS);
                if (action == GLFW_PRESS) {
                    placeBlock();  // Immediate place on click
                }
            }
        });
    }

    public void update(float deltaTime) {
        // Update chunks around player
        Vector3f playerPos = player.getPosition();
        world.updateChunks(playerPos.x, playerPos.z);
        
        // Process movement
        boolean forward = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
        boolean backward = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
        boolean left = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
        boolean right = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
        boolean jump = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
        boolean sneak = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;

        player.processMovement(forward, backward, left, right, jump, sneak, deltaTime);

    targetedBlock = raycast(6.0f, false);
    placementPreview = raycast(6.0f, true);

        // Handle held mouse buttons with delay for continuous breaking/placing
        double currentTime = glfwGetTime();
        if (currentTime - lastClickTime > 0.25) {  // Slower repeat rate
            if (leftMousePressed) {
                breakBlock();
                lastClickTime = currentTime;
            }
            if (rightMousePressed) {
                placeBlock();
                lastClickTime = currentTime;
            }
        }
    }

    private void breakBlock() {
        Vector3i blockPos = getCurrentTarget(false);
        if (blockPos != null) {
            BlockType block = world.getBlock(blockPos.x, blockPos.y, blockPos.z);
            System.out.println("Breaking block at " + blockPos + " (was " + block + ")");
            world.setBlock(blockPos.x, blockPos.y, blockPos.z, BlockType.AIR);
            world.markChunkForRebuild(blockPos.x >> 4, blockPos.z >> 4);
        } else {
            System.out.println("No block in range to break");
        }
    }

    private void placeBlock() {
        Vector3i blockPos = getCurrentTarget(true);
        if (blockPos != null) {
            // Make sure we're not placing a block where the player is
            Vector3f playerPos = player.getPosition();
            int playerX = (int) Math.floor(playerPos.x);
            int playerY = (int) Math.floor(playerPos.y);
            int playerZ = (int) Math.floor(playerPos.z);
            
            // Check both feet and head positions
            if ((blockPos.x == playerX && blockPos.z == playerZ && 
                (blockPos.y == playerY || blockPos.y == playerY + 1))) {
                System.out.println("Can't place block - would collide with player");
                return;  // Don't place block inside player
            }
            
            System.out.println("Placing " + selectedBlock + " at " + blockPos);
            world.setBlock(blockPos.x, blockPos.y, blockPos.z, selectedBlock);
            world.markChunkForRebuild(blockPos.x >> 4, blockPos.z >> 4);
        } else {
            System.out.println("No valid position to place block");
        }
    }

    private Vector3i getCurrentTarget(boolean placeMode) {
        Vector3i source = placeMode ? placementPreview : targetedBlock;
        if (source == null) {
            source = raycast(6.0f, placeMode);
            if (placeMode) {
                placementPreview = source != null ? new Vector3i(source) : null;
            } else {
                targetedBlock = source != null ? new Vector3i(source) : null;
            }
        }
        return source != null ? new Vector3i(source) : null;
    }

    private Vector3i raycast(float maxDistance, boolean placeMode) {
        Vector3f pos = player.getEyePosition();  // Start from eye position
        Vector3f dir = player.getFront();
        
        float step = 0.05f;  // Smaller step for more accuracy
        int steps = (int) (maxDistance / step);
        
        Vector3i lastAirBlock = null;
        
        for (int i = 0; i < steps; i++) {
            pos.add(dir.x * step, dir.y * step, dir.z * step);
            
            int x = (int) Math.floor(pos.x);
            int y = (int) Math.floor(pos.y);
            int z = (int) Math.floor(pos.z);
            
            BlockType block = world.getBlock(x, y, z);
            
            if (placeMode && block != BlockType.AIR && lastAirBlock != null) {
                return lastAirBlock;
            } else if (!placeMode && block != BlockType.AIR) {
                return new Vector3i(x, y, z);
            }
            
            if (block == BlockType.AIR) {
                lastAirBlock = new Vector3i(x, y, z);
            }
        }
        
        return null;
    }

    public void render() {
        renderer.render(world, player, targetedBlock);
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
