package com.seastore.auto.seaats.client;

import dev.autoominous.FarmCycleRuntime;
import dev.autoominous.HumanBehaviorProfile;
import dev.autoominous.OminousSurplusSellPolicy;
import dev.autoominous.SlashSellSettings;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_465;

public final class HumanizedSellRuntime {
   private static final int MAX_SELL_SLOTS = 45;
   private static final long NS_PER_MS = 1000000L;
   private static class_1703 handler;
   private static long sessionStartNanos;
   private static long softDeadlineNanos;
   private static long hardDeadlineNanos;
   private static long confirmWaitStartedNanos;
   private static int phase;
   private static long phaseStartedNanos;
   private static int phaseRetries;
   private static int sourceSlot = -1;
   private static int destSlot = -1;
   private static class_1799 sourceSignature;
   private static class_1799 duplicateSignature;
   private static int duplicateBurstRemaining;
   private static int quickSourceSlot = -1;
   private static int completedSincePause;
   private static boolean hardDeadlineSwordHold;

   private HumanizedSellRuntime() {
   }

   public static int afterClickSellDelayTicks() {
      return isV2() ? random(3, 8) : random(10, 20);
   }

   public static void tickMoving(class_310 var0) {
      BulkSellRuntime.tickMoving(var0);
   }

   private static void handlePickupIssued(class_310 var0, class_1703 var1) {
      class_1799 var2 = var1.method_34255();
      if (var2 != null && !var2.method_7960()) {
         int var3 = destSlot;
         if (var3 < 0 || !isSlotEmpty(var1, var3) || isConfirmSlot(var1, var3)) {
            var3 = findEmptySellSlot(var1);
         }

         if (var3 < 0) {
            returnHeldStackToSourceIfPossible(var0, var1);
            resetTransferOnly();
            scheduleConfirm();
         } else {
            destSlot = var3;
            var0.field_1761.method_2906(var1.field_7763, var3, 0, class_1713.field_7790, var0.field_1724);
            phase = 2;
            phaseStartedNanos = System.nanoTime();
            phaseRetries = 0;
            AutoSellLogic.tickDelay = delayAfterPlaceForVerification();
         }
      } else if (System.nanoTime() - phaseStartedNanos < 900000000L) {
         AutoSellLogic.tickDelay = random(2, 4);
      } else {
         resetTransferOnly();
         AutoSellLogic.tickDelay = random(3, 7);
      }
   }

   private static void handlePlaceIssued(class_310 var0, class_1703 var1) {
      class_1799 var2 = var1.method_34255();
      if (var2 != null && !var2.method_7960()) {
         if (++phaseRetries <= 3) {
            int var4 = destSlot;
            if (var4 < 0 || !isSlotEmpty(var1, var4) || isConfirmSlot(var1, var4)) {
               var4 = findEmptySellSlot(var1);
            }

            if (var4 >= 0) {
               destSlot = var4;
               var0.field_1761.method_2906(var1.field_7763, var4, 0, class_1713.field_7790, var0.field_1724);
               phaseStartedNanos = System.nanoTime();
               AutoSellLogic.tickDelay = random(3, 7);
               return;
            }
         }

         returnHeldStackToSourceIfPossible(var0, var1);
         duplicateBurstRemaining = 0;
         duplicateSignature = null;
         resetTransferOnly();
         if (findEmptySellSlot(var1) < 0 && hasBatchContents(var1)) {
            scheduleConfirm();
         } else {
            AutoSellLogic.tickDelay = random(4, 8);
         }
      } else {
         duplicateSignature = sourceSignature;
         duplicateBurstRemaining = isV2() ? random(2, 4) : random(1, 3);
         duplicateBurstRemaining = adjustDuplicateBurst(duplicateBurstRemaining);
         completedSincePause++;
         resetTransferOnlyKeepDuplicate();
         int var3 = delayAfterCompletedStack();
         AutoSellLogic.tickDelay = var3;
      }
   }

   private static void handleQuickMoveIssued(class_310 var0, class_1703 var1) {
      class_1799 var2 = quickSourceSlot >= 0 && quickSourceSlot < var1.field_7761.size() ? var1.method_7611(quickSourceSlot).method_7677() : null;
      boolean var3 = var2 == null || var2.method_7960() || sourceSignature == null || !class_1799.method_31577(var2, sourceSignature);
      if (var3) {
         duplicateBurstRemaining = Math.max(0, duplicateBurstRemaining - 1);
         completedSincePause++;
         phase = 0;
         phaseRetries = 0;
         quickSourceSlot = -1;
         sourceSignature = null;
         int var4 = isV2() ? random(2, 4) : random(3, 6);
         if (duplicateBurstRemaining == 0 || isV2() && ThreadLocalRandom.current().nextInt(100) < 18) {
            var4 += random(4, 9);
         }

         AutoSellLogic.tickDelay = var4;
      } else {
         if (++phaseRetries < 2 && System.nanoTime() - phaseStartedNanos <= 700000000L) {
            AutoSellLogic.tickDelay = random(2, 3);
         } else {
            duplicateBurstRemaining = 0;
            duplicateSignature = null;
            phase = 0;
            quickSourceSlot = -1;
            sourceSignature = null;
            AutoSellLogic.tickDelay = random(4, 8);
         }
      }
   }

