package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ClientStatusMessage {
   private ClientStatusMessage() {
   }

   public static void actionbarState(String var0, boolean var1) {
      actionbar(var0 + ": " + (var1 ? "§aON" : "§cOFF") + "§r");
   }

   public static void actionbar(String var0) {
      try {
         Class var1 = Class.forName("net.minecraft.class_310");
         Object var2 = var1.getMethod("method_1551").invoke(null);
         Object var3 = field(var2, "field_1724");
         if (var3 == null) {
            return;
         }

         Class var4 = Class.forName("net.minecraft.class_2561");
         Method var5 = var4.getMethod("method_43470", String.class);
         Object var6 = var5.invoke(null, var0);
         Method var7 = findMethod(var3.getClass(), "method_7353", 2);
         if (var7 != null) {
            var7.invoke(var3, var6, Boolean.TRUE);
         }
      } catch (Throwable var8) {
      }
   }

   private static Object field(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
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

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               var7.setAccessible(true);
               return var7;
            }
         }
      }

      return null;
   }
}
