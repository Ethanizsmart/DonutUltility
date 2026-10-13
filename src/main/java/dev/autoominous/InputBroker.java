package dev.autoominous;

public final class InputBroker {
   private static boolean wasAllowed;

   private InputBroker() {
   }

   public static void tick(boolean var0) {
      boolean var1 = ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false);
      boolean var2 = var0 && var1 && TransactionGate.isInputIdle();
      if (!var2) {
         ReflectionSupport.callStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "forceRelease");
         wasAllowed = false;
      } else {
         if (!wasAllowed) {
            ReflectionSupport.callStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "requestRearm");
         }

         ReflectionSupport.callStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "onTick");
         wasAllowed = true;
      }
   }

   public static void forceRelease() {
      ReflectionSupport.callStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "forceRelease");
      wasAllowed = false;
   }

   public static void reconcileNextTick() {
      ReflectionSupport.callStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "requestRearm");
      wasAllowed = false;
   }
}
