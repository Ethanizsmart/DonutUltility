package dev.autoominous;

public final class SafetyReconnectBridge {
   private static volatile boolean safetyPaused;
   private static volatile String reason = "";

   private SafetyReconnectBridge() {
   }

   public static void pauseForForeignPlayer() {
      safetyPaused = true;
      reason = "foreign player detected";
   }

   public static boolean allowAutoReconnect(boolean var0) {
      return var0 && !safetyPaused;
   }

   public static boolean isSafetyPaused() {
      return safetyPaused;
   }

   public static String getReason() {
      return reason;
   }

   public static void clearAfterManualReconnect() {
      safetyPaused = false;
      reason = "";
   }
}
