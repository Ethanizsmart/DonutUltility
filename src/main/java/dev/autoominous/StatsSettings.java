package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class StatsSettings {
   public static final ConfigBoolean AUTO_SELL_STATS = new ConfigBoolean("/sell Profit Stats", false);
   public static final ConfigInteger HUD_X = new ConfigInteger("HUD X", 8, -10000, 10000, "Horizontal HUD position in pixels.");
   public static final ConfigInteger HUD_Y = new ConfigInteger("HUD Y", 8, -10000, 10000, "Vertical HUD position in pixels.");
   public static final List<IConfigBase> ALL_OPTIONS = List.of(AUTO_SELL_STATS, HUD_X, HUD_Y);
   private static boolean initialized;
   private static boolean lastEnabled;
   private static int lastHudX;
   private static int lastHudY;

   private StatsSettings() {
   }

   public static boolean isEnabled() {
      return AUTO_SELL_STATS.getBooleanValue();
   }

   public static int getHudX() {
      return HUD_X.getIntegerValue();
   }

   public static int getHudY() {
      return HUD_Y.getIntegerValue();
   }

   public static void maybePersist() {
      if (initialized) {
         if (isEnabled() != lastEnabled || getHudX() != lastHudX || getHudY() != lastHudY) {
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
         var2.setProperty("hudX", Integer.toString(getHudX()));
         var2.setProperty("hudY", Integer.toString(getHudY()));

         try (BufferedWriter var3 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var2.store(var3, "Auto Ominous - /sell Profit Stats");
         }

         snapshot();
      } catch (Throwable var8) {
         System.err.println("[Auto Ominous] Failed to save /sell stats settings: " + var8.getMessage());
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

         AUTO_SELL_STATS.setBooleanValue(Boolean.parseBoolean(var1.getProperty("enabled", "false")));
         HUD_X.setIntegerValue(parseInt(var1.getProperty("hudX"), 8));
         HUD_Y.setIntegerValue(parseInt(var1.getProperty("hudY"), 8));
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Failed to load /sell stats settings: " + var7.getMessage());
      }
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0 == null ? "" : var0.trim());
      } catch (Throwable var3) {
         return var1;
      }
   }

   private static void snapshot() {
      lastEnabled = isEnabled();
      lastHudX = getHudX();
      lastHudY = getHudY();
   }

   private static Path configPath() {
      try {
         Path var0 = FileUtils.getConfigDirectoryAsPath();
         if (var0 != null) {
            return var0.resolve("autoominous-stats.properties");
         }
      } catch (Throwable var1) {
      }

      return Paths.get(System.getProperty("user.dir", "."), "config", "autoominous-stats.properties");
   }

   static {
      loadFromDisk();
      snapshot();
      initialized = true;
   }
}
