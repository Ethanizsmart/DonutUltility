package dev.autoominous;

final class BountyMoney {
   private static final long RESERVE = 1000000L;
   private static final long MIN_BOUNTY = 1000000L;
   private static final long STEP = 100000L;

   private BountyMoney() {
   }

   static String toDonutMillion(long var0) {
      long var2 = var0 - 1000000L;
      if (var2 < 1000000L) {
         return null;
      }

      long var4 = var2 / 100000L;
      long var6 = var4 / 10L;
      long var8 = var4 % 10L;
      return var6 + "." + var8 + "m";
   }
}
