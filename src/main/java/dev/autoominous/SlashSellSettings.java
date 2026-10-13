package dev.autoominous;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SlashSellSettings implements IConfigHandler {
   public static final ConfigBooleanHotkeyed AUTO_SLASH_SELL = new ConfigBooleanHotkeyed("Auto /sell", false, "");
   public static final ConfigBooleanHotkeyed AUTO_USE_PROTECTED_ITEMS = new ConfigBooleanHotkeyed("Hold RMB", false, "");
   public static final ConfigBooleanHotkeyed HUMANIZED_SELL_V2 = new ConfigBooleanHotkeyed(
      "Humanized Sell V2", true, "Human-like click timing and slot order for /sell."
   );
   public static final ConfigBooleanHotkeyed BULK_SAME_ITEM_TRANSFER = new ConfigBooleanHotkeyed(
      "Bulk Same-Item Transfer", false, "After the first manual stack, bulk-move all exact matching stacks into /sell."
   );
   public static final ConfigInteger CYCLE_1_MIN = new ConfigInteger("Cycle 1 MIN (ticks)", 20, 1, 72000, "Minimum delay for cycle 1. 20 ticks = 1 second.");
   public static final ConfigInteger CYCLE_1_MAX = new ConfigInteger("Cycle 1 MAX (ticks)", 60, 1, 72000, "Maximum delay for cycle 1.");
   public static final ConfigInteger CYCLE_2_MIN = new ConfigInteger("Cycle 2 MIN (ticks)", 20, 1, 72000, "Minimum delay for cycle 2.");
   public static final ConfigInteger CYCLE_2_MAX = new ConfigInteger("Cycle 2 MAX (ticks)", 60, 1, 72000, "Maximum delay for cycle 2.");
   public static final ConfigInteger CYCLE_3_MIN = new ConfigInteger("Cycle 3 MIN (ticks)", 20, 1, 72000, "Minimum delay for cycle 3.");
   public static final ConfigInteger CYCLE_3_MAX = new ConfigInteger("Cycle 3 MAX (ticks)", 60, 1, 72000, "Maximum delay for cycle 3.");
   public static final ConfigInteger CYCLE_4_MIN = new ConfigInteger("Cycle 4 MIN (ticks)", 20, 1, 72000, "Minimum delay for cycle 4.");
   public static final ConfigInteger CYCLE_4_MAX = new ConfigInteger("Cycle 4 MAX (ticks)", 60, 1, 72000, "Maximum delay for cycle 4.");
   public static final ConfigHotkey OPEN_SLASH_SETTINGS = new ConfigHotkey("Open /sell Settings", "");
   public static final List<IConfigBase> GENERIC_OPTIONS = List.of(
      CYCLE_1_MIN, CYCLE_1_MAX, CYCLE_2_MIN, CYCLE_2_MAX, CYCLE_3_MIN, CYCLE_3_MAX, CYCLE_4_MIN, CYCLE_4_MAX, OPEN_SLASH_SETTINGS
   );
   public static final List<ConfigBooleanHotkeyed> TOGGLE_OPTIONS;
   static {
      // The patched input assigns this final field twice. Retain construction order,
      // with one final assignment; no original code reads the intermediate list.
      List.of(AUTO_SLASH_SELL);
      TOGGLE_OPTIONS = List.of(AUTO_SLASH_SELL, AUTO_USE_PROTECTED_ITEMS, HUMANIZED_SELL_V2, BULK_SAME_ITEM_TRANSFER);
   }
   public static final List<IConfigBase> ALL_OPTIONS = List.of(
      AUTO_SLASH_SELL, CYCLE_1_MIN, CYCLE_1_MAX, CYCLE_2_MIN, CYCLE_2_MAX, CYCLE_3_MIN, CYCLE_3_MAX, CYCLE_4_MIN, CYCLE_4_MAX, OPEN_SLASH_SETTINGS
   );
   private static boolean initialized;
   private static boolean lastEnabled;
   private static final int[] lastCycles = new int[8];

   public static boolean isEnabled() {
      return AUTO_SLASH_SELL.getBooleanValue();
   }

   public static void setEnabled(boolean var0) {
      AUTO_SLASH_SELL.setBooleanValue(var0);
   }

   public static int getCycleMin(int var0) {
      int var1 = rawMin(var0);
      int var2 = rawMax(var0);
      return Math.max(1, Math.min(72000, Math.min(var1, var2)));
   }

   public static int getCycleMax(int var0) {
      int var1 = rawMin(var0);
      int var2 = rawMax(var0);
      return Math.max(1, Math.min(72000, Math.max(var1, var2)));
   }

   private static int rawMin(int var0) {
      return switch (var0) {
         case 1 -> CYCLE_1_MIN.getIntegerValue();
         case 2 -> CYCLE_2_MIN.getIntegerValue();
         case 3 -> CYCLE_3_MIN.getIntegerValue();
         case 4 -> CYCLE_4_MIN.getIntegerValue();
         default -> 20;
      };
   }

   private static int rawMax(int var0) {
      return switch (var0) {
         case 1 -> CYCLE_1_MAX.getIntegerValue();
         case 2 -> CYCLE_2_MAX.getIntegerValue();
         case 3 -> CYCLE_3_MAX.getIntegerValue();
         case 4 -> CYCLE_4_MAX.getIntegerValue();
         default -> 60;
      };
   }

   public static void maybePersist() {
      if (initialized) {
         boolean var0 = isEnabled() != lastEnabled;

         for (int var1 = 1; var1 <= 4; var1++) {
            int var2 = rawMin(var1);
            int var3 = rawMax(var1);
            int var4 = (var1 - 1) * 2;
            if (lastCycles[var4] != var2 || lastCycles[var4 + 1] != var3) {
               var0 = true;
            }
         }

         if (var0) {
            saveNow();
         }
      }
   }

   public static void saveNow() {
      try {
         new SlashSellSettings().save();
      } catch (Throwable var1) {
      }

      snapshot();
   }

   private static void snapshot() {
      lastEnabled = isEnabled();

      for (int var0 = 1; var0 <= 4; var0++) {
         int var1 = (var0 - 1) * 2;
         lastCycles[var1] = rawMin(var0);
         lastCycles[var1 + 1] = rawMax(var0);
      }
   }

   public void load() {
      try {
         Path var1 = FileUtils.getConfigDirectoryAsPath().resolve("autoominous-slash-sell.json");
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
         JsonUtils.writeJsonToFileAsPath(var2, var1.resolve("autoominous-slash-sell.json"));
      } catch (Throwable var4) {
      }
   }

   public void onLanguageChanged(String var1) {
   }

   static {
      snapshot();
      initialized = false;
   }

   public static boolean isAutoUseProtectedEnabled() {
      return AUTO_USE_PROTECTED_ITEMS.getBooleanValue();
   }

   public static void setAutoUseProtectedEnabled(boolean var0) {
      AUTO_USE_PROTECTED_ITEMS.setBooleanValue(var0);
   }

   public static boolean isHumanizedSellV2Enabled() {
      return false;
   }

   public static void setHumanizedSellV2Enabled(boolean var0) {
      HUMANIZED_SELL_V2.setBooleanValue(var0);
   }

   public static boolean isBulkSameItemTransferEnabled() {
      return false;
   }

   public static void setBulkSameItemTransferEnabled(boolean var0) {
      BULK_SAME_ITEM_TRANSFER.setBooleanValue(var0);
   }
}
