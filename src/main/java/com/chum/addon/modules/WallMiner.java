package com.chum.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;




import java.util.ArrayList;
import java.util.List;

public class WallMiner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStorage = settings.createGroup("Matrix Storage");

    private final Setting<Boolean> walkForward = sgGeneral.add(new BoolSetting.Builder()
        .name("walk-forward")
        .description("Automatically walks forward while mining.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> fastBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("fast-break")
        .description("Instantly completes block destruction layers sequentially.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> doubleMine = sgGeneral.add(new BoolSetting.Builder()
        .name("double-mine")
        .description("Forces a concurrent split packet mine on a secondary target.")
        .defaultValue(false)
        .build()
    );


    //new color pallet\\

    private final Setting<SettingColor> target1Color = sgGeneral.add(new ColorSetting.Builder()
        .name("target-1-color")
        .description("The color overlay for the primary mining target block.")
        .defaultValue(new SettingColor(255, 0, 0, 75)) // Default Transparent Red
        .build()
    );

    private final Setting<SettingColor> target2Color = sgGeneral.add(new ColorSetting.Builder()
        .name("target-2-color")
        .description("The color overlay for the secondary double-mine target block.")
        .defaultValue(new SettingColor(0, 0, 255, 75)) // Default Transparent Blue
        .build()
    );


    //end new color pallet\\


    private final Setting<Boolean>[] matrixSettings = new Setting[25];
    private final WCheckbox[] uiCheckboxCache = new WCheckbox[25];

    // Tracking variables to stop audio machine-gun spam
    private BlockPos lastTarget1 = null;
    private BlockPos lastTarget2 = null;

    public WallMiner(Category category) {
        super(category, "wall-miner", "Digs a selectable 5x5 grid layout in front of you safely.");

        for (int i = 0; i < 25; i++) {
            int row = (i / 5) + 1;
            int col = (i % 5) + 1;
            String label = "R" + row + " C" + col;

            matrixSettings[i] = sgStorage.add(new BoolSetting.Builder()
                .name("matrix-" + row + "-" + col)
                .description("Toggle for " + label)
                .defaultValue(true)
                .visible(() -> false)
                .build()
            );
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();

        list.add(theme.settings(settings));
        list.add(theme.label("--- 5x5 Mining Grid Pattern ---"));

        WHorizontalList buttonRow = list.add(theme.horizontalList()).widget();

        WButton resetBtn = buttonRow.add(theme.button("Reset (All True)")).widget();
        resetBtn.action = () -> updateAllCheckboxes(true);

        WButton clearBtn = buttonRow.add(theme.button("Clear (All False)")).widget();
        clearBtn.action = () -> updateAllCheckboxes(false);

        WTable table = list.add(theme.table()).widget();

        for (int i = 0; i < 25; i++) {
            final int index = i;
            uiCheckboxCache[index] = table.add(theme.checkbox(matrixSettings[index].get())).widget();
            uiCheckboxCache[index].action = () -> {
                matrixSettings[index].set(uiCheckboxCache[index].checked);
            };

            if ((i + 1) % 5 == 0) {
                table.row();
            }
        }

        return list;
    }

    private void updateAllCheckboxes(boolean state) {
        for (int i = 0; i < 25; i++) {
            matrixSettings[i].set(state);
            if (uiCheckboxCache[i] != null) {
                uiCheckboxCache[i].checked = state;
            }
        }
    }

    @Override
    public void onActivate() {
        lastTarget1 = null;
        lastTarget2 = null;
    }

    @Override
    public void onDeactivate() {
        if (mc.options.forwardKey.isPressed()) {
            mc.options.forwardKey.setPressed(false);
        }
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
        lastTarget1 = null;
        lastTarget2 = null;
    }

    @Override
    public String getInfoString() {
        return null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null)
            return;

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos playerPos = mc.player.getBlockPos();

        List<BlockPos> miningTargets = getMiningGrid(playerPos, facing);

        BlockPos target1 = null;
        BlockPos target2 = null;

        boolean isFastBreakEnabled = Boolean.TRUE.equals(fastBreak.get());
        boolean isDoubleMineEnabled = Boolean.TRUE.equals(doubleMine.get());

        for (BlockPos pos : miningTargets) {
            if (!mc.world.getBlockState(pos).isAir()) {
                if (target1 == null) {
                    target1 = pos;
                    if (!isDoubleMineEnabled) break;
                } else if (target2 == null && !pos.equals(target1)) {
                    target2 = pos;
                    break;
                }
            }
        }

        boolean blocksLeftToMine = (target1 != null);

        // Target 1 Mining Sequence
        if (target1 != null) {
            Direction side = facing.getOpposite();
            BlockState state1 = mc.world.getBlockState(target1);

            // Check if this is a brand new target block
            if (!target1.equals(lastTarget1)) {
                // Send standard initialization
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target1, side));
                mc.player.swingHand(Hand.MAIN_HAND);

                if (isFastBreakEnabled) {
                    // Send instant completion packet ONCE per block
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target1, side));

                    // Audio Fix: Play the break audio ONCE per block
                    if (!state1.isAir()) {
                        mc.world.playSound(mc.player, target1, state1.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }

                lastTarget1 = target1;
            }

            // Continuous mining progress if fast-break is turned OFF
            if (!isFastBreakEnabled) {
                mc.interactionManager.updateBlockBreakingProgress(target1, side);
            }
        } else {
            lastTarget1 = null;
        }

        // Target 2 (Double Mine) Sequence
        if (isDoubleMineEnabled && target2 != null) {
            Direction side = facing.getOpposite();
            BlockState state2 = mc.world.getBlockState(target2);

            // Only double mine if it's a completely new secondary target block
            if (!target2.equals(lastTarget2)) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target2, side));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target2, side));
                mc.player.swingHand(Hand.MAIN_HAND);

                // Audio Fix: Manually play the matching break audio for the background split-packet break
                if (!state2.isAir()) {
                    mc.world.playSound(mc.player, target2, state2.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                lastTarget2 = target2;
            }
        } else {
            lastTarget2 = null;
        }


