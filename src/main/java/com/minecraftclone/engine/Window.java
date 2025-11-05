package com.minecraftclone.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.opengl.GL;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window {
    private final String title;
    private int width;
    private int height;
    private long handle;
    private final Input input;
    private boolean cursorCaptured;
    private float mouseDeltaX;
    private float mouseDeltaY;
    private double lastMouseX;
    private double lastMouseY;
    private boolean resized;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.input = new Input();
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        GLFWFramebufferSizeCallback fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int newWidth, int newHeight) {
                width = newWidth;
                height = newHeight;
                resized = true;
            }
        };
        glfwSetFramebufferSizeCallback(handle, fbCallback);

        GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key < 0) {
                    return;
                }
                if (action == GLFW_PRESS) {
                    input.keyStates[key] = true;
                    input.keyPressed[key] = true;
                } else if (action == GLFW_RELEASE) {
                    input.keyStates[key] = false;
                    input.keyReleased[key] = true;
                }
            }
        };
        glfwSetKeyCallback(handle, keyCallback);

        GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button < 0 || button >= input.mouseStates.length) {
                    return;
                }
                if (action == GLFW_PRESS) {
                    input.mouseStates[button] = true;
                    input.mousePressed[button] = true;
                } else if (action == GLFW_RELEASE) {
                    input.mouseStates[button] = false;
                    input.mouseReleased[button] = true;
                }
            }
        };
        glfwSetMouseButtonCallback(handle, mouseButtonCallback);

        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (cursorCaptured) {
                    if (Double.isNaN(lastMouseX) || Double.isNaN(lastMouseY)) {
                        lastMouseX = xpos;
                        lastMouseY = ypos;
                    }
                    mouseDeltaX += (float) (xpos - lastMouseX);
                    mouseDeltaY += (float) (ypos - lastMouseY);
                }
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        };
        glfwSetCursorPosCallback(handle, cursorPosCallback);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);
        glfwShowWindow(handle);
        GL.createCapabilities();
        glViewport(0, 0, width, height);
    }

    public void beginFrame() {
        mouseDeltaX = 0.0f;
        mouseDeltaY = 0.0f;
        input.resetPerFrameStates();
        resized = false;
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void cleanup() {
        glfwDestroyWindow(handle);
        glfwTerminate();
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
        }
    }

    public Input getInput() {
        return input;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isCursorCaptured() {
        return cursorCaptured;
    }

    public void setCursorCaptured(boolean capture) {
        this.cursorCaptured = capture;
        glfwSetInputMode(handle, GLFW_CURSOR, capture ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        Arrays.fill(input.mousePressed, false);
        Arrays.fill(input.mouseReleased, false);
        if (capture) {
            lastMouseX = Double.NaN;
            lastMouseY = Double.NaN;
        }
    }

    public float consumeMouseDeltaX() {
        float dx = mouseDeltaX;
        mouseDeltaX = 0.0f;
        return dx;
    }

    public float consumeMouseDeltaY() {
        float dy = mouseDeltaY;
        mouseDeltaY = 0.0f;
        return dy;
    }

    public boolean wasResized() {
        return resized;
    }

    public void applyResize() {
        glViewport(0, 0, width, height);
        resized = false;
    }

    public long getHandle() {
        return handle;
    }

    public float getAspectRatio() {
        return width > 0 && height > 0 ? (float) width / (float) height : 1.0f;
    }

    public static final class Input {
        private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST + 1;
        private static final int MAX_BUTTONS = GLFW.GLFW_MOUSE_BUTTON_LAST + 1;

        private final boolean[] keyStates = new boolean[MAX_KEYS];
        private final boolean[] keyPressed = new boolean[MAX_KEYS];
        private final boolean[] keyReleased = new boolean[MAX_KEYS];

        private final boolean[] mouseStates = new boolean[MAX_BUTTONS];
        private final boolean[] mousePressed = new boolean[MAX_BUTTONS];
        private final boolean[] mouseReleased = new boolean[MAX_BUTTONS];

        private Input() {
        }

        private void resetPerFrameStates() {
            Arrays.fill(keyPressed, false);
            Arrays.fill(keyReleased, false);
            Arrays.fill(mousePressed, false);
            Arrays.fill(mouseReleased, false);
        }

        public boolean isKeyDown(int key) {
            return key >= 0 && key < keyStates.length && keyStates[key];
        }

        public boolean isKeyPressed(int key) {
            return key >= 0 && key < keyPressed.length && keyPressed[key];
        }

        public boolean isKeyReleased(int key) {
            return key >= 0 && key < keyReleased.length && keyReleased[key];
        }

        public boolean isMouseDown(int button) {
            return button >= 0 && button < mouseStates.length && mouseStates[button];
        }

        public boolean isMousePressed(int button) {
            return button >= 0 && button < mousePressed.length && mousePressed[button];
        }

        public boolean isMouseReleased(int button) {
            return button >= 0 && button < mouseReleased.length && mouseReleased[button];
        }
    }
}
