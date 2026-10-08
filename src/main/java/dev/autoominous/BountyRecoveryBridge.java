package dev.autoominous;

public final class BountyRecoveryBridge {
   private BountyRecoveryBridge() {
   }

   public static void onTick() {
      if (!BountyRuntime.isBlockingSell()) {
         DeathRecoveryRuntime.onTick();
      }
   }
}
