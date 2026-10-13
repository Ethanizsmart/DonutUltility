package dev.autoominous;

import java.util.Locale;

public final class CommandBroker {
   private static long cooldownUntilMs;
   private static long lastCooldownLogMs;

   private CommandBroker() {
   }

   public static void onGameMessageObject(Object var0) {
      CoreV2RuntimeBridge.onGameMessageObject(var0);
      if (var0 != null) {
         String var1 = String.valueOf(ReflectionSupport.callNoArg(var0, "getString"));
         if (var1 == null || "null".equals(var1)) {
            var1 = String.valueOf(var0);
         }

         String var2 = var1.toLowerCase(Locale.ROOT);
         if (var2.contains("need to wait another") && var2.contains("second") && var2.contains("execute a command")) {
            long var3 = System.currentTimeMillis();
            cooldownUntilMs = Math.max(cooldownUntilMs, var3 + 650L);
            accelerateSeaSellRetry();
            if (var3 - lastCooldownLogMs > 3000L) {
               lastCooldownLogMs = var3;
               System.out.println("[Auto Ominous] CommandBroker: server command cooldown detected; /sell retry window shortened to ~650ms.");
            }
         }
      }
   }

   public static boolean isBackoffActive() {
      return System.currentTimeMillis() < cooldownUntilMs;
   }

   public static void reset() {
      cooldownUntilMs = 0L;
   }

   private static void accelerateSeaSellRetry() {
      try {
         long var0 = System.nanoTime();
         ReflectionSupport.setStatic("com.seastore.auto.seaats.client.AutoSellLogic", "sellOpenDeadlineNanos", var0 + 650000000L);
         ReflectionSupport.setStatic("com.seastore.auto.seaats.client.AutoSellLogic", "sellContentsDeadlineNanos", var0 + 650000000L);
         ReflectionSupport.setStatic("com.seastore.auto.seaats.client.AutoSellLogic", "sellRetryNotBeforeNanos", var0 + 500000000L);
      } catch (Throwable var2) {
      }
   }
}
