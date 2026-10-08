package dev.autoominous;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_634;

public final class SwordRecoveryRuntime {
   private static long homeStamp = Long.MIN_VALUE;
   private static SwordRecoveryRuntime.State state = SwordRecoveryRuntime.State.WAIT_STABLE;
   private static long nextActionAtMs;
   private static long stateDeadlineMs;
   private static boolean allowAutoSell;
   private static int retries;
   private static long armedHomeStamp = Long.MIN_VALUE;
   private static boolean homeGateArmed;
   private static Object preHomeWorld;
   private static double preHomeX;
   private static double preHomeY;
   private static double preHomeZ;
   private static boolean teleportSeen;
   private static long stableSinceMs;
   private static double lastGateX;
   private static double lastGateY;
   private static double lastGateZ;

   private SwordRecoveryRuntime() {
   }

   public static boolean onPostHomeTick(long var0) {
      if (HomePositionGuardRuntime.shouldPauseSwordRecovery()) {
         return false;
      } else {
         long var2 = System.currentTimeMillis();
         class_310 var4 = class_310.method_1551();
         if (var4 != null && var4.field_1724 != null && var4.field_1761 != null) {
            if (homeStamp != var0) {
               startNew(var4, var0, var2);
            }

            if (var2 < nextActionAtMs) {
               return false;
            } else {
               try {
                  switch (state) {
                     case WAIT_STABLE:
                        if (homeGateArmed && armedHomeStamp == var0) {
                           double[] var20 = position(var4.field_1724);
                           if (var20 == null) {
                              nextActionAtMs = var2 + 100L;
                              return false;
                           }

                           Object var24 = reflectedField(var4, "field_1687");
                           boolean var7 = preHomeWorld != null && var24 != null && var24 != preHomeWorld;
                           double var8 = distanceSq(var20[0], var20[1], var20[2], preHomeX, preHomeY, preHomeZ);
                           if (!teleportSeen) {
                              if (!var7 && !(var8 >= 16.0)) {
                                 if (var2 >= stateDeadlineMs) {
                                    System.out.println("[Auto Ominous] Sword Recovery: /home position not confirmed within 10s; retrying /home.");
                                    forceReset();
                                    DeathRecoveryRuntime.retryHomeCommand();
                                    return false;
                                 }

                                 nextActionAtMs = var2 + 100L;
                                 return false;
                              }

                              teleportSeen = true;
                              stableSinceMs = var2;
                              lastGateX = var20[0];
                              lastGateY = var20[1];
                              lastGateZ = var20[2];
                              System.out.println("[Auto Ominous] Sword Recovery: /home teleport detected; waiting for position to settle.");
                              nextActionAtMs = var2 + 100L;
                              return false;
                           }

                           double var10 = distanceSq(var20[0], var20[1], var20[2], lastGateX, lastGateY, lastGateZ);
                           lastGateX = var20[0];
                           lastGateY = var20[1];
                           lastGateZ = var20[2];
                           if (var10 > 0.25) {
                              stableSinceMs = var2;
                           }

                           if (var2 - stableSinceMs < 650L) {
                              nextActionAtMs = var2 + 100L;
                              return false;
                           }

                           System.out.println("[Auto Ominous] Sword Recovery: home position confirmed stable; starting sword check.");
                           state = SwordRecoveryRuntime.State.CHECK_LOCAL;
                           nextActionAtMs = var2 + random(150, 350);
                           break;
                        }

                        armFromCurrent(var4, var0, var2);
                        return false;
                     case CHECK_LOCAL:
                        int var19 = findLootingDiamondSword(var4, true);
                        if (var19 >= 0) {
                           if (var19 < 9) {
                              return finish(var4);
                           }

                           closeOrderOrHandledScreen(var4);
                           swapInventorySlotToHotbarZero(var4, var19);
                           state = SwordRecoveryRuntime.State.VERIFY_HOTBAR;
                           stateDeadlineMs = var2 + 3000L;
                           nextActionAtMs = var2 + random(250, 500);
                        } else if (!hasEmptyMainSlot(var4)) {
                           allowAutoSell = true;
                           state = SwordRecoveryRuntime.State.FREE_SPACE;
                           stateDeadlineMs = var2 + 30000L;
                           nextActionAtMs = var2 + random(200, 400);
                        } else {
                           state = SwordRecoveryRuntime.State.OPEN_ORDER;
                           nextActionAtMs = var2 + random(250, 500);
                        }
                        break;
                     case FREE_SPACE:
                        if (hasEmptyMainSlot(var4)) {
                           suspendAutoSellNow();
                           closeOrderOrHandledScreen(var4);
                           state = SwordRecoveryRuntime.State.OPEN_ORDER;
                           nextActionAtMs = var2 + random(400, 800);
                        } else {
                           if (var2 >= stateDeadlineMs) {
                              SlashSellRuntime.resetRuntime();
                              stateDeadlineMs = var2 + 30000L;
                           }

                           nextActionAtMs = var2 + random(200, 450);
                        }
                        break;
                     case OPEN_ORDER:
                        suspendAutoSellNow();
                        closeOrderOrHandledScreen(var4);
                        sendCommand(var4, "order");
                        state = SwordRecoveryRuntime.State.WAIT_ROOT;
                        stateDeadlineMs = var2 + 6000L;
                        nextActionAtMs = var2 + random(500, 900);
                        break;
                     case WAIT_ROOT:
                        String var18 = title(var4);
                        if (var18.startsWith("Orders (Page")) {
                           int var23 = findNamedTopSlot(var4, "your orders", false);
                           if (var23 >= 0) {
                              clickTopSlot(var4, var23, class_1713.field_7790);
                              state = SwordRecoveryRuntime.State.WAIT_YOUR;
                              stateDeadlineMs = var2 + 5000L;
                              nextActionAtMs = var2 + random(350, 700);
                           } else {
                              retryOrReopen(var2);
                           }
                        } else if (var2 >= stateDeadlineMs) {
                           retryOrReopen(var2);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                        break;
                     case WAIT_YOUR:
                        String var17 = title(var4);
                        if (var17.toLowerCase(Locale.ROOT).contains("your orders")) {
                           int var22 = findDiamondSwordTopSlot(var4);
                           if (var22 >= 0) {
                              clickTopSlot(var4, var22, class_1713.field_7790);
                              state = SwordRecoveryRuntime.State.WAIT_EDIT;
                              stateDeadlineMs = var2 + 5000L;
                              nextActionAtMs = var2 + random(350, 700);
                           } else {
                              retryOrReopen(var2);
                           }
                        } else if (var2 >= stateDeadlineMs) {
                           retryOrReopen(var2);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                        break;
                     case WAIT_EDIT:
                        String var16 = title(var4);
                        if (var16.toLowerCase(Locale.ROOT).contains("edit order")) {
                           int var21 = findNamedTopSlot(var4, "collect", true);
                           if (var21 >= 0) {
                              clickTopSlot(var4, var21, class_1713.field_7790);
                              state = SwordRecoveryRuntime.State.WAIT_COLLECT;
                              stateDeadlineMs = var2 + 5000L;
                              nextActionAtMs = var2 + random(350, 700);
                           } else {
                              retryOrReopen(var2);
                           }
                        } else if (var2 >= stateDeadlineMs) {
                           retryOrReopen(var2);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                        break;
                     case WAIT_COLLECT:
                        String var15 = title(var4);
                        if (var15.toLowerCase(Locale.ROOT).contains("collect items")) {
                           int var6 = findDiamondSwordTopSlot(var4);
                           if (var6 >= 0) {
                              clickTopSlot(var4, var6, class_1713.field_7794);
                              state = SwordRecoveryRuntime.State.VERIFY_COLLECT;
                              stateDeadlineMs = var2 + 4000L;
                              nextActionAtMs = var2 + random(300, 600);
                           } else {
                              retryOrReopen(var2);
                           }
                        } else if (var2 >= stateDeadlineMs) {
                           retryOrReopen(var2);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                        break;
                     case VERIFY_COLLECT:
                        int var14 = findLootingDiamondSword(var4, true);
                        if (var14 >= 0) {
                           closeOrderOrHandledScreen(var4);
                           state = SwordRecoveryRuntime.State.MOVE_TO_HOTBAR;
                           nextActionAtMs = var2 + random(250, 500);
                        } else if (var2 >= stateDeadlineMs) {
                           state = SwordRecoveryRuntime.State.OPEN_ORDER;
                           nextActionAtMs = var2 + random(400, 800);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                        break;
                     case MOVE_TO_HOTBAR:
                        int var13 = findLootingDiamondSword(var4, true);
                        if (var13 < 0) {
                           state = SwordRecoveryRuntime.State.OPEN_ORDER;
                           nextActionAtMs = var2 + random(400, 800);
                        } else {
                           if (var13 < 9) {
                              return finish(var4);
                           }

                           closeOrderOrHandledScreen(var4);
                           swapInventorySlotToHotbarZero(var4, var13);
                           state = SwordRecoveryRuntime.State.VERIFY_HOTBAR;
                           stateDeadlineMs = var2 + 3000L;
                           nextActionAtMs = var2 + random(250, 500);
                        }
                        break;
                     case VERIFY_HOTBAR:
                        int var5 = findLootingDiamondSword(var4, true);
                        if (var5 >= 0 && var5 < 9) {
                           return finish(var4);
                        }

                        if (var2 >= stateDeadlineMs) {
                           state = SwordRecoveryRuntime.State.MOVE_TO_HOTBAR;
                           nextActionAtMs = var2 + random(250, 500);
                        } else {
                           nextActionAtMs = var2 + 150L;
                        }
                  }
               } catch (Throwable var12) {
                  allowAutoSell = false;
                  closeOrderOrHandledScreen(var4);
                  state = SwordRecoveryRuntime.State.OPEN_ORDER;
                  nextActionAtMs = var2 + random(600, 1200);
               }

               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean isRecoveryBlockingAutoSell() {
      return DeathRecoveryRuntime.isRecoveryPending() && !allowAutoSell;
   }

   public static boolean allowAutoSellDuringRecovery() {
      return allowAutoSell;
   }

   public static void forceReset() {
      allowAutoSell = false;
      homeStamp = Long.MIN_VALUE;
      state = SwordRecoveryRuntime.State.WAIT_STABLE;
      stateDeadlineMs = 0L;
      nextActionAtMs = 0L;
      retries = 0;
      armedHomeStamp = Long.MIN_VALUE;
      homeGateArmed = false;
      preHomeWorld = null;
      preHomeZ = 0.0;
      preHomeY = 0.0;
      preHomeX = 0.0;
      teleportSeen = false;
      stableSinceMs = 0L;
      lastGateZ = 0.0;
      lastGateY = 0.0;
      lastGateX = 0.0;
   }

   public static void armHomePositionGate(Object var0, Object var1, long var2) {
      forceReset();
      homeStamp = var2;
      armedHomeStamp = var2;
      double[] var4 = position(var0);
      if (var4 != null) {
         preHomeX = var4[0];
         preHomeY = var4[1];
         preHomeZ = var4[2];
         lastGateX = var4[0];
         lastGateY = var4[1];
         lastGateZ = var4[2];
         homeGateArmed = true;
      }

      preHomeWorld = var1;
      state = SwordRecoveryRuntime.State.WAIT_STABLE;
      long var5 = System.currentTimeMillis();
      nextActionAtMs = var5 + 100L;
      stateDeadlineMs = var5 + 10000L;
      System.out.println("[Auto Ominous] Sword Recovery: armed /home position gate at " + fmt(preHomeX) + "," + fmt(preHomeY) + "," + fmt(preHomeZ));
   }

   private static void startNew(class_310 var0, long var1, long var3) {
      forceReset();
      homeStamp = var1;
      armFromCurrent(var0, var1, var3);
   }

   private static void armFromCurrent(class_310 var0, long var1, long var3) {
      armedHomeStamp = var1;
      double[] var5 = position(var0.field_1724);
      if (var5 != null) {
         preHomeX = var5[0];
         preHomeY = var5[1];
         preHomeZ = var5[2];
         lastGateX = var5[0];
         lastGateY = var5[1];
         lastGateZ = var5[2];
         homeGateArmed = true;
      }

      preHomeWorld = reflectedField(var0, "field_1687");
      teleportSeen = false;
      stableSinceMs = 0L;
      state = SwordRecoveryRuntime.State.WAIT_STABLE;
      nextActionAtMs = var3 + 100L;
      stateDeadlineMs = var3 + 10000L;
   }

   private static boolean finish(class_310 var0) {
      allowAutoSell = false;
      closeOrderOrHandledScreen(var0);
      DeathRecoveryRuntime.completeAfterSwordRecovery();
      return true;
   }

   private static void suspendAutoSellNow() {
      allowAutoSell = false;

      try {
         SlashSellRuntime.resetRuntime();
      } catch (Throwable var2) {
      }

      try {
         SlashSellRuntime.onTick();
      } catch (Throwable var1) {
      }
   }

   private static void retryOrReopen(long var0) {
      HomePositionGuardRuntime.handleSwordRecoveryRetry(var0);
   }

   private static int findLootingDiamondSword(class_310 var0, boolean var1) {
      return HomePositionGuardRuntime.adjustSwordRecoverySearchResult(findDiamondSwordInternal(var0, var1));
   }

   private static int findDiamondSword(class_310 var0, boolean var1) {
      return findDiamondSwordInternal(var0, var1);
   }

   private static int findDiamondSwordInternal(class_310 var0, boolean var1) {
      class_1661 var2 = var0.field_1724.method_31548();
      if (var2 == null) {
         return -1;
      } else {
         for (int var3 = 0; var3 < 36; var3++) {
            class_1799 var4 = var2.method_5438(var3);
            if (isDiamondSword(var4) && (!var1 || hasLootingThree(var0, var4))) {
               return var3;
            }
         }

         return -1;
      }
   }

   private static boolean hasEmptyMainSlot(class_310 var0) {
      class_1661 var1 = var0.field_1724.method_31548();
      if (var1 == null) {
         return false;
      } else {
         for (int var2 = 0; var2 < 36; var2++) {
            class_1799 var3 = var1.method_5438(var2);
            if (var3 == null || var3.method_7960()) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isDiamondSword(class_1799 var0) {
      return var0 != null && !var0.method_7960() && var0.method_31574(class_1802.field_8802);
   }

   private static boolean hasLootingThree(class_310 var0, class_1799 var1) {
      try {
         List<class_2561> var2 = class_437.method_25408(var0, var1);
         if (var2 != null) {
            for (class_2561 var4 : var2) {
               String var5 = var4 == null ? "" : var4.method_10858(256).toLowerCase(Locale.ROOT);
               if (var5.contains("looting iii") || var5.contains("looting 3")) {
                  return true;
               }
            }
         }
      } catch (Throwable var6) {
      }

      return false;
   }

   private static void swapInventorySlotToHotbarZero(class_310 var0, int var1) {
      if (var1 >= 9) {
         class_1703 var2 = var0.field_1724.field_7512;
         if (var2 != null) {
            var0.field_1761.method_2906(var2.field_7763, var1, 0, class_1713.field_7791, var0.field_1724);
         }
      }
   }

   private static void sendCommand(class_310 var0, String var1) {
      class_634 var2 = var0.method_1562();
      if (var2 != null) {
         var2.method_45730(var1);
      }
   }

   private static String title(class_310 var0) {
      class_437 var1 = var0.field_1755;
      if (var1 == null) {
         return "";
      } else {
         class_2561 var2 = var1.method_25440();
         return var2 == null ? "" : var2.method_10858(256).trim();
      }
   }

   private static int findNamedTopSlot(class_310 var0, String var1, boolean var2) {
      if (!(var0.field_1755 instanceof class_465 var3)) {
         return -1;
      } else {
         class_1703 var11 = var3.method_17577();
         int var5 = Math.max(0, var11.field_7761.size() - 36);
         String var6 = var1.toLowerCase(Locale.ROOT);

         for (int var7 = 0; var7 < var5; var7++) {
            class_1799 var8 = var11.method_7611(var7).method_7677();
            if (var8 != null && !var8.method_7960()) {
               class_2561 var9 = var8.method_7964();
               String var10 = var9 == null ? "" : var9.method_10858(128).trim().toLowerCase(Locale.ROOT);
               if (var2 ? var10.equals(var6) : var10.contains(var6)) {
                  return var7;
               }
            }
         }

         return -1;
      }
   }

   private static int findDiamondSwordTopSlot(class_310 var0) {
      if (var0.field_1755 instanceof class_465 var1) {
         class_1703 var6 = var1.method_17577();
         int var3 = Math.max(0, var6.field_7761.size() - 36);

         for (int var4 = 0; var4 < var3; var4++) {
            class_1799 var5 = var6.method_7611(var4).method_7677();
            if (isDiamondSword(var5)) {
               return var4;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static void clickTopSlot(class_310 var0, int var1, class_1713 var2) {
      if (var0.field_1755 instanceof class_465 var3) {
         class_1703 var5 = var3.method_17577();
         var0.field_1761.method_2906(var5.field_7763, var1, 0, var2, var0.field_1724);
      }
   }

   private static void closeOrderOrHandledScreen(class_310 var0) {
      try {
         if (var0.field_1755 instanceof class_465) {
            var0.field_1724.method_7346();
         }
      } catch (Throwable var2) {
      }
   }

   private static double[] position(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            double var1 = ((Number)var0.getClass().getMethod("method_23317").invoke(var0)).doubleValue();
            double var3 = ((Number)var0.getClass().getMethod("method_23318").invoke(var0)).doubleValue();
            double var5 = ((Number)var0.getClass().getMethod("method_23321").invoke(var0)).doubleValue();
            return new double[]{var1, var3, var5};
         } catch (Throwable var7) {
            return null;
         }
      }
   }

   private static Object reflectedField(Object var0, String var1) {
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

   private static double distanceSq(double var0, double var2, double var4, double var6, double var8, double var10) {
      double var12 = var0 - var6;
      double var14 = var2 - var8;
      double var16 = var4 - var10;
      return var12 * var12 + var14 * var14 + var16 * var16;
   }

   private static String fmt(double var0) {
      return String.format(Locale.ROOT, "%.1f", var0);
   }

   private static int random(int var0, int var1) {
      return ThreadLocalRandom.current().nextInt(var0, var1 + 1);
   }

   private static enum State {
      WAIT_STABLE,
      CHECK_LOCAL,
      FREE_SPACE,
      OPEN_ORDER,
      WAIT_ROOT,
      WAIT_YOUR,
      WAIT_EDIT,
      WAIT_COLLECT,
      VERIFY_COLLECT,
      MOVE_TO_HOTBAR,
      VERIFY_HOTBAR;
   }
}
