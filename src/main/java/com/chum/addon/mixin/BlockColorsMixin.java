package com.chum.addon.mixin;

import com.chum.addon.modules.BlockDisguise;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public abstract class BlockColorsMixin {
    @Shadow
    public abstract int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex);

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void onGetColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        BlockDisguise module = Modules.get().get(BlockDisguise.class);
        if (module == null || !module.isActive()) return;

        BlockState replacement = module.getReplacementState(state);
        if (replacement != null && replacement != state) {
            cir.setReturnValue(this.getColor(replacement, world, pos, tintIndex));
        }
    }
}


