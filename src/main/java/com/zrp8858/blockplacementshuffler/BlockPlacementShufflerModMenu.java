package com.zrp8858.blockplacementshuffler;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;

/**
 * This mod has no settings of its own beyond the toggle keybind, which is
 * already rebindable through vanilla Controls. The "Configure" button in Mod
 * Menu's mod list just jumps straight there instead of making players hunt
 * for it themselves.
 */
public class BlockPlacementShufflerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ControlsScreen(parent, Minecraft.getInstance().options);
    }
}
