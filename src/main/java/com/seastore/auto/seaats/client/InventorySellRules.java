package com.seastore.auto.seaats.client;

/** Player inventory indices, not container slot IDs. */
public final class InventorySellRules {
   private InventorySellRules() {}

   public static boolean canSell(boolean empty, boolean protectedItem, int inventoryIndex) {
      if (empty || inventoryIndex < 0 || inventoryIndex >= 36) return false;
      return !protectedItem || inventoryIndex >= 9;
   }
}
