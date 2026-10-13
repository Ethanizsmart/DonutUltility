package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class ReflectionSupport {
   private ReflectionSupport() {
   }

   static Object mc() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getMethod("method_1551");
         return var1.invoke(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   static Field fieldInHierarchy(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   static Object field(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         return fieldInHierarchy(var0.getClass(), var1).get(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   static Object staticField(String var0, String var1) {
      try {
         Class var2 = Class.forName(var0);
         return fieldInHierarchy(var2, var1).get(null);
      } catch (Throwable var3) {
         return null;
      }
   }

   static boolean staticBoolean(String var0, String var1, boolean var2) {
      Object var3 = staticField(var0, var1);
      return var3 instanceof Boolean ? (Boolean)var3 : var2;
   }

   static long staticLong(String var0, String var1, long var2) {
      Object var4 = staticField(var0, var1);
      return var4 instanceof Number ? ((Number)var4).longValue() : var2;
   }

   static double staticDouble(String var0, String var1, double var2) {
      Object var4 = staticField(var0, var1);
      return var4 instanceof Number ? ((Number)var4).doubleValue() : var2;
   }

   static void setStatic(String var0, String var1, Object var2) {
      try {
         Class var3 = Class.forName(var0);
         fieldInHierarchy(var3, var1).set(null, var2);
      } catch (Throwable var4) {
      }
   }

   static Object callStatic(String var0, String var1, Class<?>[] var2, Object... var3) {
      try {
         Class var4 = Class.forName(var0);
         Method var5 = var4.getDeclaredMethod(var1, var2);
         var5.setAccessible(true);
         return var5.invoke(null, var3);
      } catch (Throwable var6) {
         return null;
      }
   }

   static void callStaticVoid(String var0, String var1) {
      callStatic(var0, var1, new Class[0]);
   }

   static boolean callStaticBoolean(String var0, String var1, boolean var2) {
      Object var3 = callStatic(var0, var1, new Class[0]);
      return var3 instanceof Boolean ? (Boolean)var3 : var2;
   }

   static Object callNoArg(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      Class var2 = var0.getClass();

      while (var2 != null) {
         try {
            Method var3 = var2.getDeclaredMethod(var1);
            var3.setAccessible(true);
            return var3.invoke(var0);
         } catch (NoSuchMethodException var4) {
            var2 = var2.getSuperclass();
         } catch (Throwable var5) {
            return null;
         }
      }

      return null;
   }

   static boolean callBool(Object var0, String var1, boolean var2) {
      Object var3 = callNoArg(var0, var1);
      return var3 instanceof Boolean ? (Boolean)var3 : var2;
   }

   static double[] pos(Object var0) {
      if (var0 == null) {
         return null;
      }

      try {
         Object var1 = callNoArg(var0, "method_23317");
         Object var2 = callNoArg(var0, "method_23318");
         Object var3 = callNoArg(var0, "method_23321");
         if (var1 instanceof Number && var2 instanceof Number && var3 instanceof Number) {
            return new double[]{((Number)var1).doubleValue(), ((Number)var2).doubleValue(), ((Number)var3).doubleValue()};
         }
      } catch (Throwable var6) {
      }

      try {
         Object var7 = callNoArg(var0, "method_73189");
         if (var7 != null) {
            Object var8 = field(var7, "field_1352");
            Object var9 = field(var7, "field_1351");
            Object var4 = field(var7, "field_1350");
            if (var8 instanceof Number && var9 instanceof Number && var4 instanceof Number) {
               return new double[]{((Number)var8).doubleValue(), ((Number)var9).doubleValue(), ((Number)var4).doubleValue()};
            }
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   static String worldKey(Object var0) {
      if (var0 == null) {
         return "";
      }

      for (String var4 : new String[]{"method_27983", "getRegistryKey"}) {
         try {
            Object var5 = callNoArg(var0, var4);
            if (var5 != null) {
               return String.valueOf(var5);
            }
         } catch (Throwable var6) {
         }
      }

      return var0.getClass().getName();
   }
}
