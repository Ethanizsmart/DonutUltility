package com.seastore.auto.seaats.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_310;

/** Source selection and inclusive tick ranges from the CoreV2 reference. */
final class SellRhythmRuntime {
   private static boolean initialized;
   private static int tempo;
   private static int lastSlot = -1;
   private static int sameTypeRun;
   private static class_1799 previousSignature;
   private static boolean fastFollowUp;
   private static double sessionBias;

   private SellRhythmRuntime() {}
   static void reset() {
      initialized = false;
      tempo = 1;
      lastSlot = -1;
      sameTypeRun = 0;
      previousSignature = null;
      fastFollowUp = false;
   }
   static int selectSlot(class_310 client, class_1703 menu) {
      ensureInitialized();
      List<Integer> candidates = new ArrayList<>(36);
      List<Integer> same = new ArrayList<>();
      for (int i = Math.max(0, menu.field_7761.size() - 36); i < menu.field_7761.size(); i++) {
         if (AutoSellLogic.isSellableInventorySlot(client, menu.method_7611(i))) {
            candidates.add(i);
            if (previousSignature != null && class_1799.method_31577(menu.method_7611(i).method_7677(), previousSignature)) same.add(i);
         }
      }
      if (candidates.isEmpty()) return -1;
      boolean follow = previousSignature != null && sameTypeRun < 2 && !same.isEmpty() && random(0, 99) < 20;
      List<Integer> pool = follow ? same : candidates;
      if (!follow && previousSignature != null && sameTypeRun >= 2) {
         List<Integer> different = new ArrayList<>(candidates);
         different.removeAll(same);
         if (!different.isEmpty()) pool = different;
      }
      int selected = chooseLocal(pool);
      class_1799 stack = menu.method_7611(selected).method_7677();
      boolean repeated = previousSignature != null && class_1799.method_31577(stack, previousSignature);
      sameTypeRun = repeated ? Math.min(2, sameTypeRun + 1) : 1;
      fastFollowUp = follow && repeated;
      previousSignature = stack.method_7972();
      lastSlot = selected;
      return selected;
   }
   static int nextDelayTicks() {
      ensureInitialized();
      int draw = random(0, 99);
      if (draw < 5) return random(18, 28);
      if (draw < 15) return random(9, 16);
      if (fastFollowUp) return random(2, 3);
      return switch (tempo) {
         case 0 -> random(3, 6);
         case 2 -> random(6, 11);
         default -> random(4, 8);
      };
   }
   private static int chooseLocal(List<Integer> candidates) {
      if (candidates.size() == 1) return candidates.get(0);
      if (lastSlot < 0) return candidates.get(random(0, Math.min(5, candidates.size() - 1)));
      List<Integer> ordered = new ArrayList<>(candidates);
      ordered.sort(Comparator.comparingInt(slot -> Math.abs(slot - lastSlot) + 2 * Math.abs(slot / 9 - lastSlot / 9)));
      int draw = random(0, 99);
      int size = draw < 68 ? Math.min(3, ordered.size()) : draw < 93 ? Math.min(6, ordered.size()) : ordered.size();
      return ordered.get(random(0, size - 1));
   }
   private static void ensureInitialized() {
      if (!initialized) {
         sessionBias = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
         int draw = random(0, 99);
         tempo = draw < 25 ? 0 : draw < 85 ? 1 : 2;
         initialized = true;
      }
   }
   private static int random(int min, int max) {
      double draw = Math.pow(ThreadLocalRandom.current().nextDouble(), Math.exp(-sessionBias * 0.17));
      return min + Math.min(max - min, (int)(draw * (max - min + 1)));
   }
}
