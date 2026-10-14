package com.seastore.auto.seaats.client;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_465;

/** CoreV2 transfer rhythm, under the existing /sell and recovery coordinator. */
public final class HumanizedSellRuntime {
   private static class_1703 handler;
   private static int syncId;
   private static long softDeadlineNanos;
   private static long hardDeadlineNanos;
   private static long confirmWaitStartedNanos;
   private static int splitSource = -1;
   private static class_1799 splitSignature;

   private HumanizedSellRuntime() {}
   public static int afterClickSellDelayTicks() { return 6; }

   public static void tickMoving(class_310 client) {
      if (!ensureSession(client)) return;
      if (!cursorEmpty()) {
         returnSplitCursor(client);
         AutoSellLogic.tickDelay = 3;
         if (System.nanoTime() >= hardDeadlineNanos) closeAndIdle(client);
         return;
      }
      if (System.nanoTime() >= softDeadlineNanos) {
         if (filledSellSlots() > 0) scheduleConfirm(2, 4);
         else closeAndIdle(client);
         return;
      }
      if (stageOminousSurplus(client)) {
         AutoSellLogic.tickDelay = random(3, 6);
         return;
      }
      int free = sellCapacity() - filledSellSlots();
      if (free <= 0) {
         scheduleConfirm(2, 4);
         return;
      }
      int source = SellRhythmRuntime.selectSlot(client, handler);
      if (source < 0) {
         if (filledSellSlots() > 0) scheduleConfirm(4, 8);
         else closeAndIdle(client);
         return;
      }
      class_1799 signature = handler.method_7611(source).method_7677().method_7972();
      int matching = 0;
      for (int i = playerStart(); i < handler.field_7761.size(); i++) {
         if (matchesSellable(client, i, signature)) matching++;
      }
      int moved = 0;
      if (free > 1 && matching >= 2) {
         // Vanilla QUICK_MOVE predicts each click locally; re-read after the group pause.
         for (int i = playerStart(); i < handler.field_7761.size() && moved < free; i++) {
            if (!sameSession(client) || !cursorEmpty()) break;
            if (matchesSellable(client, i, signature)) {
               click(client, i, 0, class_1713.field_7794);
               moved++;
            }
         }
      } else if (matchesSellable(client, source, signature)) {
         click(client, source, 0, class_1713.field_7794);
         moved = 1;
      }
      AutoSellLogic.tickDelay = moved >= 2 ? random(3, 6) : SellRhythmRuntime.nextDelayTicks();
   }

   public static boolean tickConfirm(class_310 client) {
      if (!ensureSession(client)) return false;
      if (confirmWaitStartedNanos != 0L) {
         AutoSellLogic.currentState = AutoSellLogic.State.CLICK_CONFIRM_DELAYED;
         AutoSellLogic.tickDelay = random(2, 5);
         return false;
      }
      if (!cursorEmpty()) {
         returnSplitCursor(client);
         AutoSellLogic.tickDelay = 3;
         if (System.nanoTime() >= hardDeadlineNanos) closeAndIdle(client);
         return false;
      }
      if (filledSellSlots() == 0) {
         AutoSellLogic.currentState = AutoSellLogic.State.MOVING_ITEMS;
         AutoSellLogic.tickDelay = random(4, 8);
         return false;
      }
      int confirm = findConfirmSlot(handler);
      if (confirm < 0) {
         closeAndIdle(client);
         return false;
      }
      confirmWaitStartedNanos = System.nanoTime();
      click(client, confirm, 0, class_1713.field_7790);
      AutoSellLogic.currentState = AutoSellLogic.State.CLICK_CONFIRM_DELAYED;
      AutoSellLogic.tickDelay = afterClickSellDelayTicks();
      return true;
   }

