package dev.autoominous.v2.service;

import dev.autoominous.v2.core.PermissionSnapshot;
import dev.autoominous.v2.core.RuntimeSnapshot;

public final class TpaService {
   public boolean shouldAccept(RuntimeSnapshot var1, PermissionSnapshot var2) {
      return var1.tpaRequested() && var2.canAutoTpa();
   }
}
