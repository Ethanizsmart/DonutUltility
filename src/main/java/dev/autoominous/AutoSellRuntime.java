package dev.autoominous;

public final class AutoSellRuntime {
   private static boolean stateKnown;
   private static boolean lastEnabled;

   private AutoSellRuntime() {
   }

   public static void onTick() {
      if (!G232SafeRuntime.isSwordRecoverySuspended()) {
         ReflectionSupport.callStaticVoid("dev.autoominous.CompactSlashSellSettings", "syncToOriginal");
         boolean var0 = ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false);
         if (!stateKnown) {
            lastEnabled = var0;
            stateKnown = true;
         } else if (var0 != lastEnabled) {
            ClientStatusMessage.actionbar("Auto Sell: " + (var0 ? "§aON" : "§cOFF"));
            lastEnabled = var0;
         }

         ReflectionSupport.callStaticVoid("dev.autoominous.AutoReconnectRuntime", "onTick");
         ReflectionSupport.callStaticVoid("dev.autoominous.BountyHudSettings", "maybePersist");
         if (TransactionGate.shouldTickBounty()) {
            ReflectionSupport.callStaticVoid("dev.autoominous.BountyRuntime", "onTick");
         }

         boolean var1 = ReflectionSupport.callStaticBoolean("dev.autoominous.HomePositionGuardRuntime", "isBlockingSell", false);
         if (!var1) {
            ReflectionSupport.callStaticVoid("dev.autoominous.SlashSellRuntime", "onTick");
         }
      }
   }
}
