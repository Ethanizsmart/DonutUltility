package dev.autoominous;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AhConfirmYesTargetFix {
   private static final Pattern LITERAL = Pattern.compile("(?i)literal\\{([^}]*)}");

   private AhConfirmYesTargetFix() {
   }

   public static boolean pressYes(Object var0) {
      try {
         if (var0 != null && isDialogScreen(var0)) {
            String var1 = norm(screenContextText(var0));
            boolean var2 = var1.contains("are you sure you want to buy this") || var1.contains("confirm purchase") || var1.contains("confirm your purchase");
            if (!var2) {
               return fail("wrong-confirm-context text=\"" + compact(var1) + "\"");
            }

            Object var3 = fieldDeep(var0, "field_60761");
            if (var3 == null) {
               return fail("dialog-unavailable");
            }

            Object var4 = null;
            String var5 = "";
            if (invoke0(var3, "comp_4317") instanceof List var7) {
               ArrayList var8 = new ArrayList();
               ArrayList var9 = new ArrayList();

               for (Object var11 : var7) {
                  if (var11 != null) {
                     String var12 = actionButtonLabel(var11);
                     String var13 = canonicalLabel(var12);
                     var9.add(var13);
                     if (var13.equals("yes")) {
                        var8.add(var11);
                     }
                  }
               }

               if (var8.size() != 1) {
                  return fail("no-unambiguous-exact-yes dialog=" + var3.getClass().getName() + " labels=" + var9);
               }

               var4 = var8.get(0);
               var5 = "multi-action-exact-yes";
            }

            if (var4 == null) {
               Object var19 = invoke0(var3, "comp_4306");
               if (var19 != null) {
                  String var21 = canonicalLabel(actionButtonLabel(var19));
                  if (!var21.equals("yes")) {
                     return fail("confirmation-yesButton-label=\"" + compact(var21) + "\"");
                  }

                  var4 = var19;
                  var5 = "confirmation-exact-yesButton";
               }
            }

            if (var4 == null) {
               return fail("no-yes-action dialog=" + var3.getClass().getName());
            }

            String var20 = actionButtonLabel(var4);
            String var22 = canonicalLabel(var20);
            if (!var22.equals("yes")) {
               return fail("selected-action-not-yes label=\"" + compact(var20) + "\"");
            }

            if (invoke0(var4, "comp_4391") instanceof Optional var24 && !var24.isEmpty()) {
               Object var25 = newDialogControls(var0);
               if (var25 == null) {
                  return fail("dialog-controls-unavailable");
               }

               Method var26 = findCompatibleMethod(var25.getClass(), "method_72139", 1, Optional.class);
               if (var26 == null) {
                  return fail("create-click-event-unavailable");
               }

               var26.setAccessible(true);
               if (var26.invoke(var25, var24) instanceof Supplier var14) {
                  Object var15 = var14.get();
                  if (var15 instanceof Optional var16 && !var16.isEmpty()) {
                     Method var17 = findCompatibleMethod(var0.getClass(), "method_72141", 1, Optional.class);
                     if (var17 == null) {
                        return fail("run-action-unavailable");
                     }

                     var17.setAccessible(true);
                     var17.invoke(var0, var15);
                     diag(
                        true,
                        "direct-dialog-action source=" + var5 + " dialog=" + var3.getClass().getName() + " label=\"" + compact(var20) + "\" canonical=yes"
                     );
                     return true;
                  } else {
                     return fail("click-event-unavailable");
                  }
               } else {
                  return fail("click-event-supplier-unavailable");
               }
            } else {
               return fail("yes-action-unavailable label=\"" + compact(var20) + "\"");
            }
         } else {
            return fail("not-dialog-screen");
         }
      } catch (Throwable var18) {
         System.err.println("[AH Confirm Target Fix] " + var18);
         return fail("exception=" + var18.getClass().getSimpleName() + ":" + compact(String.valueOf(var18.getMessage())));
      }
   }

   private static boolean fail(String var0) {
      diag(false, var0);
      return false;
   }

   private static String canonicalLabel(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.trim();
      Matcher var2 = LITERAL.matcher(var1);
      if (var2.find()) {
         var1 = var2.group(1);
      }

      var1 = var1.replaceAll("(?i)^text\\{", "").replaceAll("}$", "");
      return var1.replaceAll("§.", "").replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
   }

   private static String actionButtonLabel(Object var0) {
      try {
         if (var0 == null) {
            return "";
         }

         Object var1 = invoke0(var0, "comp_4390");
         if (var1 == null) {
            return "";
         }

         Object var2 = invoke0(var1, "comp_4298");
         return text(var2).trim();
      } catch (Throwable var3) {
         return "";
      }
   }

   private static Object newDialogControls(Object var0) {
      try {
         Class var1 = Class.forName("net.minecraft.class_11529");

         for (Constructor var5 : var1.getDeclaredConstructors()) {
            Class[] var6 = var5.getParameterTypes();
            if (var6.length == 1 && var6[0].isAssignableFrom(var0.getClass())) {
               var5.setAccessible(true);
               return var5.newInstance(var0);
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static String screenContextText(Object var0) {
      StringBuilder var1 = new StringBuilder();
      append(var1, text(invoke0(var0, "method_25440")));
      if (invoke0(var0, "method_25396") instanceof Iterable var3) {
         int var4 = 0;

         for (Object var6 : var3) {
            if (++var4 > 96) {
               break;
            }

            append(var1, widgetText(var6));
            append(var1, String.valueOf(var6));
         }
      }

      return var1.toString();
   }

   private static boolean isDialogScreen(Object var0) {
      for (Class var1 = var0.getClass(); var1 != null; var1 = var1.getSuperclass()) {
         if ("net.minecraft.class_11473".equals(var1.getName())) {
            return true;
         }
      }

      return false;
   }

   private static String widgetText(Object var0) {
      String var1 = text(invoke0(var0, "method_25369")).trim();
      return !var1.isBlank() ? var1 : text(fieldDeep(var0, "field_22754")).trim();
   }

   private static void append(StringBuilder var0, String var1) {
      if (var1 != null && !(var1 = var1.trim()).isEmpty()) {
         if (var0.length() > 0) {
            var0.append(' ');
         }

         var0.append(var1);
      }
   }

   private static String norm(String var0) {
      return var0 == null ? "" : var0.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
   }

   private static String compact(String var0) {
      if (var0 == null) {
         return "";
      }

      var0 = var0.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
      return var0.length() <= 220 ? var0 : var0.substring(0, 220);
   }

   private static Object invoke0(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

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
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }

   private static Method findCompatibleMethod(Class<?> var0, String var1, int var2, Class<?> var3) {
      for (Class var4 = var0; var4 != null; var4 = var4.getSuperclass()) {
         for (Method var8 : var4.getDeclaredMethods()) {
            if (var8.getName().equals(var1) && var8.getParameterCount() == var2) {
               Class[] var9 = var8.getParameterTypes();
               if (var2 == 1 && (var9[0].isAssignableFrom(var3) || var3.isAssignableFrom(var9[0]) || var9[0] == var3)) {
                  return var8;
               }
            }
         }
      }

      return null;
   }

   private static Object fieldDeep(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      Class var2 = var0.getClass();

      while (var2 != null) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
            var2 = var2.getSuperclass();
         } catch (Throwable var5) {
            return null;
         }
      }

      return null;
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
      } catch (Throwable var3) {
      }

      return String.valueOf(var0);
   }

   private static void diag(boolean var0, String var1) {
      try {
         Class var2 = Class.forName("dev.autoominous.AhDiagnosticRuntime");
         Method var3 = var2.getDeclaredMethod("confirmPress", boolean.class);
         var3.setAccessible(true);
         var3.invoke(null, var0);
      } catch (Throwable var4) {
      }

      System.out.println("[AH Confirm Target Fix] result=" + var0 + " " + var1);
   }
}
