package dev.autoominous;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import java.util.List;

/** Existing GUI/API names forward to the independent Auto Buy Sword setting. */
public final class MissingSwordWatchdogSettings {
   public static final ConfigBoolean AUTO_BUY_SWORD_AFTER_30S = DeathRecoverySettings.RECOVER_SWORD_AFTER_DEATH;
   public static final List<IConfigBase> ALL_OPTIONS = List.of(AUTO_BUY_SWORD_AFTER_30S);
   private MissingSwordWatchdogSettings() {}
   public static boolean isEnabled() { return DeathRecoverySettings.isSwordRecoveryEnabled(); }
   public static List<IConfigBase> getDeathRecoveryGuiOptions() { return DeathRecoverySettings.ALL_OPTIONS; }
   public static void setEnabled(boolean enabled) {
      AUTO_BUY_SWORD_AFTER_30S.setBooleanValue(enabled);
      saveNow();
      MissingSwordWatchdogRuntime.onToggleChanged(enabled);
   }
   public static void toggleFromGui() { setEnabled(!isEnabled()); }
   public static void maybePersist() { DeathRecoverySettings.maybePersist(); }
   public static void saveNow() { DeathRecoverySettings.saveNow(); }
}