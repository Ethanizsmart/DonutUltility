package dev.autoominous;

public final class BountyBridge {
   private BountyBridge() {
   }

   public static void onGameMessageObject(Object var0) {
      try {
         StatsRuntime.onGameMessageObject(var0);
      } finally {
         BountyRuntime.onGameMessageObject(var0);
      }
   }
}
