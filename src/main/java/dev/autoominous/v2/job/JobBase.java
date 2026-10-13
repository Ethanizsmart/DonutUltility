package dev.autoominous.v2.job;

import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.TerminalResult;

public abstract class JobBase {
   private final long jobId;
   private final long sessionEpoch;
   private final Owner owner;
   private final long createdAtMs;
   private final long absoluteDeadlineMs;
   private TerminalResult result = TerminalResult.RUNNING;

   protected JobBase(long var1, long var3, Owner var5, long var6, long var8) {
      this.jobId = var1;
      this.sessionEpoch = var3;
      this.owner = var5;
      this.createdAtMs = var6;
      this.absoluteDeadlineMs = var8;
   }

   public long jobId() {
      return this.jobId;
   }

   public long sessionEpoch() {
      return this.sessionEpoch;
   }

   public Owner owner() {
      return this.owner;
   }

   public long createdAtMs() {
      return this.createdAtMs;
   }

   public long absoluteDeadlineMs() {
      return this.absoluteDeadlineMs;
   }

   public TerminalResult result() {
      return this.result;
   }

   public boolean running() {
      return this.result == TerminalResult.RUNNING;
   }

   public boolean expired(long var1) {
      return var1 > this.absoluteDeadlineMs;
   }

   public void succeed() {
      this.result = TerminalResult.SUCCESS;
   }

   public void fail() {
      this.result = TerminalResult.FAILED;
   }

   public void cancel() {
      this.result = TerminalResult.CANCELLED;
   }
}
