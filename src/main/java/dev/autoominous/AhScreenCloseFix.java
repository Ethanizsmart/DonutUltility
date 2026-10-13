package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AhScreenCloseFix {
   private AhScreenCloseFix() {
   }

   public static void close(Object var0) {
      if (var0 != null) {
         boolean var1 = false;

         try {
            Object var2 = field(var0, "field_1724");
            if (var2 != null) {
               Method var3 = method(var2.getClass(), "method_7346", 0);
               if (var3 != null) {
                  var3.setAccessible(true);
                  var3.invoke(var2);
                  var1 = true;
               }
            }
         } catch (Throwable var5) {
         }

         if (!var1) {
            try {
               Method var6 = method(var0.getClass(), "method_1507", 1);
               if (var6 != null) {
                  var6.setAccessible(true);
                  var6.invoke(var0, new Object[]{null});
               }
            } catch (Throwable var4) {
            }
         }

         System.out.println("[AH-DIAG] SCREEN_CLOSE synced=" + var1);
      }
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
}
