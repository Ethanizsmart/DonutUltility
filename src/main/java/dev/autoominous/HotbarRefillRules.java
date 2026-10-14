package dev.autoominous;

import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class HotbarRefillRules {
   private HotbarRefillRules() {}

   public static boolean isSword(class_1799 stack) {
      if (stack == null || stack.method_7960()) return false;
      if (stack.method_31574(class_1802.field_8802)   // Diamond
         || stack.method_31574(class_1802.field_22022) // Netherite
         || stack.method_31574(class_1802.field_8371)  // Iron
         || stack.method_31574(class_1802.field_8845)  // Golden
         || stack.method_31574(class_1802.field_8528)  // Stone
         || stack.method_31574(class_1802.field_8091)  // Wooden
         || stack.method_31574(class_1802.field_61338)) { // Copper
         return true;
      }
      try {
         String id = class_7923.field_41178.method_10221(stack.method_7909()).toString();
         return id.endsWith("_sword");
      } catch (Throwable t) {
         return false;
      }
   }

   public static boolean isDiamondOrNetherite(class_1799 stack) {
      if (stack == null || stack.method_7960()) return false;
      if (stack.method_31574(class_1802.field_8802) || stack.method_31574(class_1802.field_22022)) {
         return true;
      }
      try {
         String id = class_7923.field_41178.method_10221(stack.method_7909()).toString();
         return "minecraft:diamond_sword".equals(id) || "minecraft:netherite_sword".equals(id);
      } catch (Throwable t) {
         return false;
      }
   }

   public static boolean hasLootingThree(class_1799 stack) {
      if (stack == null || stack.method_7960()) return false;
      try {
         for (var entry : stack.method_58657().method_57539()) {
            if (entry.getKey().method_40226(class_2960.method_60654("minecraft:looting"))
               && entry.getValue() != null && entry.getValue() >= 3) {
               return true;
            }
         }
      } catch (Throwable t) {
      }
      return false;
   }

   public static int getSwordScore(class_1799 stack) {
      if (!isSword(stack)) return 0;
      boolean diamondOrNetherite = isDiamondOrNetherite(stack);
      boolean looting3 = hasLootingThree(stack);
      if (diamondOrNetherite && looting3) return 100;
      if (looting3) return 80;
      if (diamondOrNetherite) return 60;
      return 20;
   }

   public static boolean isOminous(class_1799 stack) {
      if (stack == null || stack.method_7960()) return false;
      if (stack.method_31574(class_1802.field_50140)) return true;
      if ("net.minecraft.class_9462".equals(stack.method_7909().getClass().getName())) return true;
      try {
         String id = class_7923.field_41178.method_10221(stack.method_7909()).toString();
         return "minecraft:ominous_bottle".equals(id);
      } catch (Throwable t) {
         return false;
      }
   }

   public static boolean isFirework(class_1799 stack) {
      if (stack == null || stack.method_7960()) return false;
      if (stack.method_31574(class_1802.field_8639)) return true;
      try {
         String id = class_7923.field_41178.method_10221(stack.method_7909()).toString();
         return "minecraft:firework_rocket".equals(id);
      } catch (Throwable t) {
         return false;
      }
   }

   public static int findBestSwordBagSlot(class_1661 inventory, int currentScore, int excludedSlot) {
      if (inventory == null) return -1;
      int bestScore = currentScore;
      int bestIndex = -1;
      for (int i = 9; i < 36; i++) {
         if (i == excludedSlot) continue;
         class_1799 stack = inventory.method_5438(i);
         int score = getSwordScore(stack);
         if (score > bestScore) {
            bestScore = score;
            bestIndex = i;
         }
      }
      return bestIndex;
   }

   public static int findFirstOminousBagSlot(class_1661 inventory, int excluded1, int excluded2) {
      if (inventory == null) return -1;
      for (int i = 9; i < 36; i++) {
         if (i == excluded1 || i == excluded2) continue;
         class_1799 stack = inventory.method_5438(i);
         if (isOminous(stack)) return i;
      }
      return -1;
   }

   public static int findFirstFireworkBagSlot(class_1661 inventory, int excluded1, int excluded2) {
      if (inventory == null) return -1;
      for (int i = 9; i < 36; i++) {
         if (i == excluded1 || i == excluded2) continue;
         class_1799 stack = inventory.method_5438(i);
         if (isFirework(stack)) return i;
      }
      return -1;
   }

   public static boolean hasAnyRefillNeed(class_1661 inventory) {
      if (inventory == null) return false;
      class_1799 hotbar0 = inventory.method_5438(0);
      class_1799 hotbar1 = inventory.method_5438(1);
      class_1799 hotbar2 = inventory.method_5438(2);

      int currentSwordScore = getSwordScore(hotbar0);
      int bestSwordSlot = findBestSwordBagSlot(inventory, currentSwordScore, -1);
      boolean needSword = bestSwordSlot >= 0;

      int ominousSlot = (!isOminous(hotbar1)) ? findFirstOminousBagSlot(inventory, bestSwordSlot, -1) : -1;
      boolean needOminous = ominousSlot >= 0;

      int fireworkSlot = (!isFirework(hotbar2)) ? findFirstFireworkBagSlot(inventory, bestSwordSlot, ominousSlot) : -1;
      boolean needFirework = fireworkSlot >= 0;

      return needSword || needOminous || needFirework;
   }
}
