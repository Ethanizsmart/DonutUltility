package dev.autoominous.mixin;

import dev.autoominous.DisconnectRuntime;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_9112;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_412.class)
public class DisconnectConnectScreenMixin {
   @Inject(method = "method_36877", at = @At("HEAD"), require = 1)
   private static void autoominous$rememberServer(
      class_437 var0, class_310 var1, class_639 var2, class_642 var3, boolean var4, class_9112 var5, CallbackInfo var6
   ) {
      DisconnectRuntime.rememberServer(var2, var3);
   }
}
