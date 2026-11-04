package com.chum.addon.modules;


import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
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

    private final Setting<Boolean>[] matrixSettings = new Setting[25];
    private final WCheckbox[] uiCheckboxCache = new WCheckbox[25];

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
    public void onDeactivate() {
        if (mc.options.forwardKey.isPressed()) {
            mc.options.forwardKey.setPressed(false);
        }
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null) return;

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

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target1, side));
            mc.player.swingHand(Hand.MAIN_HAND);

            if (isFastBreakEnabled) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target1, side));

                // Audio Fix: Manually play the matching break audio since client engine skips it
                if (!state1.isAir()) {
                    mc.world.playSound(mc.player, target1, state1.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            } else {
                mc.interactionManager.updateBlockBreakingProgress(target1, side);
            }
        }

        // Target 2 (Double Mine) Sequence
        if (isDoubleMineEnabled && target2 != null) {
            Direction side = facing.getOpposite();
            BlockState state2 = mc.world.getBlockState(target2);

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target2, side));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target2, side));
            mc.player.swingHand(Hand.MAIN_HAND);

            // Audio Fix: Manually play the matching break audio for the background split-packet break
            if (!state2.isAir()) {
                mc.world.playSound(mc.player, target2, state2.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }

        if (walkForward.get()) {
            BlockPos directlyInFrontEye = playerPos.offset(facing).up();
            BlockPos directlyInFrontFoot = playerPos.offset(facing);
            boolean pathClear = mc.world.getBlockState(directlyInFrontEye).isAir()
                && mc.world.getBlockState(directlyInFrontFoot).isAir();
            mc.options.forwardKey.setPressed(pathClear && !blocksLeftToMine);
        }
    }

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
