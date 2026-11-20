package com.chum.addon.modules;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class BlockDisguise extends Module {
    private final SettingGroup sgBedrock = settings.createGroup("Bedrock");
    private final SettingGroup sgOres = settings.createGroup("Ores");
    private final SettingGroup sgDeepslateOres = settings.createGroup("Deepslate Ores");
    private final SettingGroup sgNether = settings.createGroup("Nether");


    // --- Bedrock Settings --- \\
    public final Setting<Boolean> replaceBedrock = sgBedrock.add(new BoolSetting.Builder()
        .name("replace-bedrock")
        .description("Replaces bedrock with the selected block.")
        .defaultValue(false)
        .onChanged(v -> reloadChunks())
        .build()
    );

    public final Setting<Block> bedrockReplacement = sgBedrock.add(new BlockSetting.Builder()
        .name("bedrock-replacement-block")
        .description("The block model to render in place of bedrock.")
        .defaultValue(Blocks.BARRIER)
        .onChanged(block -> reloadChunks())
        .build()
    );


    // --- Regular Ore Settings --- \\
    public final Setting<Boolean> replaceOres = sgOres.add(new BoolSetting.Builder()
        .name("replace-ores")
        .description("Replaces selected standard ores with the ore replacement block.")
        .defaultValue(false)
        .onChanged(v -> reloadChunks())
        .build()
    );

    public final Setting<Block> oreReplacement = sgOres.add(new BlockSetting.Builder()
        .name("ore-replacement-block")
        .description("The block model to render in place of standard ores.")
        .defaultValue(Blocks.BARRIER)
        .onChanged(block -> reloadChunks())
        .build()
    );

    public final Setting<List<Block>> targetOres = sgOres.add(new BlockListSetting.Builder()
        .name("target-ores")
        .description("Which standard ore blocks to disguise.")
        .defaultValue(
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.COPPER_ORE,
            Blocks.GOLD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.EMERALD_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DIAMOND_ORE
        )
        .onChanged(list -> reloadChunks())
        .build()
    );


    // --- Deepslate Ore Settings --- \\
    public final Setting<Boolean> replaceDeepslateOres = sgDeepslateOres.add(new BoolSetting.Builder()
        .name("replace-deepslate-ores")
        .description("Replaces selected deepslate ores with the deepslate ore replacement block.")
        .defaultValue(false)
        .onChanged(v -> reloadChunks())
        .build()
    );

    public final Setting<Block> deepslateOreReplacement = sgDeepslateOres.add(new BlockSetting.Builder()
        .name("deepslate-ore-replacement-block")
        .description("The block model to render in place of deepslate ores.")
        .defaultValue(Blocks.BARRIER)
        .onChanged(block -> reloadChunks())
        .build()
    );

    public final Setting<List<Block>> targetDeepslateOres = sgDeepslateOres.add(new BlockListSetting.Builder()
        .name("target-deepslate-ores")
        .description("Which deepslate ore blocks to disguise.")
        .defaultValue(
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE
        )
        .onChanged(list -> reloadChunks())
        .build()
    );



    //--- NEW NETHER SECTION ---\\
    // --- Nether Settings ---
    public final Setting<Boolean> replaceNether = sgNether.add(new BoolSetting.Builder()
        .name("replace-nether")
        .description("Replaces selected Nether blocks with the Nether replacement block.")
        .defaultValue(false)
        .onChanged(v -> reloadChunks())
        .build()
    );

    public final Setting<Block> netherReplacement = sgNether.add(new BlockSetting.Builder()
        .name("nether-replacement-block")
        .description("The block model to render in place of selected Nether blocks.")
        .defaultValue(Blocks.BARRIER)
        .onChanged(block -> reloadChunks())
        .build()
    );

    public final Setting<List<Block>> targetNetherBlocks = sgNether.add(new BlockListSetting.Builder()
        .name("target-nether-blocks")
        .description("Which Nether blocks to disguise.")
        .defaultValue(
            Blocks.NETHER_GOLD_ORE,
            Blocks.NETHER_QUARTZ_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.GRAVEL,
            Blocks.BASALT,
            Blocks.BLACKSTONE
        )
        .onChanged(list -> reloadChunks())
        .build()
    );
    //--- END NEW NETHER SECTION ---\\






    // --- Custom Sections 1 to 4 ---
    public final List<CustomRule> customRules = new ArrayList<>();

    public BlockDisguise(Category category) {
        super(category, "block-manager", "Renders Bedrock, Ores, and More using other block textures.");

        for (int i = 1; i <= 4; i++) {
            customRules.add(new CustomRule(i));
        }
    }

    public class CustomRule {
        public final SettingGroup group;
        public final Setting<Boolean> enabled;
        public final Setting<Block> replacement;
        public final Setting<List<Block>> targets;

        public CustomRule(int index) {
            group = settings.createGroup("Custom " + index, false);

            enabled = group.add(new BoolSetting.Builder()
                .name("replace-custom-" + index)
                .description("Enables custom replacement group " + index + ".")
                .defaultValue(false)
                .onChanged(v -> reloadChunks())
                .build()
            );

            replacement = group.add(new BlockSetting.Builder()
                .name("custom-" + index + "-replacement")
                .description("Replacement block for custom group " + index + ".")
                .defaultValue(Blocks.BARRIER)
                .onChanged(block -> reloadChunks())
                .build()
            );

            targets = group.add(new BlockListSetting.Builder()
                .name("custom-" + index + "-targets")
                .description("Blocks to replace in custom group " + index + ".")
                .defaultValue()
                .onChanged(list -> reloadChunks())
                .build()
            );
        }

        public void reset() {
            enabled.set(false);
            replacement.reset();
            targets.reset();
            group.sectionExpanded = false;
        }
    }



    //--- NEW HELPER XRAY HOLE BUG ---\\
    public BlockState getReplacementState(BlockState original) {
        Block block = original.getBlock();

        if (replaceBedrock.get() && block == Blocks.BEDROCK) {
            Block rep = bedrockReplacement.get();
            return (rep != Blocks.BEDROCK && rep != Blocks.BARRIER) ? rep.getDefaultState() : null;
        }

        if (replaceOres.get() && targetOres.get().contains(block)) {
            Block rep = oreReplacement.get();
            return (!targetOres.get().contains(rep) && rep != Blocks.BARRIER) ? rep.getDefaultState() : null;
        }

        if (replaceDeepslateOres.get() && targetDeepslateOres.get().contains(block)) {
            Block rep = deepslateOreReplacement.get();
            return (!targetDeepslateOres.get().contains(rep) && rep != Blocks.BARRIER) ? rep.getDefaultState() : null;
        }

        for (CustomRule rule : customRules) {
            if (rule.enabled.get() && rule.targets.get().contains(block)) {
                Block rep = rule.replacement.get();
                return (!rule.targets.get().contains(rep) && rep != Blocks.BARRIER) ? rep.getDefaultState() : null;
            }
        }

        return null;
    }
    //--- END NEW HELPER XRAY HOLE BUG ---\\
    public boolean isDisguisedAsAir(BlockState state) {
        BlockState rep = getReplacementState(state);
        return rep != null && rep.isAir();
    }
    //--- END P2 - HELPER FOR UNDER TREE STUMP BLOCK ---\\



    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList container = theme.verticalList();


        //--- Row 1: Bedrock, Ores, Deepslate ---\\
        WHorizontalList row1 = container.add(theme.horizontalList()).widget();

        WButton resetBedrock = row1.add(theme.button("Reset Bedrock")).widget();
        resetBedrock.action = () -> {
            replaceBedrock.set(false);
            bedrockReplacement.reset();
            refreshGuiAndChunks();
        };

        WButton resetOres = row1.add(theme.button("Reset Ores")).widget();
        resetOres.action = () -> {
            replaceOres.set(false);
            oreReplacement.reset();
            targetOres.reset();
            refreshGuiAndChunks();
        };

        WButton resetDeepslate = row1.add(theme.button("Reset Deepslate")).widget();
        resetDeepslate.action = () -> {
            replaceDeepslateOres.set(false);
            deepslateOreReplacement.reset();
            targetDeepslateOres.reset();
            refreshGuiAndChunks();
        };




        //--- New Nether Buttons ---\\
        WButton resetNether = row1.add(theme.button("Reset Nether")).widget();
        resetNether.action = () -> {
            replaceNether.set(false);
            netherReplacement.reset();
            targetNetherBlocks.reset();
            refreshGuiAndChunks();
        };
        //--- End New Nether Buttons ---\\



        //--- Row 2: Separate Reset Custom 1-4 buttons ---\\
        WHorizontalList row2 = container.add(theme.horizontalList()).widget();

        for (int i = 0; i < customRules.size(); i++) {
            final int index = i;
            WButton resetCustomBtn = row2.add(theme.button("Reset Custom " + (i + 1))).widget();
            resetCustomBtn.action = () -> {
                customRules.get(index).reset();
                refreshGuiAndChunks();
            };
        }

        return container;
    }

    @Override
    public void onActivate() {
        reloadChunks();
    }

    @Override
    public void onDeactivate() {
        reloadChunks();
    }

    private void refreshGuiAndChunks() {
        reloadChunks();
        mc.execute(() -> {
            if (mc.currentScreen instanceof WidgetScreen screen) {
                screen.reload();
            }
        });
    }

    private void reloadChunks() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }
}


