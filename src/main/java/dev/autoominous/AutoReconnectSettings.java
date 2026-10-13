package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class AutoReconnectSettings {
   public static final ConfigBoolean AUTO_RECONNECT = new ConfigBoolean("Auto Reconnect", false);
   public static final ConfigDouble RECONNECT_DELAY_SECONDS = new ConfigDouble("Reconnect Delay (seconds)", 5.0, 0.5, 3600.0);
   public static final ConfigInteger HUD_X = new ConfigInteger("Reconnect HUD X", 8, -10000, 10000, "Horizontal position for the Auto Reconnect HUD.");
   public static final ConfigInteger HUD_Y = new ConfigInteger("Reconnect HUD Y", 56, -10000, 10000, "Vertical position for the Auto Reconnect HUD.");
   public static final List<IConfigBase> ALL_OPTIONS = List.of(AUTO_RECONNECT, RECONNECT_DELAY_SECONDS, HUD_X, HUD_Y);
   private static boolean initialized = true;
   private static boolean lastEnabled;
   private static double lastDelay;
   private static int lastHudX;
   private static int lastHudY;

   private AutoReconnectSettings() {
   }

   public static boolean isEnabled() {
      return AUTO_RECONNECT.getBooleanValue();
   }

   public static double getReconnectDelaySeconds() {
      double var0 = RECONNECT_DELAY_SECONDS.getDoubleValue();
      return !Double.isFinite(var0) ? 5.0 : Math.max(0.5, Math.min(3600.0, var0));
   }

   public static int getHudX() {
      return HUD_X.getIntegerValue();
   }

   public static int getHudY() {
      return HUD_Y.getIntegerValue();
   }

   public static void maybePersist() {
      if (initialized) {
         if (isEnabled() != lastEnabled || Double.compare(getReconnectDelaySeconds(), lastDelay) != 0 || getHudX() != lastHudX || getHudY() != lastHudY) {
            saveNow();
         }
      }
   }

   public static void saveNow() {
      try {
         Path var0 = configPath();
         Path var1 = var0.getParent();
         if (var1 != null) {
            Files.createDirectories(var1);
         }

         Properties var2 = new Properties();
         var2.setProperty("enabled", Boolean.toString(isEnabled()));
         var2.setProperty("delaySeconds", Double.toString(getReconnectDelaySeconds()));
         var2.setProperty("hudX", Integer.toString(getHudX()));
         var2.setProperty("hudY", Integer.toString(getHudY()));

         try (BufferedWriter var3 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var2.store(var3, "Donut Utility - Auto Reconnect");
         }

         snapshot();
      } catch (Throwable var8) {
         System.err.println("[DonutUtility/AutoReconnect] Could not save settings: " + var8.getMessage());
      }
   }

   private static void loadFromDisk() {
      try {
         Path var0 = configPath();
         if (!Files.isReadable(var0)) {
            return;
         }

         Properties var1 = new Properties();

         try (BufferedReader var2 = Files.newBufferedReader(var0, StandardCharsets.UTF_8)) {
            var1.load(var2);
         }

         AUTO_RECONNECT.setBooleanValue(Boolean.parseBoolean(var1.getProperty("enabled", "false")));
         RECONNECT_DELAY_SECONDS.setDoubleValue(clamp(parseDouble(var1.getProperty("delaySeconds"), 5.0), 0.5, 3600.0));
         HUD_X.setIntegerValue(parseInt(var1.getProperty("hudX"), 8));
         HUD_Y.setIntegerValue(parseInt(var1.getProperty("hudY"), 56));
      } catch (Throwable var7) {
         System.err.println("[DonutUtility/AutoReconnect] Could not load settings: " + var7.getMessage());
      }
   }

   private static void snapshot() {
      lastEnabled = isEnabled();
      lastDelay = getReconnectDelaySeconds();
      lastHudX = getHudX();
      lastHudY = getHudY();
   }

   private static Path configPath() {
      return Paths.get(System.getProperty("user.dir", "."), "config", "donututility-autoreconnect.properties");
   }

   private static double parseDouble(String var0, double var1) {
      try {
         return Double.parseDouble(var0 == null ? "" : var0.trim());
      } catch (Exception var4) {
         return var1;
      }
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0 == null ? "" : var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }

   private static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }

   static {
      loadFromDisk();
      snapshot();
   }
}
