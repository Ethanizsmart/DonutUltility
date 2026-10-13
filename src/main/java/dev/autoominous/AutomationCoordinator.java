package dev.autoominous;

import java.lang.reflect.Field;

public final class AutomationCoordinator {
   private static boolean connected;
   private static long sessionEpoch;
   private static boolean previousRecoveryPending;
   private static long recoveryStartedAtMs;
   private static long lastHomeStamp = Long.MIN_VALUE;
   private static int homeAttempts;
   private static long anchorStableSinceMs = -1L;
   private static long lastXLogMs;

   private AutomationCoordinator() {
   }

   public static void onTick() {
      if (!MasterAutomation.isEnabled()) {
         MasterAutomation.persistSettingsWhilePaused();
         InputBroker.forceRelease();
         GuiBroker.reset();
         return;
      }
      long var0 = System.currentTimeMillis();
      Object var2 = ReflectionSupport.mc();
      Object var3 = ReflectionSupport.field(var2, "field_1724");
      Object var4 = ReflectionSupport.field(var2, "field_1687");
      boolean var5 = var2 != null && var3 != null && var4 != null;
      if (!var5) {
         if (connected) {
            resetSession("disconnect/world-unload");
         }

         connected = false;
         InputBroker.forceRelease();
         GuiBroker.reset();
      } else {
         if (!connected) {
            connected = true;
            sessionEpoch++;
            resetVolatileOnly();
            HoldRmbMigrationRuntime.ensure();
            System.out.println("[Auto Ominous] Coordinator: session epoch " + sessionEpoch + " started; stale recovery/input state cleared.");
         }

         ReflectionSupport.callStaticVoid("dev.autoominous.VexSafetyRuntime", "onTick");
         boolean var6 = ReflectionSupport.callStaticBoolean("dev.autoominous.VexSafetyRuntime", "isDeathWaitActive", false);
         boolean var7 = ReflectionSupport.callStaticBoolean("dev.autoominous.VexSafetyRuntime", "isDangerArmed", false);
         ReflectionSupport.callStaticVoid("dev.autoominous.DeathRecoveryRuntime", "onTick");
         boolean var8 = ReflectionSupport.callStaticBoolean("dev.autoominous.DeathRecoveryRuntime", "isRecoveryPending", false);
         superviseRecovery(var2, var3, var4, var8, var0);
         var8 = ReflectionSupport.callStaticBoolean("dev.autoominous.DeathRecoveryRuntime", "isRecoveryPending", false);
         if (!var8 && !var6) {
            ReflectionSupport.callStaticVoid("dev.autoominous.HomePositionGuardRuntime", "onTick");
         }

         boolean var9 = ReflectionSupport.callStaticBoolean("dev.autoominous.HomePositionGuardRuntime", "isBlockingAutomation", false);
         if (!var6 && !var7) {
            SwordRecoveryRuntime.onTick();
         } else {
            SwordRecoveryRuntime.forceReset();
         }
         GuiBroker.tick(var8, var6);
         if (!var8 && !var6 && !var7 && !var9 && !CommandBroker.isBackoffActive() && GuiBroker.canAutoTpa()) {
            ReflectionSupport.callStaticVoid("dev.autoominous.AutoTpaRuntime", "onTick");
            GuiBroker.tick(var8, var6);
         }

         if (DeathRecoveryRuntime.isPostDeathSellPending() && !var6 && !CommandBroker.isBackoffActive()) {
            // Recovery owns the GUI but must let its one-off /sell tick run.
            RecoverySellBridge.onTick();
         } else if (!var8 && !var6 && !var9 && !CommandBroker.isBackoffActive()
            && SwordRecoveryRuntime.allowAutoSellDuringRecovery()) {
            RecoverySellBridge.onSwordFreeSpaceTick();
         } else if (!var8 && !var6 && !var9 && !CommandBroker.isBackoffActive() && GuiBroker.canAutoSell()) {
            ReflectionSupport.callStaticVoid("dev.autoominous.AutoSellRuntime", "onTick");
         }

         boolean var10 = !var8 && !var6 && !var9;
         InputBroker.tick(var10);
         InputAuditRuntime.onTick();
         if (previousRecoveryPending && !var8) {
            InputBroker.reconcileNextTick();
            System.out.println("[Auto Ominous] Coordinator: recovery COMPLETE -> input reconciliation armed for next tick.");
         }

         previousRecoveryPending = var8;
      }
   }

   private static void superviseRecovery(Object var0, Object var1, Object var2, boolean var3, long var4) {
      if (!var3) {
         recoveryStartedAtMs = 0L;
         lastHomeStamp = Long.MIN_VALUE;
         homeAttempts = 0;
         anchorStableSinceMs = -1L;
      } else {
         if (recoveryStartedAtMs == 0L) {
            recoveryStartedAtMs = var4;
         }

         boolean var6 = ReflectionSupport.staticBoolean("dev.autoominous.DeathRecoveryRuntime", "homeCommandSent", false);
         long var7 = ReflectionSupport.staticLong("dev.autoominous.DeathRecoveryRuntime", "homeCommandSentAtMs", -1L);
         if (var6 && var7 > 0L && var7 != lastHomeStamp) {
            lastHomeStamp = var7;
            homeAttempts++;
            anchorStableSinceMs = -1L;
            System.out.println("[Auto Ominous] Coordinator: Home recovery attempt " + homeAttempts + "/3 observed.");
         }

         // The imported flow waits eight seconds, then sells before buying.
         // It no longer advances the legacy sword/home gate during that wait.
         anchorStableSinceMs = -1L;

         if (homeAttempts >= 3 && var6 && var7 > 0L && var4 - var7 >= 10500L) {
            terminalAbort("Home confirmation failed after 3 bounded attempts");
         } else {
            if (var4 - recoveryStartedAtMs > 90000L) {
               terminalAbort("Recovery exceeded 90s safety deadline");
            }
         }
      }
   }

