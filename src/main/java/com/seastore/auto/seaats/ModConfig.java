package com.seastore.auto.seaats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_PATH = ConfigPathCompat.getConfigDirCompat().resolve("seaats.json");

   public static void load() {
      if (CONFIG_PATH.toFile().exists()) {
         try (FileReader var0 = new FileReader(CONFIG_PATH.toFile())) {
            ModConfig.ConfigData var1 = (ModConfig.ConfigData)GSON.fromJson(var0, ModConfig.ConfigData.class);
            applyData(var1);
         } catch (IOException | RuntimeException var5) {
            var5.printStackTrace();
         }
      } else {
         save();
      }
   }

   public static void save() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         Path var0 = CONFIG_PATH.resolveSibling("seaats.json.tmp");
         Files.writeString(var0, GSON.toJson(gatherData()), StandardCharsets.UTF_8);

         try {
            Files.move(var0, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var2) {
            Files.move(var0, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (IOException var3) {
         throw new IllegalStateException("Không ghi được seaats.json: " + var3.getMessage(), var3);
      }
   }

   private static void applyData(ModConfig.ConfigData var0) {
      if (var0 != null) {
         Config.showTracker = var0.showTracker;
         int var1 = Math.max(1, Math.min(72000, (int)Math.round(var0.delayMin * 20.0)));
         int var2 = Math.max(var1, Math.min(72000, (int)Math.round(var0.delayMax * 20.0)));
         Config.cycle1MinTicks = var0.cycle1MinTicks == null ? var1 : var0.cycle1MinTicks;
         Config.cycle1MaxTicks = var0.cycle1MaxTicks == null ? var2 : var0.cycle1MaxTicks;
         Config.cycle2MinTicks = var0.cycle2MinTicks == null ? var1 : var0.cycle2MinTicks;
         Config.cycle2MaxTicks = var0.cycle2MaxTicks == null ? var2 : var0.cycle2MaxTicks;
         Config.cycle3MinTicks = var0.cycle3MinTicks == null ? var1 : var0.cycle3MinTicks;
         Config.cycle3MaxTicks = var0.cycle3MaxTicks == null ? var2 : var0.cycle3MaxTicks;
         Config.cycle4MinTicks = var0.cycle4MinTicks == null ? var1 : var0.cycle4MinTicks;
         Config.cycle4MaxTicks = var0.cycle4MaxTicks == null ? var2 : var0.cycle4MaxTicks;
         Config.enableAutoPay = var0.enableAutoPay;
         Config.payIntervalMinutes = var0.payIntervalMinutes;
         Config.payRecipient = var0.payRecipient;
         Config.payAmount = var0.payAmount;
         Config.payAll = var0.payAll;
         Config.payRandomMinMillions = var0.payRandomMinMillions == null ? var0.payAmount : var0.payRandomMinMillions;
         Config.payRandomMaxMillions = var0.payRandomMaxMillions == null ? var0.payAmount : var0.payRandomMaxMillions;
         Config.payWebhookUrl = var0.payWebhookUrl;
         Config.delayMin = var0.delayMin;
         Config.delayMax = var0.delayMax;
         Config.trackerWidth = var0.trackerWidth;
         Config.trackerHeight = var0.trackerHeight;
         Config.textScale = var0.textScale;
         double var3 = var0.textScale * Math.max(1.0, Math.min(2.0, var0.trackerWidth / 180.0));
         double var5 = var0.trackerScale == null ? var3 : var0.trackerScale;
         Config.trackerScale = Double.isFinite(var5) ? Math.max(0.65, Math.min(2.0, var5)) : 1.0;
         Config.antiPlayerEnabled = var0.antiPlayerEnabled;
         Config.enableGhostFix = var0.enableGhostFix;
         Config.ghostTimeoutMinutes = var0.ghostTimeoutMinutes;
         Config.ghostHomeCommand = var0.ghostHomeCommand;
         Config.enableHome = var0.enableHome;
         Config.homeCommandIntervalMin = var0.homeCommandIntervalMin;
         Config.homeCommand1 = var0.homeCommand1;
         Config.homeCommand2 = var0.homeCommand2;
         Config.webhookUrlProfit = var0.webhookUrlProfit;
         Config.webhookUrlAlert = var0.webhookUrlAlert;
         Config.profitIntervalMinutes = var0.profitIntervalMinutes;
         Config.noSellIntervalMinutes = var0.noSellIntervalMinutes;
         Config.discordId = var0.discordId;
         Config.friendList = var0.friendList != null ? var0.friendList : new ArrayList<>();
      }
   }

   private static ModConfig.ConfigData gatherData() {
      ModConfig.ConfigData var0 = new ModConfig.ConfigData();
      var0.showTracker = Config.showTracker;
      var0.cycle1MinTicks = Config.cycle1MinTicks;
      var0.cycle1MaxTicks = Config.cycle1MaxTicks;
      var0.cycle2MinTicks = Config.cycle2MinTicks;
      var0.cycle2MaxTicks = Config.cycle2MaxTicks;
      var0.cycle3MinTicks = Config.cycle3MinTicks;
      var0.cycle3MaxTicks = Config.cycle3MaxTicks;
      var0.cycle4MinTicks = Config.cycle4MinTicks;
      var0.cycle4MaxTicks = Config.cycle4MaxTicks;
      var0.enableAutoPay = Config.enableAutoPay;
      var0.payIntervalMinutes = Config.payIntervalMinutes;
      var0.payRecipient = Config.payRecipient;
      var0.payAmount = Config.payAmount;
      var0.payAll = Config.payAll;
      var0.payRandomMinMillions = Config.payRandomMinMillions;
      var0.payRandomMaxMillions = Config.payRandomMaxMillions;
      var0.payWebhookUrl = Config.payWebhookUrl;
      var0.delayMin = Config.delayMin;
      var0.delayMax = Config.delayMax;
      var0.trackerWidth = Config.trackerWidth;
      var0.trackerHeight = Config.trackerHeight;
      var0.textScale = Config.textScale;
      var0.trackerScale = Config.trackerScale;
      var0.antiPlayerEnabled = Config.antiPlayerEnabled;
      var0.enableGhostFix = Config.enableGhostFix;
      var0.ghostTimeoutMinutes = Config.ghostTimeoutMinutes;
      var0.ghostHomeCommand = Config.ghostHomeCommand;
      var0.enableHome = Config.enableHome;
      var0.homeCommandIntervalMin = Config.homeCommandIntervalMin;
      var0.homeCommand1 = Config.homeCommand1;
      var0.homeCommand2 = Config.homeCommand2;
      var0.webhookUrlProfit = Config.webhookUrlProfit;
      var0.webhookUrlAlert = Config.webhookUrlAlert;
      var0.profitIntervalMinutes = Config.profitIntervalMinutes;
      var0.noSellIntervalMinutes = Config.noSellIntervalMinutes;
      var0.discordId = Config.discordId;
      var0.friendList = new ArrayList<>(Config.friendList);
      return var0;
   }

   @Environment(EnvType.CLIENT)
   static class ConfigData {
      Integer cycle1MinTicks;
      Integer cycle1MaxTicks;
      Integer cycle2MinTicks;
      Integer cycle2MaxTicks;
      Integer cycle3MinTicks;
      Integer cycle3MaxTicks;
      Integer cycle4MinTicks;
      Integer cycle4MaxTicks;
      boolean showTracker = true;
      Double trackerScale;
      boolean enableAutoPay = false;
      int payIntervalMinutes = 60;
      String payRecipient = "";
      double payAmount = 0.0;
      boolean payAll = false;
      Double payRandomMinMillions;
      Double payRandomMaxMillions;
      String payWebhookUrl = "";
      double delayMin = 0.1;
      double delayMax = 0.5;
      int trackerWidth = 180;
      int trackerHeight = 80;
      double textScale = 1.0;
      boolean antiPlayerEnabled = true;
      boolean enableGhostFix = true;
      int ghostTimeoutMinutes = 1;
      String ghostHomeCommand = "home 1";
      boolean enableHome = true;
      int homeCommandIntervalMin = 5;
      String homeCommand1 = "home 1";
      String homeCommand2 = "home 2";
      String webhookUrlProfit = "";
      String webhookUrlAlert = "";
      int profitIntervalMinutes = 30;
      int noSellIntervalMinutes = 15;
      String discordId = "";
      List friendList = new ArrayList();
   }
}
