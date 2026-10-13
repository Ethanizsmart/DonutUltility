package com.seastore.auto.seaats.client;

import dev.autoominous.OminousSurplusSellPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class SellBatchRuntime {
   private static final int SELL_CAPACITY = 45;

   private SellBatchRuntime() {
   }

   public static void tickMoving(Object var0) {
      try {
         if (var0 == null) {
            resetIdle();
            return;
         }

         Object var1 = field(var0, "field_1724");
         Object var2 = field(var0, "field_1761");
         Object var3 = field(var0, "field_1755");
         if (var1 == null || var2 == null || var3 == null || !isHandledScreen(var3)) {
            resetIdle();
            return;
         }

         Object var4 = invokeNoArg(var3, "method_17577");
         if (var4 == null) {
            resetIdle();
            return;
         }

         OminousSurplusSellPolicy.prepareSellGui(var0, var4);
         Object var5 = invokeNoArg(var4, "method_34255");
         if (var5 != null && !isEmpty(var5)) {
            returnHeld(var0, var4);
            setState("MOVING_ITEMS");
            AutoSellLogic.tickDelay = 3;
            return;
         }

         int var6 = filledSellSlots(var4);
         if (var6 >= 45) {
            setState("CLICK_CONFIRM");
            AutoSellLogic.tickDelay = rand(2, 4);
            return;
         }

         int var7 = selectSlot(var4);
         if (var7 < 0) {
            setState("CLICK_CONFIRM");
            AutoSellLogic.tickDelay = rand(4, 8);
            return;
         }

         Object var8 = stackAt(var4, var7);
         if (var8 == null || isEmpty(var8) || OminousSurplusSellPolicy.isProtectedForTransfer(var8)) {
            setState("MOVING_ITEMS");
            AutoSellLogic.tickDelay = 2;
            return;
         }

         int var9 = slotCount(var4);
         int var10 = Math.max(0, var9 - 36);
         int var11 = 0;

         for (int var12 = var10; var12 < var9; var12++) {
            Object var13 = stackAt(var4, var12);
            if (var13 != null && !isEmpty(var13) && !OminousSurplusSellPolicy.isProtectedForTransfer(var13) && sameItem(var13, var8)) {
               var11++;
            }
         }

         int var17 = 45 - var6;
         int var18 = 0;
         if (var6 < 44 && var11 >= 2 && var17 > 1) {
            for (int var14 = var10; var14 < var9 && var18 < var17; var14++) {
               Object var15 = stackAt(var4, var14);
               if (var15 != null && !isEmpty(var15) && !OminousSurplusSellPolicy.isProtectedForTransfer(var15) && sameItem(var15, var8)) {
                  clickQuickMove(var2, var4, var1, var14);
                  var18++;
               }
            }
         } else {
            clickQuickMove(var2, var4, var1, var7);
            var18 = 1;
         }

         setState("MOVING_ITEMS");
         if (var18 >= 2) {
            AutoSellLogic.tickDelay = rand(3, 6);
         } else {
            AutoSellLogic.tickDelay = SellRhythmRuntime.nextDelayTicks();
         }
      } catch (Throwable var16) {
         System.err.println("[SeaATS-BATCH] " + var16.getClass().getSimpleName() + ": " + var16.getMessage());
         setState("MOVING_ITEMS");
         AutoSellLogic.tickDelay = 4;
      }
   }

   private static int filledSellSlots(Object var0) throws Exception {
      int var1 = slotCount(var0);
      int var2 = Math.min(45, var1);
      int var3 = 0;

      for (int var4 = 0; var4 < var2; var4++) {
         Object var5 = stackAt(var0, var4);
         if (var5 != null && !isEmpty(var5)) {
            var3++;
         }
      }

      return var3;
   }

   private static int selectSlot(Object var0) throws Exception {
      Method var1 = findMethod(SellRhythmRuntime.class, "selectSlot", 1);
      if (var1 == null) {
         return -1;
      }

      var1.setAccessible(true);
      Object var2 = var1.invoke(null, var0);
      return var2 instanceof Number ? ((Number)var2).intValue() : -1;
   }

   private static void clickQuickMove(Object var0, Object var1, Object var2, int var3) throws Exception {
      int var4 = intField(var1, "field_7763");
      Object var5 = staticField(Class.forName("net.minecraft.class_1713"), "field_7794");
      Method var6 = findMethod(var0.getClass(), "method_2906", 5);
      if (var6 == null) {
         throw new NoSuchMethodException("method_2906");
      }

      var6.setAccessible(true);
      var6.invoke(var0, var4, var3, 0, var5, var2);
   }

   private static void returnHeld(Object var0, Object var1) {
      try {
         Method var2 = findMethod(BulkSellRuntime.class, "returnHeld", 2);
         if (var2 != null) {
            var2.setAccessible(true);
            var2.invoke(null, var0, var1);
         }
      } catch (Throwable var3) {
      }
   }

   private static boolean isHandledScreen(Object var0) {
      try {
         return Class.forName("net.minecraft.class_465").isInstance(var0);
      } catch (Throwable var2) {
         return false;
      }
   }

   private static int slotCount(Object var0) throws Exception {
      Object var1 = field(var0, "field_7761");
      return var1 instanceof List ? ((List)var1).size() : 0;
   }

   private static Object stackAt(Object var0, int var1) throws Exception {
      Method var2 = findMethod(var0.getClass(), "method_7611", 1);
      if (var2 == null) {
         return null;
      }

      var2.setAccessible(true);
      Object var3 = var2.invoke(var0, var1);
      return var3 == null ? null : invokeNoArg(var3, "method_7677");
   }

   private static boolean isEmpty(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "method_7960");
         return var1 instanceof Boolean && (Boolean)var1;
      } catch (Throwable var2) {
         return true;
      }
   }

   private static boolean sameItem(Object var0, Object var1) {
      if (var0 != null && var1 != null) {
         try {
            Method var2 = var0.getClass().getMethod("method_31577", var0.getClass(), var0.getClass());
            Object var3 = var2.invoke(null, var0, var1);
            if (var3 instanceof Boolean) {
               return (Boolean)var3;
            }
         } catch (Throwable var5) {
         }

         try {
            return invokeNoArg(var0, "method_7909") == invokeNoArg(var1, "method_7909");
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void resetIdle() {
      try {
         HumanizedSellRuntime.forceReset();
      } catch (Throwable var1) {
      }

      setState("IDLE");
      AutoSellLogic.tickDelay = rand(6, 13);
   }

   private static void setState(String var0) {
      try {
         Class var1 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
         Class var2 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic$State");
         Field var3 = var1.getDeclaredField("currentState");
         var3.setAccessible(true);
         Enum var4 = Enum.valueOf(var2.asSubclass(Enum.class), var0);
         var3.set(null, var4);
      } catch (Throwable var5) {
      }
   }

   private static int rand(int var0, int var1) {
      return ThreadLocalRandom.current().nextInt(var0, var1);
   }

   private static Object invokeNoArg(Object var0, String var1) throws Exception {
      Method var2 = findMethod(var0.getClass(), var1, 0);
      if (var2 == null) {
         throw new NoSuchMethodException(var1);
      }

      var2.setAccessible(true);
      return var2.invoke(var0);
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
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static int intField(Object var0, String var1) throws Exception {
      Object var2 = field(var0, var1);
      return var2 instanceof Number ? ((Number)var2).intValue() : 0;
   }

   private static Object staticField(Class<?> var0, String var1) throws Exception {
      Field var2 = var0.getDeclaredField(var1);
      var2.setAccessible(true);
      return var2.get(null);
   }
}
