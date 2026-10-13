package dev.autoominous.v2.core;

public final class SessionManager {
   private long epoch;
   private boolean connected;

   public long epoch() {
      return this.epoch;
   }

   public boolean connected() {
      return this.connected;
   }

   public boolean updateConnected(boolean var1) {
      if (var1 == this.connected) {
         return false;
      }

      this.connected = var1;
      this.epoch++;
      return true;
   }
}
