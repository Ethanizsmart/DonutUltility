package dev.autoominous.v2.core;

public record RuntimeSnapshot(
   boolean connected,
   boolean playerAlive,
   boolean deathScreen,
   boolean screenOpen,
   VexScan vexScan,
   boolean vexDangerArmed,
   boolean atVerifiedHomeAnchor,
   long homeAnchorStableMs,
   boolean swordInHotbar,
   boolean swordInInventory,
   boolean inventoryHasFreeSlot,
   boolean sellRequested,
   boolean tpaRequested,
   boolean bountyRequested,
   boolean commandCooldown,
   long nowMs
) {
   public static RuntimeSnapshot offline(long var0) {
      return new RuntimeSnapshot(false, false, false, false, VexScan.UNKNOWN, false, false, 0L, false, false, false, false, false, false, false, var0);
   }
}
