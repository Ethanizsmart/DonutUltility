package dev.autoominous;

import java.lang.reflect.Method;
import java.util.Objects;

public final class G232SafeRuntime {
   private static long lastSwordSwapMs;
   private static long inhibitRmbUntilNs;
   private static long lastWarningMs;
   private static String lastWarning = "";
   private static Method guardedPriority;
   private static Method triggerItemUse;
   private static Method ensureFarmItems;
   private static Method isFarmSword;
   private static long recoverySinceMs;
   private static int recoveryAttempts;
   private static boolean recoveryPending;
   private static boolean recoveryHalted;
   private static long recoveryLastProgressMs;
   private static String recoveryPhase = "";
   private static final int MAX_RECOVERY_SWAPS = 3;
   private static final long RECOVERY_BLOCKED_LIMIT_MS = 6500L;

   private G232SafeRuntime() {
   }

   public static void preTick() {
      try {
         promoteSwordWhenSafe();
      } catch (Throwable var1) {
         warning("SWORD_PROMOTE_ERROR", var1.getClass().getSimpleName());
      }

      if (isSwordRecoverySuspended()) {
         ProtectedItemAutoUseRuntime.forceRelease();
      } else {
         FarmCycleRuntime.tick();
      }
   }

   public static boolean isSwordRecoverySuspended() {
      return recoveryPending || recoveryHalted;
   }

   public static void resetForDeathRecovery() {
      clearRecovery("new death recovery session");
   }

   private static void clearRecovery(String var0) {
      if (recoveryPending || recoveryHalted) {
         BehaviorLogRuntime.warning("G235_SWORD_RECOVERED", "phase=" + recoveryPhase + " reason=" + var0 + " attempts=" + recoveryAttempts);
      }

      recoverySinceMs = 0L;
      recoveryAttempts = 0;
      recoveryPending = false;
      recoveryHalted = false;
      recoveryLastProgressMs = 0L;
      recoveryPhase = "";
   }

   private static void haltSwordRecovery(String var0) {
      if (!recoveryHalted) {
         recoveryHalted = true;
         recoveryPending = true;
         ProtectedItemAutoUseRuntime.forceRelease();
         BehaviorLogRuntime.error(
            "G235_SWORD_RECOVERY_HALTED",
            "phase=" + recoveryPhase + " attempts=" + recoveryAttempts + " reason=" + var0 + "; safe stop, manually move sword to a hotbar slot"
         );
      }
   }

