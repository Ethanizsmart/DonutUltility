package dev.autoominous;

/** Geometry only; independent of Minecraft and configuration files. */
public final class HomeAnchorRules {
   private HomeAnchorRules() {}

   public static boolean finite(double... values) {
      for (double value : values) if (!Double.isFinite(value)) return false;
      return true;
   }

   public static double yawDifference(double a, double b) {
      return Math.abs(Math.IEEEremainder(a - b, 360.0));
   }

   public static boolean withinPosition(double x, double y, double z,
      double anchorX, double anchorY, double anchorZ, double tolerance) {
      if (!finite(x,y,z,anchorX,anchorY,anchorZ,tolerance) || tolerance < 0) return false;
      double dx = x-anchorX, dz = z-anchorZ;
      return Math.hypot(dx,dz) <= tolerance && Math.abs(y-anchorY) <= tolerance;
   }

   public static boolean withinLook(double yaw, double pitch, double anchorYaw, double anchorPitch, double tolerance) {
      return finite(yaw,pitch,anchorYaw,anchorPitch,tolerance) && tolerance >= 0
         && yawDifference(yaw,anchorYaw) <= tolerance && Math.abs(pitch-anchorPitch) <= tolerance;
   }
}
