package dev.autoominous.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310")
public abstract class SellRelogWatchdogMixin {
   private static final int FAILED_ROUNDS_BEFORE_RELOG = 1;
   private static final long RELLOG_COOLDOWN_MS = 15000L;
   private static long donututility$lastRelogAtMs;
   private static boolean donututility$waitingForReconnect;

   @Inject(method = "method_1574", at = @At("TAIL"), require = 1)
   private void donututility$sellRelogWatchdog(CallbackInfo var1) {
      try {
         Class var2 = Class.forName("net.minecraft.class_310");
         Method var3 = var2.getMethod("method_1551");
         Object var4 = var3.invoke(null);
         if (var4 == null) {
            return;
         }

         Object var5 = readField(var4, "field_1724");
         Object var6 = readField(var4, "field_1687");
         Object var7 = invokeNoArg(var4, "method_1562");
         if (donututility$waitingForReconnect) {
            if (var5 != null && var6 != null && var7 != null) {
               donututility$waitingForReconnect = false;
               setStaticInt("com.seastore.auto.seaats.client.AutoSellLogic", "sellFailureRounds", 0);
               setStaticInt("com.seastore.auto.seaats.client.AutoSellLogic", "sellCommandRetryCount", 0);
               setStaticLong("com.seastore.auto.seaats.client.AutoSellLogic", "sellOpenDeadlineNanos", 0L);
               setStaticLong("com.seastore.auto.seaats.client.AutoSellLogic", "sellRetryNotBeforeNanos", 0L);
               System.out.println("[DonutUtility SellRelog] Reconnected; sell failure counters reset.");
            }

            return;
         }

         if (var5 == null || var6 == null || var7 == null) {
            return;
         }

         if (!staticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false)) {
            return;
         }

         if (!invokeStaticBooleanArg("dev.autoominous.SafetyReconnectBridge", "allowAutoReconnect", true, true)) {
            return;
         }

         int var8 = staticInt("com.seastore.auto.seaats.client.AutoSellLogic", "sellFailureRounds", 0);
         if (var8 < 1) {
            return;
         }

         long var9 = System.currentTimeMillis();
         if (var9 - donututility$lastRelogAtMs < 15000L) {
            return;
         }

         donututility$lastRelogAtMs = var9;
         donututility$waitingForReconnect = true;

         try {
            Class var11 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
            Method var12 = var11.getMethod("resetState");
            var12.invoke(null);
         } catch (Throwable var17) {
         }

         try {
            Class var19 = Class.forName("dev.autoominous.DisconnectRuntime");
            Field var21 = var19.getField("disconnectTriggered");
            var21.setBoolean(null, true);
         } catch (Throwable var16) {
         }

         Object var20 = invokeNoArg(var7, "method_48296");
         if (var20 == null) {
            donututility$waitingForReconnect = false;
            return;
         }

         Class var22 = Class.forName("net.minecraft.class_2561");
         Method var13 = var22.getMethod("method_43470", String.class);
         Object var14 = var13.invoke(null, "Auto Sell recovery: /sell GUI failed to open after 3 attempts");
         Method var15 = var20.getClass().getMethod("method_10747", var22);
         System.out.println("[DonutUtility SellRelog] /sell GUI failed 3 consecutive attempts -> relog recovery.");
         var15.invoke(var20, var14);
      } catch (Throwable var18) {
         System.err.println("[DonutUtility SellRelog] watchdog error: " + var18.getClass().getSimpleName() + ": " + var18.getMessage());
      }
   }

   private static Object readField(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

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

   private static Object invokeNoArg(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Method var3 = var2.getDeclaredMethod(var1);
            var3.setAccessible(true);
            return var3.invoke(var0);
         } catch (Throwable var4) {
         }
      }

      return null;
   }

   private static boolean staticBoolean(String var0, String var1, boolean var2) {
      try {
         Class var3 = Class.forName(var0);
         Method var4 = var3.getMethod(var1);
         Object var5 = var4.invoke(null);
         return var5 instanceof Boolean ? (Boolean)var5 : var2;
      } catch (Throwable var6) {
         return var2;
      }
   }

   private static boolean invokeStaticBooleanArg(String var0, String var1, boolean var2, boolean var3) {
      try {
         Class var4 = Class.forName(var0);
         Method var5 = var4.getMethod(var1, boolean.class);
         Object var6 = var5.invoke(null, var2);
         return var6 instanceof Boolean ? (Boolean)var6 : var3;
      } catch (Throwable var7) {
         return var3;
      }
   }

   private static int staticInt(String var0, String var1, int var2) {
      try {
         Class var3 = Class.forName(var0);
         Field var4 = var3.getDeclaredField(var1);
         var4.setAccessible(true);
         return var4.getInt(null);
      } catch (Throwable var5) {
         return var2;
      }
   }

   private static void setStaticInt(String var0, String var1, int var2) {
      try {
         Class var3 = Class.forName(var0);
         Field var4 = var3.getDeclaredField(var1);
         var4.setAccessible(true);
         var4.setInt(null, var2);
      } catch (Throwable var5) {
      }
   }

   private static void setStaticLong(String var0, String var1, long var2) {
      try {
         Class var4 = Class.forName(var0);
         Field var5 = var4.getDeclaredField(var1);
         var5.setAccessible(true);
         var5.setLong(null, var2);
      } catch (Throwable var6) {
      }
   }
}
