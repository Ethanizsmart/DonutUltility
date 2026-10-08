package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class BountyHudSettings {
   public static final ConfigInteger HUD_X = new ConfigInteger("Bounty HUD X", 8, -10000, 10000, "Horizontal position for the Auto Bounty HUD.");
   public static final ConfigInteger HUD_Y = new ConfigInteger("Bounty HUD Y", 74, -10000, 10000, "Vertical position for the Auto Bounty HUD.");
   public static final List<IConfigBase> ALL_OPTIONS = List.of(HUD_X, HUD_Y);
   private static boolean initialized;
   private static int lastHudX;
   private static int lastHudY;

   private BountyHudSettings() {
   }

   public static int getHudX() {
      return HUD_X.getIntegerValue();
   }

   public static int getHudY() {
      return HUD_Y.getIntegerValue();
   }

   public static void maybePersist() {
      if (initialized) {
         if (getHudX() != lastHudX || getHudY() != lastHudY) {
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
         var2.setProperty("hudX", Integer.toString(getHudX()));
         var2.setProperty("hudY", Integer.toString(getHudY()));

         try (BufferedWriter var3 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var2.store(var3, "Donut Utility - Bounty HUD");
         }

         snapshot();
      } catch (Throwable var8) {
         System.err.println("[DonutUtility/BountyHUD] Could not save settings: " + var8.getMessage());
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

         HUD_X.setIntegerValue(parseInt(var1.getProperty("hudX"), 8));
         HUD_Y.setIntegerValue(parseInt(var1.getProperty("hudY"), 74));
      } catch (Throwable var7) {
         System.err.println("[DonutUtility/BountyHUD] Could not load settings: " + var7.getMessage());
      }
   }

   private static void snapshot() {
      lastHudX = getHudX();
      lastHudY = getHudY();
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0 == null ? "" : var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }

   private static Path configPath() {
      return Paths.get(System.getProperty("user.dir", "."), "config", "donututility-bounty-hud.properties");
   }

   static {
      loadFromDisk();
      snapshot();
      initialized = true;
   }
}
