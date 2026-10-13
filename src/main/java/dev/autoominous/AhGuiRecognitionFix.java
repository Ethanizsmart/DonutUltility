package dev.autoominous;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class AhGuiRecognitionFix {
   private static String lastDiag;

   private AhGuiRecognitionFix() {
   }

   public static String screenTitle(Object var0) {
      Object var1 = getField(var0, "field_1755");
      if (var1 == null) {
         return "";
      }

      String var2 = readTitle(var1);
      if (isAuctionTitle(var2)) {
         return var2;
      }

      if (!isAhWaitState()) {
         return var2;
      }

      if (!isHandledScreen(var1)) {
         return var2;
      }

      Object var3 = invoke0(var1, "method_17577", "getScreenHandler");
      if (var3 == null) {
         return var2;
      }

      Object var4 = getField(var3, "field_7761");
      int var5 = sizeOf(var4);
      int var6 = Math.max(0, var5 - 36);
      if (var6 < 45) {
         return var2;
      }

      String var7 = "fallback handled=true totalSlots=" + var5 + " serverSlots=" + var6 + " rawTitle=" + compact(var2);
      if (!Objects.equals(var7, lastDiag)) {
         System.out.println("[AH-DIAG] AH_GUI_RECOGNITION " + var7);
         lastDiag = var7;
      }

      return "Auction [handled-container fallback]";
   }

   private static boolean isAhWaitState() {
      try {
         Class var0 = Class.forName("dev.autoominous.DeathSwordAhRecoveryRuntime");
         Field var1 = findField(var0, "active");
         Field var2 = findField(var0, "state");
         var1.setAccessible(true);
         var2.setAccessible(true);
         return var1.getBoolean(null) && var2.getInt(null) == 5;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isAuctionTitle(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT);
      return var1.startsWith("auction") || var1.contains("auction (page");
   }

   private static boolean isHandledScreen(Object var0) {
      try {
         return Class.forName("net.minecraft.class_465").isInstance(var0);
      } catch (Throwable var2) {
         return false;
      }
   }

   private static String readTitle(Object var0) {
      Object var1 = invoke0(var0, "method_25440", "getTitle");
      if (var1 == null) {
         return "";
      } else if (var1 instanceof String var4) {
         return var4.trim();
      } else {
         return invoke0(var1, "method_10851", "getString") instanceof String var3 ? var3.trim() : String.valueOf(var1).trim();
      }
   }

   private static Object invoke0(Object var0, String... var1) {
      if (var0 == null) {
         return null;
      }

      for (String var5 : var1) {
         try {
            Method var6 = findMethod(var0.getClass(), var5, 0);
            var6.setAccessible(true);
            return var6.invoke(var0);
         } catch (Throwable var7) {
         }
      }

      return null;
   }

   private static Object getField(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         Field var2 = findField(var0.getClass(), var1);
         var2.setAccessible(true);
         return var2.get(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Field findField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            return var2.getDeclaredField(var1);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Method findMethod(Class<?> var0, String var1, int var2) throws NoSuchMethodException {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      throw new NoSuchMethodException(var1);
   }

   private static int sizeOf(Object var0) {
      if (var0 instanceof List var2) {
         return var2.size();
      } else if (var0 instanceof Collection var1) {
         return var1.size();
      } else {
         return var0 != null && var0.getClass().isArray() ? Array.getLength(var0) : -1;
      }
   }

   private static String compact(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replaceAll("\\s+", " ").trim();
         return var1.length() <= 120 ? var1 : var1.substring(0, 120) + "...";
      } else {
         return "<empty>";
      }
   }
}
