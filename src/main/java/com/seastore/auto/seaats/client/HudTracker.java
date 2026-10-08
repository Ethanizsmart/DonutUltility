package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public class HudTracker {
   public static void register() {
      HudRenderCallback.EVENT.register((HudRenderCallback)(var0, var1) -> render(var0));
   }

   public static void render(class_332 var0) {
      class_310 var1 = class_310.method_1551();
      if (Config.showTracker
         && var1.field_1724 != null
         && var1.field_1687 != null
         && !(var1.field_1755 instanceof SeaSettingsScreen)
         && !var1.field_1690.field_1842
         && !var1.method_53526().method_53536()) {
         renderCard(var0, var1.field_1772, 8, 8);
      }
   }

   static void renderCard(class_332 var0, class_327 var1, int var2, int var3) {
      float var4 = Double.isFinite(Config.trackerScale) ? (float)Math.max(0.65, Math.min(2.0, Config.trackerScale)) : 1.0F;
      var4 = Math.min(var4, Math.min((var0.method_51421() - 16.0F) / 180.0F, (var0.method_51443() - 16.0F) / 100.0F));
      if (!(var4 <= 0.0F)) {
         Matrix3x2fStack var5 = var0.method_51448();
         var5.pushMatrix();
         var5.translate(var2, var3);
         var5.scale(var4, var4);

         try {
            var0.method_25294(0, 0, 180, 1, -431826486);
            var0.method_25294(0, 99, 180, 100, -431826486);
            var0.method_25294(0, 1, 1, 99, -431826486);
            var0.method_25294(179, 1, 180, 99, -431826486);
            text(var0, var1, "§lSEA SELL", 10, 8, -1379076);
            String var6 = AutoSellLogic.trackerStatus();
            int var7 = Config.isAutoSellEnabled ? -10362188 : -4931376;
            if (!var6.equals("ĐANG BÁN") && !var6.equals("ĐÃ TẮT")) {
               var7 = -10879;
            }

            text(var0, var1, var6, 170 - var1.method_1727(var6), 8, var7);
            long var8 = Config.startTimeMs == 0L ? 0L : Math.max(0L, (System.currentTimeMillis() - Config.startTimeMs) / 1000L);
            int var10 = Math.max(12, 13);
            row(var0, var1, "Thu nhập", money(Config.totalMoney), 180, 29, -10362188);
            row(var0, var1, "Tốc độ", money(var8 > 0L ? Config.totalMoney * 3600.0 / var8 : 0.0) + "/h", 180, 29 + var10, -7681793);
            row(var0, var1, "Đã bán", String.format(Locale.ROOT, "%,d lượt", Config.totalSellCount), 180, 29 + var10 * 2, -1379076);
            row(
               var0, var1, "Thời gian", String.format(Locale.ROOT, "%02d:%02d:%02d", var8 / 3600L, var8 / 60L % 60L, var8 % 60L), 180, 29 + var10 * 3, -1379076
            );
            String var11 = SellCycleScheduler.currentCycle() == 0
               ? "Chờ lượt bán"
               : "C" + SellCycleScheduler.currentCycle() + " · " + SellCycleScheduler.selectedDelay() + "t · " + SellCycleScheduler.secondsRemaining() + "s";
            row(var0, var1, "Chu kỳ", var11, 180, 29 + var10 * 4, -10879);
         } finally {
            var5.popMatrix();
         }
      }
   }

   private static void row(class_332 var0, class_327 var1, String var2, String var3, int var4, int var5, int var6) {
      text(var0, var1, var2, 10, var5, -7298118);
      int var7 = var4 - 20 - var1.method_1727(var2) - 10;
      var3 = var1.method_27523(var3, Math.max(20, var7));
      text(var0, var1, var3, var4 - 10 - var1.method_1727(var3), var5, var6);
   }

   private static void text(class_332 var0, class_327 var1, String var2, int var3, int var4, int var5) {
      var0.method_51433(var1, var2, var3, var4, var5, true);
   }

   private static String money(double var0) {
      if (var0 >= 1.0E9) {
         return String.format(Locale.ROOT, "$%.2fB", var0 / 1.0E9);
      } else if (var0 >= 1000000.0) {
         return String.format(Locale.ROOT, "$%.2fM", var0 / 1000000.0);
      } else {
         return var0 >= 1000.0 ? String.format(Locale.ROOT, "$%.2fK", var0 / 1000.0) : String.format(Locale.ROOT, "$%.0f", var0);
      }
   }
}
