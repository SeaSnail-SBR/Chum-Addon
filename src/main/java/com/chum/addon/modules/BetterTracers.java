package com.chum.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import java.util.Set;

public class BetterTracers extends Module {
    private final SettingGroup sgGroupA = settings.createGroup("Group Alpha");
    private final SettingGroup sgGroupB = settings.createGroup("Group Beta");
    private final SettingGroup sgGroupC = settings.createGroup("Group Gamma");
    //note= switch Beta & Signa spots... Alpha,Sigma,Beta

    //note= 4 new groups.....//

    private final SettingGroup sgGroupD = settings.createGroup("Group Delta");
    private final SettingGroup sgGroupE = settings.createGroup("Group Sigma");

    private final SettingGroup sgGroupF = settings.createGroup("Group Zeta");
    private final SettingGroup sgGroupG = settings.createGroup("Group Omega");

    //end of 4 new groups....//


    // --- Group Alpha Settings ---\\
    private final Setting<Set<EntityType<?>>> groupAEntities = sgGroupA.add(new EntityTypeListSetting.Builder()
        .name("entities-alpha")
        .description("Entities that will use the Alpha tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupAlpha = sgGroupA.add(new ColorSetting.Builder()
        .name("Group-Alpha")
        .description("Tracer color for Alpha group entities.")
        .defaultValue(new SettingColor(125, 125, 125, 255))
        .build());

    public final Setting<Boolean> alphaEnabled = sgGroupA.add(new BoolSetting.Builder()
        .name("Enabled")
        .description("Enables tracers for the Alpha group.")
        .defaultValue(false)
        .build());
    // --- END ALPHA SETTINGS ---//

    // --- Group Beta Settings ---\\
    private final Setting<Set<EntityType<?>>> groupBEntities = sgGroupB.add(new EntityTypeListSetting.Builder()
        .name("entities-beta")
        .description("Entities that will use the Beta tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupBeta = sgGroupB.add(new ColorSetting.Builder()
        .name("Group-Beta")
        .description("Tracer color for Beta group entities.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build());

    public final Setting<Boolean> betaEnabled = sgGroupB.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Beta group.")
        .defaultValue(false)
        .build());
    // --- END BETA SETTINGS ---//

    // --- GAMMA GROUP SETTINGS --- \\
    private final Setting<Set<EntityType<?>>> groupCEntities = sgGroupC.add(new EntityTypeListSetting.Builder()
        .name("entities-delta")
        .description("Entities that will use the Delta tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupGamma = sgGroupC.add(new ColorSetting.Builder()
        .name("Group-Gamma")
        .description("Tracer color for Beta group entities.")
        .defaultValue(new SettingColor(85, 255, 85, 255))
        .build());

    public final Setting<Boolean> GammaEnabled = sgGroupC.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Gamma group.")
        .defaultValue(false)
        .build());
    // --- END GAMMA SETTINGS ---//



    //4 new groups...//


    // --- DELTA GROUP SETTINGS ---\\
    private final Setting<Set<EntityType<?>>> groupDEntities = sgGroupD.add(new EntityTypeListSetting.Builder()
        .name("entities-delta")
        .description("Entities that will use the Delta tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupDelta = sgGroupD.add(new ColorSetting.Builder()
        .name("Group-Delta")
        .description("Tracer color for Delta group entities.")
        .defaultValue(new SettingColor(150, 75, 0, 255))
        .build());

    public final Setting<Boolean> DeltaEnabled = sgGroupD.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Beta group.")
        .defaultValue(false)
        .build());
    // --- END DELTA SETTINGS ---//

    // --- Group Sigma Settings ---\\
    private final Setting<Set<EntityType<?>>> groupEEntities = sgGroupE.add(new EntityTypeListSetting.Builder()
        .name("entities-sigma")
        .description("Entities that will use the Beta tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupSigma = sgGroupE.add(new ColorSetting.Builder()
        .name("Group-Sigma")
        .description("Tracer color for Beta group entities.")
        .defaultValue(new SettingColor(0, 0, 0, 255))
        .build());

    public final Setting<Boolean> SigmaEnabled = sgGroupE.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Beta group.")
        .defaultValue(false)
        .build());
    // --- END SIGMA SETTINGS ---//



    // --- ZETA GROUP SETTINGS ---\\
    private final Setting<Set<EntityType<?>>> groupFEntities = sgGroupF.add(new EntityTypeListSetting.Builder()
        .name("entities-zeta")
        .description("Entities that will use the Zeta tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupZeta = sgGroupF.add(new ColorSetting.Builder()
        .name("Group-Zeta")
        .description("Tracer color for Zeta group entities.")
        .defaultValue(new SettingColor(0, 255, 255, 255))
        .build());

    public final Setting<Boolean> ZetaEnabled = sgGroupF.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Zetta group.")
        .defaultValue(false)
        .build());
    // --- END ZETA SETTINGS ---//

    // --- OMEGA GROUP SETTINGS ---\\
    private final Setting<Set<EntityType<?>>> groupGEntities = sgGroupG.add(new EntityTypeListSetting.Builder()
        .name("entities-omega")
        .description("Entities that will use the Omega tracer color.")
        .defaultValue()
        .build());

    private final Setting<SettingColor> GroupOmega = sgGroupG.add(new ColorSetting.Builder()
        .name("Group-omega")
        .description("Tracer color for Omega group entities.")
        .defaultValue(new SettingColor(255, 170, 0, 255))
        .build());

    public final Setting<Boolean> OmegaEnabled = sgGroupG.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Enables tracers for the Omega group.")
        .defaultValue(false)
        .build());
    // --- END OMEGA SETTINGS ---//

    //end of 4 new groups//


    //MY MODULE NAME...
    public BetterTracers(Category category) {
        // Name, description, and category shown in the click GUI
        super(category, "BetterTracers", "A better set of lines module.");
    }


    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;


            //injected from here

            // --- ALPHA GROUP LOGIC ---\\
            // Checks if Alpha button is ON AND if the entity is selected in Alpha's whitelist
            if (!alphaEnabled.get() && groupAEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.GRAY); // Change Color.GRAY to your preferred Alpha color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END ALPHA GROUP LOGIC ---\\

            // --- BETA GROUP LOGIC ---     ->switch with sigma group<-\\
            // Checks if Beta button is ON AND if the entity is selected in Beta's whitelist
            if (!betaEnabled.get() && groupBEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.RED); // Change Color.RED to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END BETA GROUP LOGIC ---\\

            // --- GAMMA GROUP LOGIC ---\\
            // Checks if Gamma button is ON AND if the entity is selected in Gamma's whitelist
            if (!GammaEnabled.get() && groupCEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.GREEN); // Change Color.GREEN to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END GAMMA GROUP LOGIC ---\\

            //end of injection


            //4 new groups...//

            // --- DELTA GROUP LOGIC ---\\
            // Checks if Delta button is ON AND if the entity is selected in Delta's whitelist
            if (!DeltaEnabled.get() && groupDEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.CYAN); // Change Color.?Cyan? to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END DELTA GROUP LOGIC ---\\

            // --- SIGMA GROUP LOGIC ---     ->switch with beta group<-\\
            // Checks if Sigma button is ON AND if the entity is selected in Beta's whitelist
            if (!SigmaEnabled.get() && groupEEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.BLACK); // Change Color.BLACK to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END SIGMA GROUP LOGIC ---\\


            // --- ZETA GROUP LOGIC ---\\
            // Checks if Zeta button is ON AND if the entity is selected in Zeta's whitelist
            if (!ZetaEnabled.get() && groupFEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.GRAY); // Change Color.GRAY to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END ZETA GROUP LOGIC ---\\

            // --- OMEGA GROUP LOGIC ---\\
            // Checks if Omega button is ON AND if the entity is selected in Omega's whitelist
            if (!OmegaEnabled.get() && groupGEntities.get().contains(entity.getType())) {
                drawTracerLine(event, entity, Color.YELLOW); // Change Color.YELLOW to your preferred Beta color
                continue; // Move to the next entity so we don't draw overlapping lines
            }
            // --- END OMEGA GROUP LOGIC ---\\

            //end of 4 new groups//


            SettingColor color = getEntityColor(entity);
            if (color == null) continue; // Not whitelisted in any group

            // Fluid entity positioning interpolation
            Vec3d target = new Vec3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * event.tickDelta,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * event.tickDelta + (entity.getStandingEyeHeight() / 2),
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * event.tickDelta
            );

            // Starting vector originating from camera/crosshair
            Vec3d origin = new Vec3d(0, 0, 75)
                .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
                .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
                .add(mc.gameRenderer.getCamera().getPos());


            // Render the line
            event.renderer.line(origin.x, origin.y, origin.z, target.x, target.y, target.z, color);
        }
    }


    //injected from here
    // Helper method to handle the actual line rendering calculation
    private void drawTracerLine(Render3DEvent event, Entity entity, Color color) {
        // Calculates where the entity is located on the screen relative to the camera
        double x = entity.prevX + (entity.getX() - entity.prevX) * event.tickDelta;
        double y = entity.prevY + (entity.getY() - entity.prevY) * event.tickDelta;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * event.tickDelta;
    }
    //end of injection


    private SettingColor getEntityColor(Entity entity) {
        EntityType<?> type = entity.getType();

        // Check explicit whitelist groups
        if (groupAEntities.get().contains(type)) {
            return GroupAlpha.get();
        }
        if (groupBEntities.get().contains(type)) {
            return GroupBeta.get();
        }
        if (groupCEntities.get().contains(type)) {
            return GroupGamma.get();
        }
        //note= switch beta & Sigma around...


        //4 new groups.... //

        if (groupDEntities.get().contains(type)) {
            return GroupDelta.get();
        }
        if (groupEEntities.get().contains(type)) {
            return GroupSigma.get();
        }
        if (groupFEntities.get().contains(type)) {
            return GroupZeta.get();
        }
        if (groupGEntities.get().contains(type)) {
            return GroupOmega.get();
        }

        //end of 4 new groups....//

        return null;
    }
}
