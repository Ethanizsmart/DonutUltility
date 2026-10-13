package dev.autoominous.v2.job;

import dev.autoominous.v2.core.Owner;

public final class RecoveryJob extends JobBase {
   private RecoveryJob.Stage stage;
   private int orderAttempts;
   private int freeSpaceAttempts;

   public RecoveryJob(long var1, long var3, long var5, long var7, boolean var9) {
      super(var1, var3, Owner.DEATH, var5, var7);
      this.stage = var9 ? RecoveryJob.Stage.WAIT_VEX_CLEAR : RecoveryJob.Stage.RESPAWN;
   }

   public RecoveryJob.Stage stage() {
      return this.stage;
   }

   public void stage(RecoveryJob.Stage var1) {
      this.stage = var1;
   }

   public int orderAttempts() {
      return this.orderAttempts;
   }

   public int freeSpaceAttempts() {
      return this.freeSpaceAttempts;
   }

   public int nextOrderAttempt() {
      return ++this.orderAttempts;
   }

   public int nextFreeSpaceAttempt() {
      return ++this.freeSpaceAttempts;
   }

   public enum Stage {
      WAIT_VEX_CLEAR,
      RESPAWN,
      HOME,
      SWORD,
      RESUME;
   }
}