   public static void tickAfterConfirm(class_310 client) {
      if (!ensureSession(client)) return;
      long now = System.nanoTime();
      if (!cursorEmpty()) {
         returnSplitCursor(client);
         AutoSellLogic.tickDelay = 3;
         if (now >= hardDeadlineNanos) closeAndIdle(client);
      } else if (filledSellSlots() > 0) {
         // A sent confirm is not a receipt. Never click again while awaiting this batch.
         if (now < hardDeadlineNanos && now - confirmWaitStartedNanos < 3_000_000_000L) {
            AutoSellLogic.tickDelay = random(2, 5);
         } else closeAndIdle(client);
      } else if (AutoSellLogic.hasInventoryItems(client) && now < softDeadlineNanos) {
         confirmWaitStartedNanos = 0L;
         AutoSellLogic.currentState = AutoSellLogic.State.MOVING_ITEMS;
         AutoSellLogic.tickDelay = random(4, 8);
      } else closeAndIdle(client);
   }

   private static boolean stageOminousSurplus(class_310 client) {
      int reserve = AutoSellLogic.reservedOminousIndex(client);
      if (reserve < 0) return false;
      for (int i = playerStart(); i < handler.field_7761.size(); i++) {
         var slot = handler.method_7611(i);
         class_1799 stack = slot.method_7677();
         if (slot.field_7871 != client.field_1724.method_31548() || slot.method_34266() != reserve
               || !AutoSellLogic.isOminous(stack) || stack.method_7947() <= 1) continue;
         int destination = -1;
         for (int j = 0; j < Math.min(45, playerStart()); j++) {
            if (handler.method_7611(j).method_7677().method_7960() && !isConfirmSlot(handler, j)) {
               destination = j;
               break;
            }
         }
         if (destination < 0) return false;
         splitSource = i;
         splitSignature = stack.method_7972();
         click(client, i, 0, class_1713.field_7790);
         if (!sameSession(client)) return true;
         if (!class_1799.method_31577(handler.method_34255(), splitSignature)
               || !handler.method_7611(i).method_7677().method_7960()) {
            returnSplitCursor(client);
            return true;
         }
         click(client, i, 1, class_1713.field_7790);
         if (!sameSession(client)) return true;
         class_1799 reserved = handler.method_7611(i).method_7677();
         if (AutoSellLogic.isOminous(reserved) && reserved.method_7947() == 1
               && handler.method_7611(destination).method_7677().method_7960()
               && class_1799.method_31577(handler.method_34255(), splitSignature)) {
            click(client, destination, 0, class_1713.field_7790);
         }
         if (!cursorEmpty()) returnSplitCursor(client);
         if (cursorEmpty()) { splitSource = -1; splitSignature = null; }
         return true;
      }
      return false;
   }

   private static void returnSplitCursor(class_310 client) {
      if (!sameSession(client) || splitSource < playerStart() || splitSource >= handler.field_7761.size()
            || splitSignature == null || cursorEmpty()) return;
      class_1799 held = handler.method_34255();
      class_1799 original = handler.method_7611(splitSource).method_7677();
      if (!class_1799.method_31577(held, splitSignature)) return;
      if (original.method_7960() || class_1799.method_31577(original, held)
            && original.method_7947() + held.method_7947() <= original.method_7914()) {
         click(client, splitSource, 0, class_1713.field_7790);
      }
   }

