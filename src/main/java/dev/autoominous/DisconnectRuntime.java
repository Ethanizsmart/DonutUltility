package dev.autoominous;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2535;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_746;

public final class DisconnectRuntime {
   private static int scanTickCounter;
   private static final int SCAN_INTERVAL_TICKS = 10;
   private static boolean sawDisconnectedState;
   public static class_639 lastServerAddress;
   public static class_642 lastServerInfo;
   public static boolean disconnectTriggered;
   public static String lastDetectedPlayer;
   public static double lastDetectedDistance = -1.0;

   private DisconnectRuntime() {
   }

   public static void rememberServer(class_639 var0, class_642 var1) {
      lastServerAddress = var0;
      lastServerInfo = var1;
   }

   public static void onTick() {
      BountyRecoveryBridge.onTick();
      if (DisconnectSettings.isEnabled()) {
         class_310 var0 = class_310.method_1551();
         if (var0 != null) {
            class_746 var1 = var0.field_1724;
            class_638 var2 = var0.field_1687;
            if (disconnectTriggered) {
               if (var1 == null || var2 == null) {
                  sawDisconnectedState = true;
               } else if (sawDisconnectedState) {
                  disconnectTriggered = false;
                  sawDisconnectedState = false;
                  scanTickCounter = 0;
               }
            } else if (var1 != null && var2 != null) {
               if (++scanTickCounter >= 10) {
                  scanTickCounter = 0;
                  class_243 var3 = var1.method_73189();
                  double var4 = DisconnectSettings.getRadius();
                  List<? extends class_1657> var6 = var2.method_18456();
                  if (var6 != null) {
                     for (class_1657 var8 : var6) {
                        if (var8 != null && var8 != var1 && !var8.method_29504() && var8.method_5805()) {
                           class_2561 var9 = var8.method_5477();
                           String var10 = var9 == null ? null : var9.getString();
                           if (var10 != null && !var10.isEmpty() && !DisconnectSettings.isWhitelisted(var10)) {
                              class_243 var11 = var8.method_73189();
                              if (var11 != null && var3 != null) {
                                 double var12 = var11.method_1022(var3);
                                 if (!(var12 > var4)) {
                                    disconnectTriggered = true;
                                    sawDisconnectedState = false;
                                    lastDetectedPlayer = var10;
                                    lastDetectedDistance = var12;
                                    DisconnectSettings.recordDetection(var10, var12);
                                    String var14 = String.format(Locale.ROOT, "Auto Disconnect: %s detected at %.1f blocks (radius %.1f)", var10, var12, var4);
                                    class_634 var15 = var0.method_1562();
                                    if (var15 != null) {
                                       class_2535 var16 = var15.method_48296();
                                       if (var16 != null) {
                                          var16.method_10747(class_2561.method_43470(var14));
                                       }
                                    }

                                    return;
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
   }

   public static void clearProtectionTrigger() {
      disconnectTriggered = false;
      sawDisconnectedState = false;
      lastDetectedPlayer = null;
      lastDetectedDistance = -1.0;
      scanTickCounter = 0;
   }
}
