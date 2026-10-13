package dev.autoominous;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public final class AutoOminousSettings {
   public static final ConfigHotkey MASTER_TOGGLE = new ConfigHotkey("Master Automation", "F8",
      KeybindSettings.create(KeybindSettings.Context.ANY, KeyAction.PRESS, true, false, false, true));
   public static final ConfigHotkey OPEN_CONFIG = new ConfigHotkey("Open Utility GUI", "X,C");

   private AutoOminousSettings() {
   }
}
