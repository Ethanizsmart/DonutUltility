package dev.autoominous;

import com.seastore.auto.seaats.client.HumanizedSellRuntime;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ProtectedItemAutoUseRuntime {
   private static boolean pressedByUs;
   private static boolean toggleKnown;
   private static boolean lastToggle;
   private static Class<?> mcClass;
   private static Class<?> itemsClass;
   private static Object ominousBottle;
   private static Object fireworkRocket;
   private static Object[] swords;
   private static int activeSlot = -1;
   private static int activePriority = 0;
   private static int repeatCooldown = 0;
   private static String lastError = "";

   private ProtectedItemAutoUseRuntime() {
   }

   public static void onTick() {
      Object var0 = getMinecraftClient();
      boolean var1 = isEnabled();
      persistToggleIfChanged(var1);
      if (var0 != null && var1) {
         try {
            Object var2 = getField(var0, "field_1724");
            Object var3 = getField(var0, "field_1755");
            if (var2 == null || var3 != null) {
               release(var0);
               resetActive();
               return;
            }

            if (isExternalSafetyBusy() || isSeaBusy()) {
               release(var0);
               resetActive();
               return;
            }

            Object var4 = invokeNoArg(var2, "method_31548");
            if (var4 == null) {
               release(var0);
               resetActive();
               return;
            }

            int var5 = invokeIntNoArg(var4, "method_67532", -1);
            int var6 = var5 >= 0 && var5 < 9 ? priorityOfStack(invokeOneInt(var4, "method_5438", var5)) : 0;
            int var7 = 0;
            int var8 = -1;

            for (int var9 = 0; var9 < 9; var9++) {
               Object var10 = invokeOneInt(var4, "method_5438", var9);
               int var11 = priorityOfStack(var10);
               if (var11 > var7) {
                  var7 = var11;
                  var8 = var9;
               }
            }

            if (var7 == 0) {
               release(var0);
               resetActive();
               return;
            }

            int var13 = var6 == var7 ? var5 : var8;
            if (var13 < 0 || var13 > 8) {
               release(var0);
               resetActive();
               return;
            }

            boolean var14 = var13 != activeSlot || var7 != activePriority;
            if (var5 != var13) {
               invokeOneIntVoid(var4, "method_61496", var13);
               var14 = true;
            }

            setUsePressed(var0, true);
            pressedByUs = true;
            boolean var15 = invokeBooleanNoArg(var2, "method_6115", false);
            if (var7 == 3) {
               if (!var15 && (var14 || repeatCooldown <= 0)) {
                  triggerItemUse(var0);
                  repeatCooldown = 4;
               }
            } else if (!var15 && (var14 || repeatCooldown <= 0)) {
               triggerItemUse(var0);
               repeatCooldown = 4;
            }

            if (repeatCooldown > 0) {
               repeatCooldown--;
            }

            activeSlot = var13;
            activePriority = var7;
            lastError = "";
         } catch (Throwable var12) {
            logErrorOnce(var12);
            release(var0);
            resetActive();
         }
      } else {
         release(var0);
         resetActive();
      }
   }

   public static void forceRelease() {
      release(getMinecraftClient());
      resetActive();
   }

   private static void resetActive() {
      activeSlot = -1;
      activePriority = 0;
      repeatCooldown = 0;
   }

   private static void triggerItemUse(Object var0) throws Exception {
      if (var0 != null) {
         Method var1;
         try {
            var1 = var0.getClass().getDeclaredMethod("method_1583");
         } catch (NoSuchMethodException var6) {
            Class var3 = var0.getClass();
            var1 = null;

            while (var3 != null && var1 == null) {
               try {
                  var1 = var3.getDeclaredMethod("method_1583");
               } catch (NoSuchMethodException var5) {
                  var3 = var3.getSuperclass();
               }
            }

            if (var1 == null) {
               throw var6;
            }
         }

         var1.setAccessible(true);
         var1.invoke(var0);
      }
   }

   private static int priorityOfStack(Object var0) {
      try {
         if (var0 == null) {
            return 0;
         }

         Object var1 = invokeNoArg(var0, "method_7960");
         if (var1 instanceof Boolean && (Boolean)var1) {
            return 0;
         }

         Object var2 = invokeNoArg(var0, "method_7909");
         if (var2 == null) {
            return 0;
         }

         ensureItems();
         if (var2 == ominousBottle || "net.minecraft.class_9462".equals(var2.getClass().getName())) {
            return HumanizedSellRuntime.priorityForOminous();
         }

         if (var2 == fireworkRocket) {
            return HumanizedSellRuntime.priorityForFirework();
         }

         if (swords != null) {
            for (Object var6 : swords) {
               if (var2 == var6) {
                  return HumanizedSellRuntime.priorityForSword();
               }
            }
         }
      } catch (Throwable var7) {
      }

      return 0;
   }

   private static boolean isSeaBusy() {
      try {
         Class var0 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
         Object var1 = getStaticField(var0, "currentState");
         if (var1 != null && !"IDLE".equals(String.valueOf(var1))) {
            return true;
         }

         Object var2 = getStaticField(var0, "antiState");
         if (var2 != null && !"IDLE".equals(String.valueOf(var2))) {
            return true;
         }

         if (getStaticBoolean(var0, "isPaying")) {
            return true;
         }

         if (getStaticBoolean(var0, "isGhostFixing")) {
            return true;
         }

         if (getStaticBoolean(var0, "isHomeRecovering")) {
            return true;
         }
      } catch (Throwable var3) {
      }

      return false;
   }

   private static boolean isExternalSafetyBusy() {
      return invokeStaticBoolean("dev.autoominous.BountyRuntime", "isBlockingSell")
         || invokeStaticBoolean("dev.autoominous.HomePositionGuardRuntime", "isAutoUseBlocked");
   }

   private static boolean isEnabled() {
      try {
         Class var0 = Class.forName("dev.autoominous.SlashSellSettings");
         Method var1 = var0.getMethod("isAutoUseProtectedEnabled");
         Object var2 = var1.invoke(null);
         return var2 instanceof Boolean && (Boolean)var2;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void persistToggleIfChanged(boolean var0) {
      if (!toggleKnown) {
         toggleKnown = true;
         lastToggle = var0;
      } else if (var0 != lastToggle) {
         lastToggle = var0;

         try {
            Class var1 = Class.forName("dev.autoominous.SlashSellSettings");
            var1.getMethod("saveNow").invoke(null);
         } catch (Throwable var2) {
         }
      }
   }

   private static void ensureItems() throws Exception {
      if (itemsClass == null) {
         itemsClass = Class.forName("net.minecraft.class_1802");
         ominousBottle = getStaticField(itemsClass, "field_50140");
         fireworkRocket = getStaticField(itemsClass, "field_8639");
         swords = new Object[]{
            getStaticField(itemsClass, "field_8091"),
            getStaticField(itemsClass, "field_61338"),
            getStaticField(itemsClass, "field_8528"),
            getStaticField(itemsClass, "field_8845"),
            getStaticField(itemsClass, "field_8371"),
            getStaticField(itemsClass, "field_8802"),
            getStaticField(itemsClass, "field_22022")
         };
      }
   }

   private static Object getMinecraftClient() {
      try {
         if (mcClass == null) {
            mcClass = Class.forName("net.minecraft.class_310");
         }

         return mcClass.getMethod("method_1551").invoke(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static void release(Object var0) {
      if (pressedByUs) {
         try {
            if (var0 == null) {
               var0 = getMinecraftClient();
            }

            if (var0 != null) {
               setUsePressed(var0, false);
            }
         } catch (Throwable var5) {
         } finally {
            pressedByUs = false;
         }
      }
   }

   private static void setUsePressed(Object var0, boolean var1) throws Exception {
      Object var2 = getField(var0, "field_1690");
      if (var2 != null) {
         Object var3 = getField(var2, "field_1904");
         if (var3 != null) {
            Method var4 = var3.getClass().getMethod("method_23481", boolean.class);
            var4.invoke(var3, var1);
         }
      }
   }

   private static Object getField(Object var0, String var1) throws Exception {
      if (var0 == null) {
         return null;
      } else {
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
   }

   private static Object getStaticField(Class<?> var0, String var1) throws Exception {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(null);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static boolean getStaticBoolean(Class<?> var0, String var1) {
      try {
         Object var2 = getStaticField(var0, var1);
         return var2 instanceof Boolean && (Boolean)var2;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static Object invokeNoArg(Object var0, String var1) throws Exception {
      Method var2 = var0.getClass().getMethod(var1);
      return var2.invoke(var0);
   }

   private static int invokeIntNoArg(Object var0, String var1, int var2) {
      try {
         Object var3 = invokeNoArg(var0, var1);
         return var3 instanceof Number ? ((Number)var3).intValue() : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static boolean invokeBooleanNoArg(Object var0, String var1, boolean var2) {
      try {
         Object var3 = invokeNoArg(var0, var1);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static Object invokeOneInt(Object var0, String var1, int var2) throws Exception {
      Method var3 = var0.getClass().getMethod(var1, int.class);
      return var3.invoke(var0, var2);
   }

   private static void invokeOneIntVoid(Object var0, String var1, int var2) throws Exception {
      Method var3 = var0.getClass().getMethod(var1, int.class);
      var3.invoke(var0, var2);
   }

   private static boolean invokeStaticBoolean(String var0, String var1) {
      try {
         Class var2 = Class.forName(var0);
         Object var3 = var2.getMethod(var1).invoke(null);
         return var3 instanceof Boolean && (Boolean)var3;
      } catch (Throwable var4) {
         return false;
      }
   }

   private static void logErrorOnce(Throwable var0) {
      String var1 = var0.getClass().getName() + ": " + var0.getMessage();
      if (!var1.equals(lastError)) {
         lastError = var1;
         System.err.println("[DonutUtility][AutoUse] " + var1);
      }
   }

   public static void requestRearm() {
      activeSlot = -1;
      activePriority = 0;
      repeatCooldown = 0;
      pressedByUs = false;
   }
}
