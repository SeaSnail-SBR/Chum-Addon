package com.chum.addon.modules;



import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;




public class SlowWalkSneak extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAuto = settings.createGroup("Auto Movement");


    //--- Walk Toggle & Multiplier ---\\
    private final Setting<Boolean> slowWalk = sgGeneral.add(new BoolSetting.Builder()
        .name("slow-walk")
        .description("Slows down your regular walking speed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> walkMultiplier = sgGeneral.add(new DoubleSetting.Builder()
            .name("walk-multiplier")
            .description("Speed multiplier while walking.")
            .defaultValue(0.74)       //<-- default for these are 0.5 --\\
            .min(0.39)       //<-- default for these are 0.05 --\\.78= 1.2bps
            .max(1.0)
            .sliderRange(0.39, 1.0)       //<-- default for these are 0.05, 1.0 --\\this goes to 1bps
//        .visible(slowWalk::get)
            .build()
    );


    //--- Sneak Toggle & Multiplier ---\\
    private final Setting<Boolean> slowSneak = sgGeneral.add(new BoolSetting.Builder()
        .name("slow-sneak")
        .description("Slows down your sneaking speed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> sneakMultiplier = sgGeneral.add(new DoubleSetting.Builder()
            .name("sneak-multiplier")
            .description("Speed multiplier while sneaking.")
            .defaultValue(0.80)       //<-- default for these are 0.5 --\\
            .min(0.22)       //<-- default for these are 0.05 --\\.45= 0.1bps
            .max(1.0)
            .sliderRange(0.22, 1.0)       //<-- default for these are 0.05, 1.0 --\\.45 goes 4bps
//        .visible(slowSneak::get)
            .build()
    );


    //--- Auto Walk & Auto Sneak Toggles ---\\
    private final Setting<Boolean> autoWalk = sgAuto.add(new BoolSetting.Builder()
        .name("auto-walk")
        .description("Automatically holds the forward key while active.")
        .defaultValue(false)
        .onChanged(enabled -> {
            if (!enabled) resetKey(mc.options.forwardKey);
        })
        .build()
    );

    private final Setting<Boolean> autoSneak = sgAuto.add(new BoolSetting.Builder()
        .name("toggle-sneak")
        .description("Automatically holds the sneak key while active.")
        .defaultValue(false)
        .onChanged(enabled -> {
            if (!enabled) resetKey(mc.options.sneakKey);
        })
        .build()
    );



    public SlowWalkSneak(Category category) {
        super(category, "slow-walk/sneak", "Slows down your walking and sneaking speed.");
    }



    @Override
    public void onDeactivate() {
        if (autoWalk.get()) resetKey(mc.options.forwardKey);
        if (autoSneak.get()) resetKey(mc.options.sneakKey);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (autoWalk.get()) mc.options.forwardKey.setPressed(true);
        if (autoSneak.get()) mc.options.sneakKey.setPressed(true);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;

        boolean sneaking = mc.player.isSneaking();

        if (sneaking && !slowSneak.get()) return;
        if (!sneaking && !slowWalk.get()) return;

        double multiplier = sneaking ? sneakMultiplier.get() : walkMultiplier.get();

        double newX = event.movement.x * multiplier;
        double newZ = event.movement.z * multiplier;

        ((IVec3d) event.movement).meteor$set(newX, event.movement.y, newZ);
    }

    private void resetKey(KeyBinding key) {
        if (mc.options == null || mc.getWindow() == null) return;
        key.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.getDefaultKey().getCode()));
    }
}
