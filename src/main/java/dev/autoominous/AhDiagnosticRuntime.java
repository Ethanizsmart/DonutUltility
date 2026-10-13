package dev.autoominous;

import java.lang.reflect.Field;
import java.util.Objects;

public final class AhDiagnosticRuntime {
   private static int lastState = Integer.MIN_VALUE;
   private static long lastExpectedPrice = Long.MIN_VALUE;
   private static int lastExpectedSlot = Integer.MIN_VALUE;
   private static boolean lastActive;
   private static String lastScreenText = null;
   private static String lastScan = null;

   private AhDiagnosticRuntime() {
   }

   private static void log(String var0) {
      System.out.println("[AH-DIAG] " + var0);
   }

   public static void state(int var0, long var1, int var3, boolean var4) {
      if (var0 != lastState || var1 != lastExpectedPrice || var3 != lastExpectedSlot || var4 != lastActive) {
         log("STATE active=" + var4 + " state=" + stateName(var0) + "(" + var0 + ") expectedSlot=" + var3 + " expectedPrice=" + var1);
         lastState = var0;
         lastExpectedPrice = var1;
         lastExpectedSlot = var3;
         lastActive = var4;
      }
   }

   public static void scanStart(Object var0) {
      String var1 = var0 == null ? "null" : var0.getClass().getName();
      log("LISTING_SCAN_START screen=" + var1);
      AhPerSlotDiagnosticRuntime.dump(var0);
   }

   public static void afterCheapest(Object var0) {
      String var1;
      if (var0 == null) {
         var1 = "LISTING_SCAN_RESULT none";
      } else {
         long var2 = longField(var0, "price", -1L);
         int var4 = intField(var0, "slot", -1);
         var1 = "LISTING_SCAN_RESULT slot=" + var4 + " price=" + var2 + " class=" + var0.getClass().getName();
      }

      if (!Objects.equals(var1, lastScan)) {
         log(var1);
         lastScan = var1;
      }
   }

   public static void listingClick(int var0) {
      log("LISTING_CLICK_SENT slot=" + var0);
   }

   public static void screenText(String var0) {
      String var1 = compact(var0, 420);
      if (!Objects.equals(var1, lastScreenText)) {
         log("CONFIRM_SCREEN_TEXT " + var1);
         lastScreenText = var1;
      }
   }

   public static void pressYes(boolean var0) {
      log("CONFIRM_PRESS_YES result=" + var0);
   }

   public static void command(String var0) {
      log("COMMAND " + var0);
   }

   public static void retry(String var0) {
      log("RETRY reason=" + compact(var0, 220));
   }

   public static void swordObserved(int var0) {
      log("SWORD_OBSERVED inventorySlot=" + var0);
   }

   private static String stateName(int var0) {
      return switch (var0) {
         case 0 -> "IDLE";
         case 1 -> "WAIT_AFTER_HOME";
         case 2 -> "SELL_BEFORE_AH";
         case 3 -> "WAIT_FREE_SLOT";
         case 4 -> "OPEN_AH";
         case 5 -> "WAIT_AH_GUI";
         case 6 -> "WAIT_CONFIRM";
         case 7 -> "WAIT_PURCHASE";
         case 8 -> "WAIT_RETRY";
         default -> "UNKNOWN";
      };
   }

   private static int intField(Object var0, String var1, int var2) {
      try {
         Field var3 = var0.getClass().getDeclaredField(var1);
         var3.setAccessible(true);
         return var3.getInt(var0);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static long longField(Object var0, String var1, long var2) {
      try {
         Field var4 = var0.getClass().getDeclaredField(var1);
         var4.setAccessible(true);
         return var4.getLong(var0);
      } catch (Throwable var5) {
         return var2;
      }
   }

   private static String compact(String var0, int var1) {
      if (var0 == null) {
         return "null";
      }

      String var2 = var0.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replaceAll("\\s+", " ").trim();
      return var2.length() <= var1 ? var2 : var2.substring(0, var1) + "...";
   }
}
