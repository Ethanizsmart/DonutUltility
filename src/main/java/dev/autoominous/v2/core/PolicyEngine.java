package dev.autoominous.v2.core;

public final class PolicyEngine {
   private PolicyEngine() {
   }

   public static PermissionSnapshot permissions(AutomationState var0, FeatureSettings var1) {
      return switch (var0) {
         case SESSION_OFFLINE, VEX_DEATH_WAIT, DEAD_RECOVERY, RESPAWN, HOME_TRANSIT -> PermissionSnapshot.denyAll();
         case SWORD_RECOVERY -> new PermissionSnapshot(false, false, false, false, true, false, true, false);
         case RECOVERY_RESUME -> PermissionSnapshot.denyAll();
         case VEX_DANGER -> new PermissionSnapshot(var1.autoSell(), var1.holdRmb(), false, false, false, var1.holdRmb(), false, false);
         case ALIVE_NORMAL -> new PermissionSnapshot(var1.autoSell(), var1.holdRmb(), var1.homeGuard(), var1.autoTpa(), false, var1.holdRmb(), false, false);
      };
   }
}
