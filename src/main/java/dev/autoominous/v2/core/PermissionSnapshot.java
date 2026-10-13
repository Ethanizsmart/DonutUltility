package dev.autoominous.v2.core;

public record PermissionSnapshot(
   boolean canSell,
   boolean canHoldRmb,
   boolean canHomeGuard,
   boolean canAutoTpa,
   boolean canOrder,
   boolean canChangeHotbar,
   boolean canMoveInventory,
   boolean canWorldLmb
) {
   public static PermissionSnapshot denyAll() {
      return new PermissionSnapshot(false, false, false, false, false, false, false, false);
   }
}
