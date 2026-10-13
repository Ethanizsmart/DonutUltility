package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigString;
import java.util.List;

public final class CompactSlashSellSettings {
   public static final ConfigString CYCLE_1 = new ConfigString("Cycle 1 MIN,MAX (ticks)", "20,60");
   public static final ConfigString CYCLE_2 = new ConfigString("Cycle 2 MIN,MAX (ticks)", "20,60");
   public static final ConfigString CYCLE_3 = new ConfigString("Cycle 3 MIN,MAX (ticks)", "20,60");
   public static final ConfigString CYCLE_4 = new ConfigString("Cycle 4 MIN,MAX (ticks)", "20,60");
   private static boolean initialized;
   private static final String[] last = new String[4];

   private CompactSlashSellSettings() {
   }

   public static List<IConfigBase> getGuiOptions() {
      ensureInitialized();
      return List.of(SlashSellSettings.AUTO_SLASH_SELL, SlashSellSettings.AUTO_USE_PROTECTED_ITEMS, CYCLE_1, CYCLE_2, CYCLE_3, CYCLE_4);
   }

   public static void ensureInitialized() {
      if (!initialized) {
         ConfigString[] var0 = fields();

         for (int var1 = 0; var1 < 4; var1++) {
            String var2 = format(SlashSellSettings.getCycleMin(var1 + 1), SlashSellSettings.getCycleMax(var1 + 1));
            var0[var1].setStringValue(var2);
            last[var1] = var2;
         }

         initialized = true;
      }
   }

   public static void syncToOriginal() {
      ensureInitialized();
      ConfigString[] var0 = fields();
      boolean var1 = false;

      for (int var2 = 0; var2 < 4; var2++) {
         String var3 = var0[var2].getStringValue();
         if (var3 == null) {
            var3 = "";
         }

         if (!var3.equals(last[var2])) {
            int[] var4 = parse(var3);
            if (var4 == null) {
               last[var2] = var3;
            } else {
               int var5 = Math.max(1, Math.min(72000, var4[0]));
               int var6 = Math.max(1, Math.min(72000, var4[1]));
               if (var6 < var5) {
                  int var7 = var5;
                  var5 = var6;
                  var6 = var7;
               }

               setCycle(var2 + 1, var5, var6);
               String var8 = format(var5, var6);
               var0[var2].setStringValue(var8);
               last[var2] = var8;
               var1 = true;
            }
         }
      }

      if (var1) {
         SlashSellSettings.saveNow();
      }
   }

   private static int[] parse(String var0) {
      String var1 = var0.trim().replace(';', ',').replace('/', ',').replace('|', ',');
      String[] var2 = var1.split("\\s*,\\s*|\\s+", -1);
      if (var2.length != 2) {
         return null;
      }

      try {
         return new int[]{Integer.parseInt(var2[0].trim()), Integer.parseInt(var2[1].trim())};
      } catch (NumberFormatException var4) {
         return null;
      }
   }

   private static void setCycle(int var0, int var1, int var2) {
      switch (var0) {
         case 1:
            SlashSellSettings.CYCLE_1_MIN.setIntegerValue(var1);
            SlashSellSettings.CYCLE_1_MAX.setIntegerValue(var2);
            break;
         case 2:
            SlashSellSettings.CYCLE_2_MIN.setIntegerValue(var1);
            SlashSellSettings.CYCLE_2_MAX.setIntegerValue(var2);
            break;
         case 3:
            SlashSellSettings.CYCLE_3_MIN.setIntegerValue(var1);
            SlashSellSettings.CYCLE_3_MAX.setIntegerValue(var2);
            break;
         case 4:
            SlashSellSettings.CYCLE_4_MIN.setIntegerValue(var1);
            SlashSellSettings.CYCLE_4_MAX.setIntegerValue(var2);
      }
   }

   private static String format(int var0, int var1) {
      return var0 + "," + var1;
   }

   private static ConfigString[] fields() {
      return new ConfigString[]{CYCLE_1, CYCLE_2, CYCLE_3, CYCLE_4};
   }
}
