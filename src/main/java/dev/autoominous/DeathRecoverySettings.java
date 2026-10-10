package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class DeathRecoverySettings {
   public static final ConfigBoolean AUTO_DEATH_RECOVERY = new ConfigBoolean("Auto Death Recovery", false);
   public static final ConfigBoolean RECOVER_SWORD_AFTER_DEATH = new ConfigBoolean("Auto Buy Sword", true, "Buy a Diamond Sword with Looting III when none is present in inventory for 5 seconds. Independent of Auto Death Recovery.");
   public static final ConfigBoolean HOME_1_POSITION_GUARD = new ConfigBoolean("Home 1 Position Guard", false);
   public static final ConfigInteger RESPAWN_DELAY_MS = new ConfigInteger(
      "Respawn Delay (ms)", 2000, 0, 30000, "Wait after the death screen appears before automatically respawning."
   );
   public static final ConfigInteger HOME_DELAY_MS = new ConfigInteger(
      "Home Delay (ms)", 1500, 0, 30000, "Wait after respawn before sending the Home 1 command."
   );
   public static final ConfigString HOME_COMMAND_1 = new ConfigString("Home 1 CMD", "/home 1");
   public static final ConfigString HOME_COMMAND_2 = new ConfigString("Home 2 CMD", "/home 2");
   private static int selectedHome = 1;
   public static final List<IConfigBase> ALL_OPTIONS = List.of(
      AUTO_DEATH_RECOVERY, RECOVER_SWORD_AFTER_DEATH, HOME_1_POSITION_GUARD, RESPAWN_DELAY_MS, HOME_DELAY_MS, HOME_COMMAND_1, HOME_COMMAND_2
   );
   private static boolean lastEnabled;
   private static boolean lastRecoverSword;
   private static boolean lastHomeGuard;
   private static int lastRespawnDelayMs;
   private static int lastHomeDelayMs;
   private static int lastSelectedHome;
   private static String lastHomeCommand1;
   private static String lastHomeCommand2;
   private static boolean initialized;

   private DeathRecoverySettings() {
   }

   public static boolean isEnabled() {
      return AUTO_DEATH_RECOVERY.getBooleanValue();
   }

   public static boolean isSwordRecoveryEnabled() {
      return RECOVER_SWORD_AFTER_DEATH.getBooleanValue();
   }

   public static boolean isHomeGuardEnabled() {
      return HOME_1_POSITION_GUARD.getBooleanValue();
   }

   public static int getRespawnDelayMs() {
      return Math.max(0, Math.min(30000, RESPAWN_DELAY_MS.getIntegerValue()));
   }

   public static int getHomeDelayMs() {
      return Math.max(0, Math.min(30000, HOME_DELAY_MS.getIntegerValue()));
   }

   public static int getSelectedHome() {
      return selectedHome >= 0 && selectedHome <= 2 ? selectedHome : 0;
   }

   public static boolean isHomeSelected(int var0) {
      return selectedHome == var0;
   }

   public static void selectHome(int var0) {
      int var1 = var0 == 2 ? 2 : 1;
      selectedHome = selectedHome == var1 ? 0 : var1;
      saveNow();
   }

   public static String getCommandForSend() {
      if (selectedHome == 1) {
         return safeHomeCommand1();
      } else {
         return selectedHome == 2 ? safeHomeCommand2() : "";
      }
   }

   public static String getHome1CommandForSend() {
      return safeHomeCommand1();
   }

   public static void maybePersist() {
      if (initialized) {
         boolean var0 = isEnabled();
         boolean var1 = isSwordRecoveryEnabled();
         boolean var2 = isHomeGuardEnabled();
         int var3 = getRespawnDelayMs();
         int var4 = getHomeDelayMs();
         String var5 = safeHomeCommand1();
         String var6 = safeHomeCommand2();
         if (var0 != lastEnabled
            || var1 != lastRecoverSword
            || var2 != lastHomeGuard
            || var3 != lastRespawnDelayMs
            || var4 != lastHomeDelayMs
            || selectedHome != lastSelectedHome
            || !var5.equals(lastHomeCommand1)
            || !var6.equals(lastHomeCommand2)) {
            saveNow();
         }
      }
   }

   public static void saveNow() {
      try {
         Path var0 = configPath();
         Files.createDirectories(var0.getParent());
         Properties var1 = new Properties();
         var1.setProperty("enabled", Boolean.toString(isEnabled()));
         var1.setProperty("recoverSwordAfterDeath", Boolean.toString(isSwordRecoveryEnabled()));
         var1.setProperty("home1PositionGuard", Boolean.toString(isHomeGuardEnabled()));
         var1.setProperty("respawnDelayMs", Integer.toString(getRespawnDelayMs()));
         var1.setProperty("homeDelayMs", Integer.toString(getHomeDelayMs()));
         var1.setProperty("selectedHome", Integer.toString(getSelectedHome()));
         var1.setProperty("homeCommand1", safeHomeCommand1());
         var1.setProperty("homeCommand2", safeHomeCommand2());

         try (BufferedWriter var2 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var1.store(var2, "Auto Ominous - Death Recovery");
         }

         snapshot();
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Death Recovery settings save failed: " + var7.getMessage());
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

         AUTO_DEATH_RECOVERY.setBooleanValue(Boolean.parseBoolean(var1.getProperty("enabled", "false")));
         RECOVER_SWORD_AFTER_DEATH.setBooleanValue(Boolean.parseBoolean(var1.getProperty("recoverSwordAfterDeath", "true")));
         HOME_1_POSITION_GUARD.setBooleanValue(Boolean.parseBoolean(var1.getProperty("home1PositionGuard", "false")));
         RESPAWN_DELAY_MS.setIntegerValue(parseInt(var1.getProperty("respawnDelayMs"), 2000));
         HOME_DELAY_MS.setIntegerValue(parseInt(var1.getProperty("homeDelayMs"), 1500));
         String var9 = var1.getProperty("homeCommand", "/home 1");
         HOME_COMMAND_1.setStringValue(var1.getProperty("homeCommand1", var9));
         HOME_COMMAND_2.setStringValue(var1.getProperty("homeCommand2", "/home 2"));
         String var3 = var1.getProperty("selectedHome");
         if (var3 != null) {
            int var4 = parseInt(var3, 1);
            selectedHome = var4 != 0 && var4 != 1 && var4 != 2 ? 1 : var4;
         } else {
            boolean var10 = Boolean.parseBoolean(var1.getProperty("useHome1", "true"));
            boolean var5 = Boolean.parseBoolean(var1.getProperty("useHome2", "false"));
            selectedHome = var5 && !var10 ? 2 : (var10 ? 1 : 0);
         }
      } catch (Throwable var8) {
         System.err.println("[Auto Ominous] Death Recovery settings load failed: " + var8.getMessage());
      }
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0 == null ? "" : var0.trim());
      } catch (Throwable var3) {
         return var1;
      }
   }

   private static Path configPath() {
      return Paths.get(System.getProperty("user.dir", "."), "config", "autoominous-death-recovery.properties");
   }

   private static String safeHomeCommand1() {
      String var0 = HOME_COMMAND_1.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   private static String safeHomeCommand2() {
      String var0 = HOME_COMMAND_2.getStringValue();
      return var0 == null ? "" : var0.trim();
   }

   private static void snapshot() {
      lastEnabled = isEnabled();
      lastRecoverSword = isSwordRecoveryEnabled();
      lastHomeGuard = isHomeGuardEnabled();
      lastRespawnDelayMs = getRespawnDelayMs();
      lastHomeDelayMs = getHomeDelayMs();
      lastSelectedHome = getSelectedHome();
      lastHomeCommand1 = safeHomeCommand1();
      lastHomeCommand2 = safeHomeCommand2();
   }

   static {
      loadFromDisk();
      if (selectedHome < 0 || selectedHome > 2) {
         selectedHome = 1;
      }

      snapshot();
      initialized = true;
   }
}
