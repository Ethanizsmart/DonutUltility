package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class DeathRecoveryRuntime {
   private static final long POST_HOME_GUARD_MS = 8000L;
   private static boolean recoveryPending;
   private static boolean respawnRequested;
   private static long deathDetectedAtMs = -1L;
   private static long worldReadyAtMs = -1L;
   private static boolean sellGuiClosedForDeath;
   private static boolean homeCommandSent;
   private static long homeCommandSentAtMs = -1L;
   private static boolean postDeathSellPending;
   private static Object postDeathSellPlayer;
   private static Object postDeathSellConnection;

   private DeathRecoveryRuntime() {
   }

   public static boolean isRecoveryPending() {
      return recoveryPending;
   }

   public static boolean isPostDeathSellPending() {
      return postDeathSellPending;
   }

   public static boolean isBlockingAutoSell() {
      return recoveryPending && !postDeathSellPending;
   }

   public static void onTick() {
      if (!MasterAutomation.isEnabled()) {  return; }
      invokeStaticVoid("dev.autoominous.DeathRecoverySettings", "maybePersist");
      if (!invokeStaticBoolean("dev.autoominous.DeathRecoverySettings", "isEnabled", false)) {
         clearState();
      } else {
         Object var0 = getMinecraftClient();
         if (var0 != null) {
            Object var1 = getDeclaredField(var0.getClass(), var0, "field_1724");
            Object var2 = getDeclaredField(var0.getClass(), var0, "field_1687");
            Object var3 = getDeclaredField(var0.getClass(), var0, "field_1755");
            boolean var4 = isInstance(var3, "net.minecraft.class_418");
            boolean var5 = var1 != null && invokeBoolean(var1, "method_29504", false);
            boolean var6 = VexSafetyRuntime.filterDeathForRecovery(var4 || var5);
            long var7 = System.currentTimeMillis();
            if (recoveryPending && (invokeNoArg(var0, "method_1562") == null
               || postDeathSellPending && (var1 == null || var2 == null))) {
               clearState();
               return;
            }
            if (var6) {
               if (!recoveryPending || homeCommandSent) {
                  beginRecovery(var7);
               }

               if (!sellGuiClosedForDeath && isInstance(var3, "net.minecraft.class_465")) {
                  closeHandledScreen(var0, var1);
                  sellGuiClosedForDeath = true;
               }

               invokeStaticVoid("dev.autoominous.SlashSellRuntime", "resetRuntime");
               if (!respawnRequested && var1 != null) {
                  long var17 = deathDetectedAtMs < 0L ? Long.MAX_VALUE : var7 - deathDetectedAtMs;
                  int var18 = invokeStaticInt("dev.autoominous.DeathRecoverySettings", "getRespawnDelayMs", 0);
                  if (var17 >= Math.max(0, var18)) {
                     try {
                        Method var19 = var1.getClass().getMethod("method_7331");
                        var19.invoke(var1);
                        respawnRequested = true;
                        System.out.println("[Auto Ominous] Death Recovery: respawn requested after delay.");
                     } catch (Throwable var15) {
                        System.err.println("[Auto Ominous] Death Recovery: respawn request failed: " + var15.getMessage());
                     }
                  }
               }
            } else if (recoveryPending) {
               if (postDeathSellPending) {
                  net.minecraft.class_310 client = (net.minecraft.class_310)var0;
                  if (var1 != postDeathSellPlayer
                     || client.method_1562() != postDeathSellConnection
                     || com.seastore.auto.seaats.client.AutoSellLogic.isInventorySellComplete(client)) {
                     clearState();
                     System.out.println("[Auto Ominous] Death Recovery: post-respawn sell finished; resuming automation.");
                  }
               } else if (homeCommandSent) {
                  if (var7 - homeCommandSentAtMs >= POST_HOME_GUARD_MS) beginPostDeathSell(var0, var1);
               } else {
                  boolean var9 = var1 != null && invokeBoolean(var1, "method_5805", false);
                  if (var1 == null || var2 == null || !var9) {
                     worldReadyAtMs = -1L;
                  } else if (worldReadyAtMs < 0L) {
                     worldReadyAtMs = var7;
                  } else {
                     int var10 = invokeStaticInt("dev.autoominous.DeathRecoverySettings", "getHomeDelayMs", 0);
                     if (var7 - worldReadyAtMs >= Math.max(0, var10)) {
                        Object var11 = invokeNoArg(var0, "method_1562");
                        if (var11 != null) {
                           String var12 = invokeStaticString("dev.autoominous.DeathRecoverySettings", "getHome1CommandForSend", "/home 1");
                           String var13 = normalizeCommandForNetwork(var12);
                           if (var13.isBlank()) {
                              System.err.println("[Auto Ominous] Death Recovery: Home 1 command is empty; continuing to post-respawn sell without /home command.");
                              beginPostDeathSell(var0, var1);
                           } else {
                              try {
                                 Method var14 = var11.getClass().getMethod("method_45730", String.class);
                                 var14.invoke(var11, var13);
                                 homeCommandSent = true;
                                 homeCommandSentAtMs = var7;
                                 System.out.println("[Auto Ominous] Death Recovery: sent /" + var13 + "; holding /sell for 8000ms.");
                              } catch (Throwable var16) {
                                 System.err.println("[Auto Ominous] Death Recovery: failed to send home command: " + var16.getMessage());
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void beginRecovery(long var0) {
      HomePositionGuardRuntime.cancelForDeathRecovery();
      SwordRecoveryRuntime.forceReset();
      postDeathSellPending = false;
      postDeathSellPlayer = null;
      postDeathSellConnection = null;
      recoveryPending = true;
      respawnRequested = false;
      deathDetectedAtMs = var0;
      worldReadyAtMs = -1L;
      sellGuiClosedForDeath = false;
      homeCommandSent = false;
      homeCommandSentAtMs = -1L;
      System.out.println("[Auto Ominous] Death Recovery: death detected; suspending /sell and waiting before respawn.");
   }

   private static void beginPostDeathSell(Object client, Object player) {
      SlashSellRuntime.resetRuntime();
      postDeathSellPending = true;
      postDeathSellPlayer = player;
      postDeathSellConnection = invokeNoArg(client, "method_1562");
      ProtectedItemAutoUseRuntime.forceRelease();
      System.out.println("[Auto Ominous] Death Recovery: selling inventory before resuming automation.");
   }

   private static String normalizeCommandForNetwork(String var0) {
      if (var0 == null) {
         return "";
      } else {
         String var1 = var0.trim();

         while (var1.startsWith("/")) {
            var1 = var1.substring(1).trim();
         }

         return var1;
      }
   }

   private static void closeHandledScreenIfPresent(Object var0, Object var1) {
      Object var2 = getDeclaredField(var0.getClass(), var0, "field_1755");
      if (isInstance(var2, "net.minecraft.class_465")) {
         closeHandledScreen(var0, var1);
      }
   }

   private static void closeHandledScreen(Object var0, Object var1) {
      try {
         if (var1 != null) {
            Method var2 = var1.getClass().getMethod("method_7346");
            var2.invoke(var1);
         }
      } catch (Throwable var5) {
      }

      try {
         Class var6 = Class.forName("net.minecraft.class_437");
         Method var3 = var0.getClass().getMethod("method_1507", var6);
         var3.invoke(var0, (Object)null);
         System.out.println("[Auto Ominous] Death Recovery: closed stale /sell container during recovery.");
      } catch (Throwable var4) {
         System.err.println("[Auto Ominous] Death Recovery: could not close stale sell GUI: " + var4.getMessage());
      }
   }

   private static void clearState() {
      postDeathSellPending = false;
      postDeathSellPlayer = null;
      postDeathSellConnection = null;
      recoveryPending = false;
      respawnRequested = false;
      deathDetectedAtMs = -1L;
      worldReadyAtMs = -1L;
      sellGuiClosedForDeath = false;
      homeCommandSent = false;
      homeCommandSentAtMs = -1L;
   }

   private static Object getMinecraftClient() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         Method var1 = var0.getMethod("method_1551");
         return var1.invoke(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static boolean isInstance(Object var0, String var1) {
      if (var0 == null) {
         return false;
      } else {
         try {
            return Class.forName(var1).isInstance(var0);
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   private static Object getDeclaredField(Class<?> var0, Object var1, String var2) {
      try {
         Field var3 = var0.getDeclaredField(var2);
         var3.setAccessible(true);
         return var3.get(var1);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Object invokeNoArg(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return var0.getClass().getMethod(var1).invoke(var0);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static boolean invokeBoolean(Object var0, String var1, boolean var2) {
      Object var3 = invokeNoArg(var0, var1);
      return var3 instanceof Boolean ? (Boolean)var3 : var2;
   }

   private static void invokeStaticVoid(String var0, String var1) {
      try {
         Class.forName(var0).getMethod(var1).invoke(null);
      } catch (Throwable var3) {
      }
   }

   private static boolean invokeStaticBoolean(String var0, String var1, boolean var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static int invokeStaticInt(String var0, String var1, int var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Number ? ((Number)var3).intValue() : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static String invokeStaticString(String var0, String var1, String var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof String ? (String)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   public static void resetForVexDeathWait() {
      clearState();
   }
}
