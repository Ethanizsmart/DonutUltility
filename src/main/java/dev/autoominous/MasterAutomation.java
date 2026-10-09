package dev.autoominous;

/** Session-only switch. Every game launch starts disabled. */
public final class MasterAutomation {
   private static boolean enabled = false;
   private MasterAutomation() {}
   public static boolean isEnabled() { return enabled; }
   public static void toggle() {
      enabled = !enabled;
      if (!enabled) {
         ProtectedItemAutoUseRuntime.forceRelease();
         SlashSellRuntime.resetRuntime();
         SwordRecoveryRuntime.forceReset();
         DeathRecoveryRuntime.resetForVexDeathWait();
         HomePositionGuardRuntime.resetForMaster();
         AutoTpaRuntime.resetForMaster();
         AutoReconnectRuntime.resetForMaster();
         VexSafetyRuntime.resetForMaster();
         com.seastore.auto.seaats.client.HumanizedSellRuntime.forceReset();
         BountyRuntime.resetForMaster();
         com.seastore.auto.seaats.client.AutoSellLogic.resetState();
      }
      ClientStatusMessage.actionbarState("Donut Utility", enabled);
   }
}