package com.seastore.auto.seaats.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;

public final class BulkSellRuntime {
   private BulkSellRuntime() {
   }

   public static void tickMoving(Object var0) {
      SellBatchRuntime.tickMoving(var0);
   }

   private static int selectSlot(Object var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.class_1703");
      Method var2 = SellRhythmRuntime.class.getDeclaredMethod("selectSlot", var1);
      var2.setAccessible(true);
      Object var3 = var2.invoke(null, var0);
      return var3 instanceof Number ? ((Number)var3).intValue() : -1;
   }

   private static void clickQuickMove(Object var0, Object var1, Object var2, Object var3, int var4) throws Exception {
      int var5 = intField(var2, "field_7763");
      Class var6 = Class.forName("net.minecraft.class_1713");
      Object var7 = var6.getField("field_7794").get(null);
      Method var8 = findMethod(var1.getClass(), "method_2906", 5);
      if (var8 == null) {
         throw new NoSuchMethodException("method_2906/5");
      }

      var8.setAccessible(true);
      var8.invoke(var1, var5, var4, 0, var7, var3);
   }

   private static void returnHeld(Object var0, Object var1) {
      try {
         Class var2 = Class.forName("net.minecraft.class_310");
         Class var3 = Class.forName("net.minecraft.class_1703");
         Method var4 = HumanizedSellRuntime.class.getDeclaredMethod("returnHeldStackToSourceIfPossible", var2, var3);
         var4.setAccessible(true);
         var4.invoke(null, var0, var1);
      } catch (Throwable var5) {
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
      if (var1 == null) {
         return 0;
      }

      Method var2 = var1.getClass().getMethod("size");
      return ((Number)var2.invoke(var1)).intValue();
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
         } catch (Throwable var4) {
         }

         try {
            Object var6 = invokeNoArg(var0, "method_7909");
            Object var7 = invokeNoArg(var1, "method_7909");
            return var6 != null && var6 == var7;
         } catch (Throwable var5) {
            return false;
         }
      } else {
         return false;
      }
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

   private static void resetIdle() {
      try {
         HumanizedSellRuntime.forceReset();
      } catch (Throwable var1) {
      }

      setState("IDLE");
      AutoSellLogic.tickDelay = ThreadLocalRandom.current().nextInt(6, 13);
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

      return null;
   }

   private static int intField(Object var0, String var1) throws Exception {
      Object var2 = field(var0, var1);
      return var2 instanceof Number ? ((Number)var2).intValue() : 0;
   }
}
