package dev.autoominous;

import dev.autoominous.v2.core.AutomationCoordinatorV2;
import dev.autoominous.v2.core.CoordinatorDecision;
import dev.autoominous.v2.core.FeatureSettings;
import dev.autoominous.v2.core.RuntimeSnapshot;
import dev.autoominous.v2.core.VexScan;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Optional;

public final class CoreV2RuntimeBridge {
   // Reconstructed from field/constructor references: both classes are absent from the input JAR.
   private static final class AnchorStatus {
      static final AnchorStatus OUTSIDE = new AnchorStatus(false, 0L);
      final boolean inside;
      final long stableMs;

      AnchorStatus(boolean inside, long stableMs) {
         this.inside = inside;
         this.stableMs = stableMs;
      }
   }

   private static final class SwordStatus {
      static final SwordStatus EMPTY = new SwordStatus(false, false, false);
      final boolean hotbar;
      final boolean inventory;
      final boolean freeSlot;

      SwordStatus(boolean hotbar, boolean inventory, boolean freeSlot) {
         this.hotbar = hotbar;
         this.inventory = inventory;
         this.freeSlot = freeSlot;
      }
   }

   private static final AutomationCoordinatorV2 CORE = new AutomationCoordinatorV2();
   private static final long VEX_SCAN_INTERVAL_MS = 100L;
   private static final long VEX_DANGER_GRACE_MS = 3000L;
   private static final double HOME_XZ_TOLERANCE = 0.3;
   private static final double HOME_Y_TOLERANCE = 1.0;
   private static long lastVexScanAtMs = -2305843009213693952L;
   private static VexScan lastVexScan = VexScan.UNKNOWN;
   private static long lastVexSeenAtMs = -2305843009213693952L;
   private static long homeInsideSinceMs = -1L;
   private static String lastStateKey = "";
   private static long lastErrorLogAtMs;
   private static Class<?> itemsClass;
   private static Object ominousBottle;
   private static Object fireworkRocket;
   private static Object diamondSword;
   private static Object[] swords;

   private CoreV2RuntimeBridge() {
   }

