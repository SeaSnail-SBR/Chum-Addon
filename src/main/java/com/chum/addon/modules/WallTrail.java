package com.chum.addon.modules;


import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.Collections;
import java.util.List;




public class WallTrail extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStorage = settings.createGroup("Matrix Storage");




    //-- 1. Added the Walk Forward checkbox setting here --\\
    private final Setting<Boolean> walkForward = sgGeneral.add(new BoolSetting.Builder()
        .name("walk-forward")
        .description("Automatically walk forward while this module is active.")
        .defaultValue(false)
        .build()
    );



    //-- 2. Added a Master Over-Ride --\\
    private final BlockListSetting masterOverride = (BlockListSetting) sgGeneral.add(new BlockListSetting.Builder()
        .name("master-override")
        .description("Master block override. If empty, the module uses the individual grid settings below.")
        .defaultValue(Collections.emptyList())
        .build()
    );
    //--- End Over-Ride ---\\



    private final Setting<Boolean>[] matrixSettings = new Setting[49];
    private final BlockListSetting[] gridBlocks = new BlockListSetting[49];
    private final WCheckbox[] uiCheckboxCache = new WCheckbox[49];


    public WallTrail(Category category) {
        super(category, "WallTrail", "Builds a customizable 7x7 wall behind you as you walk.");

        for (int i = 0; i < 49; i++) {
            int row = (i / 7) + 1;
            int col = (i % 7) + 1;
            String label = "R" + row + " C" + col;

            matrixSettings[i] = sgStorage.add(new BoolSetting.Builder()
                .name("matrix-" + row + "-" + col)
                .description("Toggle for " + label)
                .defaultValue(true)
                .visible(() -> false)
                .build()
            );

            gridBlocks[i] = (BlockListSetting) sgStorage.add(new BlockListSetting.Builder()
                .name("block-" + row + "-" + col)
                .description("Set individual block type for " + label)
                .defaultValue(Collections.emptyList())
                .visible(() -> false)
                .build()
            );
        }
    }

    private String getBlockButtonText(BlockListSetting setting, String fallback) {
        List<Block> blocks = setting.get();
        if (blocks == null || blocks.isEmpty()) {
            return fallback;
        }
        return "[+] " + blocks.get(0).getName().getString();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList rootList = theme.verticalList();
        buildUIContents(theme, rootList);
        return rootList;
    }

    private void buildUIContents(GuiTheme theme, WVerticalList rootList) {
        rootList.clear();

        WHorizontalList masterRow = rootList.add(theme.horizontalList()).widget();
        masterRow.add(theme.label("Master Override: "));

        WButton masterBtn = masterRow.add(theme.button(getBlockButtonText(this.masterOverride, "Select Override"))).widget();
        masterBtn.action = () -> {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(
                new meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen(theme, this.masterOverride) {
                    @Override
                    public void close() {
                        super.close();
                        buildUIContents(theme, rootList);
                        rootList.invalidate();
                    }
                }
            );
        };


        //--- Master Reset Button ---\\
        WButton masterReset = masterRow.add(theme.button("Reset")).widget();
        masterReset.action = () -> {
            this.masterOverride.reset();
            buildUIContents(theme, rootList);
            rootList.invalidate();
        };
        //--- End Master button ---\\


    //--- 7x7 Grid Pattern Layout  ---\\
        rootList.add(theme.label("--- 7x7 Toggle Alignment Matrix ---"));

        WHorizontalList buttonRow = rootList.add(theme.horizontalList()).widget();
        WButton resetBtn = buttonRow.add(theme.button("Reset (All True)")).widget();
        resetBtn.action = () -> updateAllCheckboxes(true);

        WButton clearBtn = buttonRow.add(theme.button("Clear (All False)")).widget();
        clearBtn.action = () -> updateAllCheckboxes(false);

        WButton resetBlocksBtn = buttonRow.add(theme.button("Reset All Blocks")).widget();
        resetBlocksBtn.action = () -> {
            for (int i = 0; i < 25; i++) {
                if (this.gridBlocks[i] != null) {
                    this.gridBlocks[i].reset();
                }
            }
            buildUIContents(theme, rootList);
            rootList.invalidate();
        };

        WTable table = rootList.add(theme.table()).widget();
        for (int i = 0; i < 49; i++) {
            final int index = i;
            uiCheckboxCache[index] = table.add(theme.checkbox(matrixSettings[index].get())).widget();
            uiCheckboxCache[index].action = () -> {
                matrixSettings[index].set(uiCheckboxCache[index].checked);
            };

            if ((i + 1) % 7 == 0) {
                table.row();
            }
        }


        //--- 7x7 Grid Block Placement Layout  ---\\
        rootList.add(theme.label("--- 7x7 Individual Block Grid Configuration ---"));

        WTable blockGridTable = rootList.add(theme.table()).widget();
        for (int i = 0; i < 49; i++) {
            final int index = i;
            WButton selectBlockBtn = blockGridTable.add(theme.button(getBlockButtonText(this.gridBlocks[index], "Select"))).widget();
            selectBlockBtn.action = () -> {
                net.minecraft.client.MinecraftClient.getInstance().setScreen(
                    new meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen(theme, this.gridBlocks[index]) {
                        @Override
                        public void close() {
                            super.close();
                            buildUIContents(theme, rootList);
                            rootList.invalidate();
                        }
                    }
                );
            };
            if ((i + 1) % 7 == 0) {
                blockGridTable.row();
            }
        }
    }
    //--- End 7x7 Grid Block Placement Layout  ---\\



    private void updateAllCheckboxes(boolean state) {
        for (int i = 0; i < 49; i++) {
            matrixSettings[i].set(state);
            if (uiCheckboxCache[i] != null) {
                uiCheckboxCache[i].checked = state;
            }
        }
    }

    //--- 2. This runs right before every movement calculation tick to force the game into walking forward ---\\
    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (mc.options == null) return;

        if (walkForward.get()) {
            mc.options.forwardKey.setPressed(true);
        }
    }


    //--- 3. This safely stops your character from sprinting off forever when you disable the module ---\\
    @Override
    public void onDeactivate() {
        if (mc.options != null && mc.options.forwardKey != null) {
            mc.options.forwardKey.setPressed(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.input.movementForward <= 0) return;

        Direction playerDir = mc.player.getHorizontalFacing();
        Direction rightDir = playerDir.rotateYClockwise();
        BlockPos basePos = mc.player.getBlockPos().offset(playerDir.getOpposite());

        for (int guiRow = 6; guiRow >= 0; guiRow--) {
            for (int colIdx = 0; colIdx < 7; colIdx++) {
                int i = (guiRow * 7) + colIdx;

                if (!matrixSettings[i].get()) continue;

                Block targetedBlock = null;
                List masterSelection = masterOverride.get();
                if (masterSelection != null && !masterSelection.isEmpty()) {
                    targetedBlock = (Block) masterSelection.get(0);
                } else {
                    List cellSelection = gridBlocks[i].get();
                    if (cellSelection != null && !cellSelection.isEmpty()) {
                        targetedBlock = (Block) cellSelection.get(0);
                    }
                }

                if (targetedBlock == null || targetedBlock == Blocks.AIR) continue;

                int rowOffset = 6 - guiRow;
                int lateralOffset = colIdx - 3;
                BlockPos targetPos = basePos.up(rowOffset).offset(rightDir, lateralOffset);
                if (!mc.world.getBlockState(targetPos).isReplaceable()) continue;
                final Block finalTargetBlock = targetedBlock;
                FindItemResult itemResult = InvUtils.findInHotbar(itemStack ->
                    itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == finalTargetBlock
                );
                if (itemResult.isHotbar()) {
                    BlockPos neighborPos = targetPos;
                    Direction side = Direction.UP;
                    if (!mc.world.getBlockState(targetPos.down()).isReplaceable()) {
                        neighborPos = targetPos.down();
                        side = Direction.UP;
                    } else if (!mc.world.getBlockState(targetPos.offset(playerDir)).isReplaceable()) {
                        neighborPos = targetPos.offset(playerDir);
                        side = playerDir.getOpposite();
                    } else if (!mc.world.getBlockState(targetPos.offset(rightDir)).isReplaceable()) {
                        neighborPos = targetPos.offset(rightDir);
                        side = rightDir.getOpposite();
                    } else if (!mc.world.getBlockState(targetPos.offset(rightDir.getOpposite())).isReplaceable()) {
                        neighborPos = targetPos.offset(rightDir.getOpposite());
                        side = rightDir;
                    }
                    Vec3d hitVec = new Vec3d(neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, side, neighborPos, false);
                    InvUtils.swap(itemResult.slot(), true);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    InvUtils.swapBack();
                }
            }
        }
    }
}
