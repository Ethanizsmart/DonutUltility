package dev.autoominous.v2.job;

import dev.autoominous.v2.core.Owner;

public final class HomeJob extends JobBase {
   private final int maxAttempts;
   private int attempts;
   private long lastCommandAtMs = -2305843009213693952L;

   public HomeJob(long var1, long var3, Owner var5, long var6, long var8, int var10) {
      super(var1, var3, var5, var6, var8);
      this.maxAttempts = Math.max(1, var10);
   }

   public int attempts() {
      return this.attempts;
   }

   public int maxAttempts() {
      return this.maxAttempts;
   }

   public long lastCommandAtMs() {
      return this.lastCommandAtMs;
   }

   public boolean canAttempt() {
      return this.attempts < this.maxAttempts && this.running();
   }

   public void markCommandSent(long var1) {
      this.attempts++;
      this.lastCommandAtMs = var1;
   }
}
