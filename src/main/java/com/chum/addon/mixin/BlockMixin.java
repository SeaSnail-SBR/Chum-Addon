package com.chum.addon.mixin;

import com.chum.addon.modules.BlockDisguise;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void onShouldDrawSide(BlockState state, BlockState otherState, Direction side, CallbackInfoReturnable<Boolean> cir) {
        BlockDisguise module = Modules.get().get(BlockDisguise.class);
        if (module == null || !module.isActive()) return;

        BlockState replacedOther = module.getReplacementState(otherState);
        if (replacedOther != null && !replacedOther.isOpaque()) {
            cir.setReturnValue(true);
        }
    }
}

