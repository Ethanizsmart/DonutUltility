package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AhPerSlotDiagnosticRuntime {
   private static final Map<Integer, Integer> LAST_HASH = new HashMap<>();
   private static long lastGuiIdentity = Long.MIN_VALUE;
   private static long lastDumpAt;

   private AhPerSlotDiagnosticRuntime() {
   }

   public static void dump(Object var0) {
      try {
         if (var0 == null) {
            return;
         }

         Object var1 = field(var0, "field_1755");
         if (var1 == null) {
            return;
         }

         Object var2 = invoke(var1, "method_17577");
         if (var2 == null) {
            return;
         }

         Object var3 = field(var2, "field_7761");
         int var4 = listSize(var3);
         int var5 = Math.max(0, var4 - 36);
         if (var5 <= 0) {
            return;
         }

         int var6 = intField(var2, "field_7763", -1);
         long var7 = (long)System.identityHashCode(var2) << 32 ^ var6 & 4294967295L;
         if (var7 != lastGuiIdentity) {
            LAST_HASH.clear();
            lastGuiIdentity = var7;
            System.out.println("[AH-SLOT] BEGIN syncId=" + var6 + " totalSlots=" + var4 + " serverSlots=" + var5 + " screen=" + var1.getClass().getName());
         }

         long var9 = System.currentTimeMillis();
         if (var9 - lastDumpAt < 120L) {
            return;
         }

         lastDumpAt = var9;
         Class var11 = Class.forName("dev.autoominous.AhSwordPurchaseFix");
         Method var12 = declared(var11, "tooltipText", 2);
         Method var13 = declared(var11, "parsePrice", 1);
         Class var14 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         Method var15 = declared(var14, "isDiamondSword", 1);
         Method var16 = declared(var14, "hasLootingThree", 2);
         if (var12 != null) {
            var12.setAccessible(true);
         }

         if (var13 != null) {
            var13.setAccessible(true);
         }

         if (var15 != null) {
            var15.setAccessible(true);
         }

         if (var16 != null) {
            var16.setAccessible(true);
         }

         int var17 = 0;

         for (int var18 = 0; var18 < var5; var18++) {
            Object var19 = invokeInt(var2, "method_7611", var18);
            if (var19 != null) {
               Object var20 = invoke(var19, "method_7677");
               if (var20 != null && !isEmpty(var20)) {
                  String var21 = "";

                  try {
                     if (var12 != null) {
                        var21 = String.valueOf(var12.invoke(null, var0, var20));
                     }
                  } catch (Throwable var40) {
                  }

                  String var22 = String.valueOf(var20);
                  String var23 = (var21 + " " + var22).toLowerCase(Locale.ROOT);
                  boolean var24 = false;
                  boolean var25 = false;
                  long var26 = -1L;

                  try {
                     if (var15 != null) {
                        var24 = Boolean.TRUE.equals(var15.invoke(null, var20));
                     }
                  } catch (Throwable var39) {
                  }

                  try {
                     if (var16 != null) {
                        var25 = Boolean.TRUE.equals(var16.invoke(null, var0, var20));
                     }
                  } catch (Throwable var38) {
                  }

                  try {
                     if (var13 != null) {
                        var26 = ((Number)var13.invoke(null, var21)).longValue();
                     }
                  } catch (Throwable var37) {
                  }

                  boolean var28 = var24 || var25 || var23.contains("sword") || var23.contains("loot");
                  if (var28) {
                     var17++;
                     int var29 = intField(var19, "field_7874", var18);
                     boolean var30 = var24 && var25 && var26 >= 0L && var26 <= 1000000L;
                     String var31 = AhPriceComponentReader.sourceFor(var20);
                     String var32 = compact(var21, 700);
                     String var33 = compact(var22, 260);
                     String var34 = "slotIndex="
                        + var18
                        + " slotId="
                        + var29
                        + " diamond="
                        + var24
                        + " looting3="
                        + var25
                        + " price="
                        + var26
                        + " priceSource="
                        + var31
                        + " accept="
                        + var30
                        + " stack=\""
                        + var33
                        + "\" tooltip=\""
                        + var32
                        + "\"";
                     int var35 = var34.hashCode();
                     Integer var36 = LAST_HASH.put(var18, var35);
                     if (var36 == null || var36 != var35) {
                        System.out.println("[AH-SLOT] " + var34);
                     }
                  }
               }
            }
         }

         if (var17 == 0 && LAST_HASH.isEmpty()) {
            System.out.println("[AH-SLOT] NO_SWORD_OR_LOOTING_CANDIDATES serverSlots=" + var5);
         }
      } catch (Throwable var41) {
         System.out.println("[AH-SLOT] DIAG_ERROR " + var41.getClass().getName() + ": " + var41.getMessage());
      }
   }

   private static String compact(String var0, int var1) {
      if (var0 == null) {
         return "";
      }

      var0 = var0.replace('\n', '|').replace('\r', '|').replaceAll("\\s+", " ").trim();
      return var0.length() <= var1 ? var0 : var0.substring(0, var1) + "...";
   }

   private static Method declared(Class<?> var0, String var1, int var2) {
      try {
         for (Method var6 : var0.getDeclaredMethods()) {
            if (var6.getName().equals(var1) && var6.getParameterCount() == var2) {
               return var6;
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static Object field(Object var0, String var1) throws Exception {
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
         }
      }

      return null;
   }

   private static int intField(Object var0, String var1, int var2) {
      try {
         Object var3 = field(var0, var1);
         return var3 instanceof Number ? ((Number)var3).intValue() : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static Object invoke(Object var0, String var1) {
      try {
         Method var2 = method(var0.getClass(), var1, 0);
         if (var2 == null) {
            return null;
         }

         var2.setAccessible(true);
         return var2.invoke(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Object invokeInt(Object var0, String var1, int var2) {
      try {
         Method var3 = method(var0.getClass(), var1, 1);
         if (var3 == null) {
            return null;
         }

         var3.setAccessible(true);
         return var3.invoke(var0, var2);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Method method(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }

   private static boolean isEmpty(Object var0) {
      try {
         Object var1 = invoke(var0, "method_7960");
         return Boolean.TRUE.equals(var1);
      } catch (Throwable var2) {
         return false;
      }
   }

   private static int listSize(Object var0) {
      if (var0 == null) {
         return 0;
      } else if (var0 instanceof Collection var3) {
         return var3.size();
      } else {
         try {
            Method var1 = var0.getClass().getMethod("size");
            return ((Number)var1.invoke(var0)).intValue();
         } catch (Throwable var2) {
            return 0;
         }
      }
   }
}
