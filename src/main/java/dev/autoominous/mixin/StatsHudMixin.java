package dev.autoominous.mixin;

import dev.autoominous.BountyHudSettings;
import dev.autoominous.BountyRuntime;
import dev.autoominous.BountySettings;
import dev.autoominous.FarmCycleHudRuntime;
import dev.autoominous.HudEnhancementBridge;
import dev.autoominous.StatsRuntime;
import dev.autoominous.StatsSettings;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_329")
public abstract class StatsHudMixin {
   private static final int WHITE = -1;
   private static final int GREEN = -11141291;
   private static final int RED = -43691;
   private static final int YELLOW = -171;
   private static final int LINE = 11;

   @Inject(method = "method_1753", at = @At("TAIL"))
   private void autoominous$renderStatsHud(class_332 var1, class_9779 var2, CallbackInfo var3) {
      if (var1 != null) {
         try {
            Object var4 = Class.forName("net.minecraft.class_310").getMethod("method_1551").invoke(null);
            if (var4 == null) {
               return;
            }

            if (!(field(var4, "field_1772") instanceof class_327 var6)) {
               return;
            }

            if (StatsSettings.isEnabled()) {
               int var7 = StatsSettings.getHudX();
               int var8 = FarmCycleHudRuntime.statsHudY();
               drawBold(var1, var6, StatsRuntime.getProfitText(), var7, var8, -1);
               drawBold(var1, var6, StatsRuntime.getProfitPerHourText(), var7, var8 + 11, -1);
               drawBold(var1, var6, StatsRuntime.getTimeText(), var7, var8 + 22, -1);
               drawBold(var1, var6, StatsRuntime.getSalesText(), var7, var8 + 33, -1);
            }

            renderAutoReconnect(var1, var6);
            FarmCycleHudRuntime.render(var1, var6);
         } catch (Throwable var9) {
         }
      }
   }

   private static void renderAutoReconnect(class_332 var0, class_327 var1) {
      HudEnhancementBridge.renderAutoReconnect(var0, var1);
   }

   private static void renderBounty(class_332 var0, class_327 var1) {
      int var2 = BountyHudSettings.getHudX();
      int var3 = FarmCycleHudRuntime.bountyHudY();
      boolean var4 = BountySettings.isEnabled();
      String var5 = "Bounty: ";
      drawBold(var0, var1, var5, var2, var3, -1);
      drawBold(var0, var1, var4 ? "ON" : "OFF", var2 + textWidth(var1, var5), var3, var4 ? -11141291 : -43691);
      if (BountyRuntime.isRetrying()) {
         String var6 = "Status: ";
         drawBold(var0, var1, var6, var2, var3 + 11, -1);
         drawBold(var0, var1, "RETRY", var2 + textWidth(var1, var6), var3 + 11, -171);
         drawBold(var0, var1, "Retry In: " + BountyRuntime.getRetryCountdownText(), var2, var3 + 22, -1);
         drawBold(var0, var1, "Last Bounty: " + BountyRuntime.getLastAdded(), var2, var3 + 33, -1);
      } else {
         drawBold(var0, var1, "Next Bounty: " + BountyRuntime.getNextBountyCountdownText(), var2, var3 + 11, -1);
         drawBold(var0, var1, "Last Bounty: " + BountyRuntime.getLastAdded(), var2, var3 + 22, -1);
      }
   }

   private static void drawBold(class_332 var0, class_327 var1, String var2, int var3, int var4, int var5) {
      var0.method_25303(var1, var2, var3, var4, var5);
   }

   private static int textWidth(Object var0, String var1) {
      if (var0 != null && var1 != null) {
         try {
            Method var2 = var0.getClass().getMethod("method_1727", String.class);
            if (var2.invoke(var0, var1) instanceof Number var4) {
               return var4.intValue();
            }
         } catch (Throwable var5) {
         }

         return var1.length() * 6;
      } else {
         return 0;
      }
   }

   private static Object field(Object var0, String var1) {
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (Throwable var4) {
         }
      }

      return null;
   }
}
