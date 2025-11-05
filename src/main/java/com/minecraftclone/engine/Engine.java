package com.minecraftclone.engine;

import com.minecraftclone.graphics.Camera;
import com.minecraftclone.graphics.ShaderProgram;
import com.minecraftclone.player.Player;
import com.minecraftclone.world.BlockType;
import com.minecraftclone.world.RayCastResult;
import com.minecraftclone.world.Raycaster;
import com.minecraftclone.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public final class Engine {
    private static final float FIELD_OF_VIEW = (float) Math.toRadians(70.0f);
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 500.0f;
    private static final float BLOCK_ACTION_COOLDOWN = 0.18f;

    private final Window window;
    private ShaderProgram chunkShader;
    private Camera camera;
    private World world;
    private Player player;
    private float elapsedSinceBlockAction;

    public Engine(String title, int width, int height) {
        this.window = new Window(title, width, height);
    }

    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    private void init() {
        window.init();
        window.setCursorCaptured(true);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glClearColor(0.53f, 0.81f, 0.92f, 0.0f);

        camera = new Camera(FIELD_OF_VIEW, window.getAspectRatio(), Z_NEAR, Z_FAR);
        world = new World();
        player = new Player(new Vector3f(0.5f, 45.0f, 0.5f));

        chunkShader = ShaderProgram.createChunkShader();
        chunkShader.bind();
        chunkShader.setUniform("projection", camera.getProjectionMatrix());
        chunkShader.unbind();
    }

    private void loop() {
        long lastTime = System.nanoTime();
        while (!window.shouldClose()) {
            window.beginFrame();

            long now = System.nanoTime();
            float deltaTime = (now - lastTime) / 1_000_000_000.0f;
            lastTime = now;

            if (window.wasResized()) {
                window.applyResize();
                camera.setAspectRatio(window.getAspectRatio());
                chunkShader.bind();
                chunkShader.setUniform("projection", camera.getProjectionMatrix());
                chunkShader.unbind();
            }

            window.pollEvents();

            handleGlobalInputs();

            player.update(window, world, deltaTime, camera);
            world.update(player.getPosition());

            handleBlockInteractions(deltaTime);

            render();
            window.swapBuffers();
        }
    }

    private void handleGlobalInputs() {
        Window.Input input = window.getInput();
        if (input.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            boolean capture = !window.isCursorCaptured();
            window.setCursorCaptured(capture);
            if (!capture) {
                input.isKeyPressed(GLFW.GLFW_KEY_ESCAPE); // consume state
            }
        }
        if (!window.isCursorCaptured() && window.getInput().isMousePressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            window.setCursorCaptured(true);
        }
    }

    private void handleBlockInteractions(float deltaTime) {
        elapsedSinceBlockAction += deltaTime;
        if (elapsedSinceBlockAction < BLOCK_ACTION_COOLDOWN) {
            return;
        }

        Window.Input input = window.getInput();
        if (!window.isCursorCaptured()) {
            return;
        }

        Vector3f eyePos = player.getEyePosition();
        Vector3f viewDir = player.getViewDirection();
        RayCastResult hit = Raycaster.raycast(world, eyePos, viewDir, 5.0f);

        if (!hit.isHit()) {
            return;
        }

        if (input.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            world.setBlock(hit.blockX(), hit.blockY(), hit.blockZ(), BlockType.AIR);
            elapsedSinceBlockAction = 0.0f;
        } else if (input.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_2)) {
            int targetX = hit.adjacentX();
            int targetY = hit.adjacentY();
            int targetZ = hit.adjacentZ();
            if (!player.intersectsBlock(targetX, targetY, targetZ)) {
                world.setBlock(targetX, targetY, targetZ, BlockType.STONE);
                elapsedSinceBlockAction = 0.0f;
            }
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        chunkShader.bind();
        chunkShader.setUniform("view", camera.getViewMatrix());
        chunkShader.setUniform("cameraPosition", camera.getPosition());
        world.render(chunkShader, camera.getPosition());
        chunkShader.unbind();
    }

    private void cleanup() {
        if (chunkShader != null) {
            chunkShader.cleanup();
        }
        if (world != null) {
            world.cleanup();
        }
        window.cleanup();
    }
}
