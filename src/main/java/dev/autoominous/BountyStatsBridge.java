package dev.autoominous;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class BountyStatsBridge {
   private static long totalAdded;
   private static long successfulAdds;
   private static String lastTarget = "-";

   private BountyStatsBridge() {
   }

   public static synchronized void recordSuccess(String var0, String var1) {
      long var2 = parseMoney(var0);
      if (var2 > 0L) {
         totalAdded = safeAdd(totalAdded, var2);
         successfulAdds++;
      }

      if (var1 != null && !var1.isBlank()) {
         lastTarget = var1.trim();
      }
   }

   public static synchronized String getTotalText() {
      return formatMoney(totalAdded);
   }

   public static synchronized long getSuccessfulAdds() {
      return successfulAdds;
   }

   public static synchronized String getLastTarget() {
      return lastTarget;
   }

   static long parseMoney(String var0) {
      if (var0 == null) {
         return 0L;
      }

      String var1 = var0.trim().toLowerCase(Locale.ROOT).replace("$", "").replace(",", "");
      if (!var1.isEmpty() && !var1.equals("-")) {
         long var2 = 1L;
         char var4 = var1.charAt(var1.length() - 1);
         if (var4 == 'k' || var4 == 'm' || var4 == 'b' || var4 == 't') {
            var1 = var1.substring(0, var1.length() - 1).trim();

            var2 = switch (var4) {
               case 'b' -> 1000000000L;
               case 'k' -> 1000L;
               case 'm' -> 1000000L;
               case 't' -> 1000000000000L;
               default -> 1L;
            };
         }

         try {
            BigDecimal var5 = new BigDecimal(var1).multiply(BigDecimal.valueOf(var2));
            if (var5.signum() <= 0) {
               return 0L;
            }

            BigDecimal var6 = BigDecimal.valueOf(Long.MAX_VALUE);
            return var5.compareTo(var6) > 0 ? Long.MAX_VALUE : var5.setScale(0, RoundingMode.DOWN).longValue();
         } catch (Throwable var7) {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   private static long safeAdd(long var0, long var2) {
      return var2 > 0L && var0 > Long.MAX_VALUE - var2 ? Long.MAX_VALUE : var0 + var2;
   }

   public static String formatMoney(long var0) {
      if (var0 <= 0L) {
         return "$0";
      } else if (var0 >= 1000000000000L) {
         return "$" + compact(var0, 1000000000000L) + "T";
      } else if (var0 >= 1000000000L) {
         return "$" + compact(var0, 1000000000L) + "B";
      } else if (var0 >= 1000000L) {
         return "$" + compact(var0, 1000000L) + "M";
      } else {
         return var0 >= 1000L ? "$" + compact(var0, 1000L) + "K" : "$" + var0;
      }
   }

   private static String compact(long var0, long var2) {
      BigDecimal var4 = BigDecimal.valueOf(var0).divide(BigDecimal.valueOf(var2), 1, RoundingMode.DOWN).stripTrailingZeros();
      return var4.toPlainString();
   }
}