   public static void onClientTick() {
      long var0 = System.currentTimeMillis();

      try {
         Object var2 = ReflectionSupport.mc();
         Object var3 = var2 == null ? null : ReflectionSupport.field(var2, "field_1724");
         Object var4 = var2 == null ? null : ReflectionSupport.field(var2, "field_1687");
         Object var5 = var2 == null ? null : ReflectionSupport.field(var2, "field_1755");
         boolean var6 = var2 != null && var3 != null && var4 != null;
         boolean var7 = isInstance(var5, "net.minecraft.class_418");
         boolean var8 = var6 && ReflectionSupport.callBool(var3, "method_5805", false) && !ReflectionSupport.callBool(var3, "method_29504", false) && !var7;
         VexScan var9 = var6 ? scanVex(var4, var0) : VexScan.UNKNOWN;
         boolean var10 = var9 == VexScan.PRESENT || lastVexSeenAtMs > 0L && var0 - lastVexSeenAtMs <= 3000L;
         AnchorStatus var11 = var6 ? anchorStatus(var3, var4, var0) : AnchorStatus.OUTSIDE;
         SwordStatus var12 = var6 ? swordStatus(var2, var3) : SwordStatus.EMPTY;
         FeatureSettings var13 = new FeatureSettings(
            safeBool("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false),
            safeBool("dev.autoominous.SlashSellSettings", "isEnabled", false),
            safeBool("dev.autoominous.AutoTpaSettings", "isEnabled", false),
            safeBool("dev.autoominous.DeathRecoverySettings", "isSwordRecoveryEnabled", true),
            safeBool("dev.autoominous.DeathRecoverySettings", "isHomeGuardEnabled", false),
            safeBool("dev.autoominous.BountySettings", "isEnabled", false)
         );
         dev.autoominous.v2.broker.InputBroker.UseIntent var14 = var6 && var13.holdRmb()
            ? desiredAutoUse(var3)
            : dev.autoominous.v2.broker.InputBroker.UseIntent.NONE;
         RuntimeSnapshot var15 = new RuntimeSnapshot(
            var6,
            var8,
            var7,
            var5 != null,
            var9,
            var10,
            var11.inside,
            var11.stableMs,
            var12.hotbar,
            var12.inventory,
            var12.freeSlot,
            false,
            false,
            false,
            safeBool("dev.autoominous.CommandBroker", "isBackoffActive", false),
            var0
         );
         CoordinatorDecision var16 = CORE.tick(var15, var13, var14);
         drainShadowCommands(var0);
         logStateChange(var16, var15, var13);
      } catch (Throwable var17) {
         if (var0 - lastErrorLogAtMs >= 5000L) {
            lastErrorLogAtMs = var0;
            System.err.println("[CoreV2 SHADOW] runtime adapter error: " + var17.getClass().getSimpleName() + ": " + var17.getMessage());
         }
      }
   }

   public static void onGameMessageObject(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = ReflectionSupport.callNoArg(var0, "getString");
            String var2 = String.valueOf(var1 == null ? var0 : var1).toLowerCase(Locale.ROOT);
            if (var2.contains("need to wait another") && var2.contains("second") && var2.contains("execute a command")) {
               CORE.commandBroker().onServerCooldown(System.currentTimeMillis(), 650L);
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static String statusLine() {
      return "state=" + CORE.state() + " epoch=" + CORE.sessionEpoch() + " input=" + CORE.inputBroker().intent();
   }

   private static void drainShadowCommands(long var0) {
      int var3 = 0;

      Optional var2;
      while (var3++ < 4 && (var2 = CORE.commandBroker().pollExecutable(CORE.sessionEpoch(), var0)).isPresent()) {
         dev.autoominous.v2.broker.CommandBroker.CommandIntent var4 = (dev.autoominous.v2.broker.CommandBroker.CommandIntent)var2.get();
         System.out
            .println(
               "[CoreV2 SHADOW] would-send command=\"/"
                  + var4.command()
                  + "\" owner="
                  + var4.owner()
                  + " jobId="
                  + var4.jobId()
                  + " epoch="
                  + var4.sessionEpoch()
            );
         CORE.commandBroker().complete(var4.owner());
      }
   }

   private static void logStateChange(CoordinatorDecision var0, RuntimeSnapshot var1, FeatureSettings var2) {
      String var3 = var0.state()
         + "|"
         + var0.note()
         + "|"
         + var1.connected()
         + "|"
         + var1.deathScreen()
         + "|"
         + var1.vexScan()
         + "|"
         + var1.atVerifiedHomeAnchor()
         + "|"
         + var0.inputIntent();
      if (!var3.equals(lastStateKey)) {
         lastStateKey = var3;
         System.out
            .println(
               "[CoreV2 SHADOW] state="
                  + var0.state()
                  + " note="
                  + var0.note()
                  + " connected="
                  + var1.connected()
                  + " death="
                  + var1.deathScreen()
                  + " vex="
                  + var1.vexScan()
                  + " home="
                  + var1.atVerifiedHomeAnchor()
                  + "/"
                  + var1.homeAnchorStableMs()
                  + "ms swordHotbar="
                  + var1.swordInHotbar()
                  + " swordInventory="
                  + var1.swordInInventory()
                  + " holdRmb="
                  + var2.holdRmb()
                  + " desiredInput="
                  + var0.inputIntent()
            );
      }
   }

   private static VexScan scanVex(Object var0, long var1) {
      if (var1 - lastVexScanAtMs < 100L) {
         return lastVexScan;
      }

      lastVexScanAtMs = var1;

      try {
         Method var3 = findMethod(var0.getClass(), "method_18112", 0);
         if (var3 == null) {
            return lastVexScan = VexScan.UNKNOWN;
         }

         if (var3.invoke(var0) instanceof Iterable var5) {
            Class var6 = Class.forName("net.minecraft.class_1634");

            for (Object var8 : var5) {
               if (var8 != null && var6.isInstance(var8)) {
                  lastVexSeenAtMs = var1;
                  return lastVexScan = VexScan.PRESENT;
               }
            }

            return lastVexScan = VexScan.ZERO;
         } else {
            return lastVexScan = VexScan.UNKNOWN;
         }
      } catch (Throwable var9) {
         return lastVexScan = VexScan.UNKNOWN;
      }
   }

   private static AnchorStatus anchorStatus(Object var0, Object var1, long var2) {
      try {
         if (!ReflectionSupport.staticBoolean("dev.autoominous.HomePositionGuardRuntime", "anchorSet", false)) {
            homeInsideSinceMs = -1L;
            return AnchorStatus.OUTSIDE;
         }

         double var4 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorX", Double.NaN);
         double var6 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorY", Double.NaN);
         double var8 = ReflectionSupport.staticDouble("dev.autoominous.HomePositionGuardRuntime", "anchorZ", Double.NaN);
         Object var10 = ReflectionSupport.staticField("dev.autoominous.HomePositionGuardRuntime", "anchorWorld");
         double[] var11 = ReflectionSupport.pos(var0);
         String var12 = ReflectionSupport.worldKey(var1);
         if (var11 != null && Double.isFinite(var4) && Double.isFinite(var6) && Double.isFinite(var8)) {
            String var13 = String.valueOf(var10);
            boolean var14 = var10 == null || "null".equals(var13) || var13.equals(var12);
            double var15 = var11[0] - var4;
            double var17 = var11[2] - var8;
            boolean var19 = var14 && var15 * var15 + var17 * var17 <= 0.09 && Math.abs(var11[1] - var6) <= 1.0;
            if (!var19) {
               homeInsideSinceMs = -1L;
               return AnchorStatus.OUTSIDE;
            }

            if (homeInsideSinceMs < 0L) {
               homeInsideSinceMs = var2;
            }

            return new AnchorStatus(true, Math.max(0L, var2 - homeInsideSinceMs));
         } else {
            homeInsideSinceMs = -1L;
            return AnchorStatus.OUTSIDE;
         }
      } catch (Throwable var20) {
         homeInsideSinceMs = -1L;
         return AnchorStatus.OUTSIDE;
      }
   }

   private static SwordStatus swordStatus(Object var0, Object var1) {
      try {
         ensureItems();
         Method var2 = findMethod(var1.getClass(), "method_31548", 0);
         if (var2 == null) {
            return SwordStatus.EMPTY;
         }

         Object var3 = var2.invoke(var1);
         if (var3 == null) {
            return SwordStatus.EMPTY;
         }

         Method var4 = findMethod(var3.getClass(), "method_5438", 1);
         if (var4 == null) {
            return SwordStatus.EMPTY;
         }

         boolean var5 = false;
         boolean var6 = false;
         boolean var7 = false;

         for (int var8 = 0; var8 < 36; var8++) {
            Object var9 = var4.invoke(var3, var8);
            if (isEmptyStack(var9)) {
               var7 = true;
            } else if (isDiamondSword(var9) && hasLootingThree(var0, var9)) {
               if (var8 < 9) {
                  var5 = true;
               } else {
                  var6 = true;
               }
            }
         }

         return new SwordStatus(var5, var6, var7);
      } catch (Throwable var10) {
         return SwordStatus.EMPTY;
      }
   }

   private static dev.autoominous.v2.broker.InputBroker.UseIntent desiredAutoUse(Object var0) {
      try {
         ensureItems();
         Method var1 = findMethod(var0.getClass(), "method_31548", 0);
         if (var1 == null) {
            return dev.autoominous.v2.broker.InputBroker.UseIntent.NONE;
         }

         Object var2 = var1.invoke(var0);
         Method var3 = var2 == null ? null : findMethod(var2.getClass(), "method_5438", 1);
         if (var3 == null) {
            return dev.autoominous.v2.broker.InputBroker.UseIntent.NONE;
         }

         boolean var4 = false;
         boolean var5 = false;

         for (int var6 = 0; var6 < 9; var6++) {
            Object var7 = var3.invoke(var2, var6);
            Object var8 = itemOf(var7);
            if (var8 != null) {
               if (var8 == ominousBottle || "net.minecraft.class_9462".equals(var8.getClass().getName())) {
                  return dev.autoominous.v2.broker.InputBroker.UseIntent.OMINOUS_BOTTLE;
               }

               if (var8 == fireworkRocket) {
                  var4 = true;
               }

               if (isSwordItem(var8)) {
                  var5 = true;
               }
            }
         }

         if (var4) {
            return dev.autoominous.v2.broker.InputBroker.UseIntent.FIREWORK_ROCKET;
         }

         if (var5) {
            return dev.autoominous.v2.broker.InputBroker.UseIntent.SWORD_RMB;
         }
      } catch (Throwable var9) {
      }

      return dev.autoominous.v2.broker.InputBroker.UseIntent.NONE;
   }

   private static boolean isDiamondSword(Object var0) {
      Object var1 = itemOf(var0);
      return var1 != null && var1 == diamondSword;
   }

   private static boolean isSwordItem(Object var0) {
      if (var0 != null && swords != null) {
         for (Object var4 : swords) {
            if (var0 == var4) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Object itemOf(Object var0) {
      return var0 != null && !isEmptyStack(var0) ? ReflectionSupport.callNoArg(var0, "method_7909") : null;
   }

   private static boolean isEmptyStack(Object var0) {
      return var0 == null || ReflectionSupport.callBool(var0, "method_7960", true);
   }

   private static boolean hasLootingThree(Object var0, Object var1) {
      try {
         Class var2 = Class.forName("net.minecraft.class_437");
         Method var3 = null;

         for (Method var7 : var2.getDeclaredMethods()) {
            if (var7.getName().equals("method_25408") && Modifier.isStatic(var7.getModifiers()) && var7.getParameterCount() == 2) {
               var3 = var7;
               break;
            }
         }

         if (var3 == null) {
            return false;
         }

         var3.setAccessible(true);
         if (!(var3.invoke(null, var0, var1) instanceof Iterable var12)) {
            return false;
         }

         for (Object var14 : var12) {
            if (var14 != null) {
               Method var8 = findMethod(var14.getClass(), "method_10858", 1);
               String var9;
               if (var8 != null) {
                  var9 = String.valueOf(var8.invoke(var14, 256));
               } else {
                  var9 = String.valueOf(ReflectionSupport.callNoArg(var14, "getString"));
               }

               var9 = var9.toLowerCase(Locale.ROOT);
               if (var9.contains("looting iii") || var9.contains("looting 3")) {
                  return true;
               }
            }
         }
      } catch (Throwable var10) {
      }

      return false;
   }

   private static void ensureItems() throws Exception {
      if (itemsClass == null) {
         itemsClass = Class.forName("net.minecraft.class_1802");
         ominousBottle = staticField(itemsClass, "field_50140");
         fireworkRocket = staticField(itemsClass, "field_8639");
         diamondSword = staticField(itemsClass, "field_8802");
         swords = new Object[]{
            staticField(itemsClass, "field_8091"),
            staticField(itemsClass, "field_61338"),
            staticField(itemsClass, "field_8528"),
            staticField(itemsClass, "field_8845"),
            staticField(itemsClass, "field_8371"),
            diamondSword,
            staticField(itemsClass, "field_22022")
         };
      }
   }

   private static Object staticField(Class<?> var0, String var1) throws Exception {
      Field var2 = var0.getDeclaredField(var1);
      var2.setAccessible(true);
      return var2.get(null);
   }

   private static boolean safeBool(String var0, String var1, boolean var2) {
      return ReflectionSupport.callStaticBoolean(var0, var1, var2);
   }

   private static boolean isInstance(Object var0, String var1) {
      if (var0 == null) {
         return false;
      }

      try {
         return Class.forName(var1).isInstance(var0);
      } catch (Throwable var3) {
         return false;
      }
   }

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               try {
                  var7.setAccessible(true);
               } catch (Throwable var9) {
               }

               return var7;
            }
         }
      }

      return null;
   }
}
