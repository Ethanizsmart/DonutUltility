package dev.autoominous;

import java.lang.reflect.Method;

public final class ConsumableRecoveryRuntime {
   private static Object ominousBottle;
   private static Object fireworkRocket;
   private static int omiBaseline = -1;
   private static int fireworkBaseline = -1;
   private static long lastPromoteMs;

   private ConsumableRecoveryRuntime() {
   }

   public static void tickHotbarPromotion(Object var0) {
      if (var0 != null) {
         String var1;
         try {
            var1 = FarmCycleRuntime.phaseName();
         } catch (Throwable var3) {
            return;
         }

         if (var1 != null) {
            try {
               if (var1.equals("WAIT_OMI") || var1.equals("DRINK_OMI")) {
                  promoteIfNeeded(var0, true);
               } else if (var1.equals("WAIT_FIREWORK") || var1.equals("USE_FIREWORK")) {
                  promoteIfNeeded(var0, false);
               }
            } catch (Throwable var4) {
               BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.promote", var4);
            }
         }
      }
   }

   public static void resetOmiBaseline() {
      omiBaseline = -1;
   }

   public static void resetFireworkBaseline() {
      fireworkBaseline = -1;
   }

   public static void armOmiBaseline() {
      try {
         omiBaseline = count(true);
      } catch (Throwable var1) {
         omiBaseline = -1;
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.armOmi", var1);
      }
   }

   public static void armFireworkBaseline() {
      try {
         fireworkBaseline = count(false);
      } catch (Throwable var1) {
         fireworkBaseline = -1;
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.armFirework", var1);
      }
   }

   public static boolean omiConsumedSinceBaseline() {
      return consumed(true);
   }

   public static boolean fireworkConsumedSinceBaseline() {
      return consumed(false);
   }

   private static boolean consumed(boolean var0) {
      try {
         int var1 = count(var0);
         int var2 = var0 ? omiBaseline : fireworkBaseline;
         if (var2 < 0) {
            if (var0) {
               omiBaseline = var1;
            } else {
               fireworkBaseline = var1;
            }

            return false;
         }

         if (var1 < var2) {
            if (var0) {
               omiBaseline = -1;
            } else {
               fireworkBaseline = -1;
            }

            return true;
         }

         if (var1 > var2) {
            if (var0) {
               omiBaseline = var1;
            } else {
               fireworkBaseline = var1;
            }
         }
      } catch (Throwable var3) {
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.consumed", var3);
      }

      return false;
   }

   private static int count(boolean var0) throws Exception {
      ensureItems();
      Object var1 = ReflectionSupport.mc();
      if (var1 == null) {
         return 0;
      }

      Object var2 = ReflectionSupport.field(var1, "field_1724");
      if (var2 == null) {
         return 0;
      }

      Object var3 = ReflectionSupport.callNoArg(var2, "method_31548");
      if (var3 == null) {
         return 0;
      }

      int var4 = 0;

      for (int var5 = 0; var5 < 36; var5++) {
         Object var6 = invokeOneInt(var3, "method_5438", var5);
         if (matches(var6, var0)) {
            Object var7 = ReflectionSupport.callNoArg(var6, "method_7947");
            if (var7 instanceof Number) {
               var4 += Math.max(0, ((Number)var7).intValue());
            }
         }
      }

      return var4;
   }

   private static void promoteIfNeeded(Object var0, boolean var1) throws Exception {
      ensureItems();
      Object var2 = ReflectionSupport.field(var0, "field_1724");
      Object var3 = ReflectionSupport.field(var0, "field_1761");
      if (var2 != null && var3 != null) {
         Object var4 = ReflectionSupport.callNoArg(var2, "method_31548");
         if (var4 != null) {
            for (int var5 = 0; var5 < 9; var5++) {
               if (matches(invokeOneInt(var4, "method_5438", var5), var1)) {
                  return;
               }
            }

            int var14 = -1;

            for (int var6 = 9; var6 < 36; var6++) {
               if (matches(invokeOneInt(var4, "method_5438", var6), var1)) {
                  var14 = var6;
                  break;
               }
            }

            if (var14 >= 0) {
               long var15 = System.currentTimeMillis();
               if (var15 - lastPromoteMs >= 250L) {
                  int var8 = -1;

                  for (int var9 = 0; var9 < 9; var9++) {
                     Object var10 = invokeOneInt(var4, "method_5438", var9);
                     if (isEmpty(var10)) {
                        var8 = var9;
                        break;
                     }
                  }

                  if (var8 < 0) {
                     for (int var16 = 0; var16 < 9; var16++) {
                        Object var18 = invokeOneInt(var4, "method_5438", var16);
                        if (!isEmpty(var18) && !OminousSurplusSellPolicy.isProtectedForTransfer(var18)) {
                           var8 = var16;
                           break;
                        }
                     }
                  }

                  if (var8 < 0) {
                     BehaviorLogRuntime.warning(var1 ? "OMI_PROMOTE_BLOCKED" : "FIREWORK_PROMOTE_BLOCKED", "no safe hotbar slot available");
                  } else {
                     Object var17 = ReflectionSupport.field(var2, "field_7512");
                     if (var17 != null) {
                        Object var19 = ReflectionSupport.field(var17, "field_7763");
                        int var11 = var19 instanceof Number ? ((Number)var19).intValue() : 0;
                        Object var12 = ReflectionSupport.staticField("net.minecraft.class_1713", "field_7791");
                        if (var12 != null) {
                           Method var13 = findMethod(var3.getClass(), "method_2906", 5);
                           if (var13 != null) {
                              var13.setAccessible(true);
                              G232SafeRuntime.invokeSwapGuarded(var13, var3, new Object[]{var11, var14, var8, var12, var2});
                              lastPromoteMs = var15;
                              BehaviorLogRuntime.warning(var1 ? "OMI_PROMOTED" : "FIREWORK_PROMOTED", "inventorySlot=" + var14 + " -> hotbar=" + var8);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean matches(Object var0, boolean var1) {
      try {
         if (var0 != null && !isEmpty(var0)) {
            Object var2 = ReflectionSupport.callNoArg(var0, "method_7909");
            return !var1 ? var2 == fireworkRocket : var2 == ominousBottle || var2 != null && "net.minecraft.class_9462".equals(var2.getClass().getName());
         } else {
            return false;
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isEmpty(Object var0) {
      return var0 == null ? true : ReflectionSupport.callBool(var0, "method_7960", true);
   }

   private static void ensureItems() {
      if (fireworkRocket == null || ominousBottle == null) {
         ominousBottle = ReflectionSupport.staticField("net.minecraft.class_1802", "field_50140");
         fireworkRocket = ReflectionSupport.staticField("net.minecraft.class_1802", "field_8639");
      }
   }

   private static Object invokeOneInt(Object var0, String var1, int var2) throws Exception {
      Method var3 = findMethod(var0.getClass(), var1, 1);
      if (var3 == null) {
         throw new NoSuchMethodException(var1);
      }

      var3.setAccessible(true);
      return var3.invoke(var0, var2);
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
}
