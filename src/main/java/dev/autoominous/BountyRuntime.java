package dev.autoominous;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BountyRuntime {
   private static final Pattern BALANCE_PATTERN = Pattern.compile("You\\s+have\\s+\\$\\s*([0-9,]+)", 2);
   private static final Pattern SUCCESS_PATTERN = Pattern.compile("You\\s+added\\s+\\$\\s*([^\\s]+)\\s+to\\s+(.+?)'s\\s+bounty", 2);
   private static final long RESPONSE_TIMEOUT_MS = 5000L;
   private static final long CONFIRM_TIMEOUT_MS = 5000L;
   private static final long SUCCESS_TIMEOUT_MS = 5000L;
   private static BountyRuntime.State state = BountyRuntime.State.OFF;
   private static boolean enabledKnown;
   private static boolean lastEnabled;
   private static long deadlineAtMs;
   private static long nextRunAtMs;
   private static long currentBalance = -1L;
   private static String currentBountyAmount = "-";
   private static String lastAdded = "-";
   private static String status = "OFF";
   private static BountyMinecraftBridge bridge;
   private static BountyMinecraftBridge.ConfirmCandidate confirmCandidate;

   private BountyRuntime() {
   }

   public static void onTick() {
      BountySettings.maybePersist();
      boolean var0 = BountySettings.isEnabled();
      long var1 = System.currentTimeMillis();
      if (!enabledKnown) {
         enabledKnown = true;
         lastEnabled = var0;
         if (var0) {
            resetAndStart(var1, false);
         } else {
            state = BountyRuntime.State.OFF;
         }
      } else if (var0 != lastEnabled) {
         lastEnabled = var0;
         ClientStatusMessage.actionbar("Auto Bounty " + (var0 ? "§aON" : "§cOFF"));
         if (var0) {
            resetAndStart(var1, true);
         } else {
            disable();
         }
      }

      if (var0) {
         String var3 = BountySettings.getTarget();
         if (!var3.matches("[A-Za-z0-9_]{1,32}")) {
            status = "Invalid target username";
            state = BountyRuntime.State.WAITING_INTERVAL;
            nextRunAtMs = var1 + minutesToMs(BountySettings.getIntervalMinutes());
         } else {
            try {
               BountyMinecraftBridge var4 = bridge();
               Object var5 = var4.getClient();
               switch (state) {
                  case OFF:
                     resetAndStart(var1, false);
                     break;
                  case WAITING_INTERVAL:
                     if (var1 >= nextRunAtMs) {
                        if (DeathRecoveryRuntime.isRecoveryPending()) {
                           status = "Waiting for Death Recovery";
                           nextRunAtMs = var1 + 1000L;
                        } else {
                           var4.closeAnyHandledScreen(var5);
                           requestBalance(var5, var1, "Checking balance...");
                        }
                     }
                     break;
                  case WAITING_BALANCE:
                     if (var1 >= deadlineAtMs) {
                        scheduleRetry("No /balance response", var1);
                     }
                     break;
                  case WAITING_COMMAND_DELAY:
                     if (var1 >= deadlineAtMs) {
                        var4.closeAnyHandledScreen(var5);
                        sendBountyCommand(var5, var1);
                     }
                     break;
                  case WAITING_CONFIRM_GUI:
                     BountyMinecraftBridge.ConfirmCandidate var6 = var4.findConfirm(var5);
                     if (var6 != null) {
                        confirmCandidate = var6;
                        state = BountyRuntime.State.WAITING_CONFIRM_DELAY;
                        deadlineAtMs = var1 + secondsToMs(BountySettings.getConfirmDelaySeconds());
                        status = "Confirm found";
                     } else if (var1 >= deadlineAtMs) {
                        scheduleRetry("Confirm Bounty GUI not found", var1);
                     }
                     break;
                  case WAITING_CONFIRM_DELAY:
                     if (var1 >= deadlineAtMs) {
                        if (var4.clickConfirm(var5, confirmCandidate)) {
                           state = BountyRuntime.State.WAITING_SUCCESS;
                           deadlineAtMs = var1 + 5000L;
                           status = "Waiting for bounty SUCCESS";
                        } else {
                           scheduleRetry("Confirm button disappeared", var1);
                        }
                     }
                     break;
                  case WAITING_SUCCESS:
                     if (var1 >= deadlineAtMs) {
                        scheduleRetry("No bounty success message", var1);
                     }
                     break;
                  case RETRY_WAIT:
                     if (var1 >= deadlineAtMs) {
                        var4.closeAnyHandledScreen(var5);
                        requestBalance(var5, var1, "Retrying: checking balance...");
                     }
               }
            } catch (Throwable var7) {
               System.err.println("[DonutUtility/Bounty] Tick failed: " + var7);
               scheduleRetry("Minecraft action failed", var1);
            }
         }
      }
   }

   public static void onGameMessageObject(Object var0) {
      if (BountySettings.isEnabled() && var0 != null) {
         try {
            onGameMessage(bridge().textToString(var0));
         } catch (Throwable var2) {
            System.err.println("[DonutUtility/Bounty] Message parse failed: " + var2);
         }
      }
   }

   private static void onGameMessage(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();
         long var2 = System.currentTimeMillis();
         Matcher var4 = SUCCESS_PATTERN.matcher(var1);
         if (var4.find() && isBlockingSell()) {
            String var5 = var4.group(2).trim();
            if (var5.equalsIgnoreCase(BountySettings.getTarget())) {
               lastAdded = currentBountyAmount;
               status = "SUCCESS: " + currentBountyAmount + " -> " + var5;
               state = BountyRuntime.State.WAITING_INTERVAL;
               nextRunAtMs = var2 + minutesToMs(BountySettings.getIntervalMinutes());
               deadlineAtMs = 0L;
               confirmCandidate = null;
               return;
            }
         }

         if (state == BountyRuntime.State.WAITING_BALANCE) {
            Matcher var8 = BALANCE_PATTERN.matcher(var1);
            if (var8.find()) {
               try {
                  currentBalance = Long.parseLong(var8.group(1).replace(",", ""));
               } catch (NumberFormatException var7) {
                  scheduleRetry("Could not parse balance", var2);
                  return;
               }

               currentBountyAmount = BountyMoney.toDonutMillion(currentBalance);
               if (currentBountyAmount == null) {
                  status = "Balance < $1M; waiting next interval";
                  state = BountyRuntime.State.WAITING_INTERVAL;
                  nextRunAtMs = var2 + minutesToMs(BountySettings.getIntervalMinutes());
                  deadlineAtMs = 0L;
                  confirmCandidate = null;
               } else {
                  state = BountyRuntime.State.WAITING_COMMAND_DELAY;
                  deadlineAtMs = var2 + secondsToMs(BountySettings.getCommandDelaySeconds());
                  status = "Waiting command delay (" + fmt(BountySettings.getCommandDelaySeconds()) + "s)";
                  confirmCandidate = null;
               }
            }
         }
      }
   }

   public static boolean isBlockingSell() {
      if (BountySettings.isEnabled()) {
         switch (state) {
            case WAITING_BALANCE:
            case WAITING_COMMAND_DELAY:
            case WAITING_CONFIRM_GUI:
            case WAITING_CONFIRM_DELAY:
            case WAITING_SUCCESS:
            case RETRY_WAIT:
               return true;
         }
      }

      return false;
   }

   public static String getStatusLine() {
      return "Bounty: " + status + " | Balance " + balanceText() + " | Next " + countdownText();
   }

   public static String balanceText() {
      return currentBalance < 0L ? "-" : "$" + NumberFormat.getIntegerInstance(Locale.US).format(currentBalance);
   }

   public static String countdownText() {
      long var0 = state == BountyRuntime.State.WAITING_INTERVAL ? nextRunAtMs : deadlineAtMs;
      if (var0 <= 0L) {
         return "-";
      } else {
         long var2 = (Math.max(0L, var0 - System.currentTimeMillis()) + 999L) / 1000L;
         return String.format(Locale.ROOT, "%02d:%02d", var2 / 60L, var2 % 60L);
      }
   }

   public static String getLastAdded() {
      return lastAdded;
   }

   public static String getCurrentAmount() {
      return currentBountyAmount;
   }

   public static boolean isRetrying() {
      return BountySettings.isEnabled() && state == BountyRuntime.State.RETRY_WAIT;
   }

   public static String getRetryCountdownText() {
      return isRetrying() ? countdownText() : "--";
   }

   public static String getNextBountyCountdownText() {
      if (!BountySettings.isEnabled()) {
         return "--";
      } else if (state == BountyRuntime.State.WAITING_INTERVAL) {
         return countdownText();
      } else {
         return state == BountyRuntime.State.RETRY_WAIT ? "--" : "00:00";
      }
   }

   private static void resetAndStart(long var0, boolean var2) {
      state = BountyRuntime.State.WAITING_BALANCE;
      deadlineAtMs = var0 + 5000L;
      nextRunAtMs = 0L;
      confirmCandidate = null;
      currentBountyAmount = "-";
      status = "Starting...";

      try {
         BountyMinecraftBridge var3 = bridge();
         Object var4 = var3.getClient();
         var3.closeAnyHandledScreen(var4);
         requestBalance(var4, var0, "Checking balance...");
      } catch (Throwable var5) {
         scheduleRetry("Could not start bounty", var0);
      }
   }

   private static void disable() {
      try {
         if (bridge != null) {
            bridge.closeConfirmIfOpen(bridge.getClient());
         }
      } catch (Throwable var1) {
      }

      state = BountyRuntime.State.OFF;
      deadlineAtMs = 0L;
      nextRunAtMs = 0L;
      confirmCandidate = null;
      status = "OFF";
   }

   private static void sendBountyCommand(Object var0, long var1) {
      try {
         BountyMinecraftBridge var3 = bridge();
         if (!var3.isConnected(var0)) {
            scheduleRetry("Disconnected before bounty command", var1);
            return;
         }

         var3.closeAnyHandledScreen(var0);
         var3.sendCommand(var0, "bounty add " + BountySettings.getTarget() + " " + currentBountyAmount);
         state = BountyRuntime.State.WAITING_CONFIRM_GUI;
         status = "Waiting for Confirm Bounty GUI";
         deadlineAtMs = var1 + 5000L;
         confirmCandidate = null;
      } catch (Throwable var4) {
         scheduleRetry("Could not send bounty command", var1);
      }
   }

   private static void requestBalance(Object var0, long var1, String var3) throws Exception {
      BountyMinecraftBridge var4 = bridge();
      if (!var4.isConnected(var0)) {
         scheduleRetry("Disconnected; waiting to retry", var1);
      } else {
         var4.closeAnyHandledScreen(var0);
         var4.sendCommand(var0, "balance");
         state = BountyRuntime.State.WAITING_BALANCE;
         status = var3;
         deadlineAtMs = var1 + 5000L;
         nextRunAtMs = 0L;
         confirmCandidate = null;
      }
   }

   private static void scheduleRetry(String var0, long var1) {
      if (!BountySettings.isEnabled()) {
         disable();
      } else {
         state = BountyRuntime.State.RETRY_WAIT;
         status = var0 + "; retry in " + fmt(BountySettings.getRetryDelaySeconds()) + "s";
         deadlineAtMs = var1 + secondsToMs(BountySettings.getRetryDelaySeconds());
         nextRunAtMs = 0L;
         confirmCandidate = null;
      }
   }

   private static BountyMinecraftBridge bridge() throws Exception {
      if (bridge == null) {
         bridge = new BountyMinecraftBridge();
      }

      return bridge;
   }

   private static long secondsToMs(double var0) {
      return Math.max(0L, Math.round(var0 * 1000.0));
   }

   private static long minutesToMs(double var0) {
      return Math.max(1L, Math.round(var0 * 60000.0));
   }

   private static String fmt(double var0) {
      return Math.rint(var0) == var0 ? Long.toString(Math.round(var0)) : Double.toString(var0);
   }

   private static enum State {
      OFF,
      WAITING_INTERVAL,
      WAITING_BALANCE,
      WAITING_COMMAND_DELAY,
      WAITING_CONFIRM_GUI,
      WAITING_CONFIRM_DELAY,
      WAITING_SUCCESS,
      RETRY_WAIT;
   }
}
