# Block Placement Shuffler

A Fabric mod for Minecraft 26.2 by **zuke2005**.

Press a hotkey (default `R`, rebindable under Options -> Controls -> Key
Binds -> Block Placement Shuffler) to toggle **shuffle mode**. While it's on, every time
you place a block, the mod randomly reselects a different placeable block
from your hotbar -- non-block items (tools, food, etc.) are skipped when
picking the next slot. Toggling shows a gold status message in the action
bar with a click sound. Client-side only: install it locally and it works in
singleplayer and on any server, no server-side install needed.

## Sourced from / credits

- Built for Minecraft 26.2 using [FabricMC's own `fabric-example-mod`](https://github.com/FabricMC/fabric-example-mod)
  (`26.2` branch) as the reference for build configuration.
- The core idea -- a hotkey that toggles random hotbar reselection on block
  placement -- was popularized by [Trikzon's "Shuffle" mod](https://github.com/Trikzon/shuffle).
  No code from that project is used here; this is an independent
  implementation written against Fabric API, built because no existing
  Shuffle-style mod supported Minecraft 26.2 at the time this was made.
- Uses [Fabric API](https://modrinth.com/mod/fabric-api) and the
  [Fabric Loader](https://fabricmc.net/) toolchain.

## Requirements

- Minecraft 26.2
- [Fabric Loader](https://fabricmc.net/use/) 0.19.3+
- [Fabric API](https://modrinth.com/mod/fabric-api) (required dependency)
- Java 25

## License

All Rights Reserved -- see [LICENSE.md](LICENSE.md). You're free to download
and play with this mod, but redistributing modified versions, re-uploading
it elsewhere, or reusing its source requires permission.

---

## For developers

### Building

You'll need JDK 25 installed and on `PATH` (Gradle itself now runs on JDK 25,
not just compiles against it, since Minecraft 26.1+ dropped the old
mappings-based build entirely -- see below).

```
./gradlew build
```

The output jar lands in `build/libs/block-placement-shuffler-1.0.0.jar`.

To test directly from the project:

```
./gradlew runClient
```

### Why the build looks the way it does

Minecraft 26.1 was the first version Mojang shipped fully unobfuscated, so
there's no separate mappings artifact to depend on anymore -- Yarn was
discontinued for the same reason, and Fabric Loom dropped its whole mappings
step. That's why this project uses the `net.fabricmc.fabric-loom` plugin
(not the older `fabric-loom`) with plain `implementation` dependencies
instead of `modImplementation`, and no `mappings` block in `build.gradle`.

A few Fabric API/vanilla names also changed as part of that transition and
the accompanying Gui/Hud split:

- `KeyBindingHelper` -> `KeyMappingHelper` (package `keymapping.v1`, not
  `keybinding.v1`), and `KeyMapping`'s category argument is now a
  `KeyMapping.Category` object (registered via `KeyMapping.Category.register(Identifier...)`),
  not a raw string.
- `ResourceLocation` was renamed to `Identifier`.
- `Player.displayClientMessage(...)` was removed; action bar messages now go
  through `Minecraft.getInstance().gui.hud.setOverlayMessage(...)`.
- `Level.random` is now protected; use `level.getRandom()`.

## Publishing checklist (CurseForge)

Things to do on the CurseForge side when creating/updating the project
listing -- none of this is in the repo itself:

- **Project type**: Mod -> Minecraft: Java Edition -> Mod loader: Fabric.
- **License**: set the CurseForge project license field to match
  `LICENSE.md` (All Rights Reserved). CurseForge asks you to confirm this
  when you create the project.
- **Relations / Dependencies**: mark **Fabric API** as a required dependency
  so CurseForge (and the CurseForge launcher) installs it automatically
  alongside this mod.
- **Game version / loader tags**: tag the uploaded jar with Minecraft 26.2
  and Fabric when you upload it.
- **Category**: something like "Utility & QoL" fits.
- **Icon**: CurseForge wants a square image (recommend at least 256x256,
  PNG) for the project icon -- there isn't one in this repo yet, so add one
  when you create the listing.
- **Description**: the "Sourced from / credits" and top description section
  of this README can be adapted directly into the CurseForge project
  description field.
- **Changelog**: paste `CHANGELOG.md`'s contents into CurseForge's changelog
  field when uploading each file.
