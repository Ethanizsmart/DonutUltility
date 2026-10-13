package dev.autoominous;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public final class ConsumableRecoveryRuntime {
   private static Object ominousBottle;
   private static Object fireworkRocket;
   private static Object[] swords;
   private static int omiBaseline = -1;
   private static int fireworkBaseline = -1;
   private static long lastPromoteMs;

   private ConsumableRecoveryRuntime() {
   }

   public static void tickHotbarPromotion(Object var0) {
      if (var0 != null && isPromotionAllowed(var0)) {
         try {
            // Ưu tiên 1: Kéo Kiếm từ inventory (9-35) xuống hotbar nếu hotbar chưa có kiếm
            boolean var1 = promoteSwordIfNeeded(var0);
            if (var1) {
               return;
            }
            // Ưu tiên 2: Kéo Ominous Bottle từ inventory (9-35) xuống hotbar nếu hotbar chưa có
            boolean var2 = promoteConsumableIfNeeded(var0, true);
            // Ưu tiên 3: Kéo Pháo hoa từ inventory (9-35) xuống hotbar nếu hotbar chưa có và không vừa swap ominous
            if (!var2) {
               promoteConsumableIfNeeded(var0, false);
            }
         } catch (Throwable var3) {
            BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.promote", var3);
         }
      }
   }

   public static boolean isPromotionAllowed() {
      return isPromotionAllowed(ReflectionSupport.mc());
   }

   public static boolean isPromotionAllowed(Object client) {
      try {
         if (client == null) {
            return false;
         }
         // Đang mở giao diện GUI bất kỳ (Chat, Menu, Container, Chest, Ah, DeathScreen, v.v.)
         if (ReflectionSupport.field(client, "field_1755") != null) {
            return false;
         }
         // Đang thực hiện các luồng giao dịch/hồi sinh quan trọng
         if (ReflectionSupport.callStaticBoolean("dev.autoominous.SwordRecoveryRuntime", "isRecoveryActive", false)) {
            return false;
         }
         if (ReflectionSupport.callStaticBoolean("dev.autoominous.DeathRecoveryRuntime", "isRecoveryPending", false)) {
            return false;
         }
         if (ReflectionSupport.callStaticBoolean("dev.autoominous.BountyRuntime", "isBlockingSell", false)) {
            return false;
         }
         return true;
      } catch (Throwable var1) {
         return true;
      }
   }

   public static void resetOmiBaseline() {
      omiBaseline = -1;
   }

   public static void resetFireworkBaseline() {
      fireworkBaseline = -1;
   }

   public static void armOmiBaseline() {
      try {
         omiBaseline = count(true);
      } catch (Throwable var1) {
         omiBaseline = -1;
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.armOmi", var1);
      }
   }

   public static void armFireworkBaseline() {
      try {
         fireworkBaseline = count(false);
      } catch (Throwable var1) {
         fireworkBaseline = -1;
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.armFirework", var1);
      }
   }

   public static boolean omiConsumedSinceBaseline() {
      return consumed(true);
   }

   public static boolean fireworkConsumedSinceBaseline() {
      return consumed(false);
   }

   private static boolean consumed(boolean var0) {
      try {
         int var1 = count(var0);
         int var2 = var0 ? omiBaseline : fireworkBaseline;
         if (var2 < 0) {
            if (var0) {
               omiBaseline = var1;
            } else {
               fireworkBaseline = var1;
            }

            return false;
         }

         if (var1 < var2) {
            if (var0) {
               omiBaseline = -1;
            } else {
               fireworkBaseline = -1;
            }

            return true;
         }

         if (var1 > var2) {
            if (var0) {
               omiBaseline = var1;
            } else {
               fireworkBaseline = var1;
            }
         }
      } catch (Throwable var3) {
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.consumed", var3);
      }

      return false;
   }

   private static int count(boolean var0) throws Exception {
      ensureItems();
      Object var1 = ReflectionSupport.mc();
      if (var1 == null) {
         return 0;
      }

      Object var2 = ReflectionSupport.field(var1, "field_1724");
      if (var2 == null) {
         return 0;
      }

      Object var3 = ReflectionSupport.callNoArg(var2, "method_31548");
      if (var3 == null) {
         return 0;
      }

      int var4 = 0;

      for (int var5 = 0; var5 < 36; var5++) {
         Object var6 = invokeOneInt(var3, "method_5438", var5);
         if (matches(var6, var0)) {
            Object var7 = ReflectionSupport.callNoArg(var6, "method_7947");
            if (var7 instanceof Number) {
               var4 += Math.max(0, ((Number)var7).intValue());
            }
         }
      }

      return var4;
   }

   private static boolean promoteSwordIfNeeded(Object var0) throws Exception {
      ensureItems();
      Object var2 = ReflectionSupport.field(var0, "field_1724");
      Object var3 = ReflectionSupport.field(var0, "field_1761");
      if (var2 == null || var3 == null) {
         return false;
      }
      Object var4 = ReflectionSupport.callNoArg(var2, "method_31548");
      if (var4 == null) {
         return false;
      }

      // Kiểm tra hotbar xem đã có kiếm chưa
      for (int i = 0; i < 9; i++) {
         Object stack = invokeOneInt(var4, "method_5438", i);
         if (isSword(stack)) {
            return false;
         }
      }

      // Tìm kiếm tốt nhất trong inventory (9-35)
      int bestInvSlot = -1;
      int bestScore = -1;
      for (int i = 9; i < 36; i++) {
         Object stack = invokeOneInt(var4, "method_5438", i);
         if (isSword(stack)) {
            int score = swordScore(var0, stack);
            if (score > bestScore) {
               bestScore = score;
               bestInvSlot = i;
            }
         }
      }
      if (bestInvSlot < 0) {
         return false;
      }

      long now = System.currentTimeMillis();
      if (now - lastPromoteMs < 250L) {
         return false;
      }

      // Chọn slot hotbar mục tiêu: ưu tiên slot 0 nếu trống hoặc chứa item không được bảo vệ
      int targetHotbar = -1;
      Object slot0Stack = invokeOneInt(var4, "method_5438", 0);
      if (isEmpty(slot0Stack) || !isProtectedItem(slot0Stack)) {
         targetHotbar = 0;
      } else {
         // Nếu slot 0 đang chứa protected item (ví dụ Omi hoặc Firework), tìm slot trống trước
         for (int i = 1; i < 9; i++) {
            Object stack = invokeOneInt(var4, "method_5438", i);
            if (isEmpty(stack)) {
               targetHotbar = i;
               break;
            }
         }
         // Nếu không có slot trống, tìm slot chứa item khác (không phải protected) để replace
         if (targetHotbar < 0) {
            for (int i = 1; i < 9; i++) {
               Object stack = invokeOneInt(var4, "method_5438", i);
               if (!isEmpty(stack) && !isProtectedItem(stack)) {
                  targetHotbar = i;
                  break;
               }
            }
         }
      }

      if (targetHotbar < 0) {
         BehaviorLogRuntime.warning("SWORD_PROMOTE_BLOCKED", "no safe hotbar slot available");
         return false;
      }

      boolean swapped = executeSwap(var0, var2, var3, var4, bestInvSlot, targetHotbar);
      if (swapped) {
         lastPromoteMs = now;
         BehaviorLogRuntime.warning("SWORD_PROMOTED", "inventorySlot=" + bestInvSlot + " -> hotbar=" + targetHotbar);
         G232SafeRuntime.clearRecovery("hotbar sword promoted");
         return true;
      }
      return false;
   }

   private static boolean promoteConsumableIfNeeded(Object var0, boolean var1) throws Exception {
      ensureItems();
      Object var2 = ReflectionSupport.field(var0, "field_1724");
      Object var3 = ReflectionSupport.field(var0, "field_1761");
      if (var2 == null || var3 == null) {
         return false;
      }
      Object var4 = ReflectionSupport.callNoArg(var2, "method_31548");
      if (var4 == null) {
         return false;
      }

      // Kiểm tra hotbar xem đã có consumable này chưa
      for (int i = 0; i < 9; i++) {
         if (matches(invokeOneInt(var4, "method_5438", i), var1)) {
            return false;
         }
      }

      // Tìm consumable trong inventory (9-35)
      int invSlot = -1;
      for (int i = 9; i < 36; i++) {
         if (matches(invokeOneInt(var4, "method_5438", i), var1)) {
            invSlot = i;
            break;
         }
      }
      if (invSlot < 0) {
         return false;
      }

      long now = System.currentTimeMillis();
      if (now - lastPromoteMs < 250L) {
         return false;
      }

      // Chọn slot hotbar mục tiêu:
      // Kiểm tra xem hotbar đã có kiếm chưa; nếu chưa mà inventory có kiếm thì dành slot 0 cho kiếm
      boolean hasSwordInHotbar = false;
      for (int i = 0; i < 9; i++) {
         if (isSword(invokeOneInt(var4, "method_5438", i))) {
            hasSwordInHotbar = true;
            break;
         }
      }
      int startSearch = (!hasSwordInHotbar && containsSwordInMain(var4)) ? 1 : 0;

      int targetHotbar = -1;
      // 1. Ưu tiên tìm slot trống
      for (int i = startSearch; i < 9; i++) {
         Object stack = invokeOneInt(var4, "method_5438", i);
         if (isEmpty(stack)) {
            targetHotbar = i;
            break;
         }
      }
      // 2. Nếu không có slot trống, tìm slot chứa item khác (không phải protected item) để replace
      if (targetHotbar < 0) {
         for (int i = startSearch; i < 9; i++) {
            Object stack = invokeOneInt(var4, "method_5438", i);
            if (!isEmpty(stack) && !isProtectedItem(stack)) {
               targetHotbar = i;
               break;
            }
         }
      }
      // Fallback: nếu chưa tìm được và startSearch > 0, kiểm tra slot 0
      if (targetHotbar < 0 && startSearch > 0) {
         Object stack0 = invokeOneInt(var4, "method_5438", 0);
         if (isEmpty(stack0) || !isProtectedItem(stack0)) {
            targetHotbar = 0;
         }
      }

      if (targetHotbar < 0) {
         BehaviorLogRuntime.warning(var1 ? "OMI_PROMOTE_BLOCKED" : "FIREWORK_PROMOTE_BLOCKED", "no safe hotbar slot available");
         return false;
      }

      boolean swapped = executeSwap(var0, var2, var3, var4, invSlot, targetHotbar);
      if (swapped) {
         lastPromoteMs = now;
         BehaviorLogRuntime.warning(var1 ? "OMI_PROMOTED" : "FIREWORK_PROMOTED", "inventorySlot=" + invSlot + " -> hotbar=" + targetHotbar);
         return true;
      }
      return false;
   }

   private static boolean executeSwap(Object client, Object player, Object interactionManager, Object inv, int invSlot, int hotbarSlot) {
      try {
         Object screenHandler = ReflectionSupport.field(player, "field_7512");
         if (screenHandler == null) {
            return false;
         }
         Object syncIdObj = ReflectionSupport.field(screenHandler, "field_7763");
         int syncId = syncIdObj instanceof Number ? ((Number)syncIdObj).intValue() : 0;
         Object swapAction = ReflectionSupport.staticField("net.minecraft.class_1713", "field_7791");
         if (swapAction == null) {
            return false;
         }
         Method clickMethod = findMethod(interactionManager.getClass(), "method_2906", 5);
         if (clickMethod == null) {
            return false;
         }

         int targetScreenSlot = -1;
         Object slotsList = ReflectionSupport.field(screenHandler, "field_7761");
         if (slotsList instanceof List<?> list) {
            for (Object slot : list) {
               Object slotInv = ReflectionSupport.field(slot, "field_7871");
               if (slotInv == inv) {
                  Object idxObj = ReflectionSupport.callNoArg(slot, "method_34266");
                  int slotIndex = idxObj instanceof Number ? ((Number)idxObj).intValue() : -1;
                  if (slotIndex == invSlot) {
                     Object idObj = ReflectionSupport.field(slot, "field_7874");
                     targetScreenSlot = idObj instanceof Number ? ((Number)idObj).intValue() : -1;
                     break;
                  }
               }
            }
         }
         if (targetScreenSlot < 0) {
            targetScreenSlot = invSlot;
         }

         clickMethod.setAccessible(true);
         G232SafeRuntime.invokeSwapGuarded(clickMethod, interactionManager, new Object[]{syncId, targetScreenSlot, hotbarSlot, swapAction, player});
         return true;
      } catch (Throwable t) {
         BehaviorLogRuntime.exception("ConsumableRecoveryRuntime.executeSwap", t);
         return false;
      }
   }

   public static boolean isProtectedItem(Object stack) {
      try {
         if (isEmpty(stack)) {
            return false;
         }
         if (isSword(stack)) {
            return true;
         }
         if (matches(stack, true)) {
            return true;
         }
         if (matches(stack, false)) {
            return true;
         }
         return false;
      } catch (Throwable t) {
         return true;
      }
   }

   public static boolean isSword(Object stack) {
      try {
         if (isEmpty(stack)) {
            return false;
         }
         Object item = ReflectionSupport.callNoArg(stack, "method_7909");
         if (item == null) {
            return false;
         }
         ensureItems();
         if (swords != null) {
            for (Object s : swords) {
               if (s != null && item == s) {
                  return true;
               }
            }
         }
         String name = String.valueOf(item).toLowerCase(Locale.ROOT);
         if (name.contains("sword") || name.contains("mace")) {
            return true;
         }
         String clsName = item.getClass().getName();
         if ("net.minecraft.class_1829".equals(clsName)) {
            return true;
         }
         return false;
      } catch (Throwable t) {
         return false;
      }
   }

   private static int swordScore(Object client, Object stack) {
      if (stack == null || isEmpty(stack)) {
         return 0;
      }
      Object item = ReflectionSupport.callNoArg(stack, "method_7909");
      boolean isDiamond = String.valueOf(item).contains("diamond_sword");
      boolean isNetherite = String.valueOf(item).contains("netherite_sword");
      boolean hasLooting3 = hasLootingThree(client, stack);
      if (hasLooting3 && (isDiamond || isNetherite)) {
         return 100;
      }
      if (hasLooting3) {
         return 80;
      }
      if (isDiamond || isNetherite) {
         return 60;
      }
      return 20;
   }

   private static boolean hasLootingThree(Object client, Object stack) {
      try {
         Object compMap = ReflectionSupport.callNoArg(stack, "method_58657");
         if (compMap != null) {
            Object enchComponent = ReflectionSupport.callNoArg(compMap, "method_57539");
            if (enchComponent instanceof Iterable<?> iter) {
               for (Object entry : iter) {
                  Object key = ReflectionSupport.callNoArg(entry, "getKey");
                  Object lvlObj = ReflectionSupport.callNoArg(entry, "getIntValue");
                  int level = lvlObj instanceof Number ? ((Number)lvlObj).intValue() : 0;
                  if (level == 3 && key != null && String.valueOf(key).contains("looting")) {
                     return true;
                  }
               }
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }

   private static boolean containsSwordInMain(Object inv) {
      try {
         for (int i = 9; i < 36; i++) {
            if (isSword(invokeOneInt(inv, "method_5438", i))) {
               return true;
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }

   private static boolean matches(Object var0, boolean var1) {
      try {
         if (var0 != null && !isEmpty(var0)) {
            Object var2 = ReflectionSupport.callNoArg(var0, "method_7909");
            if (var2 == null) {
               return false;
            }
            if (var1) {
               return var2 == ominousBottle || "net.minecraft.class_9462".equals(var2.getClass().getName()) || String.valueOf(var2).contains("ominous_bottle");
            } else {
               return var2 == fireworkRocket || String.valueOf(var2).contains("firework_rocket");
            }
         } else {
            return false;
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isEmpty(Object var0) {
      return var0 == null ? true : ReflectionSupport.callBool(var0, "method_7960", true);
   }

   private static void ensureItems() {
      if (fireworkRocket == null || ominousBottle == null || swords == null) {
         ominousBottle = ReflectionSupport.staticField("net.minecraft.class_1802", "field_50140");
         fireworkRocket = ReflectionSupport.staticField("net.minecraft.class_1802", "field_8639");
         swords = new Object[]{
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8091"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_61338"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8528"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8845"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8371"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_8802"),
            ReflectionSupport.staticField("net.minecraft.class_1802", "field_22022")
         };
      }
   }

   private static Object invokeOneInt(Object var0, String var1, int var2) throws Exception {
      Method var3 = findMethod(var0.getClass(), var1, 1);
      if (var3 == null) {
         throw new NoSuchMethodException(var1);
      }

      var3.setAccessible(true);
      return var3.invoke(var0, var2);
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
}
