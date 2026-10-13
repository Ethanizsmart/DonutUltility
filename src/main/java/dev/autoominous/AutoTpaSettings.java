package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class AutoTpaSettings {
   public static final ConfigBoolean AUTO_ACCEPT_TPA = new ConfigBoolean("Auto Accept TPA", false);
   public static final ConfigInteger TPA_DELAY_MS = new ConfigInteger(
      "TPA Accept Delay (ms)", 500, 0, 10000, "Delay before sending /tpaccept for an allowed teleport-here request."
   );
   public static final ConfigString TPA_WHITELIST = new ConfigString("TPA Whitelist (comma-separated)", "");
   public static final List<IConfigBase> ALL_OPTIONS = List.of(AUTO_ACCEPT_TPA, TPA_DELAY_MS, TPA_WHITELIST);
   private static boolean lastEnabled;
   private static int lastDelay;
   private static String lastWhitelist;
   private static boolean initialized = true;

   private AutoTpaSettings() {
   }

   public static boolean isEnabled() {
      return AUTO_ACCEPT_TPA.getBooleanValue();
   }

   public static int getDelayMs() {
      return HumanBehaviorProfile.tpaDelayMs(TPA_DELAY_MS.getIntegerValue());
   }

   public static boolean isWhitelisted(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim().toLowerCase(Locale.ROOT);

         for (String var3 : getWhitelist()) {
            if (var3.equals(var1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static Set<String> getWhitelist() {
      HashSet var0 = new HashSet();
      String var1 = TPA_WHITELIST.getStringValue();
      if (var1 != null && !var1.isBlank()) {
         for (String var5 : var1.split("[,;\\s]+")) {
            String var6 = var5.trim();
            if (!var6.isEmpty()) {
               var0.add(var6.toLowerCase(Locale.ROOT));
            }
         }

         return var0;
      } else {
         return var0;
      }
   }

   public static void decrementDelay() {
      TPA_DELAY_MS.setIntegerValue(Math.max(0, TPA_DELAY_MS.getIntegerValue() - 100));
      saveNow();
   }

   public static void incrementDelay() {
      TPA_DELAY_MS.setIntegerValue(Math.min(10000, TPA_DELAY_MS.getIntegerValue() + 100));
      saveNow();
   }

   public static void maybePersist() {
      if (initialized) {
         boolean var0 = AUTO_ACCEPT_TPA.getBooleanValue();
         int var1 = TPA_DELAY_MS.getIntegerValue();
         String var2 = safeWhitelist();
         if (var0 != lastEnabled || var1 != lastDelay || !var2.equals(lastWhitelist)) {
            saveNow();
         }
      }
   }

   public static void saveNow() {
      try {
         Path var0 = configPath();
         Files.createDirectories(var0.getParent());
         Properties var1 = new Properties();
         var1.setProperty("enabled", Boolean.toString(AUTO_ACCEPT_TPA.getBooleanValue()));
         var1.setProperty("delayMs", Integer.toString(TPA_DELAY_MS.getIntegerValue()));
         var1.setProperty("whitelist", safeWhitelist());

         try (BufferedWriter var2 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var1.store(var2, "Auto Ominous Auto Accept TPA settings");
         }

         snapshot();
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Could not save Auto TPA settings: " + var7.getMessage());
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

         AUTO_ACCEPT_TPA.setBooleanValue(Boolean.parseBoolean(var1.getProperty("enabled", "false")));
         int var8 = parseInt(var1.getProperty("delayMs"), 500);
         TPA_DELAY_MS.setIntegerValue(Math.max(0, Math.min(10000, var8)));
         TPA_WHITELIST.setStringValue(var1.getProperty("whitelist", ""));
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Could not load Auto TPA settings: " + var7.getMessage());
      }
   }

   private static Path configPath() {
      return FileUtils.getConfigDirectoryAsPath().resolve("autoominous-tpa.properties");
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0 == null ? "" : var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }

   private static String safeWhitelist() {
      String var0 = TPA_WHITELIST.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   private static void snapshot() {
      lastEnabled = AUTO_ACCEPT_TPA.getBooleanValue();
      lastDelay = TPA_DELAY_MS.getIntegerValue();
      lastWhitelist = safeWhitelist();
   }

   static {
      loadFromDisk();
      snapshot();
   }
}
