package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AutoReconnectRuntime {
   private static long nextAttemptAtMs;
   private static boolean wasTriggered;

   private AutoReconnectRuntime() {
   }

   public static void onTick() {
      AutoReconnectSettings.maybePersist();
      if (!AutoReconnectSettings.isEnabled()) {
         nextAttemptAtMs = 0L;
         wasTriggered = false;
      } else {
         try {
            Class var0 = Class.forName("dev.autoominous.DisconnectRuntime");
            Field var1 = var0.getField("disconnectTriggered");
            boolean var2 = var1.getBoolean(null);
            long var3 = System.currentTimeMillis();
            if (!var2) {
               nextAttemptAtMs = 0L;
               wasTriggered = false;
               return;
            }

            Object var5 = minecraftClient();
            if (var5 == null) {
               return;
            }

            Object var6 = field(var5, "field_1724");
            Object var7 = field(var5, "field_1687");
            Object var8 = field(var5, "field_1755");
            if (var8 != null && "net.minecraft.class_412".equals(var8.getClass().getName())) {
               nextAttemptAtMs = var3 + delayMs();
               return;
            }

            if (var6 != null && var7 != null) {
               nextAttemptAtMs = 0L;
               wasTriggered = false;
               return;
            }

            if (!wasTriggered) {
               wasTriggered = true;
               nextAttemptAtMs = var3 + delayMs();
               return;
            }

            if (nextAttemptAtMs <= 0L) {
               nextAttemptAtMs = var3 + delayMs();
            }

            if (var3 < nextAttemptAtMs) {
               return;
            }

            Object var9 = var0.getField("lastServerAddress").get(null);
            Object var10 = var0.getField("lastServerInfo").get(null);
            if (var9 == null) {
               nextAttemptAtMs = var3 + delayMs();
               return;
            }

            connect(var5, var9, var10);
            nextAttemptAtMs = var3 + delayMs();
         } catch (Throwable var11) {
            nextAttemptAtMs = System.currentTimeMillis() + delayMs();
            System.err.println("[DonutUtility/AutoReconnect] Reconnect attempt failed: " + var11.getMessage());
         }
      }
   }

   private static long delayMs() {
      return Math.max(500L, Math.round(AutoReconnectSettings.getReconnectDelaySeconds() * 1000.0));
   }

   private static Object minecraftClient() throws Exception {
      Class var0 = Class.forName("net.minecraft.class_310");
      return var0.getMethod("method_1551").invoke(null);
   }

   private static void connect(Object var0, Object var1, Object var2) throws Exception {
      Class var3 = Class.forName("net.minecraft.class_437");
      Class var4 = Class.forName("net.minecraft.class_310");
      Class var5 = Class.forName("net.minecraft.class_639");
      Class var6 = Class.forName("net.minecraft.class_642");
      Class var7 = Class.forName("net.minecraft.class_9112");
      Object var8 = Class.forName("net.minecraft.class_442").getConstructor().newInstance();
      Class var9 = Class.forName("net.minecraft.class_412");
      Method var10 = var9.getDeclaredMethod("method_36877", var3, var4, var5, var6, boolean.class, var7);
      var10.setAccessible(true);
      var10.invoke(null, var8, var0, var1, var2, false, null);
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
}
