package dev.autoominous;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AhSwordPurchaseFix {
   private static final long MAX_PRICE = 1000000L;
   private static final Pattern PRICE = Pattern.compile("(?i)\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt]?)");

   private AhSwordPurchaseFix() {
   }

   public static Object cheapestListing(Object var0) {
      AhDiagnosticRuntime.scanStart(var0);

      Object var24;
      label64: {
         label63: {
            label62: {
               try {
                  Object var1 = field(var0, "field_1755");
                  if (var1 == null) {
                     var24 = null;
                     break label64;
                  }

                  Object var2 = invoke(var1, "method_17577");
                  if (var2 == null) {
                     var24 = null;
                     break label63;
                  }

                  Object var3 = field(var2, "field_7761");
                  int var4 = listSize(var3);
                  int var5 = Math.max(0, var4 - 36);
                  int var6 = -1;
                  long var7 = Long.MAX_VALUE;
                  String var9 = null;

                  for (int var10 = 0; var10 < var5; var10++) {
                     Object var11 = invokeInt(var2, "method_7611", var10);
                     if (var11 != null) {
                        Object var12 = invoke(var11, "method_7677");
                        if (!isEmpty(var12)) {
                           String var13 = tooltipText(var0, var12);
                           String var14 = var13.toLowerCase(Locale.ROOT);
                           if (isLootingDiamondSword(var0, var12, var14)) {
                              long var15 = parsePrice(var13);
                              if (var15 >= 0L && var15 <= 1000000L) {
                                 int var17 = intField(var11, "field_7874", var10);
                                 if (var15 < var7) {
                                    var7 = var15;
                                    var6 = var17;
                                    var9 = var13;
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (var6 < 0) {
                     System.out.println("[DonutUtility AH Fix] No Looting III Diamond Sword <= $1M found in visible AH page.");
                     var24 = null;
                     break label62;
                  }

                  Class var19 = Class.forName("dev.autoominous.DeathSwordAhRecoveryRuntime$Listing");
                  Constructor var20 = var19.getDeclaredConstructor(int.class, long.class);
                  var20.setAccessible(true);
                  Object var21 = var20.newInstance(var6, var7);
                  System.out.println("[DonutUtility AH Fix] Selected AH slotId=" + var6 + " price=$" + var7 + " tooltip=" + compact(var9));
                  var24 = var21;
               } catch (Throwable var18) {
                  System.err.println("[DonutUtility AH Fix] cheapestListing failed: " + var18);
                  AhDiagnosticRuntime.afterCheapest(null);
                  return null;
               }

               AhDiagnosticRuntime.afterCheapest(var24);
               return var24;
            }

            AhDiagnosticRuntime.afterCheapest(var24);
            return var24;
         }

         AhDiagnosticRuntime.afterCheapest(var24);
         return var24;
      }

      AhDiagnosticRuntime.afterCheapest(var24);
      return var24;
   }

   public static String collectScreenText(Object var0, Object var1) {
      return AhConfirmFlowFix.collectScreenText(var0, var1);
   }

   public static boolean pressYes(Object var0) {
      return AhConfirmFlowFix.pressYes(var0);
   }

   private static void clickSlot(Object var0, Object var1, int var2) throws Exception {
      int var3 = intField(var1, "field_7763", -1);
      Object var4 = field(var0, "field_1761");
      Object var5 = field(var0, "field_1724");
      Class var6 = Class.forName("net.minecraft.class_1713");
      Field var7 = staticField(var6, "field_7790");
      Object var8 = var7.get(null);
      Method var9 = method(var4.getClass(), "method_2906", 5);
      var9.invoke(var4, var3, var2, 0, var8, var5);
   }

   private static boolean isLootingDiamondSword(Object var0, Object var1, String var2) {
      try {
         Class var3 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         Method var4 = null;
         Method var5 = null;

         for (Method var9 : var3.getDeclaredMethods()) {
            if (var9.getName().equals("isDiamondSword") && var9.getParameterCount() == 1) {
               var4 = var9;
            }

            if (var9.getName().equals("hasLootingThree") && var9.getParameterCount() == 2) {
               var5 = var9;
            }
         }

         if (var4 != null && var5 != null) {
            var4.setAccessible(true);
            var5.setAccessible(true);
            if (Boolean.TRUE.equals(var4.invoke(null, var1)) && Boolean.TRUE.equals(var5.invoke(null, var0, var1))) {
               return true;
            }
         }
      } catch (Throwable var10) {
      }

      return var2.contains("diamond sword") && (var2.contains("looting iii") || var2.contains("looting 3") || var2.contains("looting: 3"));
   }

   private static int confirmScore(String var0) {
      byte var1 = 0;
      if (isNegativeConfirmText(var0)) {
         var1 -= 100;
      }

      if (var0.contains("confirm purchase")) {
         var1 += 60;
      }

      if (var0.contains("click to confirm")) {
         var1 += 50;
      }

      if (var0.contains("confirm")) {
         var1 += 30;
      }

      if (var0.contains("purchase")) {
         var1 += 25;
      }

      if (var0.contains("buy")) {
         var1 += 20;
      }

      if (var0.matches("(?s).*\\byes\\b.*")) {
         var1 += 30;
      }

      if (var0.contains("proceed")) {
         var1 += 15;
      }

      return var1;
   }

   private static boolean isPositiveConfirmText(String var0) {
      if (var0 == null) {
         return false;
      }

      var0 = var0.toLowerCase(Locale.ROOT);
      return (var0.equals("yes") || var0.contains("confirm") || var0.contains("purchase") || var0.contains("buy") || var0.contains("proceed"))
         && !isNegativeConfirmText(var0);
   }

   private static boolean isNegativeConfirmText(String var0) {
      if (var0 == null) {
         return false;
      }

      var0 = var0.toLowerCase(Locale.ROOT);
      return var0.equals("no") || var0.contains("cancel") || var0.contains("go back") || var0.contains("back") || var0.contains("close");
   }

   private static long expectedPrice() {
      try {
         Class var0 = Class.forName("dev.autoominous.DeathSwordAhRecoveryRuntime");
         Field var1 = staticField(var0, "expectedPrice");
         return var1.getLong(null);
      } catch (Throwable var2) {
         return -1L;
      }
   }

   private static long parsePrice(String var0) {
      if (var0 == null) {
         return -1L;
      }

      Matcher var1 = PRICE.matcher(var0);
      long var2 = -1L;

      while (var1.find()) {
         try {
            double var4 = Double.parseDouble(var1.group(1).replace(",", ""));
            String var6 = var1.group(2).toLowerCase(Locale.ROOT);

            double var7 = switch (var6) {
               case "k" -> 1000.0;
               case "m" -> 1000000.0;
               case "b" -> 1.0E9;
               case "t" -> 1.0E12;
               default -> 1.0;
            };
            long var12 = Math.round(var4 * var7);
            if (var2 < 0L || var12 < var2) {
               var2 = var12;
            }
         } catch (Throwable var11) {
         }
      }

      return var2;
   }

   private static String tooltipText(Object var0, Object var1) {
      StringBuilder var2 = new StringBuilder();

      try {
         Class var3 = Class.forName("net.minecraft.class_437");
         Method var4 = method(var3, "method_25408", 2);
         Object var5 = var4.invoke(null, var0, var1);
         if (var5 instanceof Iterable) {
            for (Object var8 : (Iterable)var5) {
               append(var2, text(var8));
            }
         }
      } catch (Throwable var10) {
      }

      try {
         append(var2, text(invoke(var1, "method_7964")));
      } catch (Throwable var9) {
      }

      append(var2, String.valueOf(var1));
      AhPriceComponentReader.append(var2, var1);
      return var2.toString();
   }

   private static Object mc() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = method(var0, "method_1551", 0);
         return var1.invoke(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static boolean isEmpty(Object var0) {
      if (var0 == null) {
         return true;
      }

      try {
         return Boolean.TRUE.equals(invoke(var0, "method_7960"));
      } catch (Throwable var2) {
         return false;
      }
   }

   private static String text(Object var0) {
      if (var0 == null) {
         return "";
      }

      try {
         Method var5 = method(var0.getClass(), "method_10858", 1);
         Object var6 = var5.invoke(var0, 512);
         return var6 == null ? "" : String.valueOf(var6);
      } catch (Throwable var4) {
         try {
            Method var1 = method(var0.getClass(), "getString", 0);
            Object var2 = var1.invoke(var0);
            return var2 == null ? "" : String.valueOf(var2);
         } catch (Throwable var3) {
            return "";
         }
      }
   }

   private static boolean containsAny(String var0, String... var1) {
      if (var0 == null) {
         return false;
      }

      String var2 = var0.toLowerCase(Locale.ROOT);

      for (String var6 : var1) {
         if (var2.contains(var6)) {
            return true;
         }
      }

      return false;
   }

   private static void append(StringBuilder var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         var0.append(var1).append('\n');
      }
   }

   private static String compact(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.replace('\n', ' ').replace('\r', ' ').trim().replaceAll("\\s+", " ");
      return var1.length() > 180 ? var1.substring(0, 180) + "..." : var1;
   }

   private static int listSize(Object var0) {
      if (var0 instanceof Collection var4) {
         return var4.size();
      } else {
         if (var0 == null) {
            return 0;
         }

         try {
            return invoke(var0, "size") instanceof Number var2 ? var2.intValue() : 0;
         } catch (Throwable var3) {
            return 0;
         }
      }
   }

   private static Object invokeInt(Object var0, String var1, int var2) {
      try {
         return method(var0.getClass(), var1, 1).invoke(var0, var2);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Object invoke(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         return method(var0.getClass(), var1, 0).invoke(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Method method(Class<?> var0, String var1, int var2) throws NoSuchMethodException {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               var7.setAccessible(true);
               return var7;
            }
         }
      }

      throw new NoSuchMethodException(var0.getName() + "." + var1 + "/" + var2);
   }

   private static Field staticField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var0.getName() + "." + var1);
   }

   private static Object field(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         return staticField(var0.getClass(), var1).get(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static int intField(Object var0, String var1, int var2) {
      if (var0 == null) {
         return var2;
      }

      try {
         return staticField(var0.getClass(), var1).getInt(var0);
      } catch (Throwable var4) {
         return var2;
      }
   }
}
