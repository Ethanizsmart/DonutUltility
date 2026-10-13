package dev.autoominous;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class AhContentStabilityFix {
   private static final long MIN_CONTENT_STABLE_MS = 500L;
   private static final long RESCAN_WINDOW_MS = 2000L;
   private static final long RESCAN_INTERVAL_MS = 250L;
   private static final long NEW_ATTEMPT_GAP_MS = 3000L;
   private static Object lastScreen;
   private static long firstSeenMs;
   private static long firstNonEmptyMs;
   private static long lastCallMs;
   private static String lastDiag;

   private AhContentStabilityFix() {
   }

   public static boolean readyToScan(Object var0, long var1) {
      AhContentStabilityFix.Snapshot var3 = snapshot(var0);
      if (!var3.valid) {
         schedule(var1 + 250L);
         diag("AH_CONTENT phase=waiting_screen");
         return false;
      }

      touchAttempt(var3.screen, var1);
      if (var3.nonEmptyServerSlots <= 0) {
         firstNonEmptyMs = 0L;
         schedule(var1 + 250L);
         diag("AH_CONTENT phase=loading serverSlots=" + var3.serverSlots + " nonEmpty=0");
         return false;
      }

      if (firstNonEmptyMs == 0L) {
         firstNonEmptyMs = var1;
      }

      long var4 = Math.max(0L, var1 - firstNonEmptyMs);
      if (var4 < 500L) {
         schedule(var1 + 250L);
         diag("AH_CONTENT phase=stabilizing serverSlots=" + var3.serverSlots + " nonEmpty=" + var3.nonEmptyServerSlots + " ageMs=" + var4);
         return false;
      } else {
         diag("AH_CONTENT phase=ready serverSlots=" + var3.serverSlots + " nonEmpty=" + var3.nonEmptyServerSlots + " ageMs=" + var4);
         return true;
      }
   }

   public static boolean shouldRescanAfterMiss(Object var0, long var1) {
      AhContentStabilityFix.Snapshot var3 = snapshot(var0);
      if (!var3.valid) {
         return false;
      }

      touchAttempt(var3.screen, var1);
      if (firstNonEmptyMs == 0L && var3.nonEmptyServerSlots > 0) {
         firstNonEmptyMs = var1;
      }

      if (firstNonEmptyMs == 0L) {
         schedule(var1 + 250L);
         diag("LISTING_RESCAN phase=content_empty");
         return true;
      } else {
         long var4 = Math.max(0L, var1 - firstNonEmptyMs);
         if (var4 < 2000L) {
            schedule(var1 + 250L);
            diag("LISTING_RESCAN miss ageMs=" + var4 + " nonEmpty=" + var3.nonEmptyServerSlots);
            return true;
         } else {
            diag("LISTING_WINDOW_EXHAUSTED ageMs=" + var4 + " nonEmpty=" + var3.nonEmptyServerSlots);
            return false;
         }
      }
   }

   private static void touchAttempt(Object var0, long var1) {
      boolean var3 = var0 != lastScreen || lastCallMs > 0L && var1 - lastCallMs > 3000L;
      if (var3) {
         lastScreen = var0;
         firstSeenMs = var1;
         firstNonEmptyMs = 0L;
         lastDiag = null;
      } else if (firstSeenMs == 0L) {
         firstSeenMs = var1;
      }

      lastCallMs = var1;
   }

   private static AhContentStabilityFix.Snapshot snapshot(Object var0) {
      try {
         Object var1 = getField(var0, "field_1755");
         if (var1 == null) {
            return AhContentStabilityFix.Snapshot.INVALID;
         }

         Object var2 = invoke0(var1, "method_17577", "getScreenHandler");
         if (var2 == null) {
            return AhContentStabilityFix.Snapshot.INVALID;
         }

         Object var3 = getField(var2, "field_7761");
         int var4 = sizeOf(var3);
         int var5 = Math.max(0, var4 - 36);
         if (var5 < 45) {
            return AhContentStabilityFix.Snapshot.INVALID;
         }

         int var6 = 0;

         for (int var7 = 0; var7 < var5; var7++) {
            Object var8 = invokeInt(var2, var7, "method_7611", "getSlot");
            if (var8 != null) {
               Object var9 = invoke0(var8, "method_7677", "getStack");
               if (var9 != null) {
                  Object var10 = invoke0(var9, "method_7960", "isEmpty");
                  if (!Boolean.TRUE.equals(var10)) {
                     var6++;
                  }
               }
            }
         }

         return new AhContentStabilityFix.Snapshot(true, var1, var5, var6);
      } catch (Throwable var11) {
         return AhContentStabilityFix.Snapshot.INVALID;
      }
   }

   private static void schedule(long var0) {
      try {
         Class var2 = Class.forName("dev.autoominous.DeathSwordAhRecoveryRuntime");
         Field var3 = findField(var2, "nextAtMs");
         var3.setAccessible(true);
         var3.setLong(null, var0);
      } catch (Throwable var4) {
      }
   }

   private static void diag(String var0) {
      if (!Objects.equals(var0, lastDiag)) {
         System.out.println("[AH-DIAG] " + var0);
         lastDiag = var0;
      }
   }

   private static Object getField(Object var0, String var1) throws Exception {
      Field var2 = findField(var0.getClass(), var1);
      var2.setAccessible(true);
      return var2.get(var0);
   }

   private static Field findField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            return var2.getDeclaredField(var1);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Object invoke0(Object var0, String... var1) {
      if (var0 == null) {
         return null;
      }

      for (String var5 : var1) {
         try {
            Method var6 = findMethod(var0.getClass(), var5, 0);
            var6.setAccessible(true);
            return var6.invoke(var0);
         } catch (Throwable var7) {
         }
      }

      return null;
   }

   private static Object invokeInt(Object var0, int var1, String... var2) {
      if (var0 == null) {
         return null;
      }

      for (String var6 : var2) {
         try {
            Method var7 = findMethod(var0.getClass(), var6, 1);
            var7.setAccessible(true);
            return var7.invoke(var0, var1);
         } catch (Throwable var8) {
         }
      }

      return null;
   }

   private static Method findMethod(Class<?> var0, String var1, int var2) throws NoSuchMethodException {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      throw new NoSuchMethodException(var1);
   }

   private static int sizeOf(Object var0) {
      if (var0 instanceof List var2) {
         return var2.size();
      } else if (var0 instanceof Collection var1) {
         return var1.size();
      } else {
         return var0 != null && var0.getClass().isArray() ? Array.getLength(var0) : -1;
      }
   }

   private record Snapshot(boolean valid, Object screen, int serverSlots, int nonEmptyServerSlots) {
      static final AhContentStabilityFix.Snapshot INVALID = new AhContentStabilityFix.Snapshot(false, null, 0, 0);
   }
}
