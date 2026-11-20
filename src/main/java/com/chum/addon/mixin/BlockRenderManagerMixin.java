package com.chum.addon.mixin;

import com.chum.addon.modules.BlockDisguise;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public abstract class BlockRenderManagerMixin {
    @Shadow
    public abstract BakedModel getModel(BlockState state);

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void onGetModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
        BlockDisguise module = Modules.get().get(BlockDisguise.class);
        if (module == null || !module.isActive()) return;

        Block block = state.getBlock();


        //--- 1. Bedrock replacement ---\\
        if (module.replaceBedrock.get() && block == Blocks.BEDROCK) {
            Block replacement = module.bedrockReplacement.get();
            if (replacement != Blocks.BEDROCK && replacement != Blocks.BARRIER) {
                cir.setReturnValue(this.getModel(replacement.getDefaultState()));
            }
            return;
        }


        //--- 2. Standard ore replacement ---\\
        if (module.replaceOres.get() && module.targetOres.get().contains(block)) {
            Block replacement = module.oreReplacement.get();
            if (!module.targetOres.get().contains(replacement) && replacement != Blocks.BARRIER) {
                cir.setReturnValue(this.getModel(replacement.getDefaultState()));
            }
            return;
        }


        //--- 3. Deepslate ore replacement ---\\
        if (module.replaceDeepslateOres.get() && module.targetDeepslateOres.get().contains(block)) {
            Block replacement = module.deepslateOreReplacement.get();
            if (!module.targetDeepslateOres.get().contains(replacement) && replacement != Blocks.BARRIER) {
                cir.setReturnValue(this.getModel(replacement.getDefaultState()));
            }
            return;
        }





        //--- New Nether Replacement ---\\
        if (module.replaceNether.get() && module.targetNetherBlocks.get().contains(block)) {
            Block replacement = module.netherReplacement.get();
            if (!module.targetNetherBlocks.get().contains(replacement) && replacement != Blocks.BARRIER) {
                cir.setReturnValue(this.getModel(replacement.getDefaultState()));
            }
            return;
        }
        //--- End New Nether Replacement ---\\





        //--- 4. Custom groups 1-4 ---\\
        for (BlockDisguise.CustomRule rule : module.customRules) {
            if (rule.enabled.get() && rule.targets.get().contains(block)) {
                Block replacement = rule.replacement.get();
                if (!rule.targets.get().contains(replacement) && replacement != Blocks.BARRIER) {
                    cir.setReturnValue(this.getModel(replacement.getDefaultState()));
                }
                return;
            }
        }
    }
}


