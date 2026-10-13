package dev.autoominous;

public final class SwordMissingSafetyBridge {
   private SwordMissingSafetyBridge() {
   }

   public static boolean isAutoUseBlocked() {
      return MissingSwordWatchdogRuntime.isPurchaseActive() || HomePositionGuardRuntime.isAutoUseBlocked();
   }
}
