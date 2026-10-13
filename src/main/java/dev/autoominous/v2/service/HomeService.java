package dev.autoominous.v2.service;

import dev.autoominous.v2.broker.CommandBroker;
import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.RuntimeSnapshot;
import dev.autoominous.v2.job.HomeJob;

public final class HomeService {
   private final CommandBroker commands;
   private HomeJob job;

   public HomeService(CommandBroker var1) {
      this.commands = var1;
   }

   public HomeJob start(long var1, long var3, Owner var5, long var6) {
      this.job = new HomeJob(var1, var3, var5, var6, var6 + 35000L, 3);
      return this.job;
   }

   public HomeJob job() {
      return this.job;
   }

   public void cancel() {
      if (this.job != null && this.job.running()) {
         this.job.cancel();
      }
   }

   public void tick(RuntimeSnapshot var1, long var2) {
      if (this.job != null && this.job.running()) {
         if (this.job.sessionEpoch() != var2) {
            this.job.cancel();
         } else if (this.job.expired(var1.nowMs())) {
            this.job.fail();
         } else if (var1.atVerifiedHomeAnchor() && var1.homeAnchorStableMs() >= 650L) {
            this.job.succeed();
            this.commands.complete(this.job.owner());
         } else if (!this.job.canAttempt()) {
            this.job.fail();
         } else {
            boolean var4 = this.job.attempts() == 0;
            boolean var5 = !var4 && var1.nowMs() - this.job.lastCommandAtMs() >= 10000L;
            if (var4 || var5) {
               this.commands.submit(new CommandBroker.CommandIntent(this.job.owner(), var2, this.job.jobId(), "home 1", var1.nowMs(), var1.nowMs() + 2000L));
               this.job.markCommandSent(var1.nowMs());
            }
         }
      }
   }
}
