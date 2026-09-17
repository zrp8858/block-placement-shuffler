# Changelog — Block Placement Shuffler

## 1.0.0+26.2

- Adopted a `MAJOR.MINOR.PATCH+MCVERSION` versioning scheme; this build
  targets Minecraft 26.2.
- Fixed the toggle click sound being backwards: enabling shuffle mode now
  plays `TRIPWIRE_CLICK_ON` and disabling it plays `TRIPWIRE_CLICK_OFF`.
- Made the toggle status message clearer: it now reads "Shuffle Mode: ON/OFF"
  with an on/off glyph, in green for on and red for off, instead of the same
  gold text for both states.
- Added Mod Menu support: if Mod Menu is installed, this mod's "Configure"
  button opens vanilla's Controls screen directly.

## 1.0.0

- Initial release for Minecraft 26.2 (Fabric).
- Press a hotkey (default `R`, rebindable in Controls) to toggle shuffle mode.
- While shuffle mode is on, placing a block randomly reselects another
  placeable block from your hotbar. Non-block items are skipped.
- Status message ("Shuffle mode enabled/disabled") shows in the action bar
  in gold, with a click sound on toggle.
