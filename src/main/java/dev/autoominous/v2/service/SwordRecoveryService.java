package dev.autoominous.v2.service;

import dev.autoominous.v2.core.RuntimeSnapshot;

public final class SwordRecoveryService {
   public SwordRecoveryService.Decision decide(RuntimeSnapshot var1) {
      if (var1.swordInHotbar()) {
         return SwordRecoveryService.Decision.COMPLETE;
      } else if (var1.swordInInventory()) {
         return SwordRecoveryService.Decision.MOVE_FROM_INVENTORY;
      } else {
         return !var1.inventoryHasFreeSlot() ? SwordRecoveryService.Decision.NEED_FREE_SPACE : SwordRecoveryService.Decision.NEED_ORDER;
      }
   }

   public enum Decision {
      COMPLETE,
      MOVE_FROM_INVENTORY,
      NEED_ORDER,
      NEED_FREE_SPACE;
   }
}
