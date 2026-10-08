package dev.autoominous;

final class BountyMoney {
   private BountyMoney() {
   }

   static String toDonutMillion(long var0) {
      if (var0 < 1000000L) {
         return null;
      } else {
         long var2 = var0 / 100000L;
         long var4 = var2 / 10L;
         long var6 = var2 % 10L;
         return var4 + "." + var6 + "m";
      }
   }
}
