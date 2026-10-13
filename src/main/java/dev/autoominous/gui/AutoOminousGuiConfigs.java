package dev.autoominous.gui;

import dev.autoominous.AutoReconnectSettings;
import dev.autoominous.AutoTpaSettings;
import dev.autoominous.BountyHudSettings;
import dev.autoominous.BountyRuntime;
import dev.autoominous.BountySettings;
import dev.autoominous.CompactSlashSellSettings;
import dev.autoominous.DeathRecoverySettings;
import dev.autoominous.DisconnectSettings;
import dev.autoominous.HomePositionGuardRuntime;
import dev.autoominous.MissingSwordWatchdogSettings;
import dev.autoominous.StatsRuntime;
import dev.autoominous.StatsSettings;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import java.util.ArrayList;
import java.util.List;

public final class AutoOminousGuiConfigs extends GuiConfigsBase {
   private static AutoOminousGuiConfigs.Tab currentTab = AutoOminousGuiConfigs.Tab.TPA;

   public AutoOminousGuiConfigs() {
      super(10, 72, "autoominous", null, "autoominous.gui.title.configs", new Object[]{"1.1.4"});
   }

   public static void openSlashSellTab() {
      currentTab = AutoOminousGuiConfigs.Tab.SLASH_SELL;
   }

   public void initGui() {
      super.initGui();
      this.clearOptions();
      this.addTabButton(10, 26, 60, "TPA", AutoOminousGuiConfigs.Tab.TPA);
      this.addTabButton(72, 26, 100, "Disconnect", AutoOminousGuiConfigs.Tab.DISCONNECT);
      this.addTabButton(174, 26, 120, "Death Recovery", AutoOminousGuiConfigs.Tab.DEATH_RECOVERY);
      this.addTabButton(296, 26, 75, "/sell", AutoOminousGuiConfigs.Tab.SLASH_SELL);
      this.addTabButton(373, 26, 80, "Stats", AutoOminousGuiConfigs.Tab.STATS);
      this.addTabButton(455, 26, 90, "Bounty", AutoOminousGuiConfigs.Tab.BOUNTY);
      if (currentTab == AutoOminousGuiConfigs.Tab.DISCONNECT) {
         this.addButton(new ButtonGeneric(10, 48, 220, 20, "Clear Disconnect Log", new String[0]), (var1x, var2x) -> {
            DisconnectSettings.clearDetectionLog();
            this.reCreateListWidget();
            this.initGui();
         });
      } else if (currentTab == AutoOminousGuiConfigs.Tab.DEATH_RECOVERY) {
         String var1 = MissingSwordWatchdogSettings.isEnabled() ? "§aAuto Buy Sword (5s): ON" : "§cAuto Buy Sword (5s): OFF";
         this.addButton(new ButtonGeneric(10, 48, 290, 20, var1, new String[0]), (var1x, var2x) -> {
            MissingSwordWatchdogSettings.toggleFromGui();
            this.reCreateListWidget();
            this.initGui();
         });
         String var2 = DeathRecoverySettings.isHomeSelected(1) ? "§2✓" : "";
         String var3 = DeathRecoverySettings.isHomeSelected(2) ? "§2✓" : "";
         this.addButton(new ButtonGeneric(590, 159, 28, 20, var2, new String[0]), (var1x, var2x) -> {
            DeathRecoverySettings.selectHome(1);
            this.reCreateListWidget();
            this.initGui();
         });
         this.addButton(new ButtonGeneric(590, 181, 28, 20, var3, new String[0]), (var1x, var2x) -> {
            DeathRecoverySettings.selectHome(2);
            this.reCreateListWidget();
            this.initGui();
         });
      } else if (currentTab == AutoOminousGuiConfigs.Tab.STATS) {
         this.addButton(new ButtonGeneric(10, 48, 120, 20, "Reset Stats", new String[0]), (var1x, var2x) -> {
            StatsRuntime.reset();
            this.initGui();
         });
      } else if (currentTab == AutoOminousGuiConfigs.Tab.BOUNTY) {
         this.addButton(new ButtonGeneric(10, 48, 535, 20, BountyRuntime.getStatusLine(), new String[0]), (var1x, var2x) -> this.initGui());
      }

      HomePositionGuardRuntime.injectGuiControls(this);
   }

   private void addTabButton(int var1, int var2, int var3, String var4, AutoOminousGuiConfigs.Tab var5) {
      String var6 = currentTab == var5 ? "§a" + var4 : var4;
      this.addButton(new ButtonGeneric(var1, var2, var3, 20, var6, new String[0]), (var2x, var3x) -> this.switchTab(var5));
   }

   private void switchTab(AutoOminousGuiConfigs.Tab var1) {
      if (currentTab != var1) {
         currentTab = var1;
         this.reCreateListWidget();
      }

      this.initGui();
   }

   protected int getConfigWidth() {
      return switch (currentTab) {
         case TPA -> 360;
         case DISCONNECT -> 460;
         case DEATH_RECOVERY -> 400;
         case SLASH_SELL -> 520;
         case STATS -> 360;
         case BOUNTY -> 520;
      };
   }

   public List<ConfigOptionWrapper> getConfigs() {
      List var1 = switch (currentTab) {
         case TPA -> AutoTpaSettings.ALL_OPTIONS;
         case DISCONNECT -> merged(DisconnectSettings.ALL_OPTIONS, AutoReconnectSettings.ALL_OPTIONS);
         case DEATH_RECOVERY -> MissingSwordWatchdogSettings.getDeathRecoveryGuiOptions();
         case SLASH_SELL -> CompactSlashSellSettings.getGuiOptions();
         case STATS -> StatsSettings.ALL_OPTIONS;
         case BOUNTY -> merged(BountySettings.ALL_OPTIONS, BountyHudSettings.ALL_OPTIONS);
      };
      return ConfigOptionWrapper.createFor(var1);
   }

   private static List<IConfigBase> merged(List<? extends IConfigBase> var0, List<? extends IConfigBase> var1) {
      ArrayList var2 = new ArrayList(var0.size() + var1.size());
      var2.addAll(var0);
      var2.addAll(var1);
      return var2;
   }

   public enum Tab {
      TPA,
      DISCONNECT,
      DEATH_RECOVERY,
      SLASH_SELL,
      STATS,
      BOUNTY;
   }
}
