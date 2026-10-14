package dev.autoominous;

import com.seastore.auto.seaats.client.AutoSellLogic;
import com.seastore.auto.seaats.client.HumanizedSellRuntime;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_490;

public final class HotbarRefillRuntime {
   public enum State {
      IDLE,
      AWAIT_SYNC
   }

   private static State state = State.IDLE;
   private static long lastCheckAtMs = 0L;
   private static long openedAtMs = 0L;
   private static Object observedPlayer;
   private static Object observedWorld;

   private HotbarRefillRuntime() {}

   public static void onTick() {
      class_310 client = class_310.method_1551();
      if (!MasterAutomation.isEnabled()) {
         forceReset();
         return;
      }
      if (client == null || client.field_1724 == null || client.field_1687 == null
         || client.field_1761 == null || !client.field_1724.method_5805()) {
         forceReset();
         return;
      }
      if (observedPlayer != client.field_1724 || observedWorld != client.field_1687) {
         forceReset();
         observedPlayer = client.field_1724;
         observedWorld = client.field_1687;
      }

      if (isEmergencyOrBusy()) {
         if (state == State.AWAIT_SYNC) {
            closeInventory(client);
            state = State.IDLE;
         }
         return;
      }

      // Condition 2: No screen open except regular inventory (E / InventoryScreen).
      if (client.field_1755 != null && !(client.field_1755 instanceof class_490)) {
         if (state == State.AWAIT_SYNC) {
            state = State.IDLE;
         }
         return;
      }

      long now = System.currentTimeMillis();
      class_1661 inventory = client.field_1724.method_31548();
      if (inventory == null) return;

      if (state == State.IDLE) {
         // Condition 4: At least 400ms cooldown between checks.
         if (now - lastCheckAtMs < 400L) {
            return;
         }

         if (!HotbarRefillRules.hasAnyRefillNeed(inventory)) {
            lastCheckAtMs = now;
            return;
         }

         // Step 1: Release right-click and open inventory screen.
         beginRefill(client, now);
         return;
      }

      if (state == State.AWAIT_SYNC) {
         // Step 2: After at least 50ms / 1 game tick.
         if (now - openedAtMs < 50L) {
            return;
         }

         // Timeout safety: if stuck in AWAIT_SYNC for > 3s, abort.
         if (now - openedAtMs > 3000L) {
            closeInventory(client);
            state = State.IDLE;
            lastCheckAtMs = now;
            return;
         }

         if (client.field_1755 == null || !(client.field_1755 instanceof class_490)) {
            state = State.IDLE;
            lastCheckAtMs = now;
            return;
         }

         class_1703 screenHandler = client.field_1724.field_7512;
         if (screenHandler == null) {
            closeInventory(client);
            state = State.IDLE;
            lastCheckAtMs = now;
            return;
         }

         performSwapsAndFinish(client, screenHandler, inventory);
      }
   }

   private static void beginRefill(class_310 client, long now) {
      ProtectedItemAutoUseRuntime.forceRelease();
      if (client.field_1690 != null && client.field_1690.field_1904 != null) {
         client.field_1690.field_1904.method_23481(false);
      }

      if (!(client.field_1755 instanceof class_490)) {
         client.method_1507(new class_490(client.field_1724));
      }

      openedAtMs = now;
      state = State.AWAIT_SYNC;
   }

