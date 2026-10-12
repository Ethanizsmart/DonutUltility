package dev.autoominous;

public final class AutoSellRuntime {
   private static boolean stateKnown;
   private static boolean lastEnabled;

   private AutoSellRuntime() {
   }

   public static void onTick() {
      if (!MasterAutomation.isEnabled()) {  return; }
      CompactSlashSellSettings.syncToOriginal();
      boolean var0 = SlashSellSettings.isEnabled();
      if (!stateKnown) {
         lastEnabled = var0;
         stateKnown = true;
      } else if (var0 != lastEnabled) {
         ClientStatusMessage.actionbar("Auto Sell " + (var0 ? "§aON" : "§cOFF"));
         lastEnabled = var0;
      }

      AutoReconnectRuntime.onTick();
      BountyHudSettings.maybePersist();
      if (DeathRecoveryRuntime.isPostDeathSellPending()) {
         SlashSellRuntime.onTick();
         return;
      }
      BountyRuntime.onTick();
      SwordRecoveryRuntime.onTick();
      if (!HomePositionGuardRuntime.isBlockingSell()) {
         SlashSellRuntime.onTick();
      }
   }
}
