package com.zrp8858.blockplacementshuffler;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

/**
 * This mod has no settings of its own beyond the toggle keybind, which is
 * already rebindable through vanilla's Key Binds screen. The "Configure"
 * button in Mod Menu's mod list jumps straight there -- note this is
 * KeyBindsScreen (the actual keybind list), not ControlsScreen (the general
 * Controls options page that Key Binds is one button on).
 */
public class BlockPlacementShufflerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new KeyBindsScreen(parent, Minecraft.getInstance().options);
    }
}
