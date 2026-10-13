package dev.autoominous.v2.service;

import dev.autoominous.v2.core.RuntimeSnapshot;

public final class BountyService {
   public boolean requested(RuntimeSnapshot var1) {
      return var1.bountyRequested();
   }
}
