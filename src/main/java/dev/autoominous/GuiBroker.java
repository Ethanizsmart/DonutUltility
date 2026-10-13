package dev.autoominous;

public final class GuiBroker {
   private static String owner = "NONE";

   private GuiBroker() {
   }

   public static String owner() {
      return owner;
   }

   public static void tick(boolean var0, boolean var1) {
      if (var1) {
         TransactionGate.cancelTpaPending();
         owner = "DEATH";
      } else if (var0) {
         TransactionGate.cancelTpaPending();
         owner = "RECOVERY";
      } else if (TransactionGate.isBountyActive()) {
         owner = "BOUNTY";
      } else if (TransactionGate.isSeaSellActive()) {
         owner = "SELL";
      } else if (TransactionGate.isSwordAhActive()) {
         TransactionGate.cancelTpaPending();
         owner = "SWORD_AH";
      } else {
         Object var2 = ReflectionSupport.staticField("dev.autoominous.AutoTpaRuntime", "pendingUser");
         long var3 = ReflectionSupport.staticLong("dev.autoominous.AutoTpaRuntime", "pendingAtMillis", 0L);
         boolean var5 = ReflectionSupport.staticBoolean("dev.autoominous.AutoTpaRuntime", "awaitingConfirm", false);
         if (!var5 && (var2 == null || String.valueOf(var2).isBlank() || var3 <= 0L)) {
            Object var6 = ReflectionSupport.mc();
            Object var7 = ReflectionSupport.field(var6, "field_1755");
            if (var7 == null) {
               owner = "NONE";
            } else if ("net.minecraft.class_418".equals(var7.getClass().getName())) {
               owner = "DEATH";
            } else {
               owner = "OTHER";
            }
         } else {
            owner = "TPA";
         }
      }
   }

   public static boolean canAutoTpa() {
      return "NONE".equals(owner) || "TPA".equals(owner);
   }

   public static boolean canAutoSell() {
      return "NONE".equals(owner) || "SELL".equals(owner) || "BOUNTY".equals(owner);
   }

   public static void reset() {
      TransactionGate.cancelTpaPending();
      owner = "NONE";
   }
}
