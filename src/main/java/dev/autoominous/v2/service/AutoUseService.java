package dev.autoominous.v2.service;

import dev.autoominous.v2.broker.InputBroker;
import dev.autoominous.v2.core.FeatureSettings;
import dev.autoominous.v2.core.Owner;
import dev.autoominous.v2.core.PermissionSnapshot;

public final class AutoUseService {
   private final InputBroker input;

   public AutoUseService(InputBroker var1) {
      this.input = var1;
   }

   public void reconcile(FeatureSettings var1, PermissionSnapshot var2, InputBroker.UseIntent var3, long var4, long var6, long var8) {
      if (var1.holdRmb() && var2.canHoldRmb()) {
         this.input.request(var3, Owner.AUTO_USE, true, var4, var6, var8);
      } else {
         this.input.release();
      }
   }
}
