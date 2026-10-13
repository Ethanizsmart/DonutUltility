package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AhPriceComponentReader {
   private static final String DATA_COMPONENT_TYPES = "net.minecraft.class_9334";
   private static final String LORE_FIELD = "field_49632";
   private static final String CUSTOM_DATA_FIELD = "field_49628";
   private static final String CUSTOM_NAME_FIELD = "field_49631";
   private static final String ITEM_NAME_FIELD = "field_50239";
   private static final String GET_COMPONENT = "method_58694";
   private static final String GET_COMPONENTS = "method_57353";
   private static final String TEXT_GET_STRING = "method_10851";
   private static final Pattern LITERAL = Pattern.compile("literal\\{([^{}]*)\\}");
   private static final Pattern PRICE = Pattern.compile("(?i)\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt]?)");

   private AhPriceComponentReader() {
   }

   public static void append(StringBuilder var0, Object var1) {
      if (var0 != null && var1 != null) {
         try {
            String var2 = read(var1);
            if (!var2.isEmpty()) {
               if (var0.length() > 0) {
                  var0.append('\n');
               }

               var0.append(var2);
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static String read(Object var0) {
      if (var0 == null) {
         return "";
      }

      StringBuilder var1 = new StringBuilder();
      Object var2 = component(var0, "field_49632");
      if (var2 != null) {
         String var3 = loreText(var2);
         appendDistinct(var1, var3);
         appendDistinct(var1, normalizedPriceTextFromSerialized(String.valueOf(var2)));
      }

      appendTextComponent(var1, component(var0, "field_49631"));
      appendTextComponent(var1, component(var0, "field_50239"));
      Object var6 = component(var0, "field_49628");
      if (var6 != null) {
         String var4 = String.valueOf(var6);
         appendDistinct(var1, normalizedPriceTextFromSerialized(var4));
         appendDistinct(var1, var4);
      }

      Object var7 = invokeNoArg(var0, "method_57353");
      if (var7 != null) {
         String var5 = String.valueOf(var7);
         appendDistinct(var1, normalizedPriceTextFromSerialized(var5));
         appendDistinct(var1, var5);
      }

      return var1.toString();
   }

   public static String sourceFor(Object var0) {
      if (var0 == null) {
         return "NONE";
      }

      try {
         Object var1 = component(var0, "field_49632");
         if (var1 != null) {
            String var2 = loreText(var1) + "\n" + normalizedPriceTextFromSerialized(String.valueOf(var1));
            if (PRICE.matcher(var2).find()) {
               return "LORE_COMPONENT";
            }
         }

         Object var6 = component(var0, "field_49628");
         if (var6 != null) {
            String var3 = String.valueOf(var6);
            if (PRICE.matcher(normalizedPriceTextFromSerialized(var3)).find()) {
               return "CUSTOM_DATA_COMPONENT";
            }
         }

         Object var7 = invokeNoArg(var0, "method_57353");
         if (var7 != null) {
            String var4 = String.valueOf(var7);
            if (PRICE.matcher(normalizedPriceTextFromSerialized(var4)).find()) {
               return "COMPONENT_MAP_TEXT";
            }
         }
      } catch (Throwable var5) {
      }

      return "NONE";
   }

   public static String normalizedPriceTextFromSerialized(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         StringBuilder var1 = new StringBuilder();
         Matcher var2 = LITERAL.matcher(var0);

         while (var2.find()) {
            String var3 = var2.group(1);
            if (var3 != null) {
               var3 = var3.replace("\\n", " ").replace("\\r", " ").trim();
               if (!var3.isEmpty()) {
                  if (var1.length() > 0) {
                     var1.append(' ');
                  }

                  var1.append(var3);
               }
            }
         }

         String var8 = var1.length() > 0 ? var1.toString() : var0;
         Matcher var4 = PRICE.matcher(var8);
         StringBuilder var5 = new StringBuilder();

         while (var4.find()) {
            if (var5.length() > 0) {
               var5.append('\n');
            }

            var5.append("$ ").append(var4.group(1));
            String var6 = var4.group(2);
            if (var6 != null && !var6.isEmpty()) {
               var5.append(var6);
            }
         }

         return var5.toString();
      } else {
         return "";
      }
   }

   private static Object component(Object var0, String var1) {
      try {
         Class var2 = Class.forName("net.minecraft.class_9334");
         Field var3 = var2.getDeclaredField(var1);
         var3.setAccessible(true);
         Object var4 = var3.get(null);
         if (var4 == null) {
            return null;
         }

         Method var5 = findMethod(var0.getClass(), "method_58694", 1);
         if (var5 == null) {
            return null;
         }

         var5.setAccessible(true);
         return var5.invoke(var0, var4);
      } catch (Throwable var6) {
         return null;
      }
   }

   private static String loreText(Object var0) {
      StringBuilder var1 = new StringBuilder();

      try {
         if (var0.getClass().isRecord()) {
            RecordComponent[] var2 = var0.getClass().getRecordComponents();
            if (var2 != null && var2.length > 0) {
               Object var3 = var2[0].getAccessor().invoke(var0);
               appendIterableText(var1, var3);
               if (var1.length() > 0) {
                  return var1.toString();
               }
            }
         }
      } catch (Throwable var5) {
      }

      try {
         Method var7 = findMethod(var0.getClass(), "comp_2400", 0);
         if (var7 != null) {
            var7.setAccessible(true);
            appendIterableText(var1, var7.invoke(var0));
            if (var1.length() > 0) {
               return var1.toString();
            }
         }
      } catch (Throwable var4) {
      }

      try {
         for (Field var10 : allFields(var0.getClass())) {
            if (!Modifier.isStatic(var10.getModifiers()) && List.class.isAssignableFrom(var10.getType())) {
               var10.setAccessible(true);
               appendIterableText(var1, var10.get(var0));
               if (var1.length() > 0) {
                  return var1.toString();
               }
            }
         }
      } catch (Throwable var6) {
      }

      String var9 = String.valueOf(var0);
      String var11 = normalizedPriceTextFromSerialized(var9);
      return var11.isEmpty() ? var9 : var11;
   }

   private static void appendIterableText(StringBuilder var0, Object var1) {
      if (var1 instanceof Iterable) {
         for (Object var4 : (Iterable)var1) {
            appendDistinct(var0, text(var4));
         }
      }
   }

   private static void appendTextComponent(StringBuilder var0, Object var1) {
      if (var1 != null) {
         appendDistinct(var0, text(var1));
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
      } catch (Throwable var5) {
      }

      String var6 = String.valueOf(var0);
      Matcher var7 = LITERAL.matcher(var6);
      StringBuilder var3 = new StringBuilder();

      while (var7.find()) {
         String var4 = var7.group(1).trim();
         if (!var4.isEmpty()) {
            if (var3.length() > 0) {
               var3.append(' ');
            }

            var3.append(var4);
         }
      }

      return var3.length() > 0 ? var3.toString() : var6;
   }

   private static void appendDistinct(StringBuilder var0, String var1) {
      if (var1 != null) {
         var1 = var1.trim();
         if (!var1.isEmpty()) {
            if (var0.indexOf(var1) < 0) {
               if (var0.length() > 0) {
                  var0.append('\n');
               }

               var0.append(var1);
            }
         }
      }
   }

   private static Object invokeNoArg(Object var0, String var1) {
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

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      if (var0 == null) {
         return null;
      }

      try {
         for (Method var6 : var0.getMethods()) {
            if (var6.getName().equals(var1) && var6.getParameterCount() == var2) {
               return var6;
            }
         }
      } catch (Throwable var9) {
      }

      for (Class var10 = var0; var10 != null; var10 = var10.getSuperclass()) {
         try {
            for (Method var7 : var10.getDeclaredMethods()) {
               if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
                  return var7;
               }
            }
         } catch (Throwable var8) {
         }
      }

      return null;
   }

   private static List<Field> allFields(Class<?> var0) {
      ArrayList var1 = new ArrayList();

      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Collections.addAll(var1, var2.getDeclaredFields());
         } catch (Throwable var4) {
         }
      }

      return var1;
   }
}