   private static boolean isInsideVerifiedAnchor(Object var0, Object var1) {
      boolean var2 = ReflectionSupport.staticBoolean("dev.autoominous.HomePositionGuardRuntime", "anchorSet", false);
      boolean var3 = ReflectionSupport.staticBoolean("dev.autoominous.HomePositionGuardRuntime", "verifiedTeleportCapture", true);
      if (var2 && var3) {
         double[] var4 = ReflectionSupport.pos(var0);
         if (var4 == null) {
            return false;
         }

         double var5 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorX", Double.NaN);
         double var7 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorY", Double.NaN);
         double var9 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorZ", Double.NaN);
         if (Double.isFinite(var5) && Double.isFinite(var7) && Double.isFinite(var9)) {
            String var11 = String.valueOf(ReflectionSupport.staticField("dev.autoominous.HomePositionGuardRuntime", "anchorWorld"));
            String var12 = ReflectionSupport.worldKey(var1);
            if (var11 != null && !"null".equals(var11) && !var11.isBlank() && var12 != null && !var12.isBlank() && !var11.equals(var12)) {
               return false;
            }

            double var13 = var4[0] - var5;
            double var15 = var4[2] - var9;
            return var13 * var13 + var15 * var15 <= 0.09 && Math.abs(var4[1] - var7) <= 1.0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void promoteSwordRecoveryPastHomeGate(long var0, long var2) {
      try {
         Class var4 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         Field var5 = ReflectionSupport.fieldInHierarchy(var4, "homeStamp");
         Field var6 = ReflectionSupport.fieldInHierarchy(var4, "state");
         Field var7 = ReflectionSupport.fieldInHierarchy(var4, "nextActionAtMs");
         Field var8 = ReflectionSupport.fieldInHierarchy(var4, "stateDeadlineMs");
         Field var9 = ReflectionSupport.fieldInHierarchy(var4, "homeGateArmed");
         Object var10 = var6.get(null);
         String var11 = var10 == null ? "" : String.valueOf(var10);
         if (!"WAIT_STABLE".equals(var11)) {
            return;
         }

         Class var12 = var6.getType();
         Enum var13 = Enum.valueOf(var12.asSubclass(Enum.class), "CHECK_LOCAL");
         var5.setLong(null, var0);
         var6.set(null, var13);
         var7.setLong(null, var2);
         var8.setLong(null, var2 + 15000L);
         var9.setBoolean(null, false);
         anchorStableSinceMs = Long.MAX_VALUE;
         System.out
            .println(
               "[Auto Ominous] Coordinator: Home confirmed by saved 0.30-block anchor; advancing Sword Recovery without requiring a large teleport delta."
            );
      } catch (Throwable var14) {
         if (var2 - lastXLogMs > 5000L) {
            lastXLogMs = var2;
            System.err.println("[Auto Ominous] Coordinator: failed to advance Sword Recovery gate: " + var14.getMessage());
         }
      }
   }

   private static void terminalAbort(String var0) {
      ReflectionSupport.callStaticVoid("dev.autoominous.SwordRecoveryRuntime", "forceReset");
      ReflectionSupport.callStaticVoid("dev.autoominous.DeathRecoveryRuntime", "completeAfterSwordRecovery");
      InputBroker.reconcileNextTick();
      recoveryStartedAtMs = 0L;
      homeAttempts = 0;
      lastHomeStamp = Long.MIN_VALUE;
      anchorStableSinceMs = -1L;
      System.err.println("[Auto Ominous] Coordinator: terminal recovery abort: " + var0 + "; Sell/RMB ownership released.");
   }

   public static void resetForMaster() {
      connected = false;
      resetVolatileOnly();
      CommandBroker.reset();
      InputAuditRuntime.reset();
      GuiBroker.reset();
   }

   private static void resetSession(String var0) {
      sessionEpoch++;
      resetVolatileOnly();
      CommandBroker.reset();
      InputAuditRuntime.reset();
      System.out.println("[Auto Ominous] Coordinator: session reset (" + var0 + "), epoch=" + sessionEpoch + ".");
   }

   private static void resetVolatileOnly() {
      InputBroker.forceRelease();
      ReflectionSupport.callStaticVoid("dev.autoominous.SwordRecoveryRuntime", "forceReset");
      ReflectionSupport.callStaticVoid("dev.autoominous.DeathRecoveryRuntime", "completeAfterSwordRecovery");
      ReflectionSupport.callStaticVoid("dev.autoominous.HomePositionGuardRuntime", "cancelForDeathRecovery");
      clearVexVolatile();
      previousRecoveryPending = false;
      recoveryStartedAtMs = 0L;
      lastHomeStamp = Long.MIN_VALUE;
      homeAttempts = 0;
      anchorStableSinceMs = -1L;
   }

   private static void clearVexVolatile() {
      for (String var3 : new String[]{
         "vexPresent", "deathWaitActive", "deathWaitSatisfied", "resetIssuedForCurrentDeath", "lastScanSuccessful", "scanFailureLogged"
      }) {
         ReflectionSupport.setStatic("dev.autoominous.VexSafetyRuntime", var3, false);
      }

      for (String var10 : new String[]{"lastVexSeenAtMs", "zeroSinceMs"}) {
         ReflectionSupport.setStatic("dev.autoominous.VexSafetyRuntime", var10, 0L);
      }

      for (String var11 : new String[]{"vexCount", "consecutiveSuccessfulZeroScans"}) {
         ReflectionSupport.setStatic("dev.autoominous.VexSafetyRuntime", var11, 0);
      }
   }
}
