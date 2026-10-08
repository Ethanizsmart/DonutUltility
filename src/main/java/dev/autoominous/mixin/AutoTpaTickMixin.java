package dev.autoominous.mixin;

import dev.autoominous.AutoTpaRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310")
public abstract class AutoTpaTickMixin {
   @Inject(method = "method_1574", at = @At("HEAD"), require = 1)
   private void autoominous$tpaTick(CallbackInfo var1) {
      AutoTpaRuntime.onTick();
   }
}
