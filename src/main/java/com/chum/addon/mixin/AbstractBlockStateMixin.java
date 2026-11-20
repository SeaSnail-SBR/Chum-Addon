package com.chum.addon.mixin;

import com.chum.addon.modules.BlockDisguise;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
    private void onGetAmbientOcclusionLightLevel(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (Systems.get(Modules.class) == null) return;
        BlockDisguise module = Modules.get().get(BlockDisguise.class);
        if (module == null || !module.isActive()) return;

        BlockState self = (BlockState) (Object) this;
        BlockState replacement = module.getReplacementState(self);
        if (replacement != null && !replacement.isOpaque()) {
            cir.setReturnValue(1.0F);
        }
    }
}