   public static void tickAfterConfirm(class_310 var0) {
      if (var0 != null && var0.field_1724 != null && AutoSellLogic.isSellGuiOpen(var0)) {
         class_1703 var1 = ((class_465)var0.field_1755).method_17577();
         ensureSession(var1);
         long var2 = System.nanoTime();
         if (hasBatchContents(var1)) {
            if (confirmWaitStartedNanos == 0L) {
               confirmWaitStartedNanos = var2;
            }

            if (var2 < hardDeadlineNanos && var2 - confirmWaitStartedNanos < 3000000000L) {
               AutoSellLogic.currentState = AutoSellLogic.State.CLICK_CONFIRM_DELAYED;
               AutoSellLogic.tickDelay = random(2, 5);
            } else {
               closeAndIdle(var0);
            }
         } else {
            confirmWaitStartedNanos = 0L;
            boolean var4 = AutoSellLogic.hasInventoryItems(var0);
            if (var4 && var2 < softDeadlineNanos) {
               AutoSellLogic.currentState = AutoSellLogic.State.MOVING_ITEMS;
               AutoSellLogic.tickDelay = isV2() ? random(3, 8) : random(4, 8);
            } else {
               closeAndIdle(var0);
            }
         }
      } else {
         clearSession();
         AutoSellLogic.currentState = AutoSellLogic.State.IDLE;
         AutoSellLogic.tickDelay = random(6, 12);
      }
   }

   public static void forceReset() {
      class_310 var0 = class_310.method_1551();
      if (var0 != null && var0.field_1724 != null && var0.field_1761 != null && handler != null) {
         try {
            class_1799 var1 = handler.method_34255();
            if (var1 != null && !var1.method_7960()) {
               returnHeldStackToSourceIfPossible(var0, handler);
            }
         } catch (RuntimeException var2) {
         }
      }

      clearSession();
   }

   private static void ensureSession(class_1703 var0) {
      if (sessionStartNanos == 0L) {
         sessionStartNanos = System.nanoTime();
         long var1 = isV2() ? random(19000, 21000) : random(25000, 27000);
         softDeadlineNanos = sessionStartNanos + var1 * 1000000L;
         hardDeadlineNanos = sessionStartNanos + (isV2() ? 22000L : 29000L) * 1000000L;
         completedSincePause = 0;
      }

      if (handler != var0) {
         handler = var0;
         OminousSurplusSellPolicy.prepareSellGui(class_310.method_1551(), var0);
         phase = 0;
         phaseRetries = 0;
         quickSourceSlot = -1;
         destSlot = -1;
         sourceSlot = -1;
         sourceSignature = null;
      }
   }

   private static int findNextSellableInventorySlot(class_1703 var0) {
      return SellRhythmRuntime.selectSlot(var0);
   }

   private static int findDuplicateInventorySlot(class_1703 var0, class_1799 var1) {
      if (var1 == null) {
         return -1;
      }

      int var2 = var0.field_7761.size();
      int var3 = Math.max(0, var2 - 36);

      for (int var4 = var3; var4 < var2; var4++) {
         class_1799 var5 = var0.method_7611(var4).method_7677();
         if (!var5.method_7960() && !AutoSellLogic.isProtectedNeverSellItem(var5) && class_1799.method_31577(var5, var1)) {
            return var4;
         }
      }

      return -1;
   }

   private static boolean isSellable(class_1703 var0, int var1) {
      class_1799 var2 = var0.method_7611(var1).method_7677();
      return !var2.method_7960() && !OminousSurplusSellPolicy.isProtectedForTransfer(var2);
   }

   private static int findEmptySellSlot(class_1703 var0) {
      int var1 = var0.field_7761.size();
      int var2 = Math.max(0, var1 - 36);
      int var3 = Math.min(45, var2);

      for (int var4 = 0; var4 < var3; var4++) {
         if (isSlotEmpty(var0, var4) && !isConfirmSlot(var0, var4)) {
            return var4;
         }
      }

      return -1;
   }

