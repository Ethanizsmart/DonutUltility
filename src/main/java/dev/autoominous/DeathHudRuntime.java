package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class DeathHudRuntime {
   private static long deathStartedAtMs = -1L;
   private static boolean active = false;

   private DeathHudRuntime() {
   }

   public static void render(Object var0, Object var1) {
      try {
         boolean var2 = isDeadNow();
         long var3 = System.currentTimeMillis();
         if (!var2) {
            active = false;
            deathStartedAtMs = -1L;
            return;
         }

         if (!active || deathStartedAtMs < 0L) {
            active = true;
            deathStartedAtMs = var3;
         }

         long var5 = Math.max(0L, var3 - deathStartedAtMs);
         boolean var7 = callBool("dev.autoominous.VexSafetyRuntime", "wasLastScanSuccessful", false);
         int var8 = callInt("dev.autoominous.VexSafetyRuntime", "getVexCount", 0);
         boolean var9 = callBool("dev.autoominous.VexSafetyRuntime", "isDeathWaitActive", false);
         draw(var0, var1, "DEAD: " + format(var5), 8, 41, -43691);
         String var10 = var7 ? "VEX: " + var8 : "VEX: ?";
         if (var9) {
            var10 = var10 + " | WAIT";
         }

         draw(var0, var1, var10, 8, 52, var9 ? -171 : -1);
      } catch (Throwable var11) {
      }
   }

   public static int extraReservedHeight() {
      return active ? 22 : 0;
   }

   private static boolean isDeadNow() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Object var1 = var0.getMethod("method_1551").invoke(null);
         if (var1 == null) {
            return false;
         }

         Object var2 = field(var1, "field_1755");

         try {
            if (var2 != null && Class.forName("net.minecraft.class_418").isInstance(var2)) {
               return true;
            }
         } catch (Throwable var6) {
         }

         Object var3 = field(var1, "field_1724");
         if (var3 == null) {
            return false;
         }

         Method var4 = findMethod(var3.getClass(), "method_29504", 0);
         if (var4 == null) {
            return false;
         }

         var4.setAccessible(true);
         Object var5 = var4.invoke(var3);
         return var5 instanceof Boolean && (Boolean)var5;
      } catch (Throwable var7) {
         return false;
      }
   }

   private static void draw(Object var0, Object var1, String var2, int var3, int var4, int var5) {
      try {
         Method var6 = FarmCycleHudRuntime.class.getDeclaredMethod("draw", Object.class, Object.class, String.class, int.class, int.class, int.class);
         var6.setAccessible(true);
         var6.invoke(null, var0, var1, var2, var3, var4, var5);
      } catch (Throwable var7) {
      }
   }

   private static String format(long var0) {
      long var2 = var0 / 1000L;
      long var4 = var2 / 60L;
      long var6 = var2 % 60L;
      return String.format(Locale.ROOT, "%02d:%02d", var4, var6);
   }

   private static boolean callBool(String var0, String var1, boolean var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static int callInt(String var0, String var1, int var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Number ? ((Number)var3).intValue() : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static Object field(Object var0, String var1) throws Exception {
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

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }
}
