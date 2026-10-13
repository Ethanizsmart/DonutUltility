package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public final class AhConfirmFlowFix {
   private AhConfirmFlowFix() {
   }

   public static String collectScreenText(Object var0, Object var1) {
      StringBuilder var2 = new StringBuilder();

      try {
         append(var2, text(invoke0(var1, "method_25440")));
         if (invoke0(var1, "method_25396") instanceof Iterable var4) {
            int var5 = 0;

            for (Object var7 : var4) {
               if (++var5 > 96) {
                  break;
               }

               append(var2, text(invoke0(var7, "method_25369")));
               append(var2, text(fieldDeep(var7, "field_22754")));
            }
         }

         Object var17 = invoke0(var1, "method_17577");
         if (var17 != null) {
            int var19 = fieldDeep(var17, "field_7761") instanceof List var20 ? var20.size() : 0;
            int var21 = Math.max(0, var19 - 36);

            for (int var8 = 0; var8 < var21; var8++) {
               Object var9 = invokeInt(var17, "method_7611", var8);
               Object var10 = var9 == null ? null : invoke0(var9, "method_7677");
               if (var10 != null && !Boolean.TRUE.equals(invoke0(var10, "method_7960"))) {
                  String var11 = String.valueOf(var10);
                  String var12 = AhPriceComponentReader.read(var10);
                  String var13 = (var11 + " " + var12).toLowerCase(Locale.ROOT);
                  if (var13.contains("diamond_sword") || var13.contains("diamond sword")) {
                     append(var2, "diamond sword");
                     append(var2, var12);
                  }
               }
            }
         }
      } catch (Throwable var15) {
         System.err.println("[AH Confirm Flow Fix] collectScreenText error: " + var15);
      }

      String var16 = var2.toString();

      try {
         AhDiagnosticRuntime.screenText(var16);
      } catch (Throwable var14) {
      }

      return AhConfirmRaceGuard.observe(var0, var16);
   }

   public static boolean pressYes(Object var0) {
      return AhConfirmYesTargetFix.pressYes(var0);
   }

   private static void append(StringBuilder var0, String var1) {
      if (var1 != null) {
         var1 = var1.trim();
         if (!var1.isEmpty()) {
            if (var0.length() > 0) {
               var0.append(' ');
            }

            var0.append(var1);
         }
      }
   }

   private static String text(Object var0) {
      if (var0 == null) {
         return "";
      }

      try {
         Method var1 = findMethod(var0.getClass(), "method_10851", 0);
         if (var1 != null) {
            var1.setAccessible(true);
            Object var2 = var1.invoke(var0);
            if (var2 != null) {
               return String.valueOf(var2);
            }
         }
      } catch (Throwable var3) {
      }

      return String.valueOf(var0);
   }

   private static Object invoke0(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         Method var2 = findMethod(var0.getClass(), var1, 0);
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
      if (var0 == null) {
         return null;
      }

      for (Class var3 = var0.getClass(); var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == 1) {
               Class var8 = var7.getParameterTypes()[0];
               if (var8 == int.class || var8 == Integer.class) {
                  try {
                     var7.setAccessible(true);
                     return var7.invoke(var0, var2);
                  } catch (Throwable var10) {
                  }
               }
            }
         }
      }

      return null;
   }

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }

   private static Object fieldDeep(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (Throwable var4) {
         }
      }

      return null;
   }
}
