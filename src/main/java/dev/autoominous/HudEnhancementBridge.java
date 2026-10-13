package dev.autoominous;

import java.lang.reflect.Method;

public final class HudEnhancementBridge {
   private static final int WHITE = -1;
   private static final int GREEN = -11141291;
   private static final int RED = -43691;
   private static final int YELLOW = -171;
   private static final int CYAN = -11141121;
   private static final int LINE = 11;

   private HudEnhancementBridge() {
   }

   public static void renderAutoReconnect(Object var0, Object var1) {
      try {
         int var2 = callInt("dev.autoominous.AutoReconnectSettings", "getHudX", 8);
         int var3 = callInt("dev.autoominous.FarmCycleHudRuntime", "reconnectHudY", 56);
         boolean var4 = callBoolean("dev.autoominous.AutoReconnectSettings", "isEnabled", false);
         boolean var5 = SafetyReconnectBridge.isSafetyPaused();
         String var6 = "Auto Reconnect: ";
         draw(var0, var1, var6, var2, var3, -1);
         if (var5) {
            draw(var0, var1, "PAUSED", var2 + width(var1, var6), var3, -171);
            String var7 = "Player safety disconnect";
            draw(var0, var1, var7, var2, var3 + 11, -11141121);
         } else {
            draw(var0, var1, var4 ? "ON" : "OFF", var2 + width(var1, var6), var3, var4 ? -11141291 : -43691);
         }
      } catch (Throwable var8) {
      }
   }

   public static void renderBounty(Object var0, Object var1) {
      try {
         int var2 = callInt("dev.autoominous.BountyHudSettings", "getHudX", 8);
         int var3 = callInt("dev.autoominous.FarmCycleHudRuntime", "bountyHudY", 74);
         boolean var4 = callBoolean("dev.autoominous.BountySettings", "isEnabled", false);
         String var5 = callString("dev.autoominous.BountySettings", "getTarget", "-");
         if (var5 == null || var5.isBlank()) {
            var5 = "-";
         }

         String var6 = callString("dev.autoominous.BountyRuntime", "getLastAdded", "-");
         if (var6 == null || var6.isBlank()) {
            var6 = "-";
         }

         String var7 = BountyStatsBridge.getTotalText();
         boolean var8 = callBoolean("dev.autoominous.BountyRuntime", "isRetrying", false);
         drawPair(var0, var1, "Bounty: ", var4 ? "ON" : "OFF", var2, var3, -1, var4 ? -11141291 : -43691);
         drawPair(var0, var1, "Target: ", var5, var2, var3 + 11, -1, -171);
         drawPair(var0, var1, "Total: ", var7, var2, var3 + 22, -1, -11141291);
         drawPair(var0, var1, "Last: ", var6, var2, var3 + 33, -1, -11141291);
         if (var8) {
            String var9 = callString("dev.autoominous.BountyRuntime", "getRetryCountdownText", "--");
            drawPair(var0, var1, "Status: ", "RETRY " + var9, var2, var3 + 44, -1, -171);
         } else {
            String var11 = callString("dev.autoominous.BountyRuntime", "getNextBountyCountdownText", "--");
            drawPair(var0, var1, "Next: ", var11, var2, var3 + 44, -1, -171);
         }
      } catch (Throwable var10) {
      }
   }

   private static void drawPair(Object var0, Object var1, String var2, String var3, int var4, int var5, int var6, int var7) {
      draw(var0, var1, var2, var4, var5, var6);
      draw(var0, var1, var3, var4 + width(var1, var2), var5, var7);
   }

   private static int width(Object var0, String var1) {
      if (var0 != null && var1 != null) {
         try {
            Method var2 = var0.getClass().getMethod("method_1727", String.class);
            return var2.invoke(var0, var1) instanceof Number var4 ? var4.intValue() : var1.length() * 6;
         } catch (Throwable var5) {
            return var1.length() * 6;
         }
      } else {
         return 0;
      }
   }

   private static void draw(Object var0, Object var1, String var2, int var3, int var4, int var5) {
      if (var0 != null && var1 != null && var2 != null) {
         try {
            for (Method var9 : var0.getClass().getMethods()) {
               if (var9.getName().equals("method_25303") && var9.getParameterCount() == 5) {
                  Class[] var10 = var9.getParameterTypes();
                  if (var10[1] == String.class && var10[2] == int.class && var10[3] == int.class && var10[4] == int.class) {
                     var9.invoke(var0, var1, var2, var3, var4, var5);
                     return;
                  }
               }
            }
         } catch (Throwable var11) {
         }
      }
   }

   private static boolean callBoolean(String var0, String var1, boolean var2) {
      try {
         return Class.forName(var0).getMethod(var1).invoke(null) instanceof Boolean var4 ? var4 : var2;
      } catch (Throwable var5) {
         return var2;
      }
   }

   private static int callInt(String var0, String var1, int var2) {
      try {
         return Class.forName(var0).getMethod(var1).invoke(null) instanceof Number var4 ? var4.intValue() : var2;
      } catch (Throwable var5) {
         return var2;
      }
   }

   private static String callString(String var0, String var1, String var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 == null ? var2 : String.valueOf(var3);
      } catch (Throwable var4) {
         return var2;
      }
   }
}
