package dev.autoominous;

import com.seastore.auto.seaats.Config;
import net.minecraft.class_310;
import net.minecraft.class_465;

/** Session switch; feature settings remain unchanged and each game launch starts OFF. */
public final class MasterAutomation {
   private static boolean enabled;

   private MasterAutomation() {}

   public static boolean isEnabled() { return enabled; }

   public static void toggle() { setEnabled(!enabled); }

   static void persistSettingsWhilePaused() {
      CompactSlashSellSettings.syncToOriginal();
      SlashSellSettings.maybePersist();
      DeathRecoverySettings.maybePersist();
      AutoTpaSettings.maybePersist();
      AutoReconnectSettings.maybePersist();
      BountySettings.maybePersist();
      BountyHudSettings.maybePersist();
   }

   public static void setEnabled(boolean value) {
      if (enabled == value) return;
      boolean ownedMenu = TransactionGate.isSeaSellActive() || SwordRecoveryRuntime.isRecoveryActive()
         || HomePositionGuardRuntime.isBlockingAutomation() || BountyRuntime.isBlockingSell()
         || ReflectionSupport.staticBoolean("dev.autoominous.AutoTpaRuntime", "awaitingConfirm", false);
      enabled = value;
      SwordRecoveryRuntime.stopForMaster();
      AutomationCoordinator.resetForMaster();
      HomePositionGuardRuntime.resetForMaster();
      AutoTpaRuntime.resetForMaster();
      AutoReconnectRuntime.resetForMaster();
      DisconnectRuntime.resetForMaster();
      BountyRuntime.resetForMaster();
      VexSafetyRuntime.resetForMaster();
      G232SafeRuntime.resetForMaster();
      ReflectionSupport.callStaticVoid("dev.autoominous.FarmCycleRuntime", "resetSoft");
      ConsumableRecoveryRuntime.resetOmiBaseline();
      ConsumableRecoveryRuntime.resetFireworkBaseline();
      SlashSellRuntime.resetRuntime();
      Config.isAutoSellEnabled = false;
      if (ownedMenu) closeOwnedMenu();
      ClientStatusMessage.actionbarState("Master (F8)", enabled);
   }

   private static void closeOwnedMenu() {
      class_310 client = class_310.method_1551();
      if (client != null && client.field_1724 != null
         && (client.field_1755 instanceof class_465 || client.field_1724.field_7512 != client.field_1724.field_7498)) {
         client.field_1724.method_7346();
      }
   }
}
