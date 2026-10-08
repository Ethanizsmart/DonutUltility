package dev.autoominous.mixin;

import dev.autoominous.StatsRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310")
public abstract class StatsTickMixin {
   @Inject(method = "method_1574", at = @At("HEAD"), require = 1)
   private void autoominous$statsTick(CallbackInfo var1) {
      StatsRuntime.onTick();
   }
}
