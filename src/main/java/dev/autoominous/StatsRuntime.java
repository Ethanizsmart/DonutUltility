package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StatsRuntime {
   private static final Pattern MONEY = Pattern.compile("(?i)\\$\\s*([0-9][0-9,.]*)\\s*([KMBT])?");
   private static final Pattern AMOUNT_ONLY = Pattern.compile("(?i)^\\s*\\$\\s*[0-9][0-9,.]*\\s*[KMBT]?\\s*$");
   private static final Pattern SELL_WORD = Pattern.compile("(?iu)(?:^|[^\\p{L}])(sold|sell|bán)(?:[^\\p{L}]|$)");
   private static final Pattern MC_FORMAT = Pattern.compile("§[0-9A-FK-ORX]", 2);
   private static double totalEarned;
   private static int salesCount;
   private static long startTimeMs;
   private static boolean running;
   private static boolean previousEnabled;
   private static String lastAcceptedText;
   private static long lastAcceptedAtMs;

   private StatsRuntime() {
   }

   public static void onTick() {
      StatsProfitRateRuntime.onStatsTick();
      StatsSettings.maybePersist();
      boolean var0 = StatsSettings.isEnabled();
      if (var0 && !previousEnabled) {
         reset();
      }

      previousEnabled = var0;
      if (!var0) {
         running = false;
      } else {
         if (!running && isClientInWorld()) {
            reset();
         }
      }
   }

   public static void reset() {
      totalEarned = 0.0;
      salesCount = 0;
      startTimeMs = System.currentTimeMillis();
      running = StatsSettings.isEnabled() && isClientInWorld();
      lastAcceptedText = null;
      lastAcceptedAtMs = 0L;
      StatsProfitRateRuntime.onStatsFullReset();
   }

   public static void onGameMessageObject(Object var0) {
      if (StatsSettings.isEnabled() && running && var0 != null) {
         String var1 = extractPlainText(var0);
         if (var1 != null) {
            String var2 = normalizeMessage(var1);
            if (!var2.isEmpty() && looksLikeSellReceipt(var2)) {
               long var3 = System.currentTimeMillis();
               if (!var2.equals(lastAcceptedText) || var3 - lastAcceptedAtMs >= 250L) {
                  Matcher var5 = MONEY.matcher(var2);
                  if (var5.find()) {
                     double var6;
                     try {
                        String var8 = var5.group(1).replace(",", "");
                        var6 = Double.parseDouble(var8);
                        String var9 = var5.group(2);
                        if (var9 != null) {
                           String var10 = var9.toUpperCase(Locale.ROOT);
                           switch (var10) {
                              case "K":
                                 var6 *= 1000.0;
                                 break;
                              case "M":
                                 var6 *= 1000000.0;
                                 break;
                              case "B":
                                 var6 *= 1.0E9;
                                 break;
                              case "T":
                                 var6 *= 1.0E12;
                           }
                        }
                     } catch (Throwable var12) {
                        return;
                     }

                     if (var6 > 0.0 && !Double.isNaN(var6) && !Double.isInfinite(var6)) {
                        if (SlashSellRuntime.consumeExpectedSellReceipt()) {
                           totalEarned += var6;
                           salesCount++;
                           lastAcceptedText = var2;
                           lastAcceptedAtMs = var3;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean looksLikeSellReceipt(String var0) {
      return AMOUNT_ONLY.matcher(var0).matches() || SELL_WORD.matcher(var0).find();
   }

   private static String normalizeMessage(String var0) {
      String var1 = Normalizer.normalize(var0, Form.NFKC);
      var1 = MC_FORMAT.matcher(var1).replaceAll("");
      StringBuilder var2 = new StringBuilder(var1.length());

      for (int var3 = 0; var3 < var1.length(); var3++) {
         char var4 = var1.charAt(var3);
         if (Character.getType(var4) != 16) {
            var2.append(var4);
         }
      }

      return var2.toString().trim();
   }

   public static double getTotalEarned() {
      return totalEarned;
   }

   public static int getSalesCount() {
      return salesCount;
   }

   public static long getElapsedTimeMs() {
      return startTimeMs <= 0L ? 0L : Math.max(0L, System.currentTimeMillis() - startTimeMs);
   }

   public static String getProfitText() {
      return "Profit: " + formatCurrency(totalEarned);
   }

   public static String getProfitPerHourText() {
      return "Profit/h: " + formatCurrency(getEarnedPerHour());
   }

   public static String getTimeText() {
      return "Time: " + formatElapsed(getElapsedTimeMs());
   }

   public static String getSalesText() {
      return "Sales: " + salesCount;
   }

   public static String formatCurrency(double var0) {
      double var2 = Math.abs(var0);
      if (var2 >= 1.0E12) {
         return String.format(Locale.ROOT, "$%.2fT", var0 / 1.0E12);
      } else if (var2 >= 1.0E9) {
         return String.format(Locale.ROOT, "$%.2fB", var0 / 1.0E9);
      } else if (var2 >= 1000000.0) {
         return String.format(Locale.ROOT, "$%.2fM", var0 / 1000000.0);
      } else {
         return var2 >= 1000.0 ? String.format(Locale.ROOT, "$%.2fK", var0 / 1000.0) : String.format(Locale.ROOT, "$%.0f", var0);
      }
   }

   private static String formatElapsed(long var0) {
      long var2 = Math.max(0L, var0 / 1000L);
      long var4 = var2 / 3600L;
      long var6 = var2 % 3600L / 60L;
      long var8 = var2 % 60L;
      return String.format(Locale.ROOT, "%02d:%02d:%02d", var4, var6, var8);
   }

   private static boolean isClientInWorld() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getDeclaredMethod("method_1551");
         var1.setAccessible(true);
         Object var2 = var1.invoke(null);
         if (var2 == null) {
            return false;
         } else {
            Field var3 = var0.getDeclaredField("field_1724");
            Field var4 = var0.getDeclaredField("field_1687");
            var3.setAccessible(true);
            var4.setAccessible(true);
            return var3.get(var2) != null && var4.get(var2) != null;
         }
      } catch (Throwable var5) {
         return false;
      }
   }

   private static String extractPlainText(Object var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof String) {
         return (String)var0;
      } else {
         try {
            Object var1 = var0.getClass().getMethod("getString").invoke(var0);
            if (var1 instanceof String) {
               return (String)var1;
            }
         } catch (Throwable var4) {
         }

         try {
            Object var5 = var0.getClass().getMethod("method_10858", int.class).invoke(var0, 32767);
            if (var5 instanceof String) {
               return (String)var5;
            }
         } catch (Throwable var3) {
         }

         try {
            Object var6 = var0.getClass().getMethod("method_54160").invoke(var0);
            if (var6 instanceof String) {
               return (String)var6;
            }
         } catch (Throwable var2) {
         }

         return null;
      }
   }

   public static double getEarnedPerHour() {
      return StatsProfitRateRuntime.getEarnedPerHour();
   }
}
