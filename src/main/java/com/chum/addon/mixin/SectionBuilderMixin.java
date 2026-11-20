package com.chum.addon.mixin;

import com.chum.addon.modules.BlockDisguise;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionBuilder.class)
public abstract class SectionBuilderMixin {
    @Redirect(
        method = "build",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/chunk/ChunkRendererRegion;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
        )
    )
    private BlockState onGetBlockState(ChunkRendererRegion region, BlockPos pos) {
        BlockState state = region.getBlockState(pos);

        if (Systems.get(Modules.class) == null) return state;
        BlockDisguise module = Modules.get().get(BlockDisguise.class);
        if (module == null || !module.isActive()) return state;

        Block block = state.getBlock();
        // Check if this is a soil/mud block left behind under a tree trunk
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.ROOTED_DIRT || block == Blocks.MUD) {
            BlockState above = region.getBlockState(pos.up());
            // Only blend if the block directly above was disguised as Air / Void Air
            if (module.isDisguisedAsAir(above)) {
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockState neighbor = region.getBlockState(pos.offset(dir));
                    if (neighbor.isOf(Blocks.GRASS_BLOCK)) {
                        return Blocks.GRASS_BLOCK.getDefaultState();
                    }
                    if (neighbor.isOf(Blocks.MYCELIUM)) {
                        return Blocks.MYCELIUM.getDefaultState();
                    }
                    if (neighbor.isOf(Blocks.MOSS_BLOCK)) {
                        return Blocks.MOSS_BLOCK.getDefaultState();
                    }
                    if (neighbor.isOf(Blocks.SNOW_BLOCK)) {
                        return Blocks.SNOW_BLOCK.getDefaultState();
                    }
                }
            }
        }

        return state;
    }
}
