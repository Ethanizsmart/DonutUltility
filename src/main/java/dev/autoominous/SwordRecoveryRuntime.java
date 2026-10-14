package dev.autoominous;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_634;
import net.minecraft.class_7923;

public final class SwordRecoveryRuntime {
   private static boolean recoveryActive;
   private static final MissingSwordTimer missingSwordTimer = new MissingSwordTimer();
   private static Object observedPlayer;
   private static Object observedWorld;
   private static SwordRecoveryRuntime.State state = State.CHECK_LOCAL;
   private static long nextActionAtMs;
   private static long stateDeadlineMs;
   private static boolean allowAutoSell;
   private static class_1799 purchaseSword;
   private static long purchasePrice;
   private static class_1703 purchaseHandler;
   private static class_634 purchaseConnection;
   private static Object purchasePlayer;
   // Screenshot: third column of the sixth row (the 48th cell).
   private static final int FILTER_SLOT = 47;

   private SwordRecoveryRuntime() {
   }

   public static void onTick() {
      class_310 client = class_310.method_1551();
      DeathRecoverySettings.maybePersist();
      if (!MasterAutomation.isEnabled() || !DeathRecoverySettings.isSwordRecoveryEnabled()) {
         if (recoveryActive && client != null && client.field_1724 != null) closeRecoveryScreen(client);
         forceReset();
         return;
      }
      if (client == null || client.field_1724 == null || client.field_1687 == null
         || client.field_1761 == null || client.method_1562() == null
         || !client.field_1724.method_5805()) {
         forceReset();
         return;
      }
      if (observedPlayer != client.field_1724 || observedWorld != client.field_1687) {
         forceReset();
         observedPlayer = client.field_1724;
         observedWorld = client.field_1687;
      }
      if (DeathRecoveryRuntime.isRecoveryPending()) {
         forceReset();
         return;
      }
      if (HomePositionGuardRuntime.isBlockingAutomation() || BountyRuntime.isBlockingSell() || HotbarRefillRuntime.isRefilling()) {
         missingSwordTimer.reset();
         return;
      }
      long now = System.currentTimeMillis();
      if (!recoveryActive) {
         boolean absentForRequiredTime = missingSwordTimer.update(findLootingDiamondSword(client, true) < 0, now);
         // Keep observing inventory during /sell, but wait for its screen to close before buying.
         if (!absentForRequiredTime || client.field_1755 != null) return;
         recoveryActive = true;
         state = State.CHECK_LOCAL;
         nextActionAtMs = now;
      }
      tickRecovery(now, client);
   }

   public static boolean isRecoveryActive() {
      return recoveryActive;
   }

