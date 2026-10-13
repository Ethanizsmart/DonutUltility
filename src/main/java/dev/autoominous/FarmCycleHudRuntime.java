package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class FarmCycleHudRuntime {
   private static final int WHITE = -1;
   private static final int GREEN = -11141291;
   private static final int RED = -43691;
   private static final int YELLOW = -171;
   private static final int HUD_X = 8;
   private static final int HUD_Y = 8;
   private static final int LINE = 11;
   private static final int RESERVED_HEIGHT = 33;
   private static String lastPhase = "";
   private static long cycleStartedAtMs;

   private FarmCycleHudRuntime() {
   }

   public static void render(class_332 var0, class_327 var1) {
      DeathHudRuntime.render(var0, var1);
      renderBountyCompat(var0, var1);
      if (isEnabled() && var0 != null && var1 != null) {
         long var2 = System.currentTimeMillis();
         String var4 = safePhase();
         updateCycleClock(var4, var2);
         long var5 = cycleStartedAtMs > 0L ? Math.max(0L, var2 - cycleStartedAtMs) : 0L;
         String var7 = recoveryStatus();
         boolean var8 = "ERROR".equals(var4);
         int var9 = !var8 && var7 == null ? (isWaitingPhase(var4) ? -171 : -11141291) : -43691;
         draw(var0, var1, "Farm: " + var4 + " | " + formatSeconds(var5), 8, 8, var9);
         String var10 = actualRmbOwner(var4);
         int var11 = "NONE".equals(var10) ? -1 : -11141291;
         if ("UNKNOWN".equals(var10)) {
            var11 = -43691;
         }

         draw(var0, var1, "RMB: " + var10, 8, 19, var11);
         if (var8) {
            draw(var0, var1, "ERR: SWORD_MISSING >2s", 8, 30, -43691);
         } else if (var7 != null) {
            draw(var0, var1, "REC: " + var7, 8, 30, -43691);
         }
      } else {
         resetTracker();
      }
   }

   public static int statsHudY() {
      return callStaticInt("dev.autoominous.StatsSettings", "getHudY", 8) + (isEnabled() ? 33 : 0) + DeathHudRuntime.extraReservedHeight();
   }

   public static int reconnectHudY() {
      return callStaticInt("dev.autoominous.AutoReconnectSettings", "getHudY", 56) + (isEnabled() ? 33 : 0) + DeathHudRuntime.extraReservedHeight();
   }

   public static int bountyHudY() {
      return callStaticInt("dev.autoominous.BountyHudSettings", "getHudY", 74) + (isEnabled() ? 33 : 0) + DeathHudRuntime.extraReservedHeight();
   }

   private static boolean isEnabled() {
      try {
         return callStaticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false)
            && callStaticBoolean("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false);
      } catch (Throwable var1) {
         return false;
      }
   }

   private static String safePhase() {
      try {
         String var0 = FarmCycleRuntime.phaseName();
         return var0 != null && !var0.isBlank() ? var0 : "BOOTSTRAP";
      } catch (Throwable var1) {
         return "BOOTSTRAP";
      }
   }

   private static void updateCycleClock(String var0, long var1) {
      if (cycleStartedAtMs <= 0L) {
         cycleStartedAtMs = var1;
      }

      if ("WAIT_OMI".equals(var0) && ("STORE_SWORD".equals(lastPhase) || "STORE_RETURNED_SWORD".equals(lastPhase))) {
         cycleStartedAtMs = var1;
      }

      if (!var0.equals(lastPhase)) {
         lastPhase = var0;
      }
   }

   private static void resetTracker() {
      lastPhase = "";
      cycleStartedAtMs = 0L;
   }

   private static boolean isWaitingPhase(String var0) {
      return switch (var0) {
         case "BOOTSTRAP", "WAIT_OMI", "WAIT_FIREWORK", "MACHINE_RUNNING", "WAIT_RETURN", "LOOT_SETTLE", "VERIFY_SWORD" -> true;
         default -> false;
      };
   }

   private static String recoveryStatus() {
      try {
         if (DeathRecoveryRuntime.isRecoveryPending()) {
            return "DEATH";
         }
      } catch (Throwable var3) {
      }

      try {
         if (HomePositionGuardRuntime.isRecoveryPending()) {
            return "HOME";
         }
      } catch (Throwable var2) {
      }

      try {
         if (SwordRecoveryRuntime.isRecoveryBlockingAutoSell()) {
            return "SWORD";
         }
      } catch (Throwable var1) {
      }

      return null;
   }

   private static String actualRmbOwner(String var0) {
      boolean var1 = staticBoolean(ProtectedItemAutoUseRuntime.class, "pressedByUs", false);
      if (!var1) {
         return "NONE";
      }

      return switch (var0) {
         case "STORE_SWORD", "STORE_RETURNED_SWORD" -> "SWORD";
         case "DRINK_OMI" -> "OMI";
         case "USE_FIREWORK" -> "FIREWORK";
         default -> "UNKNOWN";
      };
   }

   private static boolean callStaticBoolean(String var0, String var1, boolean var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static int callStaticInt(String var0, String var1, int var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Number ? ((Number)var3).intValue() : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static boolean staticBoolean(Class<?> var0, String var1, boolean var2) {
      try {
         Field var3 = var0.getDeclaredField(var1);
         var3.setAccessible(true);
         return var3.getBoolean(null);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static String formatSeconds(long var0) {
      return String.format(Locale.ROOT, "%.1fs", var0 / 1000.0);
   }

   private static void renderBountyCompat(Object var0, Object var1) {
      HudEnhancementBridge.renderBounty(var0, var1);
   }

   private static String callStaticString(String var0, String var1, String var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 == null ? var2 : String.valueOf(var3);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static int textWidth(Object var0, String var1) {
      if (var0 != null && var1 != null) {
         try {
            Method var2 = var0.getClass().getMethod("method_1727", String.class);
            Object var3 = var2.invoke(var0, var1);
            return var3 instanceof Number ? ((Number)var3).intValue() : var1.length() * 6;
         } catch (Throwable var4) {
            return var1.length() * 6;
         }
      } else {
         return 0;
      }
   }

   private static void draw(Object var0, Object var1, String var2, int var3, int var4, int var5) {
      if (var0 != null && var1 != null && var2 != null) {
         try {
            Method var6 = null;

            for (Method var10 : var0.getClass().getMethods()) {
               if (var10.getName().equals("method_25303") && var10.getParameterCount() == 5) {
                  Class[] var11 = var10.getParameterTypes();
                  if (var11[1] == String.class && var11[2] == int.class && var11[3] == int.class && var11[4] == int.class) {
                     var6 = var10;
                     break;
                  }
               }
            }

            if (var6 != null) {
               var6.invoke(var0, var1, var2, var3, var4, var5);
            }
         } catch (Throwable var12) {
         }
      }
   }
}
