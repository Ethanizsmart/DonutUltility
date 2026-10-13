package dev.autoominous.v2.broker;

import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.Resource;
import dev.autoominous.v2.core.ResourceArbiter;

public final class GuiBroker {
   private final ResourceArbiter arbiter;

   public GuiBroker(ResourceArbiter var1) {
      this.arbiter = var1;
   }

   public boolean acquire(Owner var1, long var2, long var4, long var6, long var8) {
      return this.arbiter.acquire(Resource.GUI, var1, var2, var4, var6, var8).isPresent();
   }

   public boolean owns(Owner var1, long var2, long var4) {
      return this.arbiter.owns(Resource.GUI, var1, var2, var4);
   }

   public void release(Owner var1) {
      this.arbiter.release(Resource.GUI, var1);
   }
}
