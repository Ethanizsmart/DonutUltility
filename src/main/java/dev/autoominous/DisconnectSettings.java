package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigString;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class DisconnectSettings {
   public static final ConfigBoolean AUTO_DISCONNECT = new ConfigBoolean("Auto Disconnect", false);
   public static final ConfigDouble DETECTION_RADIUS = new ConfigDouble("Detection Radius (blocks)", 30.0, 1.0, 128.0);
   public static final ConfigString DISCONNECT_WHITELIST = new ConfigString("Disconnect Whitelist (comma-separated)", "");
   public static final ConfigString DETECTION_LOG = new ConfigString("Detected Players Log (latest first)", "");
   public static final List<IConfigBase> ALL_OPTIONS = List.of(AUTO_DISCONNECT, DETECTION_RADIUS, DISCONNECT_WHITELIST, DETECTION_LOG);
   private static boolean lastEnabled;
   private static double lastRadius;
   private static String lastWhitelist;
   private static String lastLog;
   private static boolean initialized = true;

   private DisconnectSettings() {
   }

   public static boolean isEnabled() {
      return AUTO_DISCONNECT.getBooleanValue();
   }

   public static double getRadius() {
      return DETECTION_RADIUS.getDoubleValue();
   }

   public static boolean isWhitelisted(String var0) {
      return var0 != null && !var0.isBlank() ? getWhitelist().contains(var0.trim().toLowerCase(Locale.ROOT)) : false;
   }

   public static Set<String> getWhitelist() {
      HashSet var0 = new HashSet();
      String var1 = safeWhitelist();
      if (var1.isBlank()) {
         return var0;
      }

      for (String var5 : var1.split("[,;\\s]+")) {
         String var6 = var5.trim();
         if (!var6.isEmpty()) {
            var0.add(var6.toLowerCase(Locale.ROOT));
         }
      }

      return var0;
   }

   public static void addToWhitelist(String var0) {
      if (var0 != null && !var0.isBlank() && !isWhitelisted(var0)) {
         String var1 = var0.trim();
         String var2 = safeWhitelist();
         DISCONNECT_WHITELIST.setStringValue(var2.isBlank() ? var1 : var2 + ", " + var1);
         saveNow();
      }
   }

   public static void recordDetection(String var0, double var1) {
      if (var0 != null && !var0.isBlank()) {
         String var3 = var0.trim();
         List<String> var4 = parseLogEntries(safeLog());
         if (var4.isEmpty() || !((String)var4.get(0)).equalsIgnoreCase(var3)) {
            LinkedHashMap var5 = new LinkedHashMap();
            var5.put(var3.toLowerCase(Locale.ROOT), var3);

            for (String var7 : var4) {
               if (var5.size() >= 50) {
                  break;
               }

               String var8 = var7.trim();
               if (!var8.isEmpty()) {
                  var5.putIfAbsent(var8.toLowerCase(Locale.ROOT), var8);
               }
            }

            DETECTION_LOG.setStringValue(String.join(", ", var5.values()));
            saveNow();
         }
      }
   }

   public static void clearDetectionLog() {
      DETECTION_LOG.setStringValue("");
      saveNow();
   }

   public static void maybePersist() {
      if (initialized) {
         boolean var0 = AUTO_DISCONNECT.getBooleanValue();
         double var1 = DETECTION_RADIUS.getDoubleValue();
         String var3 = safeWhitelist();
         String var4 = safeLog();
         if (var0 != lastEnabled || Double.compare(var1, lastRadius) != 0 || !var3.equals(lastWhitelist) || !var4.equals(lastLog)) {
            saveNow();
         }
      }
   }

   public static void saveNow() {
      try {
         Path var0 = configPath();
         Files.createDirectories(var0.getParent());
         Properties var1 = new Properties();
         var1.setProperty("enabled", Boolean.toString(AUTO_DISCONNECT.getBooleanValue()));
         var1.setProperty("radius", Double.toString(DETECTION_RADIUS.getDoubleValue()));
         var1.setProperty("whitelist", safeWhitelist());
         var1.setProperty("log", safeLog());

         try (BufferedWriter var2 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var1.store(var2, "Auto Ominous - Disconnect protection");
         }

         snapshot();
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Failed to save disconnect config: " + var7.getMessage());
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

         AUTO_DISCONNECT.setBooleanValue(Boolean.parseBoolean(var1.getProperty("enabled", "false")));
         double var8 = parseDouble(var1.getProperty("radius"), 30.0);
         DETECTION_RADIUS.setDoubleValue(Math.max(1.0, Math.min(128.0, var8)));
         DISCONNECT_WHITELIST.setStringValue(var1.getProperty("whitelist", ""));
         DETECTION_LOG.setStringValue(var1.getProperty("log", ""));
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Failed to load disconnect config: " + var7.getMessage());
      }
   }

   private static Path configPath() {
      return Paths.get(System.getProperty("user.dir", "."), "config", "autoominous-disconnect.properties");
   }

   private static double parseDouble(String var0, double var1) {
      try {
         return Double.parseDouble(var0 == null ? "" : var0.trim());
      } catch (Exception var4) {
         return var1;
      }
   }

   private static String safeWhitelist() {
      String var0 = DISCONNECT_WHITELIST.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   private static String safeLog() {
      String var0 = DETECTION_LOG.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   private static List<String> parseLogEntries(String var0) {
      ArrayList var1 = new ArrayList();
      if (var0 != null && !var0.isBlank()) {
         for (String var5 : var0.split("[,;]+")) {
            String var6 = var5.trim();
            if (!var6.isEmpty()) {
               var1.add(var6);
            }
         }

         return var1;
      } else {
         return var1;
      }
   }

   private static void snapshot() {
      lastEnabled = AUTO_DISCONNECT.getBooleanValue();
      lastRadius = DETECTION_RADIUS.getDoubleValue();
      lastWhitelist = safeWhitelist();
      lastLog = safeLog();
   }

   static {
      loadFromDisk();
      snapshot();
   }
}