   private static boolean tickRecovery(long var2, class_310 var4) {
      if (var2 < nextActionAtMs) {
         return false;
      } else {
         try {
            switch (state) {
               case CHECK_LOCAL:
                  int var19 = findLootingDiamondSword(var4, true);
                  if (var19 >= 0) {
                     if (var19 < 9) {
                        return finish(var4);
                     }

                     closeRecoveryScreen(var4);
                     swapInventorySlotToHotbarZero(var4, var19);
                     state = SwordRecoveryRuntime.State.VERIFY_HOTBAR;
                     stateDeadlineMs = var2 + 3000L;
                     nextActionAtMs = var2 + random(250, 500);
                  } else if (!hasEmptyMainSlot(var4)) {
                     beginFreeSpace(var4, var2);
                  } else {
                     state = SwordRecoveryRuntime.State.OPEN_AUCTION;
                     nextActionAtMs = var2 + random(250, 500);
                  }
                  break;
               case FREE_SPACE:
                  // Empty slots while /sell is still open may only reflect staged, unsold items.
                  if (hasEmptyMainSlot(var4)
                     && var4.field_1724.field_7512 == var4.field_1724.field_7498
                     && !(var4.field_1755 instanceof class_465)) {
                     suspendAutoSellNow();
                     closeRecoveryScreen(var4);
                     state = SwordRecoveryRuntime.State.OPEN_AUCTION;
                     nextActionAtMs = var2 + random(400, 800);
                  } else {
                     if (var2 >= stateDeadlineMs) {
                        SlashSellRuntime.resetRuntime();
                        stateDeadlineMs = var2 + 30000L;
                     }

                     nextActionAtMs = var2 + random(200, 450);
                  }
                  break;
               case OPEN_AUCTION:
                  int existing = findLootingDiamondSword(var4, true);
                  if (existing >= 0) {
                     state = SwordRecoveryRuntime.State.MOVE_TO_HOTBAR;
                     nextActionAtMs = var2 + 250L;
                  } else if (!hasEmptyMainSlot(var4)) {
                     beginFreeSpace(var4, var2);
                  } else {
                     suspendAutoSellNow();
                     closeRecoveryScreen(var4);
                     clearPurchase();
                     sendCommand(var4, "ah sword looting 3");
                     state = SwordRecoveryRuntime.State.WAIT_AUCTION;
                     stateDeadlineMs = var2 + 8000L;
                     nextActionAtMs = var2 + random(500, 900);
                  }
                  break;
               case WAIT_AUCTION:
                  if (auctionHandler(var4) != null) {
                     state = SwordRecoveryRuntime.State.SET_FILTER;
                     stateDeadlineMs = var2 + 10000L;
                     nextActionAtMs = var2 + 400L;
                  } else if (var2 >= stateDeadlineMs) {
                     reopenAuction(var4, var2);
                  } else {
                     nextActionAtMs = var2 + 150L;
                  }
                  break;
               case SET_FILTER:
                  class_1703 filterMenu = auctionHandler(var4);
                  AuctionSwordRules.FilterMode filterMode = filterMenu == null
                     ? AuctionSwordRules.FilterMode.UNKNOWN : selectedFilter(var4, filterMenu);
                  if (filterMenu == null) {
                     reopenAuction(var4, var2);
                  } else if (filterMode == AuctionSwordRules.FilterMode.LOWEST_PRICE) {
                     state = SwordRecoveryRuntime.State.SCAN_AUCTION;
                     nextActionAtMs = var2 + 500L;
                  } else if (var2 >= stateDeadlineMs) {
                     reopenAuction(var4, var2);
                  } else {
                     int filterSlot = findFilterSlot(filterMenu);
                     if (filterSlot >= 0 && (filterMode == AuctionSwordRules.FilterMode.HIGHEST_PRICE
                        || filterMode == AuctionSwordRules.FilterMode.RECENTLY_LISTED)) {
                        clickTopSlot(var4, filterSlot, class_1713.field_7790);
                        nextActionAtMs = var2 + 700L;
                     } else {
                        nextActionAtMs = var2 + 200L;
                     }
                  }
                  break;
               case SCAN_AUCTION:
                  class_1703 auctionMenu = auctionHandler(var4);
                  if (auctionMenu == null || !lowestPriceSelected(var4, auctionMenu)) {
                     reopenAuction(var4, var2);
                  } else if (!hasEmptyMainSlot(var4)) {
                     beginFreeSpace(var4, var2);
                  } else {
                     AuctionListing cheapest = findCheapestSword(var4, auctionMenu);
                     if (cheapest == null) {
                        reopenAuction(var4, var2);
                     } else {
                        // Recheck the live slot before issuing a purchase click.
                        class_1799 stack = auctionMenu.method_7611(cheapest.slot()).method_7677();
                        java.util.OptionalLong currentPrice = AuctionSwordRules.price(tooltip(var4, stack));
                        if (!isDiamondSword(stack) || !hasLootingThree(var4, stack)
                           || currentPrice.isEmpty() || currentPrice.getAsLong() != cheapest.price()
                           || !AuctionSwordRules.affordable(currentPrice.getAsLong())) {
                           reopenAuction(var4, var2);
                           break;
                        }
                        purchaseSword = stack.method_7972();
                        purchasePrice = cheapest.price();
                        purchaseHandler = auctionMenu;
                        purchaseConnection = var4.method_1562();
                        purchasePlayer = var4.field_1724;
                        clickTopSlot(var4, cheapest.slot(), class_1713.field_7790);
                        state = SwordRecoveryRuntime.State.WAIT_PURCHASE;
                        stateDeadlineMs = var2 + 8000L;
                        nextActionAtMs = var2 + random(400, 700);
                     }
                  }
                  break;
               case WAIT_PURCHASE:
                  if (findLootingDiamondSword(var4, true) >= 0) {
                     closeRecoveryScreen(var4);
                     clearPurchase();
                     state = SwordRecoveryRuntime.State.MOVE_TO_HOTBAR;
                     nextActionAtMs = var2 + 250L;
                  } else if (!purchaseContextValid(var4)) {
                     reopenAuction(var4, var2);
                  } else if (AuctionSwordRules.confirmationTitle(title(var4))) {
                     if (confirmAuctionPurchase(var4)) {
                        state = SwordRecoveryRuntime.State.VERIFY_PURCHASE;
                        stateDeadlineMs = var2 + 10000L;
                        nextActionAtMs = var2 + 500L;
                     } else if (var2 >= stateDeadlineMs) {
                        reopenAuction(var4, var2);
                     } else {
                        nextActionAtMs = var2 + 200L;
                     }
                  } else if (var2 >= stateDeadlineMs) {
                     reopenAuction(var4, var2);
                  } else {
                     nextActionAtMs = var2 + 200L;
                  }
                  break;
               case VERIFY_PURCHASE:
                  if (findLootingDiamondSword(var4, true) >= 0) {
                     closeRecoveryScreen(var4);
                     clearPurchase();
                     state = SwordRecoveryRuntime.State.MOVE_TO_HOTBAR;
                     nextActionAtMs = var2 + 250L;
                  } else if (var2 >= stateDeadlineMs) {
                     reopenAuction(var4, var2);
                  } else {
                     // Never click the confirmation twice while waiting for inventory updates.
                     nextActionAtMs = var2 + 200L;
                  }
                  break;
               case MOVE_TO_HOTBAR:
                  int var13 = findLootingDiamondSword(var4, true);
                  if (var13 < 0) {
                     state = SwordRecoveryRuntime.State.OPEN_AUCTION;
                     nextActionAtMs = var2 + random(400, 800);
                  } else {
                     if (var13 < 9) {
                        return finish(var4);
                     }

                     closeRecoveryScreen(var4);
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
            System.err.println("[Donut Utility] Auction sword recovery: " + var12);
            var12.printStackTrace();
            reopenAuction(var4, var2);
         }

         return false;
      }
   }

   public static boolean isRecoveryBlockingAutoSell() {
      return DeathRecoveryRuntime.isBlockingAutoSell() || recoveryActive && !allowAutoSell;
   }

   public static boolean allowAutoSellDuringRecovery() {
      return allowAutoSell;
   }

   public static void forceReset() {
      clearPurchase();
      recoveryActive = false;
      missingSwordTimer.reset();
      observedPlayer = null;
      observedWorld = null;
      allowAutoSell = false;
      state = State.CHECK_LOCAL;
      stateDeadlineMs = 0L;
      nextActionAtMs = 0L;
   }

   private static boolean finish(class_310 var0) {
      allowAutoSell = false;
      closeRecoveryScreen(var0);
      clearPurchase();
      forceReset();
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

   private static int findLootingDiamondSword(class_310 var0, boolean var1) {
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
         for (int var2 = 9; var2 < 36; var2++) {
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
      // Read the enchantment component, not a display name/lore that can imitate Looting III.
      for (var entry : var1.method_58657().method_57539()) {
         if (entry.getKey().method_40226(net.minecraft.class_2960.method_60654("minecraft:looting"))
            && entry.getIntValue() == 3) {
            return true;
         }
      }
      return false;
   }

   private static void swapInventorySlotToHotbarZero(class_310 var0, int var1) {
      if (var1 >= 9) {
         class_1703 var2 = var0.field_1724.field_7512;
         if (var2 != null) {
            // Inventory indices differ from screen slot IDs (crafting/armor slots precede them).
            for (class_1735 slot : var2.field_7761) {
               if (slot.field_7871 == var0.field_1724.method_31548() && slot.method_34266() == var1) {
                  var0.field_1761.method_2906(var2.field_7763, slot.field_7874, 0, class_1713.field_7791, var0.field_1724);
                  return;
               }
            }
         }
      }
   }

   private static void sendCommand(class_310 var0, String var1) {
      class_634 var2 = var0.method_1562();
      if (var2 != null) {
         var2.method_45730(var1);
      }
   }

   private static void clearPurchase() {
      purchaseSword = null;
      purchasePrice = 0L;
      purchaseHandler = null;
      purchaseConnection = null;
      purchasePlayer = null;
   }

   private static void beginFreeSpace(class_310 client, long now) {
      closeRecoveryScreen(client);
      clearPurchase();
      allowAutoSell = true;
      state = State.FREE_SPACE;
      stateDeadlineMs = now + 30000L;
      nextActionAtMs = now + 250L;
   }

   private static void reopenAuction(class_310 client, long now) {
      allowAutoSell = false;
      closeRecoveryScreen(client);
      clearPurchase();
      state = State.OPEN_AUCTION;
      nextActionAtMs = now + random(2000, 3000);
      stateDeadlineMs = 0L;
   }

   private static class_1703 auctionHandler(class_310 client) {
      if (!(client.field_1755 instanceof class_465 screen)
         || !AuctionSwordRules.normalize(title(client)).startsWith("auction (page")) return null;
      class_1703 menu = screen.method_17577();
      return menu == client.field_1724.field_7512 && topSlotCount(menu) == 54 ? menu : null;
   }

   private static int topSlotCount(class_1703 menu) {
      return Math.max(0, menu.field_7761.size() - 36);
   }

   private static int findFilterSlot(class_1703 menu) {
      if (FILTER_SLOT >= topSlotCount(menu)) return -1;
      class_1799 icon = menu.method_7611(FILTER_SLOT).method_7677();
      return !icon.method_7960()
         && "minecraft:hopper".equals(class_7923.field_41178.method_10221(icon.method_7909()).toString())
         ? FILTER_SLOT : -1;
   }

   private static List<String> tooltip(class_310 client, class_1799 stack) {
      return class_437.method_25408(client, stack).stream().map(text -> text.getString()).toList();
   }

   private static boolean lowestPriceSelected(class_310 client, class_1703 menu) {
      return selectedFilter(client, menu) == AuctionSwordRules.FilterMode.LOWEST_PRICE;
   }

   private static AuctionSwordRules.FilterMode selectedFilter(class_310 client, class_1703 menu) {
      int slot = findFilterSlot(menu);
      if (slot < 0) return AuctionSwordRules.FilterMode.UNKNOWN;
      List<AuctionSwordRules.FilterOption> options = new java.util.ArrayList<>();
      for (class_2561 line : class_437.method_25408(client, menu.method_7611(slot).method_7677())) {
         List<AuctionSwordRules.ColoredRun> runs = new java.util.ArrayList<>();
         // The styled visitor supplies inherited colors for nested/sibling text components.
         line.method_27658((style, text) -> {
            var color = style.method_10973();
            runs.add(new AuctionSwordRules.ColoredRun(text, color == null ? 0xFFFFFF : color.method_27716()));
            return java.util.Optional.empty();
         }, net.minecraft.class_2583.field_24360);
         options.add(AuctionSwordRules.filterOption(runs));
      }
      return AuctionSwordRules.selectedFilter(options);
   }

   private record AuctionListing(int slot, long price) {}

   private static AuctionListing findCheapestSword(class_310 client, class_1703 menu) {
      AuctionListing cheapest = null;
      // The first five rows are listings; the sixth row contains menu controls.
      for (int slot = 0; slot < Math.min(45, topSlotCount(menu)); slot++) {
         class_1799 stack = menu.method_7611(slot).method_7677();
         if (!isDiamondSword(stack) || !hasLootingThree(client, stack)) continue;
         java.util.OptionalLong price = AuctionSwordRules.price(tooltip(client, stack));
         if (price.isPresent() && AuctionSwordRules.affordable(price.getAsLong())
            && (cheapest == null || price.getAsLong() < cheapest.price())) {
            cheapest = new AuctionListing(slot, price.getAsLong());
         }
      }
      return cheapest;
   }

   private static boolean purchaseContextValid(class_310 client) {
      return purchaseSword != null && purchaseConnection == client.method_1562()
         && purchasePlayer == client.field_1724 && AuctionSwordRules.affordable(purchasePrice);
   }

   private static boolean confirmAuctionPurchase(class_310 client) {
      if (!purchaseContextValid(client)) return false;
      if (client.field_1755 instanceof net.minecraft.class_11473<?> dialogScreen) {
         return confirmPurchaseDialog(client, dialogScreen);
      }
      if (!(client.field_1755 instanceof class_465 screen)) return false;
      class_1703 menu = screen.method_17577();
      if (menu == purchaseHandler || menu != client.field_1724.field_7512) return false;
      boolean matchingSword = false;
      int confirmSlot = -1;
      List<String> priceLines = new java.util.ArrayList<>();
      for (int slot = 0; slot < topSlotCount(menu); slot++) {
         class_1799 stack = menu.method_7611(slot).method_7677();
         if (stack.method_7960()) continue;
         if (isDiamondSword(stack) && hasLootingThree(client, stack)
            && stack.method_58657().equals(purchaseSword.method_58657())) {
            matchingSword = true;
            priceLines.addAll(tooltip(client, stack));
         }
         if (AuctionSwordRules.buyButton(stack.method_7964().getString())) {
            if (confirmSlot >= 0) return false;
            confirmSlot = slot;
            priceLines.addAll(tooltip(client, stack));
         }
      }
      if (!matchingSword || confirmSlot < 0 || !AuctionSwordRules.confirmationPriceMatches(purchasePrice, priceLines)
         || !hasEmptyMainSlot(client)) return false;
      clickTopSlot(client, confirmSlot, class_1713.field_7790);
      return true;
   }

   private static boolean confirmPurchaseDialog(class_310 client, net.minecraft.class_11473<?> screen) {
      if (!(screen instanceof dev.autoominous.mixin.DialogScreenAccessor accessor)
         || !AuctionSwordRules.confirmationTitle(title(client)) || !hasEmptyMainSlot(client)) return false;
      var dialog = accessor.autoominous$getDialog();
      if (!AuctionSwordRules.confirmationTitle(dialog.comp_4305().comp_4301().getString())) return false;
      boolean matchingSword = false;
      List<String> priceLines = new java.util.ArrayList<>();
      for (var body : dialog.comp_4305().comp_4304()) {
         if (body instanceof net.minecraft.class_11434 itemBody) {
            class_1799 stack = itemBody.comp_4324();
            if (isDiamondSword(stack) && hasLootingThree(client, stack)
               && stack.method_58657().equals(purchaseSword.method_58657())) matchingSword = true;
            itemBody.comp_4325().ifPresent(message -> priceLines.add(message.comp_4330().getString()));
         } else if (body instanceof net.minecraft.class_11435 message) {
            priceLines.add(message.comp_4330().getString());
         }
      }
      if (!matchingSword || !AuctionSwordRules.confirmationPriceMatches(purchasePrice, priceLines)) return false;
      net.minecraft.class_4185 yesButton = null;
      boolean hasNoButton = false;
      // Server dialogs can place action buttons inside the scrollable body, not at the screen root.
      java.util.Deque<net.minecraft.class_364> widgets = new java.util.ArrayDeque<>(screen.method_25396());
      java.util.Set<net.minecraft.class_364> visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
      while (!widgets.isEmpty()) {
         var child = widgets.removeFirst();
         if (!visited.add(child)) continue;
         if (child instanceof net.minecraft.class_4069 parent) widgets.addAll(parent.method_25396());
         if (!(child instanceof net.minecraft.class_4185 button) || !button.field_22763 || !button.field_22764) continue;
         String label = AuctionSwordRules.normalize(button.method_25369().getString());
         if (label.equals("yes")) {
            if (yesButton != null) return false;
            yesButton = button;
         } else if (label.equals("no")) {
            hasNoButton = true;
         }
      }
      if (yesButton == null || !hasNoButton || client.field_1755 != screen || !purchaseContextValid(client)) return false;
      // Invoke the real button callback, so vanilla sends the dialog's server action.
      // ButtonWidget does not inspect its Click input in this version.
      yesButton.method_25306(null);
      return true;
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

   private static void clickTopSlot(class_310 var0, int var1, class_1713 var2) {
      if (var0.field_1755 instanceof class_465 var3) {
         class_1703 var5 = var3.method_17577();
         var0.field_1761.method_2906(var5.field_7763, var1, 0, var2, var0.field_1724);
      }
   }

   private static void closeRecoveryScreen(class_310 var0) {
      try {
         if (var0.field_1755 instanceof net.minecraft.class_11473<?> dialogScreen
            && AuctionSwordRules.confirmationTitle(title(var0))) {
            // Vanilla invokes the dialog's cancellation action (the No action).
            dialogScreen.method_25419();
         }
         if (var0.field_1755 instanceof class_465) {
            var0.field_1724.method_7346();
         }
      } catch (Throwable var2) {
      }
   }

   private static int random(int var0, int var1) {
      return ThreadLocalRandom.current().nextInt(var0, var1 + 1);
   }

   private static enum State {
      CHECK_LOCAL,
      FREE_SPACE,
      OPEN_AUCTION,
      WAIT_AUCTION,
      SET_FILTER,
      SCAN_AUCTION,
      WAIT_PURCHASE,
      VERIFY_PURCHASE,
      MOVE_TO_HOTBAR,
      VERIFY_HOTBAR;
   }
}
