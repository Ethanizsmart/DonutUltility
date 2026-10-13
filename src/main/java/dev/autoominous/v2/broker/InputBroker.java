package dev.autoominous.v2.broker;

import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.Resource;
import dev.autoominous.v2.core.ResourceArbiter;

public final class InputBroker {
   private final ResourceArbiter arbiter;
   private InputBroker.UseIntent intent = InputBroker.UseIntent.NONE;
   private Owner owner = Owner.NONE;

   public InputBroker(ResourceArbiter var1) {
      this.arbiter = var1;
   }

   public boolean request(InputBroker.UseIntent var1, Owner var2, boolean var3, long var4, long var6, long var8) {
      if (!var3 || var1 == InputBroker.UseIntent.NONE) {
         this.release();
         return false;
      } else if (this.arbiter.acquire(Resource.RMB, var2, var4, var6, var8, 250L).isEmpty()) {
         return false;
      } else if (this.arbiter.acquire(Resource.HOTBAR, var2, var4, var6, var8, 250L).isEmpty()) {
         this.arbiter.release(Resource.RMB, var2);
         return false;
      } else {
         this.intent = var1;
         this.owner = var2;
         return true;
      }
   }

   public InputBroker.UseIntent intent() {
      return this.intent;
   }

   public Owner owner() {
      return this.owner;
   }

   public void release() {
      if (this.owner != Owner.NONE) {
         this.arbiter.release(Resource.RMB, this.owner);
         this.arbiter.release(Resource.HOTBAR, this.owner);
      }

      this.intent = InputBroker.UseIntent.NONE;
      this.owner = Owner.NONE;
   }

   public boolean worldLmbAllowed() {
      return false;
   }

   public enum UseIntent {
      NONE,
      SWORD_RMB,
      OMINOUS_BOTTLE,
      FIREWORK_ROCKET;
   }
}
