package dev.autoominous.v2.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class ResourceArbiter {
   private final Map<Resource, Lease> leases = new EnumMap<>(Resource.class);

   public synchronized Optional<Lease> acquire(Resource var1, Owner var2, long var3, long var5, long var7, long var9) {
      if (var1 == Resource.WORLD_LMB) {
         return Optional.empty();
      }

      Lease var11 = this.leases.get(var1);
      if (var11 != null && var11.valid(var3, var7) && var11.owner() != var2) {
         return Optional.empty();
      }

      Lease var12 = new Lease(var1, var2, var3, var5, var7 + Math.max(1L, var9));
      this.leases.put(var1, var12);
      return Optional.of(var12);
   }

   public synchronized boolean owns(Resource var1, Owner var2, long var3, long var5) {
      Lease var7 = this.leases.get(var1);
      return var7 != null && var7.owner() == var2 && var7.valid(var3, var5);
   }

   public synchronized void release(Resource var1, Owner var2) {
      Lease var3 = this.leases.get(var1);
      if (var3 != null && var3.owner() == var2) {
         this.leases.remove(var1);
      }
   }

   public synchronized void releaseOwner(Owner var1) {
      this.leases.entrySet().removeIf(var1x -> var1x.getValue().owner() == var1);
   }

   public synchronized void resetForSession(long var1) {
      this.leases.entrySet().removeIf(var2 -> var2.getValue().sessionEpoch() != var1);
   }

   public synchronized void clear() {
      this.leases.clear();
   }
}
