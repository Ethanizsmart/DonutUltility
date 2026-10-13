package dev.autoominous;

import java.lang.reflect.Method;
import java.util.ArrayList;

public final class FarmCycleRuntime {
   private static FarmCycleRuntime.Phase phase = FarmCycleRuntime.Phase.BOOTSTRAP;
   private static boolean initialized;
   private static boolean storeSawSword;
   private static boolean drinkSawOmi;
   private static boolean fireworkSawRocket;
   private static boolean sellStarted;
   private static long phaseSinceMs;
   private static long verifySinceMs;
   private static long lastSignature;
   private static int stableTicks;
   private static Object ominousBottle;
   private static Object fireworkRocket;
   private static Object[] swords;
   private static FarmCycleRuntime.Snapshot lastSnapshot;
   private static long lastAdvanceNs;
   private static String errorReason = "";
   private static boolean sessionLogged;

   private FarmCycleRuntime() {
   }

   public static void tick() {
      if (!G232SafeRuntime.isSwordRecoverySuspended()) {
         long var0 = System.nanoTime();
         if (lastAdvanceNs == 0L || var0 - lastAdvanceNs >= 35000000L) {
            lastAdvanceNs = var0;

            try {
               if (!farmCycleEnabled()) {
                  if (initialized) {
                     BehaviorLogRuntime.sessionEvent("farmCycle disabled -> soft reset");
                  }

                  resetSoft();
                  return;
               }

               Object var2 = ReflectionSupport.mc();
               if (var2 == null) {
                  BehaviorLogRuntime.warning("NO_MC", "MinecraftClient unavailable");
                  resetSoft();
                  return;
               }

               Object var3 = ReflectionSupport.field(var2, "field_1724");
               if (var3 == null) {
                  BehaviorLogRuntime.warning("NO_PLAYER", "player unavailable");
                  resetSoft();
                  return;
               }

               FarmCycleRuntime.Snapshot var4 = snapshot(var2, var3);
               lastSnapshot = var4;
               long var5 = System.currentTimeMillis();
               if (!sessionLogged) {
                  sessionLogged = true;
                  BehaviorLogRuntime.sessionEvent("FarmCycle G2.7 Safety+Trace active");
               }

               if (recoveryBusy()) {
                  BehaviorLogRuntime.poll(var4, phaseName(), phaseSinceMs, var5);
                  return;
               }

               if (!initialized) {
                  initialized = true;
                  phaseSinceMs = var5;
                  if (TransactionGate.isSeaSellActive()) {
                     sellStarted = true;
                     enter(FarmCycleRuntime.Phase.SELL, var5, "bootstrap: active SeaSell transaction");
                  } else if (var4.swordHotbar() && var4.sellable()) {
                     resetSettle(var4.signature());
                     enter(FarmCycleRuntime.Phase.LOOT_SETTLE, var5, "bootstrap: sword+sellable inventory");
                  } else if (var4.swordHotbar()) {
                     storeSawSword = true;
                     enter(FarmCycleRuntime.Phase.STORE_SWORD, var5, "bootstrap: sword ready to store");
                  } else if (var4.omiHotbar()) {
                     drinkSawOmi = true;
                     enter(FarmCycleRuntime.Phase.DRINK_OMI, var5, "bootstrap: OMI already in hotbar");
                  } else if (var4.fireworkHotbar()) {
                     fireworkSawRocket = true;
                     enter(FarmCycleRuntime.Phase.USE_FIREWORK, var5, "bootstrap: firework already in hotbar");
                  } else {
                     enter(FarmCycleRuntime.Phase.BOOTSTRAP, var5, "bootstrap: wait first sword/event");
                  }
               }

               switch (phase) {
                  case BOOTSTRAP:
                     tickBootstrap(var4, var5);
                     break;
                  case STORE_SWORD:
                     tickStoreSword(var4, var5, false);
                     break;
                  case WAIT_OMI:
                     tickWaitOmi(var4, var5);
                     break;
                  case DRINK_OMI:
                     tickDrinkOmi(var4, var5);
                     break;
                  case WAIT_FIREWORK:
                     tickWaitFirework(var4, var5);
                     break;
                  case USE_FIREWORK:
                     tickUseFirework(var4, var5);
                     break;
                  case MACHINE_RUNNING:
                     tickMachineRunning(var4, var5);
                     break;
                  case WAIT_RETURN:
                     tickWaitReturn(var4, var5);
                     break;
                  case LOOT_SETTLE:
                     tickLootSettle(var4, var5);
                     break;
                  case SELL:
                     tickSell(var4, var5);
                     break;
                  case VERIFY_SWORD:
                     tickVerifySword(var4, var5);
                     break;
                  case STORE_RETURNED_SWORD:
                     tickStoreSword(var4, var5, true);
                     break;
                  case ERROR:
                     tickError(var4, var5);
               }

               BehaviorLogRuntime.poll(var4, phaseName(), phaseSinceMs, var5);
            } catch (Throwable var7) {
               errorReason = "EXCEPTION:" + var7.getClass().getSimpleName();
               BehaviorLogRuntime.exception("FarmCycleRuntime.tick", var7);
            }
         }
      }
   }

