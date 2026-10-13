package dev.autoominous.v2.service;

import dev.autoominous.v2.core.RuntimeSnapshot;
import dev.autoominous.v2.core.VexScan;

public final class VexSafetyService {
   private long zeroSinceMs = -1L;

   public boolean clearConfirmed(RuntimeSnapshot var1) {
      if (var1.vexScan() != VexScan.UNKNOWN && var1.vexScan() != VexScan.PRESENT) {
         if (this.zeroSinceMs < 0L) {
            this.zeroSinceMs = var1.nowMs();
         }

         return var1.nowMs() - this.zeroSinceMs >= 1000L;
      } else {
         this.zeroSinceMs = -1L;
         return false;
      }
   }

   public void reset() {
      this.zeroSinceMs = -1L;
   }
}
