package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class StatsProfitRateRuntime {
   private static double baselineEarned;
   private static long rateStartTimeMs;
   private static boolean previousDead;
   private static boolean initialized;

   private StatsProfitRateRuntime() {
   }

   public static void onStatsTick() {
      if (!StatsSettings.isEnabled()) {
         previousDead = false;
      } else {
         ensureInitialized();
         double var0 = StatsRuntime.getTotalEarned();
         if (var0 < baselineEarned) {
            baselineEarned = var0;
            rateStartTimeMs = System.currentTimeMillis();
         }

         boolean var2 = isClientDeadOrOnDeathScreen();
         if (var2 && !previousDead) {
            resetProfitPerHourOnly();
         }

         previousDead = var2;
      }
   }

   public static void onStatsFullReset() {
      baselineEarned = StatsRuntime.getTotalEarned();
      rateStartTimeMs = System.currentTimeMillis();
      previousDead = false;
      initialized = true;
   }

   public static void resetProfitPerHourOnly() {
      baselineEarned = StatsRuntime.getTotalEarned();
      rateStartTimeMs = System.currentTimeMillis();
      initialized = true;
   }

   public static double getEarnedPerHour() {
      if (!StatsSettings.isEnabled()) {
         return 0.0;
      }

      ensureInitialized();
      long var0 = Math.max(0L, System.currentTimeMillis() - rateStartTimeMs);
      if (var0 <= 0L) {
         return 0.0;
      }

      double var2 = StatsRuntime.getTotalEarned() - baselineEarned;
      return !(var2 <= 0.0) && !Double.isNaN(var2) && !Double.isInfinite(var2) ? var2 / (var0 / 3600000.0) : 0.0;
   }

   private static void ensureInitialized() {
      if (!initialized) {
         baselineEarned = StatsRuntime.getTotalEarned();
         rateStartTimeMs = System.currentTimeMillis();
         initialized = true;
      }
   }

   private static boolean isClientDeadOrOnDeathScreen() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getDeclaredMethod("method_1551");
         var1.setAccessible(true);
         Object var2 = var1.invoke(null);
         if (var2 == null) {
            return false;
         }

         Object var3 = getField(var2, "field_1724");
         if (var3 != null) {
            try {
               Method var4 = var3.getClass().getMethod("method_29504");
               var4.setAccessible(true);
               Object var5 = var4.invoke(var3);
               if (var5 instanceof Boolean && (Boolean)var5) {
                  return true;
               }
            } catch (Throwable var7) {
            }
         }

         Object var9 = getField(var2, "field_1755");
         if (var9 != null) {
            try {
               Class var10 = Class.forName("net.minecraft.class_418");
               if (var10.isInstance(var9)) {
                  return true;
               }
            } catch (Throwable var6) {
            }
         }
      } catch (Throwable var8) {
      }

      return false;
   }

   private static Object getField(Object var0, String var1) throws Exception {
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
         }
      }

      return null;
   }
}