   private static void tickBootstrap(FarmCycleRuntime.Snapshot var0, long var1) {
      if (TransactionGate.isSeaSellActive()) {
         sellStarted = true;
         enter(FarmCycleRuntime.Phase.SELL, var1, "SeaSell became active while waiting");
      } else if (var0.swordHotbar() && var0.sellable()) {
         resetSettle(var0.signature());
         enter(FarmCycleRuntime.Phase.LOOT_SETTLE, var1, "returned sword + sellable loot observed");
      } else if (var0.swordHotbar()) {
         storeSawSword = true;
         enter(FarmCycleRuntime.Phase.STORE_SWORD, var1, "first sword observed in hotbar");
      } else if (var0.omiHotbar()) {
         drinkSawOmi = true;
         enter(FarmCycleRuntime.Phase.DRINK_OMI, var1, "OMI observed before first sword (resume mid-cycle)");
      } else {
         if (var0.fireworkHotbar()) {
            fireworkSawRocket = true;
            enter(FarmCycleRuntime.Phase.USE_FIREWORK, var1, "firework observed before first sword (resume mid-cycle)");
         }
      }
   }

   private static void tickStoreSword(FarmCycleRuntime.Snapshot var0, long var1, boolean var3) {
      if (G232SafeRuntime.swordHotbarOrPending(var0)) {
         storeSawSword = true;
      }

      if (storeSawSword && !G232SafeRuntime.swordHotbarOrPending(var0)) {
         storeSawSword = false;
         errorReason = "";
         enter(FarmCycleRuntime.Phase.WAIT_OMI, var1, var3 ? "returned sword left hotbar -> next cycle" : "sword left hotbar -> stored in pot");
      } else {
         if (!G232SafeRuntime.swordHotbarOrPending(var0) && var0.swordAny()) {
            BehaviorLogRuntime.warning("SWORD_NOT_HOTBAR", "phase=" + phaseName() + " sword exists outside hotbar; RMB cannot confirm pot-store");
         }
      }
   }

   private static void tickWaitOmi(FarmCycleRuntime.Snapshot var0, long var1) {
      ConsumableRecoveryRuntime.resetOmiBaseline();
      if (var0.omiHotbar()) {
         drinkSawOmi = true;
         ConsumableRecoveryRuntime.armOmiBaseline();
         enter(FarmCycleRuntime.Phase.DRINK_OMI, var1, "OMI appeared in hotbar; count baseline armed");
      } else if (!var0.omiAny()) {
         // Ominous không có trong hotbar lẫn túi chính — đã được dùng trước phase này
         // (uống sớm khi kiếm đang cất) hoặc không có sẵn. Bỏ qua sang WAIT_FIREWORK.
         drinkSawOmi = false;
         enter(FarmCycleRuntime.Phase.WAIT_FIREWORK, var1, "OMI absent from inventory; consumed early or not present; skipping to firework phase");
      } else {
         BehaviorLogRuntime.warning("OMI_NOT_HOTBAR", "OMI exists in inventory; auto-promote to hotbar requested");
         if (var0.swordHotbar()) {
            enterReturnPath(var0, var1, "sword returned while waiting OMI");
         }
      }
   }

