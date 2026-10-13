package dev.autoominous;

import com.seastore.auto.seaats.client.HumanizedSellRuntime;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SlashSellRuntime {
   private static boolean initialized;
   private static boolean previousEnabled;
   private static boolean recoverySuspended;
   private static Method consumeReceiptMethod;

   private SlashSellRuntime() {
   }

   public static void onTick() {
      boolean prioritySell = DeathRecoveryRuntime.isPostDeathSellPending();
      try {
         tickSell(prioritySell);
      } finally {
         // The one-off recovery sale must not enable the user's regular Auto /sell toggle.
         if (prioritySell && !SlashSellSettings.isEnabled()) {
            setSeaBoolean("isAutoSellEnabled", false);
         }
      }
   }

   private static void tickSell(boolean prioritySell) {
      if (!MasterAutomation.isEnabled()) {  return; }
      invokeStaticVoid("dev.autoominous.SlashSellSettings", "maybePersist");
      syncSeaConfig();
      boolean var0 = invokeStaticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false);
      if (initialized && var0 == previousEnabled) {
         setSeaBoolean("isAutoSellEnabled", var0);
      } else {
         resetRuntime();
         setSeaBoolean("isAutoSellEnabled", var0);
         if (var0) {
            long var1 = System.currentTimeMillis();
            setSeaLong("startTimeMs", var1);
            setSeaInt("totalSellCount", 0);
            setSeaDouble("totalMoney", 0.0);
            setSeaLong("lastMoneyReceivedMs", var1);
         }

         previousEnabled = var0;
         initialized = true;
      }

      if (!var0 && !prioritySell) {
         recoverySuspended = false;
      } else {
         Object var8 = getMinecraftClient();
         if (var8 != null) {
            boolean var2 = invokeStaticBoolean("dev.autoominous.SwordRecoveryRuntime", "isRecoveryBlockingAutoSell", false);
            boolean var3 = isCurrentPlayerDead(var8);
            if (!var2 && !var3) {
               if (recoverySuspended) {
                  resetRuntime();
                  recoverySuspended = false;
               }

               setSeaBoolean("isAutoSellEnabled", prioritySell || HumanizedSellRuntime.shouldEnableSeaAutoSell());

               try {
                  Object var4 = getDeclaredField(var8.getClass(), var8, "field_1755");
                  if (var4 != null && "dev.autoominous.gui.AutoOminousGuiConfigs".equals(var4.getClass().getName())) {
                     return;
                  }

                  Class var5 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
                  Method var6 = findSingleArgMethod(var5, "tick");
                  if (var6 != null) {
                     var6.invoke(null, var8);
                  }
               } catch (Throwable var7) {
               }
            } else {
               if (!recoverySuspended) {
                  resetRuntime();
                  recoverySuspended = true;
               }

               setSeaBoolean("isAutoSellEnabled", false);
               if (var3) {
                  closeHandledScreenIfPresent(var8);
               }
            }
         }
      }
   }

   public static void resetRuntime() {
      HumanizedSellRuntime.clearHardDeadlineSwordHold();

      try {
         Class var0 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
         Method var1 = var0.getMethod("resetState");
         var1.invoke(null);
      } catch (Throwable var2) {
      }
   }

   public static boolean consumeExpectedSellReceipt() {
      try {
         if (consumeReceiptMethod == null) {
            Class var0 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
            consumeReceiptMethod = var0.getDeclaredMethod("consumeExpectedSellReceipt");
            consumeReceiptMethod.setAccessible(true);
         }

         Object var2 = consumeReceiptMethod.invoke(null);
         return var2 instanceof Boolean && (Boolean)var2;
      } catch (Throwable var1) {
         return false;
      }
   }

   private static boolean isCurrentPlayerDead(Object var0) {
      Object var1 = getDeclaredField(var0.getClass(), var0, "field_1724");
      if (var1 == null) {
         return false;
      } else {
         try {
            Object var2 = var1.getClass().getMethod("method_29504").invoke(var1);
            return var2 instanceof Boolean && (Boolean)var2;
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   private static void closeHandledScreenIfPresent(Object var0) {
      try {
         Object var1 = getDeclaredField(var0.getClass(), var0, "field_1755");
         if (var1 == null || !Class.forName("net.minecraft.class_465").isInstance(var1)) {
            return;
         }

         Object var2 = getDeclaredField(var0.getClass(), var0, "field_1724");
         if (var2 != null) {
            try {
               var2.getClass().getMethod("method_7346").invoke(var2);
            } catch (Throwable var4) {
            }
         }

         Class var3 = Class.forName("net.minecraft.class_437");
         var0.getClass().getMethod("method_1507", var3).invoke(var0, (Object)null);
      } catch (Throwable var5) {
      }
   }

   private static void syncSeaConfig() {
      for (int var0 = 1; var0 <= 4; var0++) {
         int var1 = invokeStaticIntWithArg("dev.autoominous.SlashSellSettings", "getCycleMin", var0, 20);
         int var2 = invokeStaticIntWithArg("dev.autoominous.SlashSellSettings", "getCycleMax", var0, 60);
         setSeaInt("cycle" + var0 + "MinTicks", var1);
         setSeaInt("cycle" + var0 + "MaxTicks", var2);
      }

      setSeaBoolean("antiPlayerEnabled", false);
      setSeaBoolean("enableAutoPay", false);
      setSeaBoolean("enableGhostFix", false);
      setSeaBoolean("enableHome", false);
      setSeaBoolean("isHomeSet", false);
      setSeaBoolean("showTracker", false);
      setSeaString("webhookUrlProfit", "");
      setSeaString("webhookUrlAlert", "");
      setSeaString("payWebhookUrl", "");
      setSeaInt("noSellIntervalMinutes", Integer.MAX_VALUE);
      setSeaInt("profitIntervalMinutes", Integer.MAX_VALUE);
   }

   private static Object getMinecraftClient() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         return var0.getMethod("method_1551").invoke(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static Method findSingleArgMethod(Class<?> var0, String var1) {
      for (Method var5 : var0.getDeclaredMethods()) {
         if (var5.getName().equals(var1) && var5.getParameterCount() == 1) {
            var5.setAccessible(true);
            return var5;
         }
      }

      return null;
   }

   private static Object getDeclaredField(Class<?> var0, Object var1, String var2) {
      try {
         Field var3 = var0.getDeclaredField(var2);
         var3.setAccessible(true);
         return var3.get(var1);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static void invokeStaticVoid(String var0, String var1) {
      try {
         Class.forName(var0).getMethod(var1).invoke(null);
      } catch (Throwable var3) {
      }
   }

   private static boolean invokeStaticBoolean(String var0, String var1, boolean var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static int invokeStaticIntWithArg(String var0, String var1, int var2, int var3) {
      try {
         Object var4 = Class.forName(var0).getMethod(var1, int.class).invoke(null, var2);
         return var4 instanceof Number ? ((Number)var4).intValue() : var3;
      } catch (Throwable var5) {
         return var3;
      }
   }

   private static Class<?> seaConfigClass() throws ClassNotFoundException {
      return Class.forName("com.seastore.auto.seaats.Config");
   }

   private static void setSeaBoolean(String var0, boolean var1) {
      setSeaField(var0, var1);
   }

   private static void setSeaInt(String var0, int var1) {
      setSeaField(var0, var1);
   }

   private static void setSeaLong(String var0, long var1) {
      setSeaField(var0, var1);
   }

   private static void setSeaDouble(String var0, double var1) {
      setSeaField(var0, var1);
   }

   private static void setSeaString(String var0, String var1) {
      setSeaField(var0, var1);
   }

   private static void setSeaField(String var0, Object var1) {
      try {
         Field var2 = seaConfigClass().getDeclaredField(var0);
         var2.setAccessible(true);
         var2.set(null, var1);
      } catch (Throwable var3) {
      }
   }
}