   private static void promoteSwordWhenSafe() throws Exception {
      long var0 = System.currentTimeMillis();
      if (ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isEnabled", false)
         && ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false)) {
         Object var2 = ReflectionSupport.mc();
         if (var2 != null) {
            Object var3 = ReflectionSupport.field(var2, "field_1724");
            Object var4 = ReflectionSupport.field(var2, "field_1761");
            if (var3 != null && var4 != null) {
               String var5 = FarmCycleRuntime.phaseName();
               if (!shouldPromoteInPhase(var5)) {
                  if (recoveryPending && !recoveryHalted) {
                     clearRecovery("phase changed");
                  }
               } else if (ReflectionSupport.field(var2, "field_1755") == null
                  && !DeathSwordAhRecoveryRuntime.isActive()
                  && !MissingSwordWatchdogRuntime.isPurchaseActive()
                  && !SwordRecoveryRuntime.isRecoveryBlockingAutoSell()
                  && !TransactionGate.isSeaSellActive()
                  && !TransactionGate.isBountyActive()) {
                  Object var6 = ReflectionSupport.callNoArg(var3, "method_31548");
                  if (var6 != null) {
                     if (ensureFarmItems == null) {
                        ensureFarmItems = FarmCycleRuntime.class.getDeclaredMethod("ensureItems");
                        ensureFarmItems.setAccessible(true);
                        isFarmSword = FarmCycleRuntime.class.getDeclaredMethod("isSword", Object.class);
                        isFarmSword.setAccessible(true);
                     }

                     ensureFarmItems.invoke(null);
                     int var7 = -1;
                     int var8 = -1;

                     for (int var9 = 0; var9 < 36; var9++) {
                        Object var10 = stackAt(var6, var9);
                        if (!isEmpty(var10) && isSword(var10)) {
                           if (var9 < 9) {
                              clearRecovery("hotbar sword confirmed");
                              return;
                           }

                           int var11 = String.valueOf(ReflectionSupport.callNoArg(var10, "method_7909")).contains("diamond_sword") ? 2 : 1;
                           if (var11 > var8) {
                              var7 = var9;
                              var8 = var11;
                           }
                        }
                     }

                     if (var7 < 9) {
                        if (!recoveryHalted) {
                           clearRecovery("sword no longer in inventory");
                        }
                     } else if (!"WAIT_FIRST_SWORD".equals(var5) || !hotbarContains(var6, "ominous_bottle") && !hotbarContains(var6, "firework_rocket")) {
                        if (!recoveryHalted) {
                           if (recoverySinceMs == 0L) {
                              recoverySinceMs = var0;
                              recoveryPhase = var5;
                              recoveryAttempts = 0;
                              BehaviorLogRuntime.warning("G235_SWORD_IN_MAIN", "phase=" + var5 + " slot=" + var7);
                           }

                           recoveryPending = true;
                           int var14 = -1;

                           for (int var15 = 0; var15 < 9; var15++) {
                              if (isEmpty(stackAt(var6, var15))) {
                                 var14 = var15;
                                 break;
                              }
                           }

                           if (var14 < 0) {
                              for (int var16 = 0; var16 < 9; var16++) {
                                 if (!OminousSurplusSellPolicy.isProtectedForTransfer(stackAt(var6, var16))) {
                                    var14 = var16;
                                    break;
                                 }
                              }
                           }

                           if (var14 < 0) {
                              warning("G235_SWORD_SWAP_BLOCKED", "No empty or unprotected hotbar slot; preserving protected items");
                              if (var0 - recoverySinceMs >= 6500L) {
                                 haltSwordRecovery("no safe hotbar slot");
                              }
                           } else if (var0 - lastSwordSwapMs >= 900L) {
                              if (recoveryAttempts >= 3) {
                                 haltSwordRecovery("swap not confirmed after 3 attempts");
                              } else {
                                 Object var17 = ReflectionSupport.field(var3, "field_7512");
                                 Object var18 = var17 == null ? null : ReflectionSupport.field(var17, "field_7763");
                                 Object var12 = ReflectionSupport.staticField("net.minecraft.class_1713", "field_7791");
                                 Method var13 = findMethod(var4.getClass(), "method_2906", 5);
                                 if (var18 instanceof Number && var12 != null && var13 != null) {
                                    beforeInventorySwap();
                                    lastSwordSwapMs = var0;
                                    recoveryAttempts++;
                                    recoveryLastProgressMs = var0;
                                    var13.setAccessible(true);
                                    var13.invoke(var4, ((Number)var18).intValue(), var7, var14, var12, var3);
                                    BehaviorLogRuntime.warning(
                                       "G235_SWORD_SWAP",
                                       "phase="
                                          + var5
                                          + " attempt="
                                          + recoveryAttempts
                                          + "/3 main="
                                          + var7
                                          + " hotbar="
                                          + var14
                                          + " (SWAP handles full inventory; verification required)"
                                    );
                                 } else {
                                    if (var0 - recoverySinceMs >= 6500L) {
                                       haltSwordRecovery("container/SWAP API unavailable");
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  if (!recoveryHalted) {
                     recoveryPending = false;
                     recoverySinceMs = 0L;
                     recoveryAttempts = 0;
                  }
               }
            }
         }
      } else {
         clearRecovery("automation disabled");
      }
   }

   private static boolean shouldPromoteInPhase(String var0) {
      return var0 == null
         || var0.equals("WAIT_FIRST_SWORD")
         || var0.equals("BOOTSTRAP")
         || var0.equals("STORE_SWORD")
         || var0.equals("WAIT_RETURN")
         || var0.equals("VERIFY_SWORD")
         || var0.equals("STORE_RETURNED_SWORD")
         || var0.equals("MACHINE_RUNNING")
         || var0.equals("LOOT_SETTLE")
         || var0.equals("SELL")
         || var0.equals("ERROR");
   }

   private static boolean hotbarContains(Object var0, String var1) {
      for (int var2 = 0; var2 < 9; var2++) {
         Object var3 = stackAt(var0, var2);
         if (!isEmpty(var3)) {
            Object var4 = ReflectionSupport.callNoArg(var3, "method_7909");
            if (var4 != null && String.valueOf(var4).contains(var1)) {
               return true;
            }
         }
      }

      return false;
   }

   private static Object stackAt(Object var0, int var1) {
      try {
         return var0.getClass().getMethod("method_5438", int.class).invoke(var0, var1);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static boolean isEmpty(Object var0) {
      return var0 == null || ReflectionSupport.callBool(var0, "method_7960", true);
   }

   private static boolean isSword(Object var0) throws Exception {
      if (isEmpty(var0)) {
         return false;
      }

      Object var1 = ReflectionSupport.callNoArg(var0, "method_7909");
      return var1 != null && Boolean.TRUE.equals(isFarmSword.invoke(null, var1));
   }

   public static boolean swordHotbarOrPending(Object var0) {
      return ReflectionSupport.callBool(var0, "swordHotbar", false) || ReflectionSupport.callBool(var0, "swordAny", false);
   }

   public static void selectHotbarSlotGuarded(Object var0, String var1, int var2) throws Exception {
      if (!"method_61496".equals(var1)) {
         throw new IllegalArgumentException("Not a selected-hotbar mutation");
      }

      beforeInventorySwap();
      Method var3 = findMethod(var0.getClass(), var1, 1);
      if (var3 == null) {
         throw new NoSuchMethodException(var1);
      }

      var3.setAccessible(true);
      var3.invoke(var0, var2);
   }

   public static Object invokeSwapGuarded(Method var0, Object var1, Object[] var2) throws Exception {
      beforeInventorySwap();
      return var0.invoke(var1, var2);
   }

   private static void beforeInventorySwap() {
      ProtectedItemAutoUseRuntime.forceRelease();
      inhibitRmbUntilNs = System.nanoTime() + 125000000L;
   }

   public static void setUsePressedGuarded(Object var0, boolean var1) throws Exception {
      if (var1 && isRmbSafe(var0)) {
         Object var2 = ReflectionSupport.field(var0, "field_1690");
         Object var3 = ReflectionSupport.field(var2, "field_1904");
         if (var3 == null) {
            ProtectedItemAutoUseRuntime.forceRelease();
         } else {
            Method var4 = findMethod(var3.getClass(), "method_23481", 1);
            if (var4 == null) {
               ProtectedItemAutoUseRuntime.forceRelease();
            } else {
               var4.setAccessible(true);
               var4.invoke(var3, true);
            }
         }
      } else {
         ProtectedItemAutoUseRuntime.forceRelease();
      }
   }

   public static void triggerItemUseGuarded(Object var0) throws Exception {
      if (!isRmbSafe(var0)) {
         ProtectedItemAutoUseRuntime.forceRelease();
      } else {
         if (triggerItemUse == null) {
            triggerItemUse = ProtectedItemAutoUseRuntime.class.getDeclaredMethod("triggerItemUse", Object.class);
            triggerItemUse.setAccessible(true);
         }

         triggerItemUse.invoke(null, var0);
      }
   }

   private static boolean isRmbSafe(Object var0) {
      try {
         if (var0 == null || isSwordRecoverySuspended() || System.nanoTime() < inhibitRmbUntilNs) {
            return false;
         }

         if (ReflectionSupport.field(var0, "field_1755") != null) {
            return false;
         }

         Object var1 = ReflectionSupport.field(var0, "field_1724");
         if (var1 == null) {
            return false;
         }

         Object var2 = ReflectionSupport.callNoArg(var1, "method_31548");
         if (var2 == null) {
            return false;
         }

         Object var3 = ReflectionSupport.callNoArg(var2, "method_67532");
         if (!(var3 instanceof Number)) {
            return false;
         }

         int var4 = ((Number)var3).intValue();
         if (var4 >= 0 && var4 < 9) {
            Object var5 = stackAt(var2, var4);
            if (isEmpty(var5)) {
               return false;
            }

            if (guardedPriority == null) {
               guardedPriority = ProtectedItemAutoUseRuntime.class.getDeclaredMethod("priorityOfStack", Object.class);
               guardedPriority.setAccessible(true);
            }

            Object var6 = guardedPriority.invoke(null, var5);
            return var6 instanceof Number && ((Number)var6).intValue() > 0;
         } else {
            return false;
         }
      } catch (Throwable var7) {
         return false;
      }
   }

   private static Method findMethod(Class<?> var0, String var1, int var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2) {
               return var7;
            }
         }
      }

      return null;
   }

   private static void warning(String var0, String var1) {
      long var2 = System.currentTimeMillis();
      if (!Objects.equals(var0, lastWarning) || var2 - lastWarningMs > 15000L) {
         lastWarning = var0;
         lastWarningMs = var2;
         BehaviorLogRuntime.warning(var0, var1);
      }
   }
}