//updated \\

        if (walkForward.get()) {
            long windowHandle = mc.getWindow().getHandle();

            // Query raw hardware window states directly using GLFW codes to avoid feedback loops
            boolean physicallyPressingW = net.minecraft.client.util.InputUtil.isKeyPressed(windowHandle, 87);  // GLFW_KEY_W is 87
            boolean physicallyPressingS = net.minecraft.client.util.InputUtil.isKeyPressed(windowHandle, 83);  // GLFW_KEY_S is 83
            boolean physicallyPressingA = net.minecraft.client.util.InputUtil.isKeyPressed(windowHandle, 65);  // GLFW_KEY_A is 65
            boolean physicallyPressingD = net.minecraft.client.util.InputUtil.isKeyPressed(windowHandle, 68);  // GLFW_KEY_D is 68

            boolean holdingManualMovement = physicallyPressingW || physicallyPressingS || physicallyPressingA || physicallyPressingD;

            if (holdingManualMovement) {
                // If you are physically pressing keys, let vanilla KeyBinding handle your movement naturally
                // If auto-walk was forcing W down, release it so your other keys work right away
                if (!physicallyPressingW && mc.options.forwardKey.isPressed()) {
                    mc.options.forwardKey.setPressed(false);
                }
            } else {
                // Only let auto-walk run if your hands are completely off WASD
                BlockPos directlyInFrontEye = playerPos.offset(facing).up();
                BlockPos directlyInFrontFoot = playerPos.offset(facing);

                boolean pathClear = mc.world.getBlockState(directlyInFrontEye).isAir()
                    && mc.world.getBlockState(directlyInFrontFoot).isAir();

                // Automatically walk forward only when grid layer is completely cleared
                mc.options.forwardKey.setPressed(pathClear && !blocksLeftToMine);
            }
        }
    }
//end update\\

//new update - visual highlights\\

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos playerPos = mc.player.getBlockPos();

        List<BlockPos> miningTargets = getMiningGrid(playerPos, facing);

        BlockPos target1 = null;
        BlockPos target2 = null;
        boolean isDoubleMineEnabled = Boolean.TRUE.equals(doubleMine.get());

        for (BlockPos pos : miningTargets) {
            if (!mc.world.getBlockState(pos).isAir()) {
                if (target1 == null) {
                    target1 = pos;
                    if (!isDoubleMineEnabled) break;
                } else if (target2 == null && !pos.equals(target1)) {
                    target2 = pos;
                    break;
                }
            }
        }

        // Pulls custom options cleanly from your Meteor GUI settings menu
        SettingColor t1Color = target1Color.get();
        SettingColor t2Color = target2Color.get();

        // Render custom colored boxes over active blocks
        if (target1 != null && !mc.world.getBlockState(target1).isAir()) {
            event.renderer.box(target1, t1Color, t1Color, ShapeMode.Both, 0);
        }

        if (target2 != null && !mc.world.getBlockState(target2).isAir()) {
            event.renderer.box(target2, t2Color, t2Color, ShapeMode.Both, 0);
        }
    }




//end new visual update\\



    private List<BlockPos> getMiningGrid(BlockPos playerPos, Direction facing) {
        List<BlockPos> grid = new ArrayList<>();
        int rX = facing.getOffsetZ();
        int rZ = -facing.getOffsetX();
        for (int distanceAhead = 1; distanceAhead <= 4; distanceAhead++) {
            boolean blocksFoundInLayer = false;
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    int index = (row * 5) + col;

                    if (matrixSettings[index].get()) {
                        int hOffset = col - 2;
                        int yOffset = 4 - row;
                        int x = playerPos.getX() + (facing.getOffsetX() * distanceAhead) + (rX * hOffset);
                        int y = playerPos.getY() + yOffset;
                        int z = playerPos.getZ() + (facing.getOffsetZ() * distanceAhead) + (rZ * hOffset);
                        BlockPos pos = new BlockPos(x, y, z);
                        grid.add(pos);
                        if (mc.world != null && !mc.world.getBlockState(pos).isAir()) {
                            blocksFoundInLayer = true;
                        }
                    }
                }
            }
            if (blocksFoundInLayer) {
                break;
            }
        }
        return grid;
    }
}
