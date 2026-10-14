package com.seastore.auto.seaats.client;

public final class InventorySellRulesChecks {
   private static int checks;

   public static void main(String[] args) {
      for (int index = 0; index < 36; index++) {
         check(!InventorySellRules.canSell(false, true, index),
            "Protected sword/firework/bottle at inventory index " + index);
         check(InventorySellRules.canSell(false, false, index),
            "Ordinary item remains sellable at inventory index " + index);
         check(!InventorySellRules.canSell(true, true, index), "Empty protected slot " + index);
         check(!InventorySellRules.canSell(true, false, index), "Empty ordinary slot " + index);
      }
      for (int index : new int[]{-1, 36, 40, 45, 47}) {
         check(!InventorySellRules.canSell(false, true, index), "Reject non-inventory protected index " + index);
         check(!InventorySellRules.canSell(false, false, index), "Reject non-inventory ordinary index " + index);
      }
      System.out.println("Inventory sell rules: " + checks + " checks passed.");
   }

   private static void check(boolean result, String message) {
      checks++;
      if (!result) throw new AssertionError(message);
   }
}
