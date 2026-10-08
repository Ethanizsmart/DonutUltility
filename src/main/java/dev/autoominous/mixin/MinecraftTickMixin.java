package dev.autoominous.mixin;

import dev.autoominous.AutoSellRuntime;
import dev.autoominous.HomePositionGuardRuntime;
import dev.autoominous.ProtectedItemAutoUseRuntime;
import dev.autoominous.VexSafetyRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310")
public abstract class MinecraftTickMixin {
   @Inject(method = "method_1574", at = @At("HEAD"), require = 1)
   private void autoominous$tick(CallbackInfo var1) {
      VexSafetyRuntime.onTick();
      HomePositionGuardRuntime.onTick();
      AutoSellRuntime.onTick();
      ProtectedItemAutoUseRuntime.onTick();
   }
}
