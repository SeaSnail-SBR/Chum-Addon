package com.chum.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.Arrays;
import java.util.List;



public class AutoCampfire extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> selectableFood = sgGeneral.add(new ItemListSetting.Builder()
        .name("food-items")
        .description("Selectable raw foods to automatically cook.")
        .defaultValue(Arrays.asList(Items.PORKCHOP, Items.BEEF, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.COD, Items.SALMON, Items.POTATO))
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Interaction range to campfires.")
        .defaultValue(4.5)
        .min(1.0)
        .max(6.0)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Tick delay between putting items on the campfire.")
        .defaultValue(4)
        .min(0)
        .max(20)
        .build()
    );

    private int ticksPassed = 0;


    public AutoCampfire(Category category) {
        super(category, "AutoCampfire", "A Way To Continually Cook Food Automatically.");
    }


    @Override
    public void onActivate() {
        ticksPassed = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        if (ticksPassed > 0) {
            ticksPassed--;
            return;
        }

        // Calculate a bounding box based on the setting range around the player
        int r = (int) Math.ceil(range.get());
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    // 1. Distance check to ensure accuracy within a sphere
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > range.get()) continue;

                    // 2. Fetch the BlockEntity safely at this specific coordinate
                    BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                    if (!(blockEntity instanceof CampfireBlockEntity campfire)) continue;

                    // 3. Make sure the campfire is actually lit
                    boolean isLit = mc.world.getBlockState(pos).get(CampfireBlock.LIT);
                    if (!isLit) continue;

                    // 4. Check if there is an empty slot on the campfire (max 4 items)
                    if (!hasFreeSlot(campfire)) continue;

                    // 5. Find the chosen food in the player's hotbar
                    int foodSlot = findFoodSlot();
                    if (foodSlot == -1) return; // No valid food items in hotbar

                    // 6. Interact with the campfire
                    int preSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = foodSlot; // Switch hotbar selection

                    BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

                    // Restore the original hotbar slot
                    mc.player.getInventory().selectedSlot = preSlot;

                    ticksPassed = delay.get();
                    return; // End the loop cycle early for packet safety
                }
            }
        }
    }



    private boolean hasFreeSlot(CampfireBlockEntity campfire) {
        // Campfires hold up to 4 items. Let's see if any slot is empty.
        for (int i = 0; i < 4; i++) {
            if (campfire.getItemsBeingCooked().get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int findFoodSlot() {
        // Only loops through hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (selectableFood.get().contains(item)) {
                return i;
            }
        }
        return -1;
    }
}
