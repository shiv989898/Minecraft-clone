# Minecraft Clone (Java Edition)

A fully-featured 3D Minecraft clone built with Java, LWJGL 3, and OpenGL.

## Features

### World Generation
- Chunk-based world system (16x128x16 blocks per chunk)
- Procedural terrain generation with multiple octaves
- Natural-looking hills and valleys
- Random tree generation
- Beach biomes with sand
- Render distance of 8 chunks

### Gameplay
- First-person camera controls
- Block breaking and placement
- 7 different block types:
  - Grass
  - Dirt
  - Stone
  - Wood (Oak logs)
  - Leaves
  - Sand
  - Cobblestone
- Ray-casting for precise block selection
- Creative mode (fly around freely)

### Technical Features
- Modern OpenGL 3.3 Core Profile
- Efficient chunk meshing (only visible faces rendered)
- Frustum culling for performance
- Shader-based rendering
- 60 FPS target with V-Sync

## Requirements

- Java 17 or higher
- Maven 3.6+
- Windows (configured for Windows natives, but can be adapted for Linux/Mac)

## Building and Running

### Option 1: Using Maven

```bash
# Compile the project
mvn clean compile

# Run the game
mvn exec:java
```

### Option 2: Using Maven Package

```bash
# Build JAR
mvn clean package

# Run the JAR
java -jar target/minecraft-clone-1.0-SNAPSHOT.jar
```

## Controls

### Movement
- **W** - Move forward
- **S** - Move backward
- **A** - Strafe left
- **D** - Strafe right
- **SPACE** - Fly up (Creative mode)
- **LEFT SHIFT** - Fly down (Creative mode)
- **MOUSE** - Look around

### Building
- **LEFT CLICK** - Break block
- **RIGHT CLICK** - Place block

### Block Selection (Number Keys)
- **1** - Grass
- **2** - Dirt
- **3** - Stone
- **4** - Wood
- **5** - Leaves
- **6** - Sand
- **7** - Cobblestone

### Other
- **ESC** - Exit game

## Architecture

```
src/main/java/com/minecraft/
├── Main.java              # Entry point and GLFW window setup
├── Game.java              # Game logic and input handling
├── block/
│   └── BlockType.java     # Block definitions and properties
├── entity/
│   └── Player.java        # Player movement and camera
├── world/
│   ├── World.java         # World management and chunk loading
│   └── Chunk.java         # Chunk data and terrain generation
└── render/
    ├── Renderer.java      # Main rendering loop
    ├── ShaderProgram.java # Shader compilation and management
    └── ChunkMesh.java     # Chunk mesh generation and rendering
```

## Performance

- Optimized chunk meshing only generates visible block faces
- Only chunks near the player are generated
- Mesh rebuilding only occurs when blocks change
- Target: 60 FPS on modern hardware

## Future Enhancements

Potential features to add:
- [ ] Day/night cycle
- [ ] Lighting system
- [ ] More biomes (desert, snow, forest)
- [ ] Water simulation
- [ ] Survival mode (health, hunger)
- [ ] Mob entities
- [ ] Crafting system
- [ ] World saving/loading
- [ ] Multiplayer support
- [ ] Texture atlas instead of solid colors

## Credits

Built with:
- LWJGL 3 - Lightweight Java Game Library
- JOML - Java OpenGL Math Library
- OpenGL 3.3 - Graphics API

Enjoy building in your own Minecraft world!