   private static void performSwapsAndFinish(class_310 client, class_1703 screenHandler, class_1661 inventory) {
      int usedSlot1 = -1;
      int usedSlot2 = -1;
      int usedSlot3 = -1;

      // 1. Kiếm (phím 1 - button 0)
      class_1799 hotbar0 = inventory.method_5438(0);
      int currentSwordScore = HotbarRefillRules.getSwordScore(hotbar0);
      int bestSwordSlot = HotbarRefillRules.findBestSwordBagSlot(inventory, currentSwordScore, -1);
      if (bestSwordSlot >= 0) {
         int slotId = findScreenSlotId(screenHandler, client.field_1724, bestSwordSlot);
         if (slotId >= 0) {
            swapSlot(client, screenHandler, slotId, 0);
            usedSlot1 = bestSwordSlot;
         }
      }

      // 2. Ominous Bottle (phím 2 - button 1)
      class_1799 hotbar1 = inventory.method_5438(1);
      if (!HotbarRefillRules.isOminous(hotbar1)) {
         int ominousSlot = HotbarRefillRules.findFirstOminousBagSlot(inventory, usedSlot1, -1);
         if (ominousSlot >= 0) {
            int slotId = findScreenSlotId(screenHandler, client.field_1724, ominousSlot);
            if (slotId >= 0) {
               swapSlot(client, screenHandler, slotId, 1);
               usedSlot2 = ominousSlot;
            }
         }
      }

      // 3. Pháo hoa (phím 3 - button 2)
      class_1799 hotbar2 = inventory.method_5438(2);
      if (!HotbarRefillRules.isFirework(hotbar2)) {
         int fireworkSlot = HotbarRefillRules.findFirstFireworkBagSlot(inventory, usedSlot1, usedSlot2);
         if (fireworkSlot >= 0) {
            int slotId = findScreenSlotId(screenHandler, client.field_1724, fireworkSlot);
            if (slotId >= 0) {
               swapSlot(client, screenHandler, slotId, 2);
               usedSlot3 = fireworkSlot;
            }
         }
      }

      if (usedSlot1 >= 0 || usedSlot2 >= 0 || usedSlot3 >= 0) {
         System.out.println("[DonutUtility][HotbarRefill] Refilled hotbar (sword=" + usedSlot1
            + ", ominous=" + usedSlot2 + ", firework=" + usedSlot3 + ")");
      }

      // Close inventory immediately
      closeInventory(client);
      lastCheckAtMs = System.currentTimeMillis();
      state = State.IDLE;
      ProtectedItemAutoUseRuntime.requestRearm();
   }

   public static int findScreenSlotId(class_1703 screenHandler, class_1657 player, int invIndex) {
      if (screenHandler == null || player == null) return -1;
      for (class_1735 slot : screenHandler.field_7761) {
         if (slot.field_7871 == player.method_31548() && slot.method_34266() == invIndex) {
            return slot.field_7874;
         }
      }
      return -1;
   }

   public static void swapSlot(class_310 client, class_1703 screenHandler, int slotId, int hotbarButton) {
      if (slotId >= 0 && client != null && client.field_1761 != null && client.field_1724 != null && screenHandler != null) {
         client.field_1761.method_2906(
            screenHandler.field_7763,
            slotId,
            hotbarButton,
            class_1713.field_7791,
            client.field_1724
         );
      }
   }

   private static void closeInventory(class_310 client) {
      if (client != null) {
         if (client.field_1724 != null) {
            try {
               client.field_1724.method_7346();
            } catch (Throwable t) {}
         }
         if (client.field_1755 != null) {
            try {
               client.method_1507(null);
            } catch (Throwable t) {}
         }
      }
   }

   public static void forceReset() {
      if (state == State.AWAIT_SYNC) {
         class_310 client = class_310.method_1551();
         closeInventory(client);
      }
      state = State.IDLE;
      openedAtMs = 0L;
   }

   public static boolean isRefilling() {
      return state == State.AWAIT_SYNC;
   }

   public static State getState() {
      return state;
   }

   private static boolean isEmergencyOrBusy() {
      if (DeathRecoveryRuntime.isRecoveryPending() || DeathRecoveryRuntime.isPostDeathSellPending()) {
         return true;
      }
      if (SwordRecoveryRuntime.isRecoveryActive()) {
         return true;
      }
      if (HomePositionGuardRuntime.isBlockingAutomation()) {
         return true;
      }
      if (BountyRuntime.isBlockingSell()) {
         return true;
      }
      if (AutoSellLogic.isBusy()) {
         return true;
      }
      if (HumanizedSellRuntime.isBusy()) {
         return true;
      }
      return false;
   }
}

