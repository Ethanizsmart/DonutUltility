package dev.autoominous;

/** Legacy API compatibility: the single five-second buyer now owns sword purchases. */
public final class MissingSwordWatchdogRuntime {
   private MissingSwordWatchdogRuntime() {}
   public static boolean isPurchaseActive() { return SwordRecoveryRuntime.isRecoveryActive(); }
   public static long missingForMs() { return SwordRecoveryRuntime.missingForMs(); }
   public static void onToggleChanged(boolean enabled) { SwordRecoveryRuntime.forceReset(); }
   public static void onTick() { SwordRecoveryRuntime.onTick(); }
}