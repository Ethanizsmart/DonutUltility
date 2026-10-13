package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DeathSwordAhRecoveryRuntime {
   private static final long WAIT_AFTER_HOME_MS = 5000L;
   private static final long RETRY_MS = 5000L;
   private static final long GUI_OPEN_TIMEOUT_MS = 5000L;
   private static final long CONFIRM_TIMEOUT_MS = 5000L;
   private static final long PURCHASE_TIMEOUT_MS = 5000L;
   private static final long MAX_PRICE = 1000000L;
   private static final int IDLE = 0;
   private static final int WAIT_AFTER_HOME = 1;
   private static final int SELL_BEFORE_AH = 2;
   private static final int WAIT_FREE_SLOT = 3;
   private static final int OPEN_AH = 4;
   private static final int WAIT_AH_GUI = 5;
   private static final int WAIT_CONFIRM = 6;
   private static final int WAIT_PURCHASE = 7;
   private static final int WAIT_RETRY = 8;
   private static boolean active;
   private static int state = 0;
   private static long nextAtMs;
   private static long deadlineMs;
   private static long expectedPrice = -1L;
   private static int expectedListingSlot = -1;
   private static boolean forceSellAll;
   private static boolean sellOverridesArmed;
   private static boolean previousAutoSell;
   private static boolean previousAntiPlayer;
   private static String lastStatus = "";
   private static final Pattern PRICE = Pattern.compile("(?i)\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmb]?)");

   private DeathSwordAhRecoveryRuntime() {
   }

   public static boolean isActive() {
      return active;
   }

   public static boolean onTick() {
      AhDiagnosticRuntime.state(state, expectedPrice, expectedListingSlot, active);
      long var0 = System.currentTimeMillis();
      if (playerSafetyTriggered()) {
         status("PLAYER_LOCK: cancelling AH recovery");
         restoreSellOverrides(true);
         return false;
      }

      refreshCoordinatorDeadline(var0);
      Object var2 = mc();
      if (var2 == null || field(var2, "field_1724") == null || field(var2, "field_1687") == null) {
         return false;
      }

      if (!active) {
         active = true;
         state = 1;
         nextAtMs = var0 + 5000L;
         deadlineMs = 0L;
         expectedPrice = -1L;
         expectedListingSlot = -1;
         forceSellAll = inventoryFull(var2);
         status("ARMED: wait 5s after Home; inventoryFull=" + forceSellAll);
         return false;
      }

      int var3 = findLootingSword(var2);
      if (var3 >= 0) {
         return completeWithSword(var2, var3);
      }

      if (var0 < nextAtMs) {
         return false;
      }

      try {
         switch (state) {
            case 1:
               if (forceSellAll) {
                  state = 2;
                  armSellOverrides();
                  status("SELL_BEFORE_AH: full inventory; selling all sellable items first");
               } else {
                  state = 4;
               }

               nextAtMs = var0;
               break;
            case 2:
               boolean var13 = hasSellableInventory(var2);
               boolean var15 = seaSellActive() || sellGuiOpen(var2) || autoSellStateBusy();
               if (!var13 && !var15) {
                  restoreSellOverrides(false);
                  if (!hasEmptyMainSlot(var2)) {
                     state = 3;
                     nextAtMs = var0 + 500L;
                     status("AH WAITING: NO FREE SLOT after sell-all");
                  } else {
                     state = 4;
                     nextAtMs = var0 + 250L;
                     status("SELL_BEFORE_AH complete; opening AH");
                  }
               } else {
                  armSellOverrides();
                  tickAutoSell(var2);
                  nextAtMs = var0 + 50L;
               }
               break;
            case 3:
               if (hasEmptyMainSlot(var2)) {
                  state = 4;
                  nextAtMs = var0;
               } else {
                  nextAtMs = var0 + 500L;
               }
               break;
            case 4:
               if (!hasEmptyMainSlot(var2)) {
                  state = 3;
                  nextAtMs = var0 + 500L;
                  status("AH WAITING: NO FREE SLOT");
               } else {
                  closeScreen(var2);
                  sendCommand(var2, "ah sword looting 3");
                  state = 5;
                  deadlineMs = var0 + 5000L;
                  nextAtMs = var0 + 250L;
                  status("/ah sword looting 3 sent");
               }
               break;
            case 5:
               String var12 = screenTitle(var2);
               if (isAuctionTitle(var12) && isHandledScreen(field(var2, "field_1755"))) {
                  if (AhContentStabilityFix.readyToScan(var2, var0)) {
                     DeathSwordAhRecoveryRuntime.Listing var14 = cheapestListing(var2);
                     if (var14 != null) {
                        expectedPrice = var14.price;
                        expectedListingSlot = var14.slot;
                        clickHandledSlot(var2, var14.slot);
                        state = 6;
                        deadlineMs = var0 + 2000L;
                        nextAtMs = var0 + 150L;
                        status("AH listing clicked: slot=" + var14.slot + " price=$" + var14.price);
                     } else if (!AhContentStabilityFix.shouldRescanAfterMiss(var2, var0)) {
                        closeScreen(var2);
                        scheduleRetry(var0, "no Looting III diamond sword <= $1M");
                     }
                  }
               } else if (var0 >= deadlineMs) {
                  closeScreen(var2);
                  scheduleRetry(var0, "auction GUI timeout");
               } else {
                  nextAtMs = var0 + 100L;
               }
               break;
            case 6:
               Object var11 = field(var2, "field_1755");
               if (var11 == null) {
                  scheduleRetry(var0, "confirm screen disappeared");
               } else {
                  String var5 = collectScreenText(var2, var11).toLowerCase(Locale.ROOT);
                  long var6 = parsePrice(var5);
                  boolean var8 = var5.contains("are you sure") || var5.contains("yes") && var5.contains("no");
                  boolean var9 = var5.contains("diamond sword");
                  if (var8) {
                     if (pressYes(var11)) {
                        state = 7;
                        deadlineMs = var0 + 5000L;
                        nextAtMs = var0 + 150L;
                        status("AH confirm YES pressed at verified price=$" + var6);
                     } else {
                        closeScreen(var2);
                        scheduleRetry(var0, "Yes button not found");
                     }
                  } else if (!var8 || var6 <= 1000000L && (var6 < 0L || var6 == expectedPrice)) {
                     if (var0 >= deadlineMs) {
                        closeScreen(var2);
                        scheduleRetry(var0, "confirm verification timeout");
                     } else {
                        nextAtMs = var0 + 100L;
                     }
                  } else {
                     closeScreen(var2);
                     scheduleRetry(var0, "confirm price mismatch/over cap: expected=" + expectedPrice + " got=" + var6);
                  }
               }
               break;
            case 7:
               int var4 = findLootingSword(var2);
               if (var4 >= 0) {
                  return completeWithSword(var2, var4);
               }

               if (var0 >= deadlineMs) {
                  closeScreen(var2);
                  scheduleRetry(var0, "purchase not observed in inventory");
               } else {
                  nextAtMs = var0 + 100L;
               }
               break;
            case 8:
               if (hasEmptyMainSlot(var2)) {
                  state = 4;
                  nextAtMs = var0;
               } else {
                  state = 3;
                  nextAtMs = var0 + 500L;
               }
               break;
            default:
               state = 1;
               nextAtMs = var0 + 5000L;
         }
      } catch (Throwable var10) {
         status("ERROR: " + var10.getClass().getSimpleName() + ": " + var10.getMessage());
         closeScreen(var2);
         scheduleRetry(var0, "exception");
      }

      return false;
   }

   public static void reset() {
      restoreSellOverrides(true);
      active = false;
      state = 0;
      nextAtMs = 0L;
      deadlineMs = 0L;
      expectedPrice = -1L;
      expectedListingSlot = -1;
      forceSellAll = false;
      lastStatus = "";
   }

   private static boolean completeWithSword(Object var0, int var1) {
      AhDiagnosticRuntime.swordObserved(var1);

      try {
         restoreSellOverrides(true);
         if (var1 >= 9) {
            invokeSwordPrivate("swapInventorySlotToHotbarZero", new Object[]{var0, var1});
            int var2 = findLootingSword(var0);
            if (var2 < 0 || var2 >= 9) {
               nextAtMs = System.currentTimeMillis() + 150L;
               return false;
            }
         }

         status("COMPLETE: Looting III Diamond Sword recovered");
         active = false;
         state = 0;
         Class var5 = Class.forName("dev.autoominous.DeathRecoveryRuntime");
         Method var3 = var5.getMethod("completeAfterSwordRecovery");
         var3.invoke(null);
         return true;
      } catch (Throwable var4) {
         status("complete failed: " + var4.getClass().getSimpleName());
         return false;
      }
   }

   private static void scheduleRetry(long var0, String var2) {
      AhDiagnosticRuntime.retry(var2);
      expectedPrice = -1L;
      expectedListingSlot = -1;
      state = 8;
      nextAtMs = var0 + 5000L;
      deadlineMs = 0L;
      status("RETRY in 5s: " + var2);
   }

   private static void armSellOverrides() {
      if (!sellOverridesArmed) {
         try {
            Class var0 = Class.forName("com.seastore.auto.seaats.Config");
            Field var1 = staticField(var0, "isAutoSellEnabled");
            Field var2 = staticField(var0, "antiPlayerEnabled");
            previousAutoSell = var1.getBoolean(null);
            previousAntiPlayer = var2.getBoolean(null);
            var1.setBoolean(null, true);
            var2.setBoolean(null, false);
            sellOverridesArmed = true;
            invokeAutoSellReset();
         } catch (Throwable var3) {
            status("sell override arm failed: " + var3.getClass().getSimpleName());
         }
      }
   }

   private static void restoreSellOverrides(boolean var0) {
      if (sellOverridesArmed) {
         try {
            Class var1 = Class.forName("com.seastore.auto.seaats.Config");
            staticField(var1, "isAutoSellEnabled").setBoolean(null, previousAutoSell);
            staticField(var1, "antiPlayerEnabled").setBoolean(null, previousAntiPlayer);
         } catch (Throwable var2) {
         }

         sellOverridesArmed = false;
         if (var0) {
            invokeAutoSellReset();
         }
      }
   }

   private static void tickAutoSell(Object var0) throws Exception {
      Class var1 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
      Method var2 = method(var1, "tick", 1);
      var2.invoke(null, var0);
   }

   private static void invokeAutoSellReset() {
      try {
         Class var0 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
         Method var1 = method(var0, "resetState", 0);
         var1.invoke(null);
      } catch (Throwable var2) {
      }
   }

   private static boolean autoSellStateBusy() {
      try {
         Class var0 = Class.forName("com.seastore.auto.seaats.client.AutoSellLogic");
         Field var1 = staticField(var0, "currentState");
         Object var2 = var1.get(null);
         return var2 != null && !"IDLE".equals(String.valueOf(var2));
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean seaSellActive() {
      try {
         Class var0 = Class.forName("dev.autoominous.TransactionGate");
         return (Boolean)var0.getMethod("isSeaSellActive").invoke(null);
      } catch (Throwable var1) {
         return false;
      }
   }

   private static boolean hasSellableInventory(Object var0) {
      Object var1 = field(var0, "field_1724");
      Object var2 = invoke(var1, "method_31548");
      if (var2 == null) {
         return false;
      }

      for (int var3 = 0; var3 < 36; var3++) {
         Object var4 = invokeInt(var2, "method_5438", var3);
         if (!empty(var4)) {
            try {
               Class var5 = Class.forName("dev.autoominous.OminousSurplusSellPolicy");
               Method var6 = var5.getMethod("isProtectedForTransfer", Object.class);
               boolean var7 = (Boolean)var6.invoke(null, var4);
               if (!var7) {
                  return true;
               }
            } catch (Throwable var8) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean inventoryFull(Object var0) {
      return !hasEmptyMainSlot(var0);
   }

   private static boolean hasEmptyMainSlot(Object var0) {
      Object var1 = field(var0, "field_1724");
      Object var2 = invoke(var1, "method_31548");
      if (var2 == null) {
         return false;
      }

      for (int var3 = 0; var3 < 36; var3++) {
         if (empty(invokeInt(var2, "method_5438", var3))) {
            return true;
         }
      }

      return false;
   }

   private static int findLootingSword(Object var0) {
      Object var1 = invokeSwordPrivate("findDiamondSwordInternal", new Object[]{var0, Boolean.TRUE});
      return var1 instanceof Number ? ((Number)var1).intValue() : -1;
   }

   private static Object invokeSwordPrivate(String var0, Object[] var1) {
      try {
         Class var2 = Class.forName("dev.autoominous.SwordRecoveryRuntime");

         for (Method var6 : var2.getDeclaredMethods()) {
            if (var6.getName().equals(var0) && var6.getParameterCount() == var1.length) {
               var6.setAccessible(true);
               return var6.invoke(null, var1);
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static DeathSwordAhRecoveryRuntime.Listing cheapestListing(Object var0) {
      return (DeathSwordAhRecoveryRuntime.Listing)AhSwordPurchaseFix.cheapestListing(var0);
   }

   private static boolean isLootingDiamondSword(Object var0, Object var1) {
      try {
         Class var2 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         Method var3 = null;
         Method var4 = null;

         for (Method var8 : var2.getDeclaredMethods()) {
            if (var8.getName().equals("isDiamondSword") && var8.getParameterCount() == 1) {
               var3 = var8;
            }

            if (var8.getName().equals("hasLootingThree") && var8.getParameterCount() == 2) {
               var4 = var8;
            }
         }

         if (var3 != null && var4 != null) {
            var3.setAccessible(true);
            var4.setAccessible(true);
            return (Boolean)var3.invoke(null, var1) && (Boolean)var4.invoke(null, var0, var1);
         } else {
            return false;
         }
      } catch (Throwable var9) {
         return false;
      }
   }

   private static String tooltipText(Object var0, Object var1) {
      StringBuilder var2 = new StringBuilder();

      try {
         Class var3 = Class.forName("net.minecraft.class_437");
         Method var4 = method(var3, "method_25408", 2);
         Object var5 = var4.invoke(null, var0, var1);
         if (var5 instanceof Iterable) {
            for (Object var8 : (Iterable)var5) {
               var2.append(text(var8)).append('\n');
            }
         }
      } catch (Throwable var9) {
      }

      return var2.toString();
   }

   private static long parsePrice(String var0) {
      if (var0 == null) {
         return -1L;
      }

      Matcher var1 = PRICE.matcher(var0);
      long var2 = -1L;

      while (var1.find()) {
         String var4 = var1.group(1).replace(",", "");
         String var5 = var1.group(2).toLowerCase(Locale.ROOT);

         try {
            double var6 = Double.parseDouble(var4);
            double var8 = var5.equals("k") ? 1000.0 : (var5.equals("m") ? 1000000.0 : (var5.equals("b") ? 1.0E9 : 1.0));
            long var10 = Math.round(var6 * var8);
            if (var2 < 0L || var10 < var2) {
               var2 = var10;
            }
         } catch (NumberFormatException var12) {
         }
      }

      return var2;
   }

   private static void clickHandledSlot(Object var0, int var1) throws Exception {
      AhConfirmRaceGuard.requirePreClickValid(var0, var1);
      AhDiagnosticRuntime.listingClick(var1);
      Object var2 = field(var0, "field_1755");
      Object var3 = invoke(var2, "method_17577");
      int var4 = intField(var3, "field_7763", -1);
      Object var5 = field(var0, "field_1761");
      Object var6 = field(var0, "field_1724");
      Class var7 = Class.forName("net.minecraft.class_1713");
      Object var8 = staticField(var7, "field_7790").get(null);
      Method var9 = method(var5.getClass(), "method_2906", 5);
      var9.invoke(var5, var4, var1, 0, var8, var6);
   }

   private static boolean sellGuiOpen(Object var0) {
      try {
         Object var1 = field(var0, "field_1755");
         if (!isHandledScreen(var1)) {
            return false;
         }

         String var2 = screenTitle(var0).toLowerCase(Locale.ROOT);
         return var2.equals("sell") || var2.startsWith("sell ") || var2.contains("sell");
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isHandledScreen(Object var0) {
      try {
         return var0 != null && Class.forName("net.minecraft.class_465").isInstance(var0);
      } catch (Throwable var2) {
         return false;
      }
   }

   private static boolean isAuctionTitle(String var0) {
      String var1 = var0 == null ? "" : var0.toLowerCase(Locale.ROOT);
      return var1.startsWith("auction") || var1.contains("auction (page");
   }

   private static String screenTitle(Object var0) {
      return AhGuiRecognitionFix.screenTitle(var0);
   }

   private static String collectScreenText(Object var0, Object var1) {
      return AhSwordPurchaseFix.collectScreenText(var0, var1);
   }

   private static void appendObjectText(Object var0, StringBuilder var1, Object var2) {
      if (var2 != null) {
         if (var2 instanceof String var9) {
            append(var1, var9);
         } else {
            String var3 = var2.getClass().getName();
            if (var3.equals("net.minecraft.class_2561") || implementsInterfaceNamed(var2.getClass(), "net.minecraft.class_2561")) {
               append(var1, text(var2));
            } else if (var3.equals("net.minecraft.class_1799")) {
               append(var1, tooltipText(var0, var2));
            } else {
               if (var2 instanceof Iterable var4) {
                  int var5 = 0;

                  for (Object var7 : var4) {
                     if (var5++ > 64) {
                        break;
                     }

                     if (var7 instanceof String) {
                        appendObjectText(var0, var1, var7);
                     } else {
                        String var8 = var7 == null ? "" : var7.getClass().getName();
                        if (var8.contains("class_2561") || var8.equals("net.minecraft.class_1799")) {
                           appendObjectText(var0, var1, var7);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean implementsInterfaceNamed(Class<?> var0, String var1) {
      for (Class var5 : var0.getInterfaces()) {
         if (var5.getName().equals(var1)) {
            return true;
         }
      }

      Class var6 = var0.getSuperclass();
      return var6 != null && var6 != Object.class && implementsInterfaceNamed(var6, var1);
   }

   private static boolean pressYes(Object var0) {
      return AhSwordPurchaseFix.pressYes(var0);
   }

   private static void sendCommand(Object var0, String var1) {
      AhDiagnosticRuntime.command(var1);

      try {
         Object var2 = invoke(var0, "method_1562");
         if (var2 != null) {
            method(var2.getClass(), "method_45730", 1).invoke(var2, var1);
         }
      } catch (Throwable var3) {
         status("send command failed: " + var3.getClass().getSimpleName());
      }
   }

   private static void closeScreen(Object var0) {
      AhScreenCloseFix.close(var0);
   }

   private static boolean playerSafetyTriggered() {
      try {
         Class var0 = Class.forName("dev.autoominous.DisconnectRuntime");
         return var0.getField("disconnectTriggered").getBoolean(null);
      } catch (Throwable var1) {
         return false;
      }
   }

   private static void refreshCoordinatorDeadline(long var0) {
      if (active) {
         try {
            Class var2 = Class.forName("dev.autoominous.AutomationCoordinator");
            Field var3 = staticField(var2, "recoveryStartedAtMs");
            var3.setLong(null, var0);
         } catch (Throwable var4) {
         }
      }
   }

   private static Object mc() {
      try {
         return Class.forName("net.minecraft.class_310").getMethod("method_1551").invoke(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static boolean empty(Object var0) {
      if (var0 == null) {
         return true;
      }

      Object var1 = invoke(var0, "method_7960");
      return var1 instanceof Boolean && (Boolean)var1;
   }

   private static String text(Object var0) {
      if (var0 == null) {
         return "";
      }

      try {
         Method var5 = method(var0.getClass(), "method_10858", 1);
         Object var6 = var5.invoke(var0, 512);
         return var6 == null ? "" : String.valueOf(var6);
      } catch (Throwable var4) {
         try {
            Method var1 = method(var0.getClass(), "getString", 0);
            Object var2 = var1.invoke(var0);
            return var2 == null ? "" : String.valueOf(var2);
         } catch (Throwable var3) {
            return "";
         }
      }
   }

   private static void append(StringBuilder var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         var0.append(var1).append('\n');
      }
   }

   private static int listSize(Object var0) {
      if (var0 instanceof Collection var2) {
         return var2.size();
      } else {
         Object var1 = invoke(var0, "size");
         return var1 instanceof Number ? ((Number)var1).intValue() : 0;
      }
   }

   private static Object invoke(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         return method(var0.getClass(), var1, 0).invoke(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Object invokeInt(Object var0, String var1, int var2) {
      if (var0 == null) {
         return null;
      }

      try {
         return method(var0.getClass(), var1, 1).invoke(var0, var2);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Method method(Class<?> var0, String var1, int var2) throws NoSuchMethodException {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               var7.setAccessible(true);
               return var7;
            }
         }
      }

      throw new NoSuchMethodException(var0.getName() + "." + var1 + "/" + var2);
   }

   private static Field staticField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var0.getName() + "." + var1);
   }

   private static Object field(Object var0, String var1) {
      if (var0 == null) {
         return null;
      }

      try {
         return staticField(var0.getClass(), var1).get(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static int intField(Object var0, String var1, int var2) {
      if (var0 == null) {
         return var2;
      }

      try {
         return staticField(var0.getClass(), var1).getInt(var0);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static void status(String var0) {
      if (!Objects.equals(lastStatus, var0)) {
         lastStatus = var0;
         System.out.println("[Auto Ominous] Death Sword AH: " + var0);
      }
   }

   private record Listing(int slot, long price) {
   }
}
