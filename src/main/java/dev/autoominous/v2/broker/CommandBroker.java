package dev.autoominous.v2.broker;

import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.Resource;
import dev.autoominous.v2.core.ResourceArbiter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class CommandBroker {
   private final ResourceArbiter arbiter;
   private final Deque<CommandBroker.CommandIntent> queue = new ArrayDeque<>();
   private long minGapMs = 500L;
   private long lastSentAtMs = -2305843009213693952L;
   private long backoffUntilMs;

   public CommandBroker(ResourceArbiter var1) {
      this.arbiter = var1;
   }

   public void setMinGapMs(long var1) {
      this.minGapMs = Math.max(0L, var1);
   }

   public void submit(CommandBroker.CommandIntent var1) {
      this.queue.addLast(var1);
   }

   public void onServerCooldown(long var1, long var3) {
      this.backoffUntilMs = Math.max(this.backoffUntilMs, var1 + Math.max(500L, var3));
   }

   public Optional<CommandBroker.CommandIntent> pollExecutable(long var1, long var3) {
      while (!this.queue.isEmpty()) {
         CommandBroker.CommandIntent var5 = this.queue.peekFirst();
         if (var5.sessionEpoch() == var1 && var3 <= var5.deadlineMs()) {
            if (var3 >= var5.earliestAtMs() && var3 >= this.backoffUntilMs && var3 - this.lastSentAtMs >= this.minGapMs) {
               Optional var6 = this.arbiter.acquire(Resource.COMMAND, var5.owner(), var1, var5.jobId(), var3, 1000L);
               if (var6.isEmpty()) {
                  return Optional.empty();
               }

               this.queue.removeFirst();
               this.lastSentAtMs = var3;
               return Optional.of(var5);
            }

            return Optional.empty();
         }

         this.queue.removeFirst();
      }

      return Optional.empty();
   }

   public void complete(Owner var1) {
      this.arbiter.release(Resource.COMMAND, var1);
   }

   public void clear() {
      this.queue.clear();
      this.backoffUntilMs = 0L;
      this.lastSentAtMs = -2305843009213693952L;
   }

   public record CommandIntent(Owner owner, long sessionEpoch, long jobId, String command, long earliestAtMs, long deadlineMs) {
   }
}