   private static void tickDrinkOmi(FarmCycleRuntime.Snapshot var0, long var1) {
      if (ConsumableRecoveryRuntime.omiConsumedSinceBaseline()) {
         drinkSawOmi = false;
         enter(FarmCycleRuntime.Phase.WAIT_FIREWORK, var1, "OMI count decreased by 1 -> drink completed");
      }
   }

   private static void tickWaitFirework(FarmCycleRuntime.Snapshot var0, long var1) {
      ConsumableRecoveryRuntime.resetFireworkBaseline();
      if (var0.fireworkHotbar()) {
         fireworkSawRocket = true;
         ConsumableRecoveryRuntime.armFireworkBaseline();
         enter(FarmCycleRuntime.Phase.USE_FIREWORK, var1, "firework appeared in hotbar; count baseline armed");
      } else if (!var0.fireworkAny()) {
         // Pháo hoa không có trong hotbar lẫn túi chính — đã được dùng trước phase này
         // hoặc không có sẵn. Bỏ qua sang MACHINE_RUNNING.
         fireworkSawRocket = false;
         enter(FarmCycleRuntime.Phase.MACHINE_RUNNING, var1, "firework absent from inventory; consumed early or not present; skipping to machine phase");
      } else {
         BehaviorLogRuntime.warning("FIREWORK_NOT_HOTBAR", "firework exists in inventory; auto-promote to hotbar requested");
         if (var0.swordHotbar()) {
            enterReturnPath(var0, var1, "sword returned before firework phase completed");
         }
      }
   }


   private static void tickUseFirework(FarmCycleRuntime.Snapshot var0, long var1) {
      if (ConsumableRecoveryRuntime.fireworkConsumedSinceBaseline()) {
         fireworkSawRocket = false;
         enter(FarmCycleRuntime.Phase.MACHINE_RUNNING, var1, "firework count decreased by 1 -> use completed");
      }
   }

   private static void tickMachineRunning(FarmCycleRuntime.Snapshot var0, long var1) {
      if (var0.swordHotbar()) {
         enterReturnPath(var0, var1, "machine returned sword");
      }
   }

   private static void tickWaitReturn(FarmCycleRuntime.Snapshot var0, long var1) {
      if (!var0.swordHotbar()) {
         if (var0.swordAny()) {
            BehaviorLogRuntime.warning("RETURNED_SWORD_NOT_HOTBAR", "returned sword exists outside hotbar; waiting safely, RMB cannot arm");
         } else {
            storeSawSword = false;
            errorReason = "";
            enter(FarmCycleRuntime.Phase.WAIT_OMI, var1, "returned sword left inventory while RMB armed -> pot accepted; next cycle");
         }
      } else {
         if (var0.sellable()) {
            resetSettle(var0.signature());
            enter(FarmCycleRuntime.Phase.LOOT_SETTLE, var1, "sellable loot observed while returned sword is armed");
         }
      }
   }

   private static void tickLootSettle(FarmCycleRuntime.Snapshot var0, long var1) {
      if (!var0.swordHotbar()) {
         enter(FarmCycleRuntime.Phase.WAIT_RETURN, var1, "sword not in hotbar during loot settle");
      } else if (!var0.sellable()) {
         enter(FarmCycleRuntime.Phase.WAIT_RETURN, var1, "sellable loot disappeared before sell start");
      } else {
         if (var0.signature() == lastSignature) {
            stableTicks++;
         } else {
            lastSignature = var0.signature();
            stableTicks = 0;
            BehaviorLogRuntime.warning("LOOT_CHANGED", "settle counter reset signature=" + var0.signature());
         }

         if (stableTicks >= 5) {
            sellStarted = false;
            enter(FarmCycleRuntime.Phase.SELL, var1, "inventory stable for 5 real client ticks");
         }
      }
   }

