# Changelog — Block Placement Shuffler

## 1.0.0+26.2

- Adopted a `MAJOR.MINOR.PATCH+MCVERSION` versioning scheme; this build
  targets Minecraft 26.2.
- Fixed the toggle click sound being backwards: enabling shuffle mode now
  plays `TRIPWIRE_CLICK_ON` and disabling it plays `TRIPWIRE_CLICK_OFF`.
- Redesigned the toggle status message: it now reads a gray "Shuffle: "
  label followed by a bold green "ON" or bold red "OFF", instead of the same
  gold text for both states, and is drawn just above the hotbar instead of
  vanilla's action-bar position (which left a large gap above the hotbar).
- Added Mod Menu support: if Mod Menu is installed, this mod's "Configure"
  button opens vanilla's Controls screen directly.

## 1.0.0

- Initial release for Minecraft 26.2 (Fabric).
- Press a hotkey (default `R`, rebindable in Controls) to toggle shuffle mode.
- While shuffle mode is on, placing a block randomly reselects another
  placeable block from your hotbar. Non-block items are skipped.
- Status message ("Shuffle mode enabled/disabled") shows in the action bar
  in gold, with a click sound on toggle.
