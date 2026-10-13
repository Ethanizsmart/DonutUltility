package dev.autoominous;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

final class BehaviorLogRuntime {
   private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());
   private static final DateTimeFormatter LINE_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneId.systemDefault());
   private static final Map<String, String> LAST = new LinkedHashMap<>();
   private static BufferedWriter writer;
   private static Path file;
   private static boolean started;
   private static long lastFlushMs;
   private static long lastInventorySignature = Long.MIN_VALUE;
   private static long sessionStartedMs;
   private static String cachedAccount = "unknown";
   private static String lastPhase = "";

   private BehaviorLogRuntime() {
   }

   static synchronized void sessionEvent(String var0) {
      ensureStarted();
      write("SESSION", var0, true);
   }

   static synchronized void phase(String var0, String var1, String var2) {
      ensureStarted();
      lastPhase = var1 == null ? "" : var1;
      write("PHASE", "from=" + safe(var0) + " to=" + safe(var1) + " reason=" + safe(var2), true);
   }

   static synchronized void warning(String var0, String var1) {
      ensureStarted();
      String var2 = "warn:" + safe(var0);
      String var3 = safe(var1);
      String var4 = LAST.put(var2, var3);
      if (var4 == null || !var4.equals(var3)) {
         write("WARN", "code=" + safe(var0) + " " + var3, true);
      }
   }

   static synchronized void error(String var0, String var1) {
      ensureStarted();
      write("ERROR", "code=" + safe(var0) + " " + safe(var1), true);
   }

   static synchronized void exception(String var0, Throwable var1) {
      ensureStarted();
      String var2 = var1 == null ? "null" : var1.getClass().getName();
      String var3 = var1 == null ? "" : String.valueOf(var1.getMessage());
      write("EXCEPTION", "where=" + safe(var0) + " type=" + safe(var2) + " msg=" + safe(var3), true);
   }

   static synchronized void poll(FarmCycleRuntime.Snapshot var0, String var1, long var2, long var4) {
      ensureStarted();
      cachedAccount = accountName();
      if (var0 != null && var0.signature() != lastInventorySignature) {
         lastInventorySignature = var0.signature();
         write(
            "INVENTORY",
            "signature="
               + var0.signature()
               + " swordHotbar="
               + var0.swordHotbar()
               + " swordAny="
               + var0.swordAny()
               + " omiHotbar="
               + var0.omiHotbar()
               + " omiAny="
               + var0.omiAny()
               + " fireworkHotbar="
               + var0.fireworkHotbar()
               + " fireworkAny="
               + var0.fireworkAny()
               + " sellable="
               + var0.sellable()
               + " slots="
               + safe(var0.inventorySummary()),
            false
         );
      }

      stateChange("sell", staticString("com.seastore.auto.seaats.client.AutoSellLogic", "currentState"), "SELL_STATE");
      stateChange("sellCommand", staticString("com.seastore.auto.seaats.client.AutoSellLogic", "lastSellCommandSentMs"), "COMMAND_SELL");
      stateChange(
         "sellRetry",
         "retry="
            + staticString("com.seastore.auto.seaats.client.AutoSellLogic", "sellCommandRetryCount")
            + ",failRounds="
            + staticString("com.seastore.auto.seaats.client.AutoSellLogic", "sellFailureRounds"),
         "SELL_RETRY"
      );
      stateChange("anti", staticString("com.seastore.auto.seaats.client.AutoSellLogic", "antiState"), "ANTI_STATE");
      stateChange("ghost", boolState("com.seastore.auto.seaats.client.AutoSellLogic", "isGhostFixing"), "GHOST_FIX");
      stateChange("homeRecovering", boolState("com.seastore.auto.seaats.client.AutoSellLogic", "isHomeRecovering"), "SEA_HOME_RECOVERY");
      stateChange(
         "bounty", staticString("dev.autoominous.BountyRuntime", "state") + ":" + staticString("dev.autoominous.BountyRuntime", "status"), "BOUNTY_STATE"
      );
      stateChange(
         "bountyTiming",
         "amount="
            + staticString("dev.autoominous.BountyRuntime", "currentBountyAmount")
            + ",deadline="
            + staticString("dev.autoominous.BountyRuntime", "deadlineAtMs")
            + ",nextRun="
            + staticString("dev.autoominous.BountyRuntime", "nextRunAtMs"),
         "BOUNTY_TIMING"
      );
      stateChange(
         "tpa",
         "pending="
            + staticString("dev.autoominous.AutoTpaRuntime", "pendingUser")
            + ",pendingAt="
            + staticString("dev.autoominous.AutoTpaRuntime", "pendingAtMillis")
            + ",confirm="
            + boolState("dev.autoominous.AutoTpaRuntime", "awaitingConfirm")
            + ",confirmDeadline="
            + staticString("dev.autoominous.AutoTpaRuntime", "confirmDeadlineMillis"),
         "TPA_STATE"
      );
      stateChange(
         "home",
         staticString("dev.autoominous.HomePositionGuardRuntime", "transit")
            + ",pending="
            + boolCall("dev.autoominous.HomePositionGuardRuntime", "isRecoveryPending")
            + ",homeSends="
            + staticString("dev.autoominous.HomePositionGuardRuntime", "homeSendCount")
            + ",transitAt="
            + staticString("dev.autoominous.HomePositionGuardRuntime", "transitStartedAtMs"),
         "HOME_STATE"
      );
      stateChange(
         "death",
         boolCall("dev.autoominous.DeathRecoveryRuntime", "isRecoveryPending")
            + ",homeSent="
            + boolState("dev.autoominous.DeathRecoveryRuntime", "homeCommandSent")
            + ",homeSentAt="
            + staticString("dev.autoominous.DeathRecoveryRuntime", "homeCommandSentAtMs"),
         "DEATH_RECOVERY"
      );
      stateChange(
         "swordRecovery",
         staticString("dev.autoominous.SwordRecoveryRuntime", "state")
            + ",blocking="
            + boolCall("dev.autoominous.SwordRecoveryRuntime", "isRecoveryBlockingAutoSell"),
         "SWORD_RECOVERY"
      );
      stateChange(
         "reconnect",
         "triggered="
            + boolState("dev.autoominous.AutoReconnectRuntime", "wasTriggered")
            + ",nextAt="
            + staticString("dev.autoominous.AutoReconnectRuntime", "nextAttemptAtMs"),
         "RECONNECT_STATE"
      );
      stateChange(
         "rmb",
         "pressed="
            + boolState("dev.autoominous.ProtectedItemAutoUseRuntime", "pressedByUs")
            + ",priority="
            + staticString("dev.autoominous.ProtectedItemAutoUseRuntime", "activePriority")
            + ",slot="
            + staticString("dev.autoominous.ProtectedItemAutoUseRuntime", "activeSlot"),
         "RMB_STATE"
      );
      stateChange("core", callString("dev.autoominous.CoreV2RuntimeBridge", "statusLine"), "CORE_STATE");
      if (var1 != null && !var1.equals(lastPhase)) {
         String var6 = lastPhase;
         lastPhase = var1;
         write("PHASE_OBSERVED", "from=" + safe(var6) + " to=" + safe(var1), false);
      }

      long var9 = Math.max(0L, var4 - var2);
      String var8 = watchdogBucket(var1, var9);
      stateChange("watchdog:" + safe(var1), var8, "WATCHDOG");
      flushMaybe(false);
   }

   static synchronized String filePath() {
      ensureStarted();
      return file == null ? "" : file.toAbsolutePath().toString();
   }

   private static void ensureStarted() {
      if (!started) {
         started = true;
         sessionStartedMs = System.currentTimeMillis();
         cachedAccount = accountName();

         try {
            Path var0 = Path.of("logs", "donututility");
            Files.createDirectories(var0);
            file = var0.resolve("farm-actions-" + FILE_TS.format(Instant.ofEpochMilli(sessionStartedMs)) + ".jsonl");
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            write("SESSION_START", "log=" + file.toAbsolutePath(), true);
         } catch (Throwable var1) {
            writer = null;
            System.err.println("[DonutUtility Log] Cannot open action log: " + var1);
         }
      }
   }

   private static void stateChange(String var0, String var1, String var2) {
      var1 = safe(var1);
      String var3 = LAST.put(var0, var1);
      if (var3 == null || !var3.equals(var1)) {
         write(var2, "from=" + safe(var3) + " to=" + var1, false);
      }
   }

   private static void write(String var0, String var1, boolean var2) {
      long var3 = System.currentTimeMillis();
      String var5 = safePhase();
      String var6 = "{\"time\":\""
         + json(LINE_TS.format(Instant.ofEpochMilli(var3)))
         + "\",\"epochMs\":"
         + var3
         + ",\"sessionMs\":"
         + Math.max(0L, var3 - sessionStartedMs)
         + ",\"account\":\""
         + json(cachedAccount)
         + "\",\"event\":\""
         + json(var0)
         + "\",\"phase\":\""
         + json(var5)
         + "\",\"detail\":\""
         + json(var1)
         + "\"}";

      try {
         if (writer != null) {
            writer.write(var6);
            writer.newLine();
         }

         if (var2) {
            flushMaybe(true);
         }
      } catch (Throwable var8) {
         System.err.println("[DonutUtility Log] write failed: " + var8);
      }
   }

   private static void flushMaybe(boolean var0) {
      long var1 = System.currentTimeMillis();
      if (var0 || var1 - lastFlushMs >= 2000L) {
         lastFlushMs = var1;

         try {
            if (writer != null) {
               writer.flush();
            }
         } catch (IOException var4) {
         }
      }
   }

   private static String safePhase() {
      try {
         String var0 = FarmCycleRuntime.phaseName();
         return var0 == null ? "" : var0;
      } catch (Throwable var1) {
         return lastPhase;
      }
   }

   private static String accountName() {
      try {
         Object var0 = ReflectionSupport.mc();
         Object var1 = var0 == null ? null : ReflectionSupport.field(var0, "field_1724");
         if (var1 == null) {
            return cachedAccount == null ? "unknown" : cachedAccount;
         }

         Object var2 = ReflectionSupport.callNoArg(var1, "method_5477");
         if (var2 != null) {
            Object var3 = ReflectionSupport.callNoArg(var2, "getString");
            if (var3 != null && !"null".equals(String.valueOf(var3))) {
               return String.valueOf(var3);
            }

            return String.valueOf(var2);
         }
      } catch (Throwable var4) {
      }

      return cachedAccount == null ? "unknown" : cachedAccount;
   }

   private static String staticString(String var0, String var1) {
      try {
         Object var2 = ReflectionSupport.staticField(var0, var1);
         return String.valueOf(var2);
      } catch (Throwable var3) {
         return "?";
      }
   }

   private static String boolState(String var0, String var1) {
      try {
         return String.valueOf(ReflectionSupport.staticBoolean(var0, var1, false));
      } catch (Throwable var3) {
         return "?";
      }
   }

   private static String boolCall(String var0, String var1) {
      try {
         return String.valueOf(ReflectionSupport.callStaticBoolean(var0, var1, false));
      } catch (Throwable var3) {
         return "?";
      }
   }

   private static String callString(String var0, String var1) {
      try {
         Object var2 = ReflectionSupport.callStatic(var0, var1, new Class[0]);
         return String.valueOf(var2);
      } catch (Throwable var3) {
         return "?";
      }
   }

   private static String watchdogBucket(String var0, long var1) {
      if (var0 == null) {
         return "none";
      }

      long var3 = switch (var0) {
         case "STORE_SWORD", "DRINK_OMI", "USE_FIREWORK" -> 5000L;
         case "WAIT_OMI", "WAIT_FIREWORK" -> 8000L;
         case "MACHINE_RUNNING" -> 35000L;
         case "WAIT_RETURN" -> 5000L;
         case "LOOT_SETTLE" -> 3000L;
         case "SELL" -> 22000L;
         case "VERIFY_SWORD" -> 1500L;
         case "STORE_RETURNED_SWORD" -> 5000L;
         case "WAIT_FIRST_SWORD" -> 15000L;
         default -> Long.MAX_VALUE;
      };
      if (var1 < var3) {
         return "ok";
      } else {
         return var1 < var3 + 5000L ? "slow:" + var1 : "stuck:" + var1;
      }
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0.replace('\n', ' ').replace('\r', ' ');
   }

   private static String json(String var0) {
      if (var0 == null) {
         return "";
      }

      StringBuilder var1 = new StringBuilder(var0.length() + 16);

      for (int var2 = 0; var2 < var0.length(); var2++) {
         char var3 = var0.charAt(var2);
         switch (var3) {
            case '\t':
               var1.append("\\t");
               break;
            case '\n':
               var1.append("\\n");
               break;
            case '\r':
               var1.append("\\r");
               break;
            case '"':
               var1.append("\\\"");
               break;
            case '\\':
               var1.append("\\\\");
               break;
            default:
               if (var3 < ' ') {
                  var1.append(String.format("\\u%04x", Integer.valueOf(var3)));
               } else {
                  var1.append(var3);
               }
         }
      }

      return var1.toString();
   }
}
