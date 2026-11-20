# Chum Addon

A custom [Meteor Client](https://meteorclient.com) addon built for Minecraft **1.21.4**.

## ✨ Features

### New in v0.2.1
* **BlockDisguise**: Renders Bedrock, Ores, and more using other block textures.
* **SlowWalkSneak**: Slows down your walking and sneaking speed.
* **XCarry**: Allows you to store items in your crafting inventory after closing it.

### Existing Modules
* **AutoCampfire**: A way to continually cook food automatically.
* **BetterTracers**: A highly configurable group-based tracers module.
* **WallMiner**: Digs a selectable 7x7 grid layout in front of you as you walk.
* **WallTrail**: Builds a customizable 7x7 wall behind you as you walk.

### HUD Elements
* **ClockHud**: A clock just like you can craft in-game with a custom scale.

## 📸 Screenshots
*(Drag and drop your screenshots here when editing on GitHub)*
![Preview](link-to-your-screenshot.png)

## 📥 Installation
1. Ensure you have **Fabric Loader** and **Meteor Client** for Minecraft **1.21.4** installed.
2. Download `chum-0.2.1.jar` from the [Releases](../../releases) tab.
3. Drop the `.jar` file into your `.minecraft/mods` folder and launch the game.

## 🛠️ Building from Source
1. Clone this repository.
2. Run `./gradlew build` (`gradlew build` on Windows) in the root directory.
3. Grab the compiled `.jar` from `build/libs/`.

## 📝 Changelog
### v0.2.1
* **Added**: **BlockDisguise** (with custom block rendering & state mixins), **SlowWalkSneak**, and **XCarry**.
* **Improved**:
    * **BetterTracers**: Added **Distance** and **Thickness** sliders (using `TracerGroup`).
    * **WallMiner**: Added a **Distance Ahead** slider and expanded the grid to **7x7**.
    * **WallTrail**: Expanded the grid to **7x7**.
* **Fixed**: Bug fixes and stability improvements for **WallMiner** and **WallTrail**.

### v0.1.1
* Initial release.

