package dev.autoominous;

/** Requires continuous absence; interruptions must start a fresh observation window. */
final class MissingSwordTimer {
   public static final long REQUIRED_ABSENCE_MS = 15000L;
   private long missingSinceMs = -1L;

   boolean update(boolean missing, long nowMs) {
      if (!missing) {
         reset();
         return false;
      }
      if (missingSinceMs < 0L || nowMs < missingSinceMs) missingSinceMs = nowMs;
      return nowMs - missingSinceMs >= REQUIRED_ABSENCE_MS;
   }

   void reset() {
      missingSinceMs = -1L;
   }
}
