package dev.autoominous;

/** Requires continuous absence; interruptions must start a fresh observation window. */
final class MissingSwordTimer {
   private long missingSinceMs = -1L;

   boolean update(boolean missing, long nowMs) {
      if (!missing) {
         reset();
         return false;
      }
      if (missingSinceMs < 0L || nowMs < missingSinceMs) missingSinceMs = nowMs;
      return nowMs - missingSinceMs >= 15000L;
   }

   void reset() {
      missingSinceMs = -1L;
   }

   long missingForMs(long nowMs) {
      return missingSinceMs < 0L ? 0L : Math.max(0L, nowMs - missingSinceMs);
   }
}
