package dev.autoominous.v2.broker;

import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.Resource;
import dev.autoominous.v2.core.ResourceArbiter;

public final class InventoryBroker {
   private final ResourceArbiter arbiter;

   public InventoryBroker(ResourceArbiter var1) {
      this.arbiter = var1;
   }

   public boolean acquire(Owner var1, long var2, long var4, long var6, long var8) {
      return this.arbiter.acquire(Resource.INVENTORY, var1, var2, var4, var6, var8).isPresent();
   }

   public void release(Owner var1) {
      this.arbiter.release(Resource.INVENTORY, var1);
   }
}
