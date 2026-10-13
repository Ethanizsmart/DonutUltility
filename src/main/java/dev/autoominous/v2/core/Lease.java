package dev.autoominous.v2.core;

public record Lease(Resource resource, Owner owner, long sessionEpoch, long jobId, long expiresAtMs) {
   public boolean valid(long var1, long var3) {
      return this.sessionEpoch == var1 && var3 <= this.expiresAtMs;
   }
}
