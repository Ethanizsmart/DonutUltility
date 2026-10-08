package dev.autoominous;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_634;

public final class AutoTpaRuntime {
   private static final Pattern TELEPORT_HERE = Pattern.compile("(?im)^\\s*([A-Za-z0-9_]{1,16})\\s+sent you a teleport here request\\s*$");
   private static final long DUPLICATE_SUPPRESS_MS = 2000L;
   private static final long CONFIRM_TIMEOUT_MS = 8000L;
   private static long pendingAtMillis = -1L;
   private static String pendingUser;
   private static String lastRequestUser;
   private static long lastRequestMillis;
   private static boolean awaitingConfirm;
   private static long confirmDeadlineMillis = -1L;

   private AutoTpaRuntime() {
   }

   public static void onGameMessageObject(Object var0) {
      if (var0 != null) {
         String var1 = extractPlainText(var0);
         if (var1 != null) {
            onGameMessage(var1);
         }
      }
   }

   private static String extractPlainText(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            Method var1 = var0.getClass().getMethod("getString");
            if (var1.invoke(var0) instanceof String var8) {
               return var8;
            }
         } catch (Throwable var5) {
         }

         try {
            Method var6 = var0.getClass().getMethod("method_10858", int.class);
            if (var6.invoke(var0, 32767) instanceof String var3) {
               return var3;
            }
         } catch (Throwable var4) {
         }

         return null;
      }
   }

   public static void onGameMessage(String var0) {
      if (AutoTpaSettings.isEnabled() && var0 != null && !var0.isBlank()) {
         Matcher var1 = TELEPORT_HERE.matcher(var0.replace('\r', '\n'));

         while (var1.find()) {
            String var2 = var1.group(1);
            if (AutoTpaSettings.isWhitelisted(var2)) {
               long var3 = System.currentTimeMillis();
               if (lastRequestUser != null && lastRequestUser.equalsIgnoreCase(var2) && var3 - lastRequestMillis < 2000L) {
                  return;
               }

               lastRequestUser = var2;
               lastRequestMillis = var3;
               pendingUser = var2;
               pendingAtMillis = var3 + AutoTpaSettings.getDelayMs();
               awaitingConfirm = false;
               confirmDeadlineMillis = -1L;
               System.out.println("[Auto Ominous] Auto TPA queued for " + var2 + " (delay " + AutoTpaSettings.getDelayMs() + " ms)");
               return;
            }
         }
      }
   }

   public static void onTick() {
      AutoTpaSettings.maybePersist();
      if (!VexSafetyRuntime.allowAutoTpa(AutoTpaSettings.isEnabled())) {
         clearAll();
      } else {
         long var0 = System.currentTimeMillis();
         if (awaitingConfirm) {
            if (tryConfirmGui()) {
               System.out.println("[Auto Ominous] Auto TPA GUI confirmed for " + pendingUser);
               clearAll();
            } else {
               if (confirmDeadlineMillis > 0L && var0 > confirmDeadlineMillis) {
                  System.err.println("[Auto Ominous] Auto TPA confirm GUI timed out for " + pendingUser);
                  clearAll();
               }
            }
         } else if (pendingAtMillis >= 0L && var0 >= pendingAtMillis) {
            try {
               class_310 var2 = class_310.method_1551();
               if (var2 == null) {
                  return;
               }

               class_634 var3 = var2.method_1562();
               if (var3 == null) {
                  return;
               }

               var3.method_45730("tpaccept");
               pendingAtMillis = -1L;
               awaitingConfirm = true;
               confirmDeadlineMillis = var0 + 8000L;
               System.out.println("[Auto Ominous] Auto TPA sent /tpaccept for " + pendingUser + "; waiting for Confirm GUI");
            } catch (Throwable var4) {
               System.err.println("[Auto Ominous] Auto TPA /tpaccept failed: " + var4.getMessage());
               clearAll();
            }
         }
      }
   }

   private static boolean tryConfirmGui() {
      try {
         class_310 var0 = class_310.method_1551();
         if (var0 == null || var0.field_1755 == null || var0.field_1724 == null || var0.field_1761 == null) {
            return false;
         }

         String var1 = extractPlainText(var0.field_1755.method_25440());
         if (var1 == null) {
            return false;
         }

         String var2 = var1.trim().toLowerCase(Locale.ROOT);
         if (!var2.contains("accept tpahere request")) {
            return false;
         }

         class_1703 var3 = var0.field_1724.field_7512;
         if (var3 == null || var3.field_7761 == null) {
            return false;
         }

         for (class_1735 var5 : var3.field_7761) {
            if (var5 != null && var5.method_7681()) {
               class_1799 var6 = var5.method_7677();
               if (var6 != null) {
                  String var7 = extractPlainText(var6.method_7964());
                  if (var7 != null && var7.trim().equalsIgnoreCase("Confirm")) {
                     var0.field_1761.method_2906(var3.field_7763, var5.field_7874, 0, class_1713.field_7790, var0.field_1724);
                     return true;
                  }
               }
            }
         }
      } catch (Throwable var8) {
         System.err.println("[Auto Ominous] Auto TPA confirm scan failed: " + var8.getMessage());
      }

      return false;
   }

   private static void clearAll() {
      pendingAtMillis = -1L;
      pendingUser = null;
      awaitingConfirm = false;
      confirmDeadlineMillis = -1L;
   }
}
