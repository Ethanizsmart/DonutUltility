package com.seastore.auto.seaats.client;

import dev.autoominous.HumanBehaviorProfile;
import dev.autoominous.OminousSurplusSellPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1703;
import net.minecraft.class_1799;

final class SellRhythmRuntime {
   private static boolean initialized;
   private static int tempo;
   private static int lastSlot = -1;
   private static int sameTypeRun;
   private static int actionCount;
   private static class_1799 previousSignature;
   private static boolean fastFollowUp;

   private SellRhythmRuntime() {
   }

   static void reset() {
      initialized = false;
      tempo = 1;
      lastSlot = -1;
      sameTypeRun = 0;
      actionCount = 0;
      previousSignature = null;
      fastFollowUp = false;
   }

   static int selectSlot(class_1703 var0) {
      if (var0 != null && var0.field_7761 != null) {
         ensureInitialized();
         int var1 = var0.field_7761.size();
         int var2 = Math.max(0, var1 - 36);
         ArrayList<Integer> var3 = new ArrayList<>(36);
         ArrayList<Integer> var4 = new ArrayList<>(8);

         for (int var5 = var2; var5 < var1; var5++) {
            class_1799 var6;
            try {
               var6 = var0.method_7611(var5).method_7677();
            } catch (Throwable var11) {
               continue;
            }

            if (sellable(var6)) {
               var3.add(var5);
               if (previousSignature != null && sameItem(var6, previousSignature)) {
                  var4.add(var5);
               }
            }
         }

         if (var3.isEmpty()) {
            return -1;
         }

         boolean var12 = previousSignature != null && sameTypeRun < 2 && !var4.isEmpty() && rand(0, 99) < 20;
         ArrayList<Integer> var13;
         if (var12) {
            var13 = var4;
         } else if (previousSignature != null && sameTypeRun >= 2) {
            ArrayList<Integer> var7 = new ArrayList<>(var3.size());

            for (int var9 : var3) {
               class_1799 var10 = var0.method_7611(var9).method_7677();
               if (!sameItem(var10, previousSignature)) {
                  var7.add(var9);
               }
            }

            var13 = var7.isEmpty() ? var3 : var7;
         } else {
            var13 = var3;
         }

         int var14 = chooseLocal(var13);
         class_1799 var15 = var0.method_7611(var14).method_7677();
         boolean var16 = previousSignature != null && sameItem(var15, previousSignature);
         sameTypeRun = var16 ? Math.min(2, sameTypeRun + 1) : 1;
         fastFollowUp = var12 && var16;
         previousSignature = safeCopy(var15);
         lastSlot = var14;
         actionCount++;
         return var14;
      } else {
         return -1;
      }
   }

   static int nextDelayTicks() {
      ensureInitialized();
      int var0 = rand(0, 99);
      if (var0 < 5) {
         return rand(18, 28);
      }

      if (var0 < 15) {
         return rand(9, 16);
      }

      if (fastFollowUp) {
         return rand(2, 3);
      }

      return switch (tempo) {
         case 0 -> rand(3, 6);
         case 2 -> rand(6, 11);
         default -> rand(4, 8);
      };
   }

   private static void ensureInitialized() {
      if (!initialized) {
         int var0 = rand(0, 99);
         tempo = var0 < 25 ? 0 : (var0 < 85 ? 1 : 2);
         lastSlot = -1;
         sameTypeRun = 0;
         actionCount = 0;
         previousSignature = null;
         fastFollowUp = false;
         initialized = true;
      }
   }

   private static int chooseLocal(List<Integer> var0) {
      if (var0.size() == 1) {
         return (Integer)var0.get(0);
      }

      if (lastSlot < 0) {
         int var4 = Math.min(var0.size() - 1, 5);
         return (Integer)var0.get(rand(0, var4));
      }

      ArrayList var1 = new ArrayList(var0);
      var1.sort(Comparator.comparingInt(SellRhythmRuntime::distanceFromLast));
      int var2 = rand(0, 99);
      int var3;
      if (var2 < 68) {
         var3 = Math.min(3, var1.size());
      } else {
         if (var2 >= 93) {
            return (Integer)var1.get(rand(0, var1.size() - 1));
         }

         var3 = Math.min(6, var1.size());
      }

      return (Integer)var1.get(rand(0, var3 - 1));
   }

   private static int distanceFromLast(int var0) {
      int var1 = Math.abs(var0 - lastSlot);
      int var2 = Math.max(0, var0) / 9;
      int var3 = Math.max(0, lastSlot) / 9;
      return var1 + Math.abs(var2 - var3) * 2;
   }

   private static boolean sellable(class_1799 var0) {
      if (var0 != null && !var0.method_7960()) {
         try {
            return !OminousSurplusSellPolicy.isProtectedForTransfer(var0);
         } catch (Throwable var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean sameItem(class_1799 var0, class_1799 var1) {
      if (var0 != null && var1 != null && !var0.method_7960() && !var1.method_7960()) {
         try {
            return class_1799.method_31577(var0, var1);
         } catch (Throwable var5) {
            try {
               return var0.method_7909() == var1.method_7909();
            } catch (Throwable var4) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static class_1799 safeCopy(class_1799 var0) {
      try {
         return var0 == null ? null : var0.method_7972();
      } catch (Throwable var2) {
         return var0;
      }
   }

   private static int rand(int var0, int var1) {
      if (var1 <= var0) {
         return var0;
      }

      try {
         return HumanBehaviorProfile.sellRandom(var0, var1);
      } catch (Throwable var3) {
         return var0 + (int)(Math.random() * (var1 - var0 + 1));
      }
   }
}
