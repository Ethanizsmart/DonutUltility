package dev.autoominous;

public final class TransactionGate {
   private TransactionGate() {
   }

   public static boolean isSeaSellActive() {
      try {
         Object var0 = ReflectionSupport.staticField("com.seastore.auto.seaats.client.AutoSellLogic", "currentState");
         return var0 != null && !"IDLE".equals(String.valueOf(var0));
      } catch (Throwable var1) {
         return false;
      }
   }

   public static boolean isBountyActive() {
      return ReflectionSupport.callStaticBoolean("dev.autoominous.BountyRuntime", "isBlockingSell", false);
   }

   public static boolean isSwordAhActive() {
      boolean var0 = ReflectionSupport.callStaticBoolean("dev.autoominous.MissingSwordWatchdogRuntime", "isPurchaseActive", false);
      boolean var1 = ReflectionSupport.callStaticBoolean("dev.autoominous.DeathSwordAhRecoveryRuntime", "isActive", false);
      return var0 || var1 || SwordRecoveryRuntime.isRecoveryActive();
   }

   public static boolean shouldTickBounty() {
      if (DeathRecoveryRuntime.isRecoveryPending()) {
         return false;
      }
      if (SwordRecoveryRuntime.isRecoveryActive()) {
         return false;
      }
      if (isBountyActive()) {
         return true;
      } else if (isSwordAhActive()) {
         return false;
      } else {
         return isSeaSellActive() ? false : !FarmCycleRuntime.isCycleBlockingAuxiliaryActions();
      }
   }

   public static boolean isInputIdle() {
      try {
         return "NONE".equals(GuiBroker.owner());
      } catch (Throwable var1) {
         return false;
      }
   }

   public static void cancelTpaPending() {
      try {
         ReflectionSupport.setStatic("dev.autoominous.AutoTpaRuntime", "pendingUser", null);
         ReflectionSupport.setStatic("dev.autoominous.AutoTpaRuntime", "pendingAtMillis", -1L);
         ReflectionSupport.setStatic("dev.autoominous.AutoTpaRuntime", "awaitingConfirm", Boolean.FALSE);
         ReflectionSupport.setStatic("dev.autoominous.AutoTpaRuntime", "confirmDeadlineMillis", -1L);
      } catch (Throwable var1) {
      }
   }
}
