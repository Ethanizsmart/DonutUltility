package dev.autoominous;

import fi.dy.masa.malilib.config.options.ConfigHotkey;

public final class AutoOminousSettings {
   public static final ConfigHotkey OPEN_CONFIG = new ConfigHotkey("Open Utility GUI", "X,C");

   public static final ConfigHotkey MASTER_TOGGLE = new ConfigHotkey("Toggle All Automation", "F8");

   private AutoOminousSettings() {
   }
}