   private static void tickSell(FarmCycleRuntime.Snapshot var0, long var1) {
      boolean var3 = TransactionGate.isSeaSellActive();
      if (var3) {
         sellStarted = true;
      }

      if (!var3 && !var0.sellable() && (sellStarted || var1 - phaseSinceMs >= 1000L)) {
         verifySinceMs = var1;
         enter(FarmCycleRuntime.Phase.VERIFY_SWORD, var1, sellStarted ? "SeaSell finished; verify sword" : "nothing sellable after entering SELL; verify sword");
      }
   }

   private static void tickVerifySword(FarmCycleRuntime.Snapshot var0, long var1) {
      if (var0.swordHotbar()) {
         storeSawSword = true;
         errorReason = "";
         enter(FarmCycleRuntime.Phase.STORE_RETURNED_SWORD, var1, "post-sell sword present in hotbar");
      } else {
         if (var0.swordAny()) {
            BehaviorLogRuntime.warning("POST_SELL_SWORD_NOT_HOTBAR", "sword exists outside hotbar; waiting up to approved 2s timeout");
         }

         if (var1 - verifySinceMs >= 2000L) {
            errorReason = var0.swordAny() ? "SWORD_NOT_HOTBAR_AFTER_SELL >2s" : "SWORD_MISSING_AFTER_SELL >2s";
            BehaviorLogRuntime.error("POST_SELL_SWORD_TIMEOUT", errorReason);
            enter(FarmCycleRuntime.Phase.ERROR, var1, errorReason);
         }
      }
   }

   private static void tickError(FarmCycleRuntime.Snapshot var0, long var1) {
      if (var0.swordHotbar()) {
         storeSawSword = true;
         String var3 = errorReason;
         errorReason = "";
         enter(FarmCycleRuntime.Phase.STORE_RETURNED_SWORD, var1, "self-recover: sword returned after error " + var3);
      }
   }

   private static void enterReturnPath(FarmCycleRuntime.Snapshot var0, long var1, String var3) {
      if (var0.sellable()) {
         resetSettle(var0.signature());
         enter(FarmCycleRuntime.Phase.LOOT_SETTLE, var1, var3 + "; sellable loot already present");
      } else {
         enter(FarmCycleRuntime.Phase.WAIT_RETURN, var1, var3 + "; wait loot");
      }
   }

   public static boolean shouldEnableSeaAutoSell() {
      if (G232SafeRuntime.isSwordRecoverySuspended()) {
         return false;
      } else {
         tick();
         if (!farmCycleEnabled()) {
            return true;
         } else {
            return recoveryBusy() ? false : phase == FarmCycleRuntime.Phase.SELL;
         }
      }
   }

   public static int priorityForOminous() {
      tick();
      if (!farmCycleEnabled()) {
         return 3;
      }

      if (recoveryBusy()) {
         return 0;
      }

      if (phase == FarmCycleRuntime.Phase.WAIT_OMI || phase == FarmCycleRuntime.Phase.DRINK_OMI) {
         FarmCycleRuntime.Snapshot var0 = lastSnapshot;
         return var0 != null && !var0.omiHotbar() ? 0 : 3;
      }

      // Khi kiếm đang chờ được cất (STORE_SWORD / STORE_RETURNED_SWORD) mà ominous đã có
      // trong hotbar, ưu tiên uống ominous trước (priority 3 > sword 1).
      if (phase == FarmCycleRuntime.Phase.STORE_SWORD || phase == FarmCycleRuntime.Phase.STORE_RETURNED_SWORD) {
         FarmCycleRuntime.Snapshot var0 = lastSnapshot;
         return var0 != null && var0.omiHotbar() ? 3 : 0;
      }

      return 0;
   }

   public static int priorityForFirework() {
      tick();
      if (!farmCycleEnabled()) {
         return 2;
      }

      if (recoveryBusy()) {
         return 0;
      }

      if (phase == FarmCycleRuntime.Phase.WAIT_FIREWORK || phase == FarmCycleRuntime.Phase.USE_FIREWORK) {
         FarmCycleRuntime.Snapshot var0 = lastSnapshot;
         return var0 != null && !var0.fireworkHotbar() ? 0 : 2;
      }

      // Cho pháo hoa priority 2 khi kiếm đang chờ cất và pháo hoa đã có trong hotbar,
      // nhưng chỉ khi không có ominous (ominous ưu tiên cao hơn).
      if (phase == FarmCycleRuntime.Phase.STORE_SWORD || phase == FarmCycleRuntime.Phase.STORE_RETURNED_SWORD) {
         FarmCycleRuntime.Snapshot var0 = lastSnapshot;
         return var0 != null && var0.fireworkHotbar() && !var0.omiHotbar() ? 2 : 0;
      }

      return 0;
   }

