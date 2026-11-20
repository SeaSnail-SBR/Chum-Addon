//---*NOTE*--->SLIT IN TWO-->THIS IS P1----*\\
// ---*P2 OF THIS MODULE-JAR FILE =TracerGroup (module)*---\\



package com.chum.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class BetterTracers extends Module {
    private final List<TracerGroup> groups = new ArrayList<>();
    private final List<CachedBlock> cachedBlocks = new ArrayList<>();
    private int scanTimer = 0;


    public BetterTracers(Category category) {
        super(category, "BetterTracers", "A highly configurable group-based tracers module.");

        groups.add(new TracerGroup(settings, "Group Alpha", new SettingColor(125, 125, 125, 255)));
        groups.add(new TracerGroup(settings, "Group Beta", new SettingColor(255, 0, 0, 255)));
        groups.add(new TracerGroup(settings, "Group Gamma", new SettingColor(85, 255, 85, 255)));
        groups.add(new TracerGroup(settings, "Group Delta", new SettingColor(153, 75, 0, 255)));
        groups.add(new TracerGroup(settings, "Group Sigma", new SettingColor(0, 0, 0, 255)));
        groups.add(new TracerGroup(settings, "Group Zeta", new SettingColor(0, 255, 255, 255)));
        groups.add(new TracerGroup(settings, "Group Omega", new SettingColor(255, 170, 0, 255)));
    }

    @Override
    public void onActivate() {
        cachedBlocks.clear();
        scanTimer = 0;
    }

    @Override
    public void onDeactivate() {
        cachedBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        if (scanTimer > 0) {
            scanTimer--;
            return;
        }
        scanTimer = 20;

        cachedBlocks.clear();

        Set<Block> targetBlocks = new HashSet<>();
        int maxRadius = 0;

        for (TracerGroup group : groups) {
            if (group.enabled.get() && !group.blocks.get().isEmpty()) {
                targetBlocks.addAll(group.blocks.get());
                int r = (int) Math.min(48, Math.ceil(group.distance.get()));
                if (r > maxRadius) maxRadius = r;
            }
        }

        if (targetBlocks.isEmpty() || maxRadius <= 0) return;

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        int minX = playerPos.getX() - maxRadius;
        int maxX = playerPos.getX() + maxRadius;
        int minY = Math.max(mc.world.getBottomY(), playerPos.getY() - maxRadius);
        int maxY = Math.min(mc.world.getTopYInclusive(), playerPos.getY() + maxRadius);
        int minZ = playerPos.getZ() - maxRadius;
        int maxZ = playerPos.getZ() + maxRadius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    if (!targetBlocks.contains(block)) continue;

                    double distSq = mc.player.squaredDistanceTo(x + 0.5, y + 0.5, z + 0.5);
                    for (TracerGroup group : groups) {
                        if (group.isValidBlock(block, distSq)) {
                            cachedBlocks.add(new CachedBlock(x + 0.5, y + 0.5, z + 0.5, group));
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            double distSq = mc.player.squaredDistanceTo(entity);

            for (TracerGroup group : groups) {
                if (!group.isValidEntity(entity, distSq)) continue;

                double tx = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * event.tickDelta;
                double ty = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * event.tickDelta + (entity.getStandingEyeHeight() / 2);
                double tz = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * event.tickDelta;

                drawTracer(event, tx, ty, tz, group.color.get(), group.thickness.get());
                break;
            }
        }

        boolean anyStorageActive = false;
        for (TracerGroup group : groups) {
            if (group.enabled.get() && !group.storageBlocks.get().isEmpty()) {
                anyStorageActive = true;
                break;
            }
        }

        if (anyStorageActive) {
            for (BlockEntity blockEntity : Utils.blockEntities()) {
                BlockPos bPos = blockEntity.getPos();
                double tx = bPos.getX() + 0.5;
                double ty = bPos.getY() + 0.5;
                double tz = bPos.getZ() + 0.5;
                double distSq = mc.player.squaredDistanceTo(tx, ty, tz);

                for (TracerGroup group : groups) {
                    if (!group.isValidStorageBlock(blockEntity.getType(), distSq)) continue;

                    drawTracer(event, tx, ty, tz, group.color.get(), group.thickness.get());
                    break;
                }
            }
        }

        for (CachedBlock cached : cachedBlocks) {
            if (!cached.group.enabled.get()) continue;
            drawTracer(event, cached.x, cached.y, cached.z, cached.group.color.get(), cached.group.thickness.get());
        }
    }





//original\\
//    private void drawTracer(Render3DEvent event, double tx, double ty, double tz, SettingColor color, double thickness) {
//        double ox = RenderUtils.center.x;
//        double oy = RenderUtils.center.y;
//        double oz = RenderUtils.center.z;

//        event.renderer.line(ox, oy, oz, tx, ty, tz, color);

//        int extraLines = (int) Math.round(thickness) - 1;
//        if (extraLines <= 0) return;

//        double step = 0.030;
//        for (int i = 1; i <= extraLines; i++) {
//            double offset = i * step;
//            event.renderer.line(ox, oy, oz, tx + offset, ty, tz, color);
//            event.renderer.line(ox, oy, oz, tx - offset, ty, tz, color);
//            event.renderer.line(ox, oy, oz, tx, ty + offset, tz, color);
//            event.renderer.line(ox, oy, oz, tx, ty - offset, tz, color);
//        }
//    }
//end original\\



    //added after\\
    private void drawTracer(Render3DEvent event, double tx, double ty, double tz, SettingColor color, double thickness) {
        double ox = RenderUtils.center.x;
        double oy = RenderUtils.center.y;
        double oz = RenderUtils.center.z;

        // Always draw the center line
        event.renderer.line(ox, oy, oz, tx, ty, tz, color);

        if (thickness <= 1.05) return;

        // Constant 4 outer lines regardless of slider value (zero FPS drop at max 10.0)
        // Scales spread smoothly from 0.004 at thickness 2.0 up to 0.040 at thickness 10.0
                 //----//defaults--\/1.0---\/0.0045\\-----\\new dbl-spread= 0.05) *0.0015;\\
        double spread = (thickness - 0.05) * 0.0012;

        event.renderer.line(ox, oy, oz, tx + spread, ty + spread, tz, color);
        event.renderer.line(ox, oy, oz, tx - spread, ty - spread, tz, color);
        event.renderer.line(ox, oy, oz, tx + spread, ty - spread, tz, color);
        event.renderer.line(ox, oy, oz, tx - spread, ty + spread, tz, color);
    }
//end added after\\





    private static class CachedBlock {
        public final double x;
        public final double y;
        public final double z;
        public final TracerGroup group;

        public CachedBlock(double x, double y, double z, TracerGroup group) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.group = group;
        }
    }
}
