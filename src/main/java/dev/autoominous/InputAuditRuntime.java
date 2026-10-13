package dev.autoominous;

public final class InputAuditRuntime {
   private static boolean lastAttack;
   private static long lastLogMs;

   private InputAuditRuntime() {
   }

   public static void onTick() {
      try {
         Object var0 = ReflectionSupport.mc();
         if (var0 == null) {
            return;
         }

         Object var1 = ReflectionSupport.field(var0, "field_1724");
         Object var2 = ReflectionSupport.field(var0, "field_1755");
         if (var1 == null || var2 != null) {
            lastAttack = false;
            return;
         }

         Object var3 = ReflectionSupport.field(var0, "field_1690");
         Object var4 = ReflectionSupport.field(var3, "field_1886");
         boolean var5 = ReflectionSupport.callBool(var4, "method_1434", false);
         if (var5 && !lastAttack) {
            long var6 = System.currentTimeMillis();
            if (var6 - lastLogMs > 1000L) {
               lastLogMs = var6;
               System.out.println("[Auto Ominous] LMB AUDIT: world attack key became pressed; DonutUtility has no LMB owner (source=EXTERNAL_OR_UNKNOWN).");
            }
         }

         lastAttack = var5;
      } catch (Throwable var8) {
      }
   }

   public static void reset() {
      lastAttack = false;
   }
}