   public static int priorityForSword() {
      tick();
      if (!farmCycleEnabled()) {
         return 1;
      }

      if (recoveryBusy()) {
         return 0;
      }

      if (phase != FarmCycleRuntime.Phase.STORE_SWORD
         && phase != FarmCycleRuntime.Phase.STORE_RETURNED_SWORD
         && phase != FarmCycleRuntime.Phase.MACHINE_RUNNING
         && phase != FarmCycleRuntime.Phase.WAIT_RETURN) {
         return 0;
      }

      FarmCycleRuntime.Snapshot var0 = lastSnapshot;
      if (var0 == null || !var0.swordHotbar()) {
         return 0;
      }

      // Nhường ưu tiên cho ominous (3) và pháo hoa (2) nếu chúng đang có trong hotbar.
      // Giữ thứ tự: ominous → pháo hoa → kiếm.
      if (var0.omiHotbar() || var0.fireworkHotbar()) {
         return 0;
      }

      return 1;
   }

   public static String phaseName() {
      return phase == FarmCycleRuntime.Phase.BOOTSTRAP ? "WAIT_FIRST_SWORD" : phase.name();
   }

   public static String errorReason() {
      return errorReason == null ? "" : errorReason;
   }

   public static String behaviorLogPath() {
      return BehaviorLogRuntime.filePath();
   }

   public static boolean isCycleBlockingAuxiliaryActions() {
      if (!farmCycleEnabled()) {
         return false;
      }

      return switch (phase) {
         case BOOTSTRAP, STORE_SWORD, WAIT_OMI, DRINK_OMI, WAIT_FIREWORK, USE_FIREWORK, WAIT_RETURN, LOOT_SETTLE, SELL, VERIFY_SWORD, STORE_RETURNED_SWORD, ERROR -> true;
         case MACHINE_RUNNING -> false;
      };
   }

   private static void enter(FarmCycleRuntime.Phase var0, long var1, String var3) {
      if (phase != var0) {
         FarmCycleRuntime.Phase var4 = phase;
         phase = var0;
         phaseSinceMs = var1;
         stableTicks = var0 == FarmCycleRuntime.Phase.LOOT_SETTLE ? stableTicks : 0;
         BehaviorLogRuntime.phase(display(var4), display(var0), var3);
      }
   }

   private static String display(FarmCycleRuntime.Phase var0) {
      return var0 == FarmCycleRuntime.Phase.BOOTSTRAP ? "WAIT_FIRST_SWORD" : var0.name();
   }

   private static void resetSettle(long var0) {
      lastSignature = var0;
      stableTicks = 0;
   }

   private static void resetSoft() {
      initialized = false;
      phase = FarmCycleRuntime.Phase.BOOTSTRAP;
      storeSawSword = false;
      drinkSawOmi = false;
      fireworkSawRocket = false;
      sellStarted = false;
      stableTicks = 0;
      lastSignature = 0L;
      lastSnapshot = null;
      errorReason = "";
      phaseSinceMs = System.currentTimeMillis();
   }

