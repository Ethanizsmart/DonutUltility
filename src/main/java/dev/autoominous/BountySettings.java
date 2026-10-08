package dev.autoominous;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BountySettings implements IConfigHandler {
   public static final ConfigBooleanHotkeyed AUTO_BOUNTY = new ConfigBooleanHotkeyed("Auto Bounty", false, "");
   public static final ConfigString TARGET_USERNAME = new ConfigString("Target Username", "");
   public static final ConfigDouble INTERVAL_MINUTES = new ConfigDouble("Interval (minutes)", 7.0, 0.1, 1440.0);
   public static final ConfigDouble RETRY_DELAY_SECONDS = new ConfigDouble("Retry Delay (seconds)", 7.0, 0.1, 3600.0);
   public static final ConfigDouble COMMAND_DELAY_SECONDS = new ConfigDouble("Command Delay (seconds)", 0.5, 0.0, 60.0);
   public static final ConfigDouble CONFIRM_DELAY_SECONDS = new ConfigDouble("Confirm Delay (seconds)", 1.0, 0.0, 60.0);
   public static final List<IConfigBase> GENERIC_OPTIONS = List.of(
      TARGET_USERNAME, INTERVAL_MINUTES, RETRY_DELAY_SECONDS, COMMAND_DELAY_SECONDS, CONFIRM_DELAY_SECONDS
   );
   public static final List<ConfigBooleanHotkeyed> TOGGLE_OPTIONS = List.of(AUTO_BOUNTY);
   public static final List<IConfigBase> ALL_OPTIONS = List.of(
      AUTO_BOUNTY, TARGET_USERNAME, INTERVAL_MINUTES, RETRY_DELAY_SECONDS, COMMAND_DELAY_SECONDS, CONFIRM_DELAY_SECONDS
   );
   private static boolean initialized;
   private static boolean lastEnabled;
   private static String lastTarget = "";
   private static double lastInterval;
   private static double lastRetry;
   private static double lastCommandDelay;
   private static double lastConfirm;
   private static long lastPeriodicSaveAt;

   public static boolean isEnabled() {
      return AUTO_BOUNTY.getBooleanValue();
   }

   public static String getTarget() {
      String var0 = TARGET_USERNAME.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   public static double getIntervalMinutes() {
      return clamp(INTERVAL_MINUTES.getDoubleValue(), 0.1, 1440.0, 7.0);
   }

   public static double getRetryDelaySeconds() {
      return clamp(RETRY_DELAY_SECONDS.getDoubleValue(), 0.1, 3600.0, 7.0);
   }

   public static double getCommandDelaySeconds() {
      return clamp(COMMAND_DELAY_SECONDS.getDoubleValue(), 0.0, 60.0, 0.5);
   }

   public static double getConfirmDelaySeconds() {
      return clamp(CONFIRM_DELAY_SECONDS.getDoubleValue(), 0.0, 60.0, 1.0);
   }

   public static void maybePersist() {
      long var0 = System.currentTimeMillis();
      if (!initialized) {
         snapshot();
         initialized = true;
         lastPeriodicSaveAt = var0;
      } else {
         boolean var2 = isEnabled() != lastEnabled
            || !getTarget().equals(lastTarget)
            || Double.compare(getIntervalMinutes(), lastInterval) != 0
            || Double.compare(getRetryDelaySeconds(), lastRetry) != 0
            || Double.compare(getCommandDelaySeconds(), lastCommandDelay) != 0
            || Double.compare(getConfirmDelaySeconds(), lastConfirm) != 0;
         if (var2 || var0 - lastPeriodicSaveAt >= 60000L) {
            saveNow();
            lastPeriodicSaveAt = var0;
         }
      }
   }

   public static void saveNow() {
      try {
         new BountySettings().save();
      } catch (Throwable var1) {
      }

      snapshot();
   }

   private static void snapshot() {
      lastEnabled = isEnabled();
      lastTarget = getTarget();
      lastInterval = getIntervalMinutes();
      lastRetry = getRetryDelaySeconds();
      lastCommandDelay = getCommandDelaySeconds();
      lastConfirm = getConfirmDelaySeconds();
   }

   public void load() {
      try {
         Path var1 = FileUtils.getConfigDirectoryAsPath().resolve("donututility-bounty.json");
         if (Files.exists(var1) && Files.isReadable(var1)) {
            JsonElement var2 = JsonUtils.parseJsonFileAsPath(var1);
            if (var2 != null && var2.isJsonObject()) {
               JsonObject var3 = var2.getAsJsonObject();
               ConfigUtils.readConfigBase(var3, "Generic", GENERIC_OPTIONS);
               ConfigUtils.readHotkeyToggleOptions(var3, "ToggleHotkeys", "Toggles", TOGGLE_OPTIONS);
            }
         }
      } catch (Throwable var4) {
      }

      snapshot();
      initialized = true;
      lastPeriodicSaveAt = System.currentTimeMillis();
   }

   public void save() {
      try {
         Path var1 = FileUtils.getConfigDirectoryAsPath();

         try {
            Files.createDirectories(var1);
         } catch (Exception var3) {
         }

         JsonObject var2 = new JsonObject();
         ConfigUtils.writeConfigBase(var2, "Generic", GENERIC_OPTIONS);
         ConfigUtils.writeHotkeyToggleOptions(var2, "ToggleHotkeys", "Toggles", TOGGLE_OPTIONS);
         JsonUtils.writeJsonToFileAsPath(var2, var1.resolve("donututility-bounty.json"));
      } catch (Throwable var4) {
      }
   }

   public void onLanguageChanged(String var1) {
   }

   private static double clamp(double var0, double var2, double var4, double var6) {
      return !Double.isFinite(var0) ? var6 : Math.max(var2, Math.min(var4, var0));
   }
}
