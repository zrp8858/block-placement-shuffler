package com.zuke2005.blockshuffle;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
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
public class BlockShuffleClient implements ClientModInitializer {
    public static final String MOD_ID = "block-placement-shuffler";
    public static final Logger LOG = LoggerFactory.getLogger("Block Placement Shuffler");

    // Vanilla "gold" (matches ChatFormatting.GOLD's RGB value, 0xFFAA00) used
    // for the toggle status message.
    private static final Style STATUS_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xFFAA00));

    private static KeyMapping toggleKey;

    private static boolean shuffleEnabled = false;
    private static boolean keyWasDown = false;

    // Slot to switch to on the next client tick, or -1 for "no pending switch".
    private static int pendingSlot = -1;

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

        ClientTickEvents.END_CLIENT_TICK.register(BlockShuffleClient::onEndTick);
        UseBlockCallback.EVENT.register(BlockShuffleClient::onUseBlock);

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

            if (shuffleEnabled) {
                client.gui.hud.setOverlayMessage(
                        Component.translatable("message.block-placement-shuffler.enabled").withStyle(STATUS_STYLE), false);
                player.playSound(SoundEvents.TRIPWIRE_CLICK_OFF, 0.5f, 1.0f);
            } else {
                client.gui.hud.setOverlayMessage(
                        Component.translatable("message.block-placement-shuffler.disabled").withStyle(STATUS_STYLE), false);
                player.playSound(SoundEvents.TRIPWIRE_CLICK_ON, 0.5f, 1.0f);
            }
        }
        keyWasDown = keyIsDown;

        if (pendingSlot >= 0 && pendingSlot <= 8) {
            player.getInventory().setSelectedSlot(pendingSlot);
            pendingSlot = -1;
        }
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