   private static boolean hasBatchContents(class_1703 var0) {
      int var1 = var0.field_7761.size();
      int var2 = Math.max(0, var1 - 36);
      int var3 = Math.min(45, var2);

      for (int var4 = 0; var4 < var3; var4++) {
         if (!isSlotEmpty(var0, var4) && !isConfirmSlot(var0, var4)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isConfirmSlot(class_1703 var0, int var1) {
      try {
         if (var0 != null && var1 >= 0 && var1 < var0.field_7761.size()) {
            class_1799 var2 = var0.method_7611(var1).method_7677();
            if (var2 != null && !var2.method_7960() && var2.method_31574(class_1802.field_8581)) {
               class_2561 var3 = var2.method_7964();
               String var4 = var3 == null ? "" : var3.method_10858(128);
               String var5 = var4 == null ? "" : var4.trim().toLowerCase(Locale.ROOT);
               return var1 >= 45 || var5.equals("sell") || var5.startsWith("sell ") || var5.contains("sell items") || var5.contains("click to sell");
            } else {
               return false;
            }
         } else {
            return false;
         }
      } catch (Throwable var6) {
         return false;
      }
   }

   private static boolean isSlotEmpty(class_1703 var0, int var1) {
      return var0.method_7611(var1).method_7677().method_7960();
   }

   private static void scheduleConfirm() {
      duplicateBurstRemaining = 0;
      duplicateSignature = null;
      AutoSellLogic.currentState = AutoSellLogic.State.CLICK_CONFIRM;
      AutoSellLogic.tickDelay = isV2() ? random(4, 12) : random(3, 6);
   }

   private static void returnHeldStackToSourceIfPossible(class_310 var0, class_1703 var1) {
      try {
         class_1799 var2 = var1.method_34255();
         if (var2 == null || var2.method_7960()) {
            return;
         }

         if (sourceSlot >= 0 && sourceSlot < var1.field_7761.size() && isSlotEmpty(var1, sourceSlot)) {
            var0.field_1761.method_2906(var1.field_7763, sourceSlot, 0, class_1713.field_7790, var0.field_1724);
         }
      } catch (Throwable var3) {
      }
   }

   private static void closeAndIdle(class_310 var0) {
      latchHardDeadlineIfReached();
      AutoSellLogic.closeAllGui(var0);
      clearSession();
      AutoSellLogic.currentState = AutoSellLogic.State.IDLE;
      AutoSellLogic.tickDelay = isV2() ? random(8, 24) : random(6, 14);
   }

   private static void resetTransferOnly() {
      phase = 0;
      phaseRetries = 0;
      quickSourceSlot = -1;
      destSlot = -1;
      sourceSlot = -1;
      sourceSignature = null;
   }

   private static void resetTransferOnlyKeepDuplicate() {
      phase = 0;
      phaseRetries = 0;
      quickSourceSlot = -1;
      destSlot = -1;
      sourceSlot = -1;
      sourceSignature = null;
   }

   private static void clearSession() {
      handler = null;
      confirmWaitStartedNanos = 0L;
      hardDeadlineNanos = 0L;
      softDeadlineNanos = 0L;
      sessionStartNanos = 0L;
      phase = 0;
      phaseRetries = 0;
      phaseStartedNanos = 0L;
      quickSourceSlot = -1;
      destSlot = -1;
      sourceSlot = -1;
      duplicateSignature = null;
      sourceSignature = null;
      duplicateBurstRemaining = 0;
      completedSincePause = 0;
      SellRhythmRuntime.reset();
   }

   private static boolean isV2() {
      try {
         return SlashSellSettings.isHumanizedSellV2Enabled();
      } catch (Throwable var1) {
         return true;
      }
   }

   private static int random(int var0, int var1) {
      return HumanBehaviorProfile.sellRandom(var0, var1);
   }

   public static boolean isHardDeadlineSwordHold() {
      return false;
   }

   public static void clearHardDeadlineSwordHold() {
      hardDeadlineSwordHold = false;
   }

   public static boolean shouldEnableSeaAutoSell() {
      return FarmCycleRuntime.shouldEnableSeaAutoSell();
   }

   public static int priorityForOminous() {
      return FarmCycleRuntime.priorityForOminous();
   }

   public static int priorityForFirework() {
      return FarmCycleRuntime.priorityForFirework();
   }

   public static int priorityForSword() {
      return FarmCycleRuntime.priorityForSword();
   }

   private static boolean isBulkSameItemTransfer() {
      return SlashSellSettings.isBulkSameItemTransferEnabled();
   }

   private static int adjustDuplicateBurst(int var0) {
      return var0 * (1 - (isBulkSameItemTransfer() ? 1 : 0)) + 999 * (isBulkSameItemTransfer() ? 1 : 0);
   }

   private static void latchHardDeadlineIfReached() {
      hardDeadlineSwordHold = ((((Long.compare(hardDeadlineNanos, 0L) + 1) >>> 1)
            & ((Long.compare(System.nanoTime(), hardDeadlineNanos) >>> 31) ^ 1))
         | (hardDeadlineSwordHold ? 1 : 0)) != 0;
   }

   private static int delayBetweenPickupAndPlace() {
      if (!isV2()) {
         return random(3, 6);
      } else {
         int var0 = random(0, 99);
         if (var0 < 65) {
            return random(4, 6);
         } else {
            return var0 < 92 ? random(6, 9) : random(9, 13);
         }
      }
   }

   private static int delayAfterPlaceForVerification() {
      return isV2() ? random(3, 5) : random(3, 6);
   }

   private static int delayAfterCompletedStack() {
      return SellRhythmRuntime.nextDelayTicks();
   }
}
