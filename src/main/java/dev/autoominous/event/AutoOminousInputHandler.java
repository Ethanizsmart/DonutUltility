package dev.autoominous.event;

import dev.autoominous.AutoOminousSettings;
import dev.autoominous.BountySettings;
import dev.autoominous.MasterAutomation;
import dev.autoominous.SlashSellSettings;
import dev.autoominous.gui.AutoOminousGuiConfigs;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import java.util.List;

public final class AutoOminousInputHandler implements IKeybindProvider {
   private static final AutoOminousInputHandler INSTANCE = new AutoOminousInputHandler();

   private AutoOminousInputHandler() {
   }

   public static AutoOminousInputHandler getInstance() {
      return INSTANCE;
   }

   public static void initCallbacks() {
      AutoOminousSettings.MASTER_TOGGLE.getKeybind().setCallback((action, keybind) -> {
         if (action != fi.dy.masa.malilib.hotkeys.KeyAction.PRESS) return false;
         MasterAutomation.toggle();
         net.minecraft.class_310 client = net.minecraft.class_310.method_1551();
         if (client != null && client.field_1755 instanceof AutoOminousGuiConfigs gui) gui.initGui();
         return true;
      });
      AutoOminousSettings.OPEN_CONFIG.getKeybind().setCallback((var0, var1) -> {
         GuiBase.openGui(new AutoOminousGuiConfigs());
         return true;
      });
   }

   public void addKeysToMap(IKeybindManager var1) {
      var1.addKeybindToMap(AutoOminousSettings.MASTER_TOGGLE.getKeybind());
      var1.addKeybindToMap(AutoOminousSettings.OPEN_CONFIG.getKeybind());
      var1.addKeybindToMap(SlashSellSettings.AUTO_SLASH_SELL.getKeybind());
      var1.addKeybindToMap(BountySettings.AUTO_BOUNTY.getKeybind());
   }

   public void addHotkeys(IKeybindManager var1) {
      var1.addHotkeysForCategory(
         "Donut Utility", "Donut Utility Hotkeys", List.of(AutoOminousSettings.MASTER_TOGGLE, AutoOminousSettings.OPEN_CONFIG, SlashSellSettings.AUTO_SLASH_SELL, BountySettings.AUTO_BOUNTY)
      );
   }
}
