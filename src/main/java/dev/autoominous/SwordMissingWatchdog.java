package dev.autoominous;

public final class SwordMissingWatchdog {
   private SwordMissingWatchdog() {
   }

   public static boolean isRecoveryBlockingAutoSell() {
      return MissingSwordWatchdogRuntime.isPurchaseActive() || SwordRecoveryRuntime.isRecoveryBlockingAutoSell();
   }
}
