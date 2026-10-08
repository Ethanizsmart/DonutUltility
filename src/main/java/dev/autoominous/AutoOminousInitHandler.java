package dev.autoominous;

import dev.autoominous.event.AutoOminousInputHandler;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;

public final class AutoOminousInitHandler implements IInitializationHandler {
   public void registerModHandlers() {
      IConfigManager var1 = ConfigManager.getInstance();
      var1.registerConfigHandler("autoominous-slash-sell", new SlashSellSettings());
      var1.registerConfigHandler("donututility-bounty", new BountySettings());
      AutoOminousInputHandler.initCallbacks();
      InputEventHandler.getKeybindManager().registerKeybindProvider(AutoOminousInputHandler.getInstance());
   }
}
