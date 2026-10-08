package dev.autoominous.mixin;

import dev.autoominous.AutoTpaRuntime;
import dev.autoominous.BountyBridge;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_7594")
public abstract class MessageHandlerMixin {
   @Inject(method = "method_44736", at = @At("HEAD"), require = 1)
   private void autoominous$onGameMessage(class_2561 var1, boolean var2, CallbackInfo var3) {
      if (!var2 && var1 != null) {
         AutoTpaRuntime.onGameMessageObject(var1);
         BountyBridge.onGameMessageObject(var1);
      }
   }
}
