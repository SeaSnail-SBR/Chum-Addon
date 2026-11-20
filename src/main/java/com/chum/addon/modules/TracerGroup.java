//---*NOTE*--->SPLIT IN TWO-->THIS IS P2----P1 OF THIS MODULE-JAR FILE =BetterTracers (module)*---\\
//--- NO NEED TO INITIALIZE IN MAIN ADDON CLASS JAR FILE ---\\


package com.chum.addon.modules;



import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import java.util.List;
import java.util.Set;



public class TracerGroup {
    public final Setting<Set<EntityType<?>>> entities;
    public final Setting<List<BlockEntityType<?>>> storageBlocks;
    public final Setting<List<Block>> blocks;
    public final Setting<SettingColor> color;
    public final Setting<Double> distance;
    public final Setting<Double> thickness;
    public final Setting<Boolean> enabled;


    public TracerGroup(Settings settings, String groupName, SettingColor defaultColor) {
        SettingGroup sg = settings.createGroup(groupName);

        this.entities = sg.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Entities to trace for " + groupName + ".")
            .defaultValue()
            .build()
        );

        this.storageBlocks = sg.add(new StorageBlockListSetting.Builder()
            .name("storage-blocks")
            .description("Storage blocks to trace for " + groupName + ".")
            .build()
        );

        this.blocks = sg.add(new BlockListSetting.Builder()
            .name("blocks-&-tables")
            .description("Regular blocks to trace for " + groupName + ".")
            .build()
        );

        this.color = sg.add(new ColorSetting.Builder()
            .name("color")
            .description("Tracer color for " + groupName + ".")
            .defaultValue(defaultColor)
            .build()
        );

        this.distance = sg.add(new DoubleSetting.Builder()
            .name("distance")
            .description("Max tracer distance for " + groupName + ".")
            .defaultValue(160.0)
            .min(0.0)
            .sliderMax(352.0)
            .build()
        );

        this.thickness = sg.add(new DoubleSetting.Builder()
            .name("thickness")
            .description("Tracer line thickness for " + groupName + ".")
            .defaultValue(2.05)
            .min(1.0)

            //added after\\--default=10.0--\\new-setting=15.0--\\
            .max(17.0)
            //end added after\\

            .sliderMax(5.6) //<---default i put was 8.7 --\\
            .build()
        );

        this.enabled = sg.add(new BoolSetting.Builder()
            .name("enabled")
            .description("Enables tracers for " + groupName + ".")
            .defaultValue(false)
            .build()
        );
    }

    public boolean isValidEntity(Entity entity, double distSq) {
        if (!enabled.get()) return false;
        double maxDist = distance.get();
        if (distSq > maxDist * maxDist) return false;
        return entities.get().contains(entity.getType());
    }

    public boolean isValidStorageBlock(BlockEntityType<?> type, double distSq) {
        if (!enabled.get()) return false;
        double maxDist = distance.get();
        if (distSq > maxDist * maxDist) return false;
        return storageBlocks.get().contains(type);
    }

    public boolean isValidBlock(Block block, double distSq) {
        if (!enabled.get()) return false;
        double maxDist = distance.get();
        if (distSq > maxDist * maxDist) return false;
        return blocks.get().contains(block);
    }
}
