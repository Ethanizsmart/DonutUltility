package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class OminousSurplusSellPolicy {
   private static final String OMINOUS_ITEM_CLASS = "net.minecraft.class_9462";
   private static final String AUTO_SELL_LOGIC = "com.seastore.auto.seaats.client.AutoSellLogic";
   private static long lastFailureLogMs;

   private OminousSurplusSellPolicy() {
   }

   public static boolean isProtectedForInventoryProbe(Object var0) {
      try {
         if (!isOminous(var0)) {
            return legacyProtected(var0);
         }

         Object var1 = minecraftClient();
         int var2 = totalOminous(var1);
         return var2 <= 1;
      } catch (Throwable var3) {
         logFailure("inventory probe", var3);
         return true;
      }
   }

   public static boolean isProtectedForTransfer(Object var0) {
      try {
         if (!isOminous(var0)) {
            return legacyProtected(var0);
         } else {
            Object var1 = minecraftClient();
            OminousSurplusSellPolicy.Reserve var2 = reserve(var1);
            if (var2 == null) {
               return true;
            } else {
               return var0 == var2.stack ? true : totalOminous(var1) <= 1;
            }
         }
      } catch (Throwable var3) {
         logFailure("transfer guard", var3);
         return true;
      }
   }

   public static void prepareSellGui(Object var0, Object var1) {
      if (var0 != null && var1 != null) {
         try {
            OminousSurplusSellPolicy.Reserve var2 = reserve(var0);
            if (var2 == null || var2.total <= 1) {
               return;
            }

            int var3 = count(var2.stack);
            if (var3 <= 1) {
               return;
            }

            int var4 = slotCount(var1);
            if (var4 < 36) {
               return;
            }

            int var5 = var4 - 36;
            int var6 = screenSlotForInventoryIndex(var5, var2.inventoryIndex);
            if (var6 < var5 || var6 >= var4) {
               return;
            }

            Object var7 = stackAt(var1, var6);
            if (!isOminous(var7) || count(var7) <= 1) {
               return;
            }

            int var8 = findEmptySellSlot(var1, var5);
            if (var8 < 0) {
               return;
            }

            Object var9 = field(var0, "field_1761");
            Object var10 = field(var0, "field_1724");
            if (var9 == null || var10 == null) {
               return;
            }

            int var11 = intField(var1, "field_7763", -1);
            if (var11 < 0) {
               return;
            }

            Object var12 = staticField(Class.forName("net.minecraft.class_1713"), "field_7790");
            if (var12 == null) {
               return;
            }

            clickSlot(var9, var11, var6, 0, var12, var10);
            clickSlot(var9, var11, var6, 1, var12, var10);
            clickSlot(var9, var11, var8, 0, var12, var10);
            Object var13 = invokeNoArg(var1, "method_34255");
            Object var14 = stackAt(var1, var6);
            if (!isEmpty(var13) || !isOminous(var14) || count(var14) != 1) {
               if (!isEmpty(var13)) {
                  clickSlot(var9, var11, var6, 0, var12, var10);
               }

               log("reserve split verification failed; restored cursor when possible; final bottle remains protected");
               return;
            }

            log("reserved exactly 1 Ominous Bottle; moved " + (var3 - 1) + " surplus bottle(s) into sell batch");
         } catch (Throwable var15) {
            logFailure("prepare sell GUI", var15);
         }
      }
   }

   private static OminousSurplusSellPolicy.Reserve reserve(Object var0) throws Exception {
      Object var1 = field(var0, "field_1724");
      if (var1 == null) {
         return null;
      }

      Object var2 = invokeNoArg(var1, "method_31548");
      if (var2 == null) {
         return null;
      }

      int var3 = 0;
      Object[] var4 = new Object[36];

      for (int var5 = 0; var5 < 36; var5++) {
         Object var6 = invokeOneInt(var2, "method_5438", var5);
         var4[var5] = var6;
         if (isOminous(var6)) {
            var3 += Math.max(0, count(var6));
         }
      }

      if (var3 <= 0) {
         return null;
      }

      int var7 = invokeIntNoArg(var2, "method_67532", -1);
      if (var7 >= 0 && var7 < 9 && isOminous(var4[var7])) {
         return new OminousSurplusSellPolicy.Reserve(var7, var4[var7], var3);
      }

      for (int var8 = 0; var8 < 9; var8++) {
         if (isOminous(var4[var8])) {
            return new OminousSurplusSellPolicy.Reserve(var8, var4[var8], var3);
         }
      }

      for (int var9 = 9; var9 < 36; var9++) {
         if (isOminous(var4[var9])) {
            return new OminousSurplusSellPolicy.Reserve(var9, var4[var9], var3);
         }
      }

      return null;
   }

   private static int totalOminous(Object var0) throws Exception {
      OminousSurplusSellPolicy.Reserve var1 = reserve(var0);
      return var1 == null ? 0 : var1.total;
   }

   private static boolean isOminous(Object var0) throws Exception {
      if (var0 != null && !isEmpty(var0)) {
         Object var1 = invokeNoArg(var0, "method_7909");
         return var1 != null && "net.minecraft.class_9462".equals(var1.getClass().getName());
      } else {
         return false;
      }
   }

   private static boolean isEmpty(Object var0) throws Exception {
      if (var0 == null) {
         return true;
      }

      Object var1 = invokeNoArg(var0, "method_7960");
      return var1 instanceof Boolean && (Boolean)var1;
   }

   private static int count(Object var0) throws Exception {
      return var0 != null && !isEmpty(var0) ? invokeIntNoArg(var0, "method_7947", 1) : 0;
   }

   private static int slotCount(Object var0) throws Exception {
      Object var1 = field(var0, "field_7761");
      if (var1 == null) {
         return 0;
      }

      Method var2 = var1.getClass().getMethod("size");
      return ((Number)var2.invoke(var1)).intValue();
   }

   private static Object stackAt(Object var0, int var1) throws Exception {
      Object var2 = invokeOneInt(var0, "method_7611", var1);
      return var2 == null ? null : invokeNoArg(var2, "method_7677");
   }

   private static int findEmptySellSlot(Object var0, int var1) throws Exception {
      for (int var2 = 0; var2 < var1; var2++) {
         Object var3 = stackAt(var0, var2);
         if (isEmpty(var3)) {
            return var2;
         }
      }

      return -1;
   }

   private static int screenSlotForInventoryIndex(int var0, int var1) {
      if (var1 >= 9 && var1 <= 35) {
         return var0 + (var1 - 9);
      } else {
         return var1 >= 0 && var1 <= 8 ? var0 + 27 + var1 : -1;
      }
   }

   private static void clickSlot(Object var0, int var1, int var2, int var3, Object var4, Object var5) throws Exception {
      Method var6 = null;

      for (Method var10 : var0.getClass().getMethods()) {
         if (var10.getName().equals("method_2906") && var10.getParameterCount() == 5) {
            var6 = var10;
            break;
         }
      }

      if (var6 == null) {
         throw new NoSuchMethodException("method_2906");
      }

      var6.setAccessible(true);
      var6.invoke(var0, var1, var2, var3, var4, var5);
   }

   private static boolean legacyProtected(Object var0) throws Exception {
      Class var1 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
      Method var2 = null;

      for (Method var6 : var1.getDeclaredMethods()) {
         if (var6.getName().equals("isProtectedNeverSellItem") && var6.getParameterCount() == 1) {
            var2 = var6;
            break;
         }
      }

      if (var2 == null) {
         return true;
      }

      var2.setAccessible(true);
      Object var7 = var2.invoke(null, var0);
      return !(var7 instanceof Boolean) || (Boolean)var7;
   }

   private static Object minecraftClient() throws Exception {
      Class var0 = Class.forName("net.minecraft.class_310");
      Method var1 = var0.getMethod("method_1551");
      return var1.invoke(null);
   }

   private static Object invokeNoArg(Object var0, String var1) throws Exception {
      if (var0 == null) {
         return null;
      }

      Method var2 = findMethod(var0.getClass(), var1, 0);
      if (var2 == null) {
         throw new NoSuchMethodException(var1);
      }

      var2.setAccessible(true);
      return var2.invoke(var0);
   }

   private static Object invokeOneInt(Object var0, String var1, int var2) throws Exception {
      Method var3 = findMethod(var0.getClass(), var1, 1);
      if (var3 == null) {
         throw new NoSuchMethodException(var1);
      }

      var3.setAccessible(true);
      return var3.invoke(var0, var2);
   }

   private static int invokeIntNoArg(Object var0, String var1, int var2) throws Exception {
      Object var3 = invokeNoArg(var0, var1);
      return var3 instanceof Number ? ((Number)var3).intValue() : var2;
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

   private static Object field(Object var0, String var1) throws Exception {
      if (var0 == null) {
         return null;
      }

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

   private static int intField(Object var0, String var1, int var2) throws Exception {
      Object var3 = field(var0, var1);
      return var3 instanceof Number ? ((Number)var3).intValue() : var2;
   }

   private static Object staticField(Class<?> var0, String var1) throws Exception {
      Field var2 = var0.getDeclaredField(var1);
      var2.setAccessible(true);
      return var2.get(null);
   }

   private static void logFailure(String var0, Throwable var1) {
      long var2 = System.currentTimeMillis();
      if (var2 - lastFailureLogMs >= 10000L) {
         lastFailureLogMs = var2;
         log(var0 + " failed safely: " + var1.getClass().getSimpleName() + ": " + var1.getMessage());
      }
   }

   private static void log(String var0) {
      System.out.println("[SeaATS] Ominous surplus: " + var0);
   }

   private static final class Reserve {
      final int inventoryIndex;
      final Object stack;
      final int total;

      Reserve(int var1, Object var2, int var3) {
         this.inventoryIndex = var1;
         this.stack = var2;
         this.total = var3;
      }
   }
}
