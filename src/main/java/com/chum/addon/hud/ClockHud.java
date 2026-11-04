package com.chum.addon.hud;

import com.chum.addon.ChumAddon;
import meteordevelopment.meteorclient.settings.BoolSetting; // Use BoolSetting
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ClockHud extends HudElement {
    public static final HudElementInfo<ClockHud> INFO = new HudElementInfo<>(
        ChumAddon.HUD_GROUP,
        "clock-hud",
        "A barebones HUD element with a custom scale.",
        ClockHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // A standard boolean setting that controls whether the HUD draws anything
    private final Setting<Boolean> showHud = sgGeneral.add(new BoolSetting.Builder()
        .name("show-scale")
        .description("Toggles whether this HUD element is drawn.")
        .defaultValue(true)
        .build()
    );

    // This slider hides dynamically when "showHud" is toggled off
    private final Setting<Double> customScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The custom scale of this HUD element.")
        .defaultValue(4.041)
        .min(1.8)
        .max(10.0)
        .sliderMin(1.8)
        .sliderMax(10.0)
        .visible(showHud::get) // <--- Correct API to hide/show settings dynamically
        .build()
    );


    //clock added\\
    private final ItemStack clockStack = new ItemStack(Items.CLOCK);
    //end clock add\\


    public ClockHud() {
        super(INFO);
    }


    //added clock here\\
    // Helper method to update the boundary box logic cleanly
    private void calculateSize() {
        this.box.setSize(16 * customScale.get(), 16 * customScale.get());
    }
    //end clock adding\\



    //added clock here\\
    @Override
    public void render(HudRenderer renderer) {
        if (mc.world == null || mc.player == null) return;

        // Force match the drag boundary layout to our scale
        calculateSize();

        int x = (int) box.getRenderX();
        int y = (int) box.getRenderY();

        renderer.drawContext.getMatrices().push();
        renderer.drawContext.getMatrices().translate(x, y, 0);
        renderer.drawContext.getMatrices().scale(customScale.get().floatValue(), customScale.get().floatValue(), 1.0f);

        renderer.drawContext.drawItem(clockStack, 0, 0);

        renderer.drawContext.getMatrices().pop();
    }
    //end clock adding\\

}


