package com.zrp8858.blockplacementshuffler;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Block Placement Shuffler: press a hotkey (default R, rebindable in
 * Controls) to toggle "shuffle mode". While shuffle mode is on, every time
 * you place a block the mod randomly reselects a different placeable block
 * from your hotbar, so your next placement is a surprise.
 */
public class BlockPlacementShufflerClient implements ClientModInitializer {
    public static final String MOD_ID = "block-placement-shuffler";
    public static final Logger LOG = LoggerFactory.getLogger("Block Placement Shuffler");

    // Vanilla "gray" for the static "Shuffle: " label, "green"/"red" (matching
    // ChatFormatting.GREEN/RED) for the ON/OFF state so it reads at a glance.
    private static final Style PREFIX_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA));
    private static final Style ENABLED_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x55FF55)).withBold(true);
    private static final Style DISABLED_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)).withBold(true);

    // Identifier for our custom HUD element (see onInitializeClient).
    private static final Identifier STATUS_OVERLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "shuffle_status");

    // How many ticks the status message stays visible, and how many of those
    // (at the tail end) it spends fading out -- matches vanilla's action-bar
    // message timing (see Hud.setOverlayMessage/extractOverlayMessage).
    private static final int STATUS_DURATION_TICKS = 60;
    private static final int STATUS_FADE_TICKS = 20;

    // Pixels above the hotbar (which starts at guiHeight() - 22) to draw the
    // status message -- tight, unlike vanilla's action bar which leaves a
    // large gap above the hotbar.
    private static final int STATUS_Y_OFFSET_ABOVE_HOTBAR = 11;

    private static KeyMapping toggleKey;

    private static boolean shuffleEnabled = false;
    private static boolean keyWasDown = false;

    // Slot to switch to on the next client tick, or -1 for "no pending switch".
    private static int pendingSlot = -1;

    // The current status message and how many ticks are left before it fully
    // fades out, or 0 for "nothing to show".
    private static Component statusMessage;
    private static int statusTicksLeft = 0;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main")
        );

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.block-placement-shuffler.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(BlockPlacementShufflerClient::onEndTick);
        UseBlockCallback.EVENT.register(BlockPlacementShufflerClient::onUseBlock);
        HudElementRegistry.addLast(STATUS_OVERLAY_ID, BlockPlacementShufflerClient::extractStatusOverlay);

        LOG.info("Block Placement Shuffler initialized. Default toggle key: R (change it in Controls -> Block Placement Shuffler).");
    }

    private static void onEndTick(Minecraft client) {
        Player player = client.player;
        if (player == null) {
            return;
        }

        // Edge-detect the toggle key so holding it down doesn't spam-toggle.
        boolean keyIsDown = toggleKey.isDown();
        if (keyIsDown && !keyWasDown) {
            shuffleEnabled = !shuffleEnabled;

            statusMessage = buildStatusMessage(shuffleEnabled);
            statusTicksLeft = STATUS_DURATION_TICKS;
            player.playSound(shuffleEnabled ? SoundEvents.TRIPWIRE_CLICK_ON : SoundEvents.TRIPWIRE_CLICK_OFF, 0.5f, 1.0f);
        }
        keyWasDown = keyIsDown;

        if (statusTicksLeft > 0) {
            statusTicksLeft--;
        }

        if (pendingSlot >= 0 && pendingSlot <= 8) {
            player.getInventory().setSelectedSlot(pendingSlot);
            pendingSlot = -1;
        }
    }

    /** "Shuffle: " in gray, followed by a bold green "ON" or bold red "OFF". */
    private static MutableComponent buildStatusMessage(boolean enabled) {
        return Component.translatable("message.block-placement-shuffler.prefix").withStyle(PREFIX_STYLE)
                .append(Component.translatable(enabled
                                ? "message.block-placement-shuffler.on"
                                : "message.block-placement-shuffler.off")
                        .withStyle(enabled ? ENABLED_STYLE : DISABLED_STYLE));
    }

    /**
     * Draws the current status message just above the hotbar, fading it out
     * over the last {@link #STATUS_FADE_TICKS} ticks it's visible for -- the
     * same fade timing vanilla uses for its action-bar message, just
     * positioned much closer to the hotbar instead of vanilla's fixed spot
     * far above it.
     */
    private static void extractStatusOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (statusMessage == null || statusTicksLeft <= 0) {
            return;
        }

        float ticksLeft = statusTicksLeft - deltaTracker.getGameTimeDeltaPartialTick(false);
        int alpha = Math.min(255, (int) (ticksLeft * 255.0f / STATUS_FADE_TICKS));
        if (alpha <= 0) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int width = font.width(statusMessage);
        int x = graphics.guiWidth() / 2 - width / 2;
        int y = graphics.guiHeight() - 22 - STATUS_Y_OFFSET_ABOVE_HOTBAR;
        graphics.text(font, statusMessage, x, y, ARGB.white(alpha));
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand,
                                                 BlockHitResult hitResult) {
        if (!shuffleEnabled || !level.isClientSide() || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        // Only shuffle if the held item is actually a block (i.e. this use is a placement).
        if (Block.byItem(heldStack.getItem()) == Blocks.AIR) {
            return InteractionResult.PASS;
        }

        pendingSlot = pickRandomBlockSlot(player, level.getRandom());
        return InteractionResult.PASS;
    }

    /**
     * Picks a random hotbar slot (0-8) that currently holds a placeable block.
     * Non-block items (tools, food, etc.) are skipped. Returns -1 if no hotbar
     * slot has a block in it.
     */
    private static int pickRandomBlockSlot(Player player, RandomSource random) {
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        List<Integer> candidateSlots = new ArrayList<>(9);

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = items.get(slot);
            if (Block.byItem(stack.getItem()) != Blocks.AIR) {
                candidateSlots.add(slot);
            }
        }

        if (candidateSlots.isEmpty()) {
            return -1;
        }

        return candidateSlots.get(random.nextInt(candidateSlots.size()));
    }
}