   private static boolean farmCycleEnabled() {
      boolean var0 = ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false);
      boolean var1 = ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false);
      return var0 && var1;
   }

   private static boolean recoveryBusy() {
      if (ReflectionSupport.callStaticBoolean("dev.autoominous.SwordMissingWatchdog", "isRecoveryBlockingAutoSell", false)) {
         return true;
      } else if (ReflectionSupport.callStaticBoolean("dev.autoominous.HomePositionGuardRuntime", "isAutoUseBlocked", false)) {
         return true;
      } else {
         return ReflectionSupport.callStaticBoolean("dev.autoominous.BountyRuntime", "isBlockingSell", false)
            ? true
            : ReflectionSupport.staticBoolean("dev.autoominous.AutoTpaRuntime", "awaitingConfirm", false);
      }
   }

   private static FarmCycleRuntime.Snapshot snapshot(Object var0, Object var1) throws Exception {
      ensureItems();
      Object var2 = ReflectionSupport.callNoArg(var1, "method_31548");
      boolean var3 = false;
      boolean var4 = false;
      boolean var5 = false;
      boolean var6 = false;
      boolean var7 = false;
      boolean var8 = false;
      long var9 = 1469598103934665603L;
      ArrayList var11 = new ArrayList();
      if (var2 != null) {
         for (int var12 = 0; var12 < 36; var12++) {
            Object var13 = invokeOneInt(var2, "method_5438", var12);
            if (var13 != null && !ReflectionSupport.callBool(var13, "method_7960", false)) {
               Object var14 = ReflectionSupport.callNoArg(var13, "method_7909");
               boolean var15 = var12 < 9;
               boolean var16 = isSword(var14);
               boolean var17 = var14 == ominousBottle || var14 != null && "net.minecraft.class_9462".equals(var14.getClass().getName());
               boolean var18 = var14 == fireworkRocket;
               if (var16) {
                  var4 = true;
                  if (var15) {
                     var3 = true;
                  }
               }

               if (var17) {
                  var6 = true;
                  if (var15) {
                     var5 = true;
                  }
               }

               if (var18) {
                  var8 = true;
                  if (var15) {
                     var7 = true;
                  }
               }

               String var19 = String.valueOf(var13);
               var11.add(var12 + ":" + var19);
               var9 ^= var19.hashCode() + var12 * 2654435769L;
               var9 *= 1099511628211L;
            }
         }
      }

      boolean var21 = hasSellable(var0);
      String var22 = String.join("|", var11);
      if (var22.length() > 1800) {
         var22 = var22.substring(0, 1800) + "...";
      }

      return new FarmCycleRuntime.Snapshot(var3, var4, var5, var6, var7, var8, var21, var9, var22);
   }

   private static boolean hasSellable(Object var0) {
      try {
         Class var1 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");

         for (Method var5 : var1.getDeclaredMethods()) {
            if (var5.getName().equals("hasInventoryItems") && var5.getParameterCount() == 1) {
               var5.setAccessible(true);
               Object var6 = var5.invoke(null, var0);
               return var6 instanceof Boolean && (Boolean)var6;
            }
         }
      } catch (Throwable var7) {
         BehaviorLogRuntime.exception("FarmCycleRuntime.hasSellable", var7);
      }

      return false;
   }

   private static Object invokeOneInt(Object var0, String var1, int var2) throws Exception {
      Method var3 = var0.getClass().getMethod(var1, int.class);
      return var3.invoke(var0, var2);
   }

   private static void ensureItems() {
      if (swords == null) {
         ominousBottle = ReflectionSupport.staticField("net.minecraft.class_1802", "field_50140");
         fireworkRocket = ReflectionSupport.staticField("net.minecraft.class_1802", "field_8639");
         swords = new Object[]{
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8091"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_61338"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8528"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8845"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8371"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8802"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_22022")
         };
      }
   }

   private static boolean isSword(Object var0) {
      if (var0 != null && swords != null) {
         for (Object var4 : swords) {
            if (var4 != null && var0 == var4) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   enum Phase {
      BOOTSTRAP,
      STORE_SWORD,
      WAIT_OMI,
      DRINK_OMI,
      WAIT_FIREWORK,
      USE_FIREWORK,
      MACHINE_RUNNING,
      WAIT_RETURN,
      LOOT_SETTLE,
      SELL,
      VERIFY_SWORD,
      STORE_RETURNED_SWORD,
      ERROR;
   }

   record Snapshot(
      boolean swordHotbar,
      boolean swordAny,
      boolean omiHotbar,
      boolean omiAny,
      boolean fireworkHotbar,
      boolean fireworkAny,
      boolean sellable,
      long signature,
      String inventorySummary
   ) {
   }
}
