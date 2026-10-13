package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class AhConfirmRaceGuard {
   private static final long MAX_PRICE = 1000000L;
   private static boolean blocked;
   private static long keyPrice = -1L;
   private static int keySlot = -1;
   private static long lastObservedPrice = -1L;
   private static String reason = "";

   private AhConfirmRaceGuard() {
   }

   public static String observe(Object var0, String var1) {
      return var1;
   }

   public static void requirePreClickValid(Object var0, int var1) {
      if (!preClickValidate(var0, var1)) {
         throw new IllegalStateException("AH pre-click race blocked: " + currentReason());
      }
   }

   public static boolean preClickValidate(Object var0, int var1) {
      long var2 = staticLong(DeathSwordAhRecoveryRuntime.class, "expectedPrice", -1L);
      int var4 = staticInt(DeathSwordAhRecoveryRuntime.class, "expectedListingSlot", -1);
      keyPrice = var2;
      keySlot = var4;
      blocked = false;
      reason = "";
      lastObservedPrice = -1L;

      try {
         if (var2 < 0L || var2 > 1000000L) {
            return block("invalid expected price", var2, -1L, var1);
         }

         if (var4 >= 0 && var1 == var4) {
            Object var5 = stackAtSlot(var0, var1);
            if (var5 == null) {
               return block("selected listing missing", var2, -1L, var1);
            }

            String var6 = scannerTooltip(var0, var5);
            if (!scannerAccepts(var0, var5, var6)) {
               return block("selected listing no longer Diamond Sword + Looting III", var2, -1L, var1);
            }

            long var7 = scannerPrice(var6);
            lastObservedPrice = var7;
            if (var7 < 0L) {
               return block("selected listing price unreadable", var2, var7, var1);
            }

            if (var7 > 1000000L) {
               return block("selected listing exceeded $1M", var2, var7, var1);
            }

            if (var7 != var2) {
               return block("selected listing price changed", var2, var7, var1);
            }

            System.out.println("[AH Race Guard] PRECLICK PASS slot=" + var1 + " expected=$" + var2 + " actual=$" + var7 + " item=DiamondSword looting3=true");
            return true;
         } else {
            return block("slot mismatch", var2, -1L, var1);
         }
      } catch (Throwable var9) {
         return block("pre-click validation exception: " + var9.getClass().getSimpleName(), var2, lastObservedPrice, var1);
      }
   }

   public static boolean blockedForCurrentExpected() {
      return blocked;
   }

   public static String currentReason() {
      return reason == null ? "" : reason;
   }

   private static boolean block(String var0, long var1, long var3, int var5) {
      blocked = true;
      reason = var0;
      lastObservedPrice = var3;
      System.out.println("[AH Race Guard] PRECLICK BLOCK slot=" + var5 + " expected=$" + var1 + " actual=$" + var3 + " reason=" + var0);
      return false;
   }

   private static Object stackAtSlot(Object var0, int var1) throws Exception {
      Object var2 = fieldDeep(var0, "field_1755");
      if (var2 == null) {
         return null;
      }

      Object var3 = invoke0(var2, "method_17577");
      if (var3 == null) {
         return null;
      }

      Object var4 = invoke1Int(var3, "method_7611", var1);
      if (var4 == null) {
         return null;
      }

      Object var5 = invoke0(var4, "method_7677");
      if (var5 == null) {
         return null;
      }

      Object var6 = invoke0(var5, "method_7960");
      return Boolean.TRUE.equals(var6) ? null : var5;
   }

   private static String scannerTooltip(Object var0, Object var1) throws Exception {
      Method var2 = privateStatic(AhSwordPurchaseFix.class, "tooltipText", Object.class, Object.class);
      Object var3 = var2.invoke(null, var0, var1);
      return var3 == null ? "" : String.valueOf(var3);
   }

   private static boolean scannerAccepts(Object var0, Object var1, String var2) throws Exception {
      Method var3 = privateStatic(AhSwordPurchaseFix.class, "isLootingDiamondSword", Object.class, Object.class, String.class);
      Object var4 = var3.invoke(null, var0, var1, var2.toLowerCase(Locale.ROOT));
      return Boolean.TRUE.equals(var4);
   }

   private static long scannerPrice(String var0) throws Exception {
      Method var1 = privateStatic(AhSwordPurchaseFix.class, "parsePrice", String.class);
      return var1.invoke(null, var0) instanceof Number var3 ? var3.longValue() : -1L;
   }

   private static Method privateStatic(Class<?> var0, String var1, Class<?>... var2) throws Exception {
      Method var3 = var0.getDeclaredMethod(var1, var2);
      var3.setAccessible(true);
      return var3;
   }

   private static Object invoke0(Object var0, String var1) throws Exception {
      if (var0 == null) {
         return null;
      }

      Method var2 = findMethod(var0.getClass(), var1, 0);
      if (var2 == null) {
         return null;
      }

      var2.setAccessible(true);
      return var2.invoke(var0);
   }

   private static Object invoke1Int(Object var0, String var1, int var2) throws Exception {
      if (var0 == null) {
         return null;
      }

      Method var3 = findMethod(var0.getClass(), var1, 1);
      if (var3 == null) {
         return null;
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

   private static int staticInt(Class<?> var0, String var1, int var2) {
      try {
         Field var3 = var0.getDeclaredField(var1);
         var3.setAccessible(true);
         return var3.getInt(null);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static long staticLong(Class<?> var0, String var1, long var2) {
      try {
         Field var4 = var0.getDeclaredField(var1);
         var4.setAccessible(true);
         return var4.getLong(null);
      } catch (Throwable var5) {
         return var2;
      }
   }
}
