package com.minecraftclone;

import com.minecraftclone.engine.Engine;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Engine engine = new Engine("Minecraft Clone", 1280, 720);
        engine.run();
    }
}