   private static boolean matchesSellable(class_310 client, int index, class_1799 signature) {
      return AutoSellLogic.isSellableInventorySlot(client, handler.method_7611(index))
         && class_1799.method_31577(handler.method_7611(index).method_7677(), signature);
   }
   private static boolean ensureSession(class_310 client) {
      if (client == null || client.field_1724 == null || client.field_1761 == null || !AutoSellLogic.isSellGuiOpen(client)) {
         clearSession();
         AutoSellLogic.currentState = AutoSellLogic.State.IDLE;
         AutoSellLogic.tickDelay = random(6, 12);
         return false;
      }
      class_1703 current = ((class_465)client.field_1755).method_17577();
      if (handler != null && (handler != current || syncId != current.field_7763)) {
         clearSession();
         AutoSellLogic.currentState = AutoSellLogic.State.IDLE;
         AutoSellLogic.tickDelay = 2;
         return false;
      }
      if (handler == null) {
         handler = current;
         syncId = current.field_7763;
         long now = System.nanoTime();
         softDeadlineNanos = now + random(25000, 27000) * 1_000_000L;
         hardDeadlineNanos = now + 29_000_000_000L;
         SellRhythmRuntime.reset();
      }
      return true;
   }
   private static boolean sameSession(class_310 client) {
      return client != null && client.field_1724 != null && client.field_1761 != null && handler != null
         && client.field_1724.field_7512 == handler && handler.field_7763 == syncId
         && client.field_1755 instanceof class_465 screen && screen.method_17577() == handler;
   }
   private static void click(class_310 client, int index, int button, class_1713 action) {
      client.field_1761.method_2906(syncId, handler.method_7611(index).field_7874, button, action, client.field_1724);
   }
   private static boolean cursorEmpty() { return handler.method_34255().method_7960(); }
   private static int playerStart() { return Math.max(0, handler.field_7761.size() - 36); }
   private static int sellCapacity() {
      int count = 0;
      for (int i = 0; i < Math.min(45, playerStart()); i++) if (!isConfirmSlot(handler, i)) count++;
      return count;
   }
   private static int filledSellSlots() {
      int count = 0;
      for (int i = 0; i < Math.min(45, playerStart()); i++) {
         if (!isConfirmSlot(handler, i) && !handler.method_7611(i).method_7677().method_7960()) count++;
      }
      return count;
   }
   static int findConfirmSlot(class_1703 menu) {
      for (int i = 0; i < Math.max(0, menu.field_7761.size() - 36); i++) if (isConfirmSlot(menu, i)) return i;
      return -1;
   }
   public static boolean isConfirmSlot(class_1703 menu, int index) {
      if (menu == null || index < 0 || index >= Math.max(0, menu.field_7761.size() - 36)) return false;
      class_1799 stack = menu.method_7611(index).method_7677();
      if (stack.method_7960() || !stack.method_31574(class_1802.field_8581)) return false;
      String name = stack.method_7964().getString().trim().toLowerCase(Locale.ROOT);
      return index >= 45 || name.equals("sell") || name.startsWith("sell ")
         || name.contains("sell items") || name.contains("click to sell");
   }
   private static void scheduleConfirm(int min, int max) {
      AutoSellLogic.currentState = AutoSellLogic.State.CLICK_CONFIRM;
      AutoSellLogic.tickDelay = random(min, max);
   }
   private static void closeAndIdle(class_310 client) {
      if (sameSession(client)) AutoSellLogic.closeAllGui(client);
      clearSession();
      AutoSellLogic.currentState = AutoSellLogic.State.IDLE;
      AutoSellLogic.tickDelay = random(6, 14);
   }
   static void clearSession() {
      handler = null;
      splitSource = -1;
      splitSignature = null;
      softDeadlineNanos = hardDeadlineNanos = confirmWaitStartedNanos = 0L;
      SellRhythmRuntime.reset();
   }
   public static void forceReset() {
      class_310 client = class_310.method_1551();
      if (sameSession(client)) returnSplitCursor(client);
      clearSession();
   }
   private static int random(int min, int max) { return ThreadLocalRandom.current().nextInt(min, max + 1); }
   public static boolean isHardDeadlineSwordHold() { return false; }
   public static void clearHardDeadlineSwordHold() {}
   public static boolean shouldEnableSeaAutoSell() { return true; }
   public static int priorityForOminous() { return 3; }
   public static int priorityForFirework() { return 2; }
   public static int priorityForSword() { return 1; }
   public static boolean isBusy() { return handler != null; }
}
