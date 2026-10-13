package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class HumanBehaviorProfile {
   private static final String FALLBACK_KEY = System.getProperty("user.dir", "") + "|" + System.getProperty("user.name", "");
   private static final double SESSION_MOOD = ThreadLocalRandom.current().nextDouble(-0.012, 0.012);
   private static volatile String accountKey;
   private static volatile long nextResolveAtMs;
   private static volatile long lastTpaSampleAt;
   private static volatile int lastTpaSample;
   private static volatile long lastReconnectSampleAt;
   private static volatile long lastReconnectSample;
   private static volatile long lastDeathKey = Long.MIN_VALUE;
   private static volatile int lastRespawnDelay;
   private static volatile long lastWorldReadyKey = Long.MIN_VALUE;
   private static volatile int lastHomeDelay;

   private HumanBehaviorProfile() {
   }

   public static long bountyIntervalMs(double var0) {
      double var2 = Math.max(0.0, var0) * 60000.0;
      double var4 = 1.0 + trait(1) * 0.03 + SESSION_MOOD + ThreadLocalRandom.current().nextDouble(-0.03, 0.03);
      var4 = clamp(var4, 0.93, 1.07);
      return Math.max(0L, Math.round(var2 * var4));
   }

   public static long bountyActionDelayMs(double var0) {
      double var2 = Math.max(0.0, var0) * 1000.0;
      double var4 = 1.0 + trait(2) * 0.08 + SESSION_MOOD + ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
      var4 = clamp(var4, 0.78, 1.22);
      return Math.max(0L, Math.round(var2 * var4));
   }

   public static int tpaDelayMs(int var0) {
      int var1 = Math.max(0, var0);
      long var2 = System.currentTimeMillis();
      if (var2 - lastTpaSampleAt < 250L && lastTpaSample >= 0) {
         return lastTpaSample;
      }

      double var4 = 1.0 + trait(3) * 0.07 + SESSION_MOOD + ThreadLocalRandom.current().nextDouble(-0.07, 0.07);
      int var6 = (int)Math.round(var1 * clamp(var4, 0.84, 1.16));
      if (var1 > 0) {
         var6 = Math.max(80, var6);
      }

      lastTpaSample = var6;
      lastTpaSampleAt = var2;
      return var6;
   }

   public static long reconnectDelayMs(double var0) {
      long var2 = System.currentTimeMillis();
      if (var2 - lastReconnectSampleAt < 750L && lastReconnectSample > 0L) {
         return lastReconnectSample;
      }

      double var4 = Math.max(0.5, var0) * 1000.0;
      double var6 = 1.0 + trait(4) * 0.08 + SESSION_MOOD + ThreadLocalRandom.current().nextDouble(-0.09, 0.09);
      long var8 = Math.max(500L, Math.round(var4 * clamp(var6, 0.82, 1.18)));
      lastReconnectSample = var8;
      lastReconnectSampleAt = var2;
      return var8;
   }

   public static int recoveryRespawnDelayMs(int var0) {
      int var1 = Math.max(0, var0);
      long var2 = readStaticLong("dev.autoominous.DeathRecoveryRuntime", "deathDetectedAtMs", 0L);
      if (var2 != lastDeathKey || lastRespawnDelay <= 0) {
         lastDeathKey = var2;
         lastRespawnDelay = eventDelay(var1, var2, 5, 0.075, 0.105);
      }

      return lastRespawnDelay;
   }

   public static int recoveryHomeDelayMs(int var0) {
      int var1 = Math.max(0, var0);
      long var2 = readStaticLong("dev.autoominous.DeathRecoveryRuntime", "worldReadyAtMs", 0L);
      if (var2 != lastWorldReadyKey || lastHomeDelay <= 0) {
         lastWorldReadyKey = var2;
         lastHomeDelay = eventDelay(var1, var2, 6, 0.075, 0.11);
      }

      return lastHomeDelay;
   }

   public static int homeGuardRandom(int var0, int var1) {
      return skewedRange(var0, var1, trait(7), 0.25);
   }

   public static int recoveryRandom(int var0, int var1) {
      return skewedRange(var0, var1, trait(8), 0.22);
   }

   public static int sellRandom(int var0, int var1) {
      return skewedRange(var0, var1, trait(9), 0.17);
   }

   public static int sellCycleDelayTicks(int var0, int var1) {
      if (var0 <= 1) {
         return Math.max(1, var0);
      }

      double var2 = trait(10) * 0.04;
      double var4 = trait(20 + Math.max(0, var1)) * 0.025;
      double var6 = clamp(1.0 + var2 + var4 + SESSION_MOOD, 0.92, 1.08);
      return Math.max(1, (int)Math.round(var0 * var6));
   }

   private static int eventDelay(int var0, long var1, int var3, double var4, double var6) {
      if (var0 <= 0) {
         return 0;
      }

      double var8 = unitSigned(mix64(seed() ^ var1 ^ -7046029254386353131L * var3));
      double var10 = 1.0 + trait(var3) * var4 + SESSION_MOOD + var8 * var6;
      var10 = clamp(var10, 0.8, 1.2);
      return Math.max(100, (int)Math.round(var0 * var10));
   }

   private static int skewedRange(int var0, int var1, double var2, double var4) {
      if (var1 < var0) {
         int var6 = var0;
         var0 = var1;
         var1 = var6;
      }

      if (var1 <= var0) {
         return var0;
      }

      double var14 = ThreadLocalRandom.current().nextDouble();
      double var8 = Math.exp(-var2 * var4);
      double var10 = Math.pow(var14, var8);
      int var12 = var1 - var0 + 1;
      int var13 = var0 + Math.min(var12 - 1, (int)Math.floor(var10 * var12));
      return Math.max(var0, Math.min(var1, var13));
   }

   private static double trait(int var0) {
      return unitSigned(mix64(seed() ^ -3335678366873096957L * (var0 + 1L)));
   }

   private static long seed() {
      return mix64(fnv1a64(resolveAccountKey()));
   }

   private static String resolveAccountKey() {
      String var0 = accountKey;
      if (var0 != null) {
         return var0;
      }

      long var1 = System.currentTimeMillis();
      if (var1 >= nextResolveAtMs) {
         nextResolveAtMs = var1 + 5000L;
         String var3 = tryPlayerName();
         if (var3 != null && !var3.isBlank()) {
            accountKey = "mc:" + var3.trim().toLowerCase(Locale.ROOT);
            return accountKey;
         }
      }

      return FALLBACK_KEY;
   }

   private static String tryPlayerName() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getMethod("method_1551");
         Object var2 = var1.invoke(null);
         if (var2 == null) {
            return null;
         }

         Field var3 = findField(var0, "field_1724");
         if (var3 == null) {
            return null;
         }

         Object var4 = var3.get(var2);
         if (var4 == null) {
            return null;
         }

         Method var5 = var4.getClass().getMethod("method_5477");
         Object var6 = var5.invoke(var4);
         if (var6 == null) {
            return null;
         }

         try {
            Method var7 = var6.getClass().getMethod("getString");
            Object var8 = var7.invoke(var6);
            return var8 == null ? null : String.valueOf(var8);
         } catch (Throwable var9) {
            return String.valueOf(var6);
         }
      } catch (Throwable var10) {
         return null;
      }
   }

   private static Field findField(Class<?> var0, String var1) {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (Throwable var4) {
         }
      }

      return null;
   }

   private static long readStaticLong(String var0, String var1, long var2) {
      try {
         Class var4 = Class.forName(var0);
         Field var5 = var4.getDeclaredField(var1);
         var5.setAccessible(true);
         return var5.getLong(null);
      } catch (Throwable var6) {
         return var2;
      }
   }

   private static long fnv1a64(String var0) {
      long var1 = -3750763034362895579L;

      for (int var3 = 0; var3 < var0.length(); var3++) {
         var1 ^= var0.charAt(var3);
         var1 *= 1099511628211L;
      }

      return var1;
   }

   private static long mix64(long var0) {
      var0 = (var0 ^ var0 >>> 30) * -4658895280553007687L;
      var0 = (var0 ^ var0 >>> 27) * -7723592293110705685L;
      return var0 ^ var0 >>> 31;
   }

   private static double unitSigned(long var0) {
      long var2 = var0 >>> 11 & 9007199254740991L;
      double var4 = var2 / 9.007199E15F;
      return var4 * 2.0 - 1.0;
   }

   private static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }
}
