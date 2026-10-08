package dev.autoominous.mixin;

import dev.autoominous.DisconnectRuntime;
import dev.autoominous.DisconnectSettings;
import net.minecraft.class_2561;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_8667;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_419.class)
public abstract class DisconnectDisconnectedScreenMixin extends class_437 {
   @Shadow
   @Final
   private class_8667 field_44552;
   @Unique
   private boolean autoominous$blockedByDisconnect;

   protected DisconnectDisconnectedScreenMixin(class_2561 var1) {
      super(var1);
   }

   @Inject(method = "method_25426", at = @At("TAIL"), require = 1)
   private void autoominous$addDisconnectButtons(CallbackInfo var1) {
      this.autoominous$blockedByDisconnect = DisconnectRuntime.disconnectTriggered;
      if (this.autoominous$blockedByDisconnect) {
         if (DisconnectRuntime.lastServerAddress != null && this.field_44552 != null) {
            String var2 = DisconnectRuntime.lastDetectedPlayer;
            if (var2 == null || var2.isBlank()) {
               var2 = "player";
            }

            String var3 = var2;
            this.field_44552.method_52736(class_4185.method_46430(class_2561.method_43470("Reconnect"), var1x -> {
               DisconnectRuntime.clearProtectionTrigger();
               this.autoominous$tryConnecting();
            }).method_46431());
            this.field_44552.method_52736(class_4185.method_46430(class_2561.method_43470("Add " + var3 + " to Whitelist & Reconnect"), var2x -> {
               DisconnectSettings.addToWhitelist(var3);
               DisconnectRuntime.clearProtectionTrigger();
               this.autoominous$tryConnecting();
            }).method_46431());
            this.field_44552.method_48222();
            this.field_44552.method_48206(var1x -> this.method_37063(var1x));
         }
      }
   }

   @Unique
   private void autoominous$tryConnecting() {
      if (DisconnectRuntime.lastServerAddress != null && this.field_22787 != null) {
         class_412.method_36877(new class_442(), this.field_22787, DisconnectRuntime.lastServerAddress, DisconnectRuntime.lastServerInfo, false, null);
      }
   }
}
