package dev.autoominous.v2.service;

import dev.autoominous.v2.core.PermissionSnapshot;
import dev.autoominous.v2.core.RuntimeSnapshot;

public final class SellService {
   public boolean shouldRequest(RuntimeSnapshot var1, PermissionSnapshot var2) {
      return var1.sellRequested() && var2.canSell();
   }
}
