package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import dev.autoominous.HumanBehaviorProfile;
import java.util.Random;

public final class SellCycleScheduler {
   static final int PERIOD_TICKS = 1200;
   private static final Random RANDOM = new Random();
   private static final int[] order = new int[]{0, 1, 2, 3};
   private static int cursor = 4;
   private static int currentCycle = -1;
   private static int selectedDelay = 0;
   private static int round = 0;
   private static int periodTicks = 0;

   private SellCycleScheduler() {
   }

   public static void reset() {
      cursor = 4;
      currentCycle = -1;
      selectedDelay = 0;
      round = 0;
      periodTicks = 0;
   }

   public static void tick(boolean var0) {
      if (var0) {
         ensureCycle();
         if (++periodTicks >= 1200) {
            selectNextCycle();
         }
      }
   }

   public static int nextDelayTicks() {
      ensureCycle();
      int[] var0 = new int[]{Config.cycle1MinTicks, Config.cycle2MinTicks, Config.cycle3MinTicks, Config.cycle4MinTicks};
      int[] var1 = new int[]{Config.cycle1MaxTicks, Config.cycle2MaxTicks, Config.cycle3MaxTicks, Config.cycle4MaxTicks};
      int var2 = Math.max(1, Math.min(72000, var0[currentCycle]));
      int var3 = Math.max(var2, Math.min(72000, var1[currentCycle]));
      selectedDelay = var2 + RANDOM.nextInt(var3 - var2 + 1);
      return selectedDelay = HumanBehaviorProfile.sellCycleDelayTicks(selectedDelay, currentCycle);
   }

   private static void ensureCycle() {
      if (currentCycle < 0) {
         selectNextCycle();
      }
   }

   private static void selectNextCycle() {
      if (cursor == 4) {
         int var0 = 0;

         while (var0 < 4) {
            order[var0] = var0++;
         }

         for (int var3 = 3; var3 > 0; var3--) {
            int var1 = RANDOM.nextInt(var3 + 1);
            int var2 = order[var3];
            order[var3] = order[var1];
            order[var1] = var2;
         }

         cursor = 0;
         round++;
      }

      currentCycle = order[cursor++];
      selectedDelay = 0;
      periodTicks = 0;
   }

   public static int currentCycle() {
      return currentCycle + 1;
   }

   public static int position() {
      return currentCycle < 0 ? 0 : cursor;
   }

   public static int selectedDelay() {
      return selectedDelay;
   }

   public static int secondsRemaining() {
      return currentCycle < 0 ? 60 : Math.max(0, (1200 - periodTicks + 19) / 20);
   }

   public static int round() {
      return round;
   }
}
