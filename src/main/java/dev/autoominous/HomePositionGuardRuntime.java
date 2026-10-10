package dev.autoominous;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public final class HomePositionGuardRuntime {
   private static final double HORIZONTAL_TOLERANCE = 0.3;
   private static final double Y_TOLERANCE = 1.0;
   private static final long SETTLE_MS = 650L;
   private static final long TRANSIT_TIMEOUT_MS = 10000L;
   private static final int MAX_HOME_SENDS = 2;
   private static final long TPA_GRACE_MS = 2000L;
   private static boolean loaded;
   private static boolean anchorSet;
   private static double anchorX;
   private static double anchorY;
   private static double anchorZ;
   private static String anchorWorld = "";
   private static boolean captureRequested;
   private static HomePositionGuardRuntime.Transit transit = HomePositionGuardRuntime.Transit.NONE;
   private static long transitStartedAtMs;
   private static int homeSendCount;
   private static boolean teleportSeen;
   private static Object preWorld;
   private static String preWorldKey = "";
   private static double preX;
   private static double preY;
   private static double preZ;
   private static long stableSinceMs;
   private static double lastX;
   private static double lastY;
   private static double lastZ;
   private static long driftSinceMs = -1L;
   private static long driftRequiredMs = 500L;
   private static long guardCooldownUntilMs;
   private static boolean returnFailureLatched;
   private static boolean sawTpaBusy;
   private static long tpaGraceUntilMs;
   private static boolean pauseSwordRecovery;
   private static String pausedSwordState = "";
   private static String lastLogKey = "";

   private HomePositionGuardRuntime() {
   }

   public static void resetForMaster() { cancelTransitForHigherPriority("Master OFF", false); captureRequested = false; driftSinceMs = -1L; sawTpaBusy = false; tpaGraceUntilMs = 0L; pauseSwordRecovery = false; }
   public static void onTick() {
      if (!MasterAutomation.isEnabled()) {  return; }
      ensureLoaded();
      invokeStaticVoid("dev.autoominous.DeathRecoverySettings", "maybePersist");
      long var0 = System.currentTimeMillis();
      Object var2 = getMinecraftClient();
      if (var2 != null) {
         Object var3 = getField(var2, "field_1724");
         Object var4 = getField(var2, "field_1687");
         if (var3 != null && var4 != null) {
            boolean var5 = isAutoTpaBusy();
            if (var5) {
               sawTpaBusy = true;
               tpaGraceUntilMs = var0 + 2000L;
            } else if (sawTpaBusy) {
               sawTpaBusy = false;
               tpaGraceUntilMs = var0 + 2000L;
            }

            if (transit != HomePositionGuardRuntime.Transit.NONE) {
               tickTransit(var2, var3, var4, var0);
            } else if (captureRequested) {
               if (!isDeathRecoveryPending() && !var5 && var0 >= tpaGraceUntilMs && !isVexDangerArmed()) {
                  captureRequested = false;
                  beginTransit(var2, var3, var4, HomePositionGuardRuntime.Transit.CAPTURE, var0, false);
               }
            } else if (!isHomeGuardEnabled() || !anchorSet) {
               driftSinceMs = -1L;
               if (!isConfiguredHomeGuardEnabled()) {
                  returnFailureLatched = false;
               }
            } else if (var0 >= guardCooldownUntilMs && !var5 && var0 >= tpaGraceUntilMs) {
               boolean var6 = isDeathRecoveryPending();
               if (var6) {
                  driftSinceMs = -1L;
               } else {
                  double[] var7 = position(var3);
                  if (var7 != null) {
                     String var8 = worldKey(var4);
                     boolean var9 = isOutsideAnchor(var7[0], var7[1], var7[2], var8);
                     if (!var9) {
                        driftSinceMs = -1L;
                        returnFailureLatched = false;
                     } else if (returnFailureLatched) {
                        driftSinceMs = -1L;
                     } else if (driftSinceMs < 0L) {
                        driftSinceMs = var0;
                        driftRequiredMs = random(400, 600);
                     } else if (var0 - driftSinceMs >= driftRequiredMs) {
                        boolean var10 = SwordRecoveryRuntime.isRecoveryActive();
                        beginTransit(var2, var3, var4, HomePositionGuardRuntime.Transit.RETURN, var0, var10);
                        driftSinceMs = -1L;
                     }
                  }
               }
            } else {
               driftSinceMs = -1L;
            }
         }
      }
   }

   public static void requestCapture() {
      ensureLoaded();
      captureRequested = true;
      returnFailureLatched = false;
      actionbar("Home 1 capture queued: waiting for a safe moment...");
      System.out.println("[Auto Ominous] Home Guard: Home 1 capture queued.");
   }

   public static boolean isBlockingAutomation() {
      return transit != HomePositionGuardRuntime.Transit.NONE;
   }

   public static boolean isRecoveryPending() {
      return isBlockingAutomation() || isDeathRecoveryPending() || SwordRecoveryRuntime.isRecoveryActive();
   }

   public static boolean isAutoUseBlocked() {
      return isBlockingAutomation() || isDeathRecoveryPending() || SwordRecoveryRuntime.isRecoveryActive();
   }

   public static void cancelForVexDanger() {
      cancelTransitForHigherPriority("Vex danger", false);
   }

   public static void cancelForDeathRecovery() {
      cancelTransitForHigherPriority("Death Recovery", false);
   }

   public static boolean isBlockingSell() {
      return isBlockingAutomation() || invokeStaticBoolean("dev.autoominous.BountyRuntime", "isBlockingSell", false);
   }

   public static boolean shouldPauseSwordRecovery() {
      return transit != HomePositionGuardRuntime.Transit.NONE && pauseSwordRecovery;
   }

   public static void injectGuiControls(Object var0) {
      if (var0 != null) {
         try {
            Field var1 = declaredField(var0.getClass(), "currentTab");
            Object var2 = var1.get(null);
            if (var2 == null || !"DEATH_RECOVERY".equals(String.valueOf(var2))) {
               return;
            }

            Class var3 = Class.forName("fi.dy.masa.malilib.gui.button.ButtonGeneric");
            Constructor var4 = var3.getConstructor(int.class, int.class, int.class, int.class, String.class, String[].class);
            Object var5 = var4.newInstance(10, 48, 535, 20, getCaptureButtonLabel(), new String[0]);
            Class var6 = Class.forName("fi.dy.masa.malilib.gui.button.IButtonActionListener");
            Object var7 = Proxy.newProxyInstance(var6.getClassLoader(), new Class[]{var6}, (var1x, var2x, var3x) -> {
               String var4x = var2x.getName();
               if ("actionPerformedWithButton".equals(var4x)) {
                  requestCapture();

                  try {
                     Method var5x = var0.getClass().getMethod("initGui");
                     var5x.invoke(var0);
                  } catch (Throwable var6x) {
                  }

                  return null;
               } else if ("toString".equals(var4x)) {
                  return "Home1CaptureListener";
               } else if ("hashCode".equals(var4x)) {
                  return System.identityHashCode(var1x);
               } else {
                  return "equals".equals(var4x) ? var1x == (var3x == null ? null : var3x[0]) : null;
               }
            });
            Method var8 = findMethodInHierarchy(var0.getClass(), "addButton", 2);
            if (var8 != null) {
               var8.setAccessible(true);
               var8.invoke(var0, var5, var7);
            }
         } catch (Throwable var9) {
            logOnce("GUI injection", var9);
         }
      }
   }

   public static String getCaptureButtonLabel() {
      ensureLoaded();
      return !anchorSet
         ? "Capture Home 1  |  Saved: NOT SET  |  tolerance: 0.30 block"
         : String.format(Locale.ROOT, "Capture Home 1  |  Saved: %.2f, %.2f, %.2f  |  tol 0.30", anchorX, anchorY, anchorZ);
   }

   private static void beginTransit(Object var0, Object var1, Object var2, HomePositionGuardRuntime.Transit var3, long var4, boolean var6) {
      double[] var7 = position(var1);
      if (var7 != null) {
         transit = var3;
         transitStartedAtMs = var4;
         homeSendCount = 0;
         teleportSeen = false;
         stableSinceMs = 0L;
         preWorld = var2;
         preWorldKey = worldKey(var2);
         preX = lastX = var7[0];
         preY = lastY = var7[1];
         preZ = lastZ = var7[2];
         pauseSwordRecovery = var6;
         pausedSwordState = var6 ? swordStateName() : "";
         prepareForHomeTransit(var0, var1);
         sendHome1(var0);
         homeSendCount = 1;
         System.out
            .println(
               "[Auto Ominous] Home Guard: sent /home 1 for " + (var3 == HomePositionGuardRuntime.Transit.CAPTURE ? "capture" : "position correction") + "."
            );
      }
   }

   private static void tickTransit(Object var0, Object var1, Object var2, long var3) {
      double[] var5 = position(var1);
      if (var5 != null) {
         String var6 = worldKey(var2);
         if (transit == HomePositionGuardRuntime.Transit.RETURN) {
            if (isInsideAnchor(var5[0], var5[1], var5[2], var6)) {
               if (stableSinceMs == 0L) {
                  stableSinceMs = var3;
                  lastX = var5[0];
                  lastY = var5[1];
                  lastZ = var5[2];
               } else {
                  if (distanceSq(var5[0], var5[1], var5[2], lastX, lastY, lastZ) > 0.01) {
                     stableSinceMs = var3;
                  }

                  lastX = var5[0];
                  lastY = var5[1];
                  lastZ = var5[2];
                  if (var3 - stableSinceMs >= 650L) {
                     finishReturn(var3);
                     return;
                  }
               }
            } else {
               stableSinceMs = 0L;
            }
         } else if (transit == HomePositionGuardRuntime.Transit.CAPTURE) {
            boolean var7 = !sameWorld(preWorldKey, var6);
            double var8 = distanceSq(var5[0], var5[1], var5[2], preX, preY, preZ);
            if (!teleportSeen && (var7 || var8 >= 0.25)) {
               teleportSeen = true;
               stableSinceMs = var3;
               lastX = var5[0];
               lastY = var5[1];
               lastZ = var5[2];
               System.out.println("[Auto Ominous] Home Guard: /home 1 teleport detected for capture.");
            }

            if (teleportSeen) {
               if (distanceSq(var5[0], var5[1], var5[2], lastX, lastY, lastZ) > 0.01) {
                  stableSinceMs = var3;
               }

               lastX = var5[0];
               lastY = var5[1];
               lastZ = var5[2];
               if (var3 - stableSinceMs >= 650L) {
                  saveAnchor(var5[0], var5[1], var5[2], var6);
                  transit = HomePositionGuardRuntime.Transit.NONE;
                  guardCooldownUntilMs = var3 + 1000L;
                  returnFailureLatched = false;
                  requestAutoUseRearm();
                  actionbar(String.format(Locale.ROOT, "Home 1 saved: %.2f %.2f %.2f", anchorX, anchorY, anchorZ));
                  System.out
                     .println(
                        "[Auto Ominous] Home Guard: Home 1 anchor saved at " + fmt(anchorX) + "," + fmt(anchorY) + "," + fmt(anchorZ) + " world=" + anchorWorld
                     );
                  return;
               }
            }
         }

         if (var3 - transitStartedAtMs >= 10000L) {
            if (transit == HomePositionGuardRuntime.Transit.CAPTURE) {
               System.err.println("[Auto Ominous] Home Guard: Capture Home 1 failed; no confirmed teleport, anchor unchanged.");
               actionbar("Capture Home 1 failed: teleport was not confirmed; anchor unchanged.");
               abortTransit(var3, false);
               return;
            }

            if (homeSendCount < 2) {
               prepareForHomeTransit(var0, var1);
               sendHome1(var0);
               homeSendCount++;
               transitStartedAtMs = var3;
               teleportSeen = false;
               stableSinceMs = 0L;
               preWorld = var2;
               preWorldKey = var6;
               preX = lastX = var5[0];
               preY = lastY = var5[1];
               preZ = lastZ = var5[2];
               System.out.println("[Auto Ominous] Home Guard: /home 1 timeout; retry " + homeSendCount + "/2.");
            } else {
               System.err
                  .println(
                     "[Auto Ominous] Home Guard: /home 1 correction failed after bounded retries; auto-correction latched FAILED until Home is reached or recaptured."
                  );
               actionbar("Home 1 correction failed; guard is latched until Home is reached or recaptured.");
               returnFailureLatched = true;
               abortTransit(var3, true);
            }
         }
      }
   }

   private static void finishReturn(long var0) {
      System.out.println("[Auto Ominous] Home Guard: returned to saved Home 1 anchor and position is stable.");
      transit = HomePositionGuardRuntime.Transit.NONE;
      guardCooldownUntilMs = var0 + 1000L;
      returnFailureLatched = false;
      if (pauseSwordRecovery) {
         resumeSwordRecoveryAfterGuard(var0);
      }

      requestAutoUseRearm();
      pauseSwordRecovery = false;
      pausedSwordState = "";
   }

   private static void abortTransit(long var0, boolean var2) {
      transit = HomePositionGuardRuntime.Transit.NONE;
      guardCooldownUntilMs = var0 + 1000L;
      if (!var2) {
         returnFailureLatched = false;
      }

      if (pauseSwordRecovery) {
         resumeSwordRecoveryAfterGuard(var0);
      }

      pauseSwordRecovery = false;
      pausedSwordState = "";
      requestAutoUseRearm();
   }

   private static void cancelTransitForHigherPriority(String var0, boolean var1) {
      if (transit != HomePositionGuardRuntime.Transit.NONE) {
         long var2 = System.currentTimeMillis();
         HomePositionGuardRuntime.Transit var4 = transit;
         transit = HomePositionGuardRuntime.Transit.NONE;
         driftSinceMs = -1L;
         guardCooldownUntilMs = var2 + 500L;
         if (var1 && pauseSwordRecovery) {
            resumeSwordRecoveryAfterGuard(var2);
         }

         pauseSwordRecovery = false;
         pausedSwordState = "";
         teleportSeen = false;
         stableSinceMs = 0L;
         homeSendCount = 0;
         requestAutoUseRearm();
         System.out.println("[Auto Ominous] Home Guard: cancelled " + var4 + " transit for higher priority: " + var0 + ".");
      }
   }

   private static void prepareForHomeTransit(Object var0, Object var1) {
      invokeStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "forceRelease");
      invokeStaticVoid("com.seastore.auto.seaats.client.HumanizedSellRuntime", "forceReset");
      invokeStaticVoid("dev.autoominous.SlashSellRuntime", "resetRuntime");
      closeHandledScreen(var0, var1);
   }

   private static void requestAutoUseRearm() {
      invokeStaticVoid("dev.autoominous.ProtectedItemAutoUseRuntime", "requestRearm");
   }

   private static void sendHome1(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "method_1562");
         if (var1 == null) {
            return;
         }

         String var2 = invokeStaticString("dev.autoominous.DeathRecoverySettings", "getHome1CommandForSend", "/home 1");
         var2 = normalizeCommand(var2);
         if (var2.isBlank()) {
            var2 = "home 1";
         }

         Method var3 = var1.getClass().getMethod("method_45730", String.class);
         var3.invoke(var1, var2);
      } catch (Throwable var4) {
         System.err.println("[Auto Ominous] Home Guard: send /home 1 failed: " + var4.getMessage());
      }
   }

   private static void resumeSwordRecoveryAfterGuard(long var0) {
      try {
         if (!SwordRecoveryRuntime.isRecoveryActive()) {
            return;
         }

         String var2 = pausedSwordState;
         String var3;
         if (!"WAIT_AUCTION".equals(var2) && !"SET_FILTER".equals(var2) && !"SCAN_AUCTION".equals(var2)
            && !"WAIT_PURCHASE".equals(var2) && !"VERIFY_PURCHASE".equals(var2) && !"OPEN_AUCTION".equals(var2)) {
            var3 = "CHECK_LOCAL";
         } else {
            var3 = "OPEN_AUCTION";
         }

         Class var4 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         setSwordState(var3);
         declaredField(var4, "nextActionAtMs").setLong(null, var0 + 300L);
         declaredField(var4, "stateDeadlineMs").setLong(null, var0 + 6000L);
         declaredField(var4, "allowAutoSell").setBoolean(null, false);
         System.out.println("[Auto Ominous] Home Guard: resumed Auction Sword Recovery at " + var3 + ".");
      } catch (Throwable var5) {
         System.err.println("[Auto Ominous] Home Guard: resume Sword Recovery failed: " + var5.getMessage());
      }
   }

   private static String swordStateName() {
      try {
         Class var0 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
         Field var1 = declaredField(var0, "state");
         Object var2 = var1.get(null);
         return var2 == null ? "" : String.valueOf(var2);
      } catch (Throwable var3) {
         return "";
      }
   }

   private static void setSwordState(String var0) throws Exception {
      Class var1 = Class.forName("dev.autoominous.SwordRecoveryRuntime");
      Field var2 = declaredField(var1, "state");
      Class var3 = var2.getType();
      Enum var4 = Enum.valueOf(var3, var0);
      var2.set(null, var4);
   }

   private static boolean isOutsideAnchor(double var0, double var2, double var4, String var6) {
      if (!sameWorld(anchorWorld, var6)) {
         return true;
      } else {
         double var7 = var0 - anchorX;
         double var9 = var4 - anchorZ;
         return var7 * var7 + var9 * var9 > 0.09 || Math.abs(var2 - anchorY) > 1.0;
      }
   }

   private static boolean isInsideAnchor(double var0, double var2, double var4, String var6) {
      return !isOutsideAnchor(var0, var2, var4, var6);
   }

   private static boolean sameWorld(String var0, String var1) {
      return var0 != null && !var0.isBlank() && var1 != null && !var1.isBlank() ? var0.equals(var1) : true;
   }

   private static boolean isConfiguredHomeGuardEnabled() {
      return invokeStaticBoolean("dev.autoominous.DeathRecoverySettings", "isHomeGuardEnabled", false);
   }

   private static boolean isHomeGuardEnabled() {
      boolean var0 = isConfiguredHomeGuardEnabled();
      return !var0 ? false : invokeStaticBoolean("dev.autoominous.VexSafetyRuntime", "allowHomeGuardConfigured", true);
   }

   private static boolean isSwordRecoveryEnabled() {
      return invokeStaticBoolean("dev.autoominous.DeathRecoverySettings", "isSwordRecoveryEnabled", true);
   }

   private static boolean isDeathRecoveryPending() {
      return invokeStaticBoolean("dev.autoominous.DeathRecoveryRuntime", "isRecoveryPending", false);
   }

   private static boolean isVexDangerArmed() {
      return invokeStaticBoolean("dev.autoominous.VexSafetyRuntime", "isDangerArmed", false);
   }

   private static boolean isAutoTpaBusy() {
      try {
         Class var0 = Class.forName("dev.autoominous.AutoTpaRuntime");
         Field var1 = declaredField(var0, "pendingUser");
         Field var2 = declaredField(var0, "awaitingConfirm");
         return var1.get(null) != null || var2.getBoolean(null);
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void saveAnchor(double var0, double var2, double var4, String var6) {
      anchorX = var0;
      anchorY = var2;
      anchorZ = var4;
      anchorWorld = var6 == null ? "" : var6;
      anchorSet = true;
      saveAnchorFile();
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         try {
            Path var0 = anchorPath();
            if (!Files.isReadable(var0)) {
               return;
            }

            Properties var1 = new Properties();

            try (BufferedReader var2 = Files.newBufferedReader(var0, StandardCharsets.UTF_8)) {
               var1.load(var2);
            }

            boolean var8 = Boolean.parseBoolean(var1.getProperty("verifiedTeleportCapture", "false"));
            anchorSet = var8 && Boolean.parseBoolean(var1.getProperty("set", "false"));
            anchorX = parseDouble(var1.getProperty("x"), 0.0);
            anchorY = parseDouble(var1.getProperty("y"), 0.0);
            anchorZ = parseDouble(var1.getProperty("z"), 0.0);
            anchorWorld = var1.getProperty("world", "");
            if (!var8 && Boolean.parseBoolean(var1.getProperty("set", "false"))) {
               System.out.println("[Auto Ominous] Home Guard: ignored legacy unverified Home 1 anchor; Capture Home 1 again on FIX11.");
            }
         } catch (Throwable var7) {
            System.err.println("[Auto Ominous] Home Guard: anchor load failed: " + var7.getMessage());
         }
      }
   }

   private static void saveAnchorFile() {
      try {
         Path var0 = anchorPath();
         Files.createDirectories(var0.getParent());
         Properties var1 = new Properties();
         var1.setProperty("set", Boolean.toString(anchorSet));
         var1.setProperty("x", Double.toString(anchorX));
         var1.setProperty("y", Double.toString(anchorY));
         var1.setProperty("z", Double.toString(anchorZ));
         var1.setProperty("world", anchorWorld == null ? "" : anchorWorld);
         var1.setProperty("horizontalTolerance", "0.30");
         var1.setProperty("verifiedTeleportCapture", "true");

         try (BufferedWriter var2 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            var1.store(var2, "Auto Ominous - Home 1 Position Guard");
         }
      } catch (Throwable var7) {
         System.err.println("[Auto Ominous] Home Guard: anchor save failed: " + var7.getMessage());
      }
   }

   private static Path anchorPath() {
      return Paths.get(System.getProperty("user.dir", "."), "config", "autoominous-home1-guard.properties");
   }

   private static Object getMinecraftClient() {
      try {
         Class var0 = Class.forName("net.minecraft.class_310");
         return var0.getMethod("method_1551").invoke(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static double[] position(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            double var1 = ((Number)var0.getClass().getMethod("method_23317").invoke(var0)).doubleValue();
            double var3 = ((Number)var0.getClass().getMethod("method_23318").invoke(var0)).doubleValue();
            double var5 = ((Number)var0.getClass().getMethod("method_23321").invoke(var0)).doubleValue();
            return new double[]{var1, var3, var5};
         } catch (Throwable var7) {
            return null;
         }
      }
   }

   private static String worldKey(Object var0) {
      if (var0 == null) {
         return "";
      } else {
         for (String var4 : new String[]{"method_27983", "getRegistryKey"}) {
            try {
               Object var5 = var0.getClass().getMethod(var4).invoke(var0);
               if (var5 != null) {
                  return String.valueOf(var5);
               }
            } catch (Throwable var6) {
            }
         }

         return var0.getClass().getName();
      }
   }

   private static void closeHandledScreen(Object var0, Object var1) {
      try {
         Object var2 = getField(var0, "field_1755");
         if (var2 != null && Class.forName("net.minecraft.class_465").isInstance(var2)) {
            if (var1 != null) {
               try {
                  var1.getClass().getMethod("method_7346").invoke(var1);
               } catch (Throwable var6) {
               }
            }

            Class var3 = Class.forName("net.minecraft.class_437");

            try {
               var0.getClass().getMethod("method_1507", var3).invoke(var0, (Object)null);
            } catch (Throwable var5) {
            }
         }
      } catch (Throwable var7) {
      }
   }

   private static Object getField(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return declaredField(var0.getClass(), var1).get(var0);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static Field declaredField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Method findMethodInHierarchy(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }

   private static Object invokeNoArg(Object var0, String var1) {
      try {
         return var0 == null ? null : var0.getClass().getMethod(var1).invoke(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static void invokeStaticVoid(String var0, String var1) {
      try {
         Class.forName(var0).getMethod(var1).invoke(null);
      } catch (Throwable var3) {
      }
   }

   private static boolean invokeStaticBoolean(String var0, String var1, boolean var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 instanceof Boolean ? (Boolean)var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static String invokeStaticString(String var0, String var1, String var2) {
      try {
         Object var3 = Class.forName(var0).getMethod(var1).invoke(null);
         return var3 == null ? var2 : String.valueOf(var3);
      } catch (Throwable var4) {
         return var2;
      }
   }

   private static void actionbar(String var0) {
      try {
         Class.forName("dev.autoominous.ClientStatusMessage").getMethod("actionbar", String.class).invoke(null, var0);
      } catch (Throwable var2) {
      }
   }

   private static String normalizeCommand(String var0) {
      if (var0 == null) {
         return "";
      } else {
         String var1 = var0.trim();

         while (var1.startsWith("/")) {
            var1 = var1.substring(1).trim();
         }

         return var1;
      }
   }

   private static int random(int var0, int var1) {
      return ThreadLocalRandom.current().nextInt(var0, var1 + 1);
   }

   private static double parseDouble(String var0, double var1) {
      try {
         return Double.parseDouble(var0);
      } catch (Throwable var4) {
         return var1;
      }
   }

   private static double distanceSq(double var0, double var2, double var4, double var6, double var8, double var10) {
      double var12 = var0 - var6;
      double var14 = var2 - var8;
      double var16 = var4 - var10;
      return var12 * var12 + var14 * var14 + var16 * var16;
   }

   private static String fmt(double var0) {
      return String.format(Locale.ROOT, "%.3f", var0);
   }

   private static void logOnce(String var0, Throwable var1) {
      String var2 = var0 + ":" + var1.getClass().getName() + ":" + var1.getMessage();
      if (!var2.equals(lastLogKey)) {
         lastLogKey = var2;
         System.err.println("[Auto Ominous] Home Guard " + var0 + " failed: " + var1.getMessage());
      }
   }

   private static enum Transit {
      NONE,
      CAPTURE,
      RETURN;
   }
}
