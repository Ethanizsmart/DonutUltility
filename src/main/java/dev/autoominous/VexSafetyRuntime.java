package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class VexSafetyRuntime {
   private static final long DEATH_ARM_GRACE_MS = 3000L;
   private static final long ZERO_STABLE_MS = 1000L;
   private static final int MIN_ZERO_SUCCESS_SCANS = 5;
   private static long lastVexSeenAtMs = -1L;
   private static boolean vexPresent;
   private static int vexCount;
   private static boolean deathWaitActive;
   private static boolean deathWaitSatisfied;
   private static long zeroSinceMs = -1L;
   private static int consecutiveSuccessfulZeroScans;
   private static boolean lastLoggedPresent;
   private static boolean resetIssuedForCurrentDeath;
   private static boolean lastScanSuccessful;
   private static boolean scanFailureLogged;

   private VexSafetyRuntime() {
   }

   public static void resetForMaster() {
      lastVexSeenAtMs = -1L; vexPresent = false; vexCount = 0;
      deathWaitActive = false; deathWaitSatisfied = false; zeroSinceMs = -1L;
      consecutiveSuccessfulZeroScans = 0; resetIssuedForCurrentDeath = false;
      lastScanSuccessful = false; lastLoggedPresent = false; scanFailureLogged = false;
   }

   public static void onTick() {
      if (!MasterAutomation.isEnabled()) {  return; }
      scanVexNow();
   }

   public static boolean allowHomeGuardConfigured() {
      return !isDangerArmed();
   }

   public static boolean allowHomeGuard(boolean var0) {
      return var0 && allowHomeGuardConfigured();
   }

   public static boolean allowAutoTpa(boolean var0) {
      return !var0 ? false : !isDangerArmed();
   }

   public static boolean filterDeathForRecovery(boolean var0) {
      boolean var1 = scanVexNow();
      long var2 = System.currentTimeMillis();
      if (!var0) {
         deathWaitActive = false;
         deathWaitSatisfied = false;
         zeroSinceMs = -1L;
         consecutiveSuccessfulZeroScans = 0;
         resetIssuedForCurrentDeath = false;
         return false;
      } else if (deathWaitSatisfied) {
         return true;
      } else {
         if (!deathWaitActive) {
            boolean var4 = vexPresent || lastVexSeenAtMs >= 0L && var2 - lastVexSeenAtMs <= 3000L;
            if (!var4) {
               return true;
            }

            deathWaitActive = true;
            zeroSinceMs = -1L;
            consecutiveSuccessfulZeroScans = 0;
            if (!resetIssuedForCurrentDeath) {
               resetIssuedForCurrentDeath = true;
               resetExistingDeathRecovery();
            }

            System.out.println("[Auto Ominous] Vex Safety: death while Vex danger armed; holding Death Screen until Vex clears.");
         }

         if (!var1) {
            zeroSinceMs = -1L;
            consecutiveSuccessfulZeroScans = 0;
            return false;
         } else if (vexPresent) {
            zeroSinceMs = -1L;
            consecutiveSuccessfulZeroScans = 0;
            return false;
         } else if (zeroSinceMs < 0L) {
            zeroSinceMs = var2;
            consecutiveSuccessfulZeroScans = 1;
            return false;
         } else {
            consecutiveSuccessfulZeroScans++;
            if (consecutiveSuccessfulZeroScans < 5) {
               return false;
            } else if (var2 - zeroSinceMs < 1000L) {
               return false;
            } else {
               deathWaitActive = false;
               deathWaitSatisfied = true;
               System.out.println("[Auto Ominous] Vex Safety: Vex absence confirmed by successful scans for 1000ms; releasing Death Recovery.");
               return true;
            }
         }
      }
   }

   public static boolean isDangerArmed() {
      long var0 = System.currentTimeMillis();
      return deathWaitActive || deathWaitSatisfied || vexPresent || lastVexSeenAtMs >= 0L && var0 - lastVexSeenAtMs <= 3000L;
   }

   public static boolean isDeathWaitActive() {
      return deathWaitActive && !deathWaitSatisfied;
   }

   public static int getVexCount() {
      return vexCount;
   }

   public static boolean wasLastScanSuccessful() {
      return lastScanSuccessful;
   }

   private static boolean scanVexNow() {
      int var0 = 0;
      boolean var1 = false;

      try {
         Object var2 = getMinecraftClient();
         Object var3 = var2 == null ? null : getField(var2, "field_1687");
         if (var3 == null) {
            throw new IllegalStateException("world unavailable");
         }

         Method var4 = var3.getClass().getMethod("method_18112");
         if (!(var4.invoke(var3) instanceof Iterable var6)) {
            throw new IllegalStateException("entity iterable unavailable");
         }

         Class var7 = Class.forName("net.minecraft.class_1634");

         for (Object var9 : var6) {
            if (var9 != null && var7.isInstance(var9)) {
               var0++;
            }
         }

         var1 = true;
      } catch (Throwable var10) {
         lastScanSuccessful = false;
         if (deathWaitActive) {
            zeroSinceMs = -1L;
            consecutiveSuccessfulZeroScans = 0;
         }

         if (!scanFailureLogged) {
            scanFailureLogged = true;
            System.err.println("[Auto Ominous] Vex Safety: entity scan unavailable; treating result as UNKNOWN, not zero Vex.");
         }

         return false;
      }

      lastScanSuccessful = var1;
      scanFailureLogged = false;
      vexCount = var0;
      boolean var12 = var0 > 0;
      vexPresent = var12;
      if (var12) {
         lastVexSeenAtMs = System.currentTimeMillis();
         zeroSinceMs = -1L;
         consecutiveSuccessfulZeroScans = 0;
         if (!lastLoggedPresent) {
            lastLoggedPresent = true;
            cancelHomeGuardForVex();
            System.out
               .println(
                  "[Auto Ominous] Vex Safety: Vex detected (count=" + var0 + "); active Home Guard transit cancelled; Auto TPA suspended; Sell/RMB unchanged."
               );
         }
      } else if (lastLoggedPresent) {
         lastLoggedPresent = false;
         System.out.println("[Auto Ominous] Vex Safety: no Vex visible in successful scan; confirming clear state.");
      }

      return true;
   }

   private static void cancelHomeGuardForVex() {
      try {
         Class var0 = Class.forName("dev.autoominous.HomePositionGuardRuntime");
         Method var1 = var0.getMethod("cancelForVexDanger");
         var1.invoke(null);
      } catch (Throwable var2) {
      }
   }

   private static void resetExistingDeathRecovery() {
      try {
         Class var0 = Class.forName("dev.autoominous.DeathRecoveryRuntime");
         Method var1 = var0.getMethod("resetForVexDeathWait");
         var1.invoke(null);
      } catch (Throwable var2) {
      }
   }

   private static Object getMinecraftClient() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getMethod("method_1551");
         return var1.invoke(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static Object getField(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
         Class var2 = var0.getClass();

         while (var2 != null) {
            try {
               Field var3 = var2.getDeclaredField(var1);
               var3.setAccessible(true);
               return var3.get(var0);
            } catch (NoSuchFieldException var4) {
               var2 = var2.getSuperclass();
            } catch (Throwable var5) {
               return null;
            }
         }

         return null;
      }
   }
}
