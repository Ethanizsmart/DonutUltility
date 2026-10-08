package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import com.seastore.auto.seaats.ModConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_465;

public final class SeaSettingsScreen extends class_437 {
   private static final String[] TABS = new String[]{"Auto Sell", "Tracker", "Bảo vệ", "Auto Pay", "Discord"};
   private static final String[] HELP = new String[]{
      "MIN / MAX (tick) · Đủ 4 chu kỳ, xáo trộn mỗi vòng.",
      "Nền trong suốt · Kích thước đổi đồng bộ cả bảng và chữ.",
      "Ghost Fix: tắt bán → đóng GUI → nhấn/nhả Shift 2 lần → bán lại.",
      "Pay All: đọc /bal rồi chuyển đúng số dư. Hoặc chọn MIN–MAX triệu.",
      "Báo cáo và cảnh báo. Để trống webhook nếu không sử dụng."
   };
   private final class_437 parent;
   private final List<List<SeaSettingsScreen.Entry>> groups = new ArrayList<>();
   private final Map<String, String> draft = new LinkedHashMap<>();
   private int tab;
   private int offset;
   private int x;
   private int y;
   private int w;
   private int h;
   private int side;
   private int contentX;
   private int contentW;
   private int rows;
   private int startY;
   private int rowHeight;
   private String error = "";

   public SeaSettingsScreen(class_437 var1) {
      super(class_2561.method_43470("Sea Sell · Cài đặt"));
      this.parent = var1;

      for (int var2 = 0; var2 < TABS.length; var2++) {
         this.groups.add(new ArrayList<>());
      }

      for (int var3 = 1; var3 <= 4; var3++) {
         this.add(0, "Chu kỳ " + var3, "cycle" + var3 + "MinTicks", "cycle" + var3 + "MaxTicks", 1.0, 72000.0, "MIN / MAX · tick");
      }

      this.add(1, "Hiện tracker", "showTracker", null, 0.0, 0.0, "Bật / tắt bảng theo dõi");
      this.add(1, "Kích thước", "trackerScale", null, 0.65, 2.0, "0.65–2.0 · 1.0 = 100%");
      this.add(2, "Chống ghost", "enableGhostFix", null, 0.0, 0.0, "Đồng bộ bằng Shift 2 lần");
      this.add(2, "Chờ mất thu nhập", "ghostTimeoutMinutes", null, 1.0, 10.0, "1–10 phút trước khi xử lý");
      this.add(2, "Chống người lạ", "antiPlayerEnabled", null, 0.0, 0.0, "Bán lại khi người lạ rời đi");
      this.add(2, "Bạn bè", "friendList", null, 0.0, 0.0, "Tên cách nhau bằng dấu phẩy");
      this.add(2, "Tự về home", "enableHome", null, 0.0, 0.0, "Sử dụng vị trí HomeSet đã lưu");
      this.add(2, "Thử lại home", "homeCommandIntervalMin", null, 1.0, 30.0, "1–30 phút");
      this.add(2, "Home chính", "homeCommand1", null, 0.0, 0.0, "Ví dụ: home 1");
      this.add(2, "Home dự phòng", "homeCommand2", null, 0.0, 0.0, "Ví dụ: home 2");
      this.add(3, "Tự chuyển tiền", "enableAutoPay", null, 0.0, 0.0, "Bật / tắt Auto Pay");
      this.add(3, "Chu kỳ chuyển", "payIntervalMinutes", null, 1.0, 1440.0, "1–1440 phút");
      this.add(3, "Người nhận", "payRecipient", null, 0.0, 0.0, "Tên nhân vật trong game");
      this.add(3, "Chế độ Pay All", "payAll", null, 0.0, 0.0, "BẬT: đọc /bal rồi chuyển số dư");
      this.add(3, "Pay ngẫu nhiên", "payRandomMinMillions", "payRandomMaxMillions", 0.0, 999999.0, "MIN / MAX · đơn vị triệu (M)");
      this.add(3, "Webhook chuyển", "payWebhookUrl", null, 0.0, 0.0, "URL thông báo Auto Pay");
      this.add(4, "Webhook thu nhập", "webhookUrlProfit", null, 0.0, 0.0, "URL báo cáo lợi nhuận");
      this.add(4, "Báo cáo mỗi", "profitIntervalMinutes", null, 1.0, 120.0, "1–120 phút");
      this.add(4, "Webhook cảnh báo", "webhookUrlAlert", null, 0.0, 0.0, "URL thông báo sự cố");
      this.add(4, "Cảnh báo sau", "noSellIntervalMinutes", null, 1.0, 60.0, "1–60 phút không bán");
      this.add(4, "Discord ID", "discordId", null, 0.0, 0.0, "ID nhận thông báo");
      this.draft.put("trackerScale", Double.toString(Double.isFinite(Config.trackerScale) ? Math.max(0.65, Math.min(2.0, Config.trackerScale)) : 1.0));
   }

   private void add(int var1, String var2, String var3, String var4, double var5, double var7, String var9) {
      this.groups.get(var1).add(new SeaSettingsScreen.Entry(var2, var3, var4, var5, var7, var9));
      this.read(var3);
      if (var4 != null) {
         this.read(var4);
      }
   }

   private void read(String var1) {
      try {
         Object var2 = Config.class.getField(var1).get(null);
         this.draft.put(var1, var2 instanceof List var3 ? String.join(", ", var3.stream().map(Object::toString).toList()) : Objects.toString(var2, ""));
      } catch (ReflectiveOperationException var4) {
         throw new IllegalStateException(var4);
      }
   }

   protected void method_25426() {
      this.rebuild();
   }

   private void rebuild() {
      this.method_37067();
      this.w = Math.min(640, this.field_22789 - 20);
      this.h = Math.min(358, this.field_22790 - 20);
      this.x = (this.field_22789 - this.w) / 2;
      this.y = (this.field_22790 - this.h) / 2;
      this.side = this.w < 440 ? 88 : 112;
      this.contentX = this.x + this.side + 16;
      this.contentW = this.w - this.side - 30;
      this.startY = this.y + 76;
      this.rowHeight = this.tab == 0 ? 26 : 32;
      this.rows = Math.max(1, (this.h - 114) / this.rowHeight);
      this.offset = Math.max(0, Math.min(this.offset, Math.max(0, this.groups.get(this.tab).size() - this.rows)));

      for (int var1 = 0; var1 < TABS.length; var1++) {
         final int tabIndex = var1;
         this.button((var1 == this.tab ? "§b" : "§7") + TABS[var1], this.x + 8, this.y + 49 + var1 * 25, this.side - 8, 20, () -> {
            this.tab = tabIndex;
            this.offset = 0;
            this.error = "";
            this.rebuild();
         });
      }

      List var8 = this.groups.get(this.tab);

      for (int var2 = this.offset; var2 < Math.min(var8.size(), this.offset + this.rows); var2++) {
         SeaSettingsScreen.Entry var3 = (SeaSettingsScreen.Entry)var8.get(var2);
         int var4 = this.startY + (var2 - this.offset) * this.rowHeight;
         int var5 = Math.max(80, Math.min(156, this.contentW / 2));
         int var6 = this.contentX + this.contentW;
         if (var3.maxKey() != null) {
            int var7 = (var5 - 8) / 2;
            this.field(var3.key(), var3.label() + " tối thiểu", var6 - var5, var4 + 5, var7);
            this.field(var3.maxKey(), var3.label() + " tối đa", var6 - var7, var4 + 5, var7);
         } else if (isBoolean(var3.key())) {
            boolean var12 = Boolean.parseBoolean(this.draft.get(var3.key()));
            this.button(var12 ? "§aBẬT" : "§7TẮT", var6 - 64, var4 + 4, 64, 20, () -> {
               this.draft.put(var3.key(), Boolean.toString(!Boolean.parseBoolean(this.draft.get(var3.key()))));
               this.rebuild();
            });
         } else {
            this.field(var3.key(), var3.label(), var6 - var5, var4 + 5, var5);
         }
      }

      int var9 = this.y + this.h - 28;
      if (var8.size() > this.rows) {
         class_4185 var10 = this.button("‹", this.contentX, var9, 22, 20, () -> {
            this.offset = Math.max(0, this.offset - this.rows);
            this.rebuild();
         });
         var10.field_22763 = this.offset > 0;
         class_4185 var11 = this.button("›", this.contentX + 25, var9, 22, 20, () -> {
            this.offset = Math.min(Math.max(0, var8.size() - this.rows), this.offset + this.rows);
            this.rebuild();
         });
         var11.field_22763 = this.offset + this.rows < var8.size();
      }

      this.button("Hủy", this.x + this.w - 148, var9, 56, 20, this::method_25419);
      this.button("§bLưu & đóng", this.x + this.w - 86, var9, 76, 20, this::save);
   }

   private class_4185 button(String var1, int var2, int var3, int var4, int var5, Runnable var6) {
      return (class_4185)this.method_37063(new SeaButton(var2, var3, var4, var5, var1, var1x -> var6.run()));
   }

   private void field(String var1, String var2, int var3, int var4, int var5) {
      class_342 var6 = new class_342(this.field_22793, var3, var4, var5, 18, class_2561.method_43470(var2));
      var6.method_1880(var1.toLowerCase(Locale.ROOT).contains("webhook") ? 2048 : 512);
      var6.method_1852(this.draft.get(var1));
      var6.method_1863(var2x -> {
         this.draft.put(var1, var2x);
         this.error = "";
      });
      this.method_37063(var6);
   }

   private static boolean isBoolean(String var0) {
      try {
         return Config.class.getField(var0).getType() == boolean.class;
      } catch (ReflectiveOperationException var2) {
         throw new IllegalStateException(var2);
      }
   }

   private Object parse(SeaSettingsScreen.Entry var1, String var2) throws ReflectiveOperationException {
      Class var3 = Config.class.getField(var2).getType();
      String var4 = this.draft.get(var2).trim();
      if (var3 == boolean.class) {
         return Boolean.parseBoolean(var4);
      } else if (var3 == String.class) {
         return var4;
      } else if (var3 == List.class) {
         return new ArrayList<>(Arrays.stream(var4.split(",")).map(String::trim).filter(var0 -> !var0.isEmpty()).distinct().toList());
      } else {
         try {
            double var5 = Double.parseDouble(var4.replace(',', '.'));
            if (Double.isFinite(var5) && !(var5 < var1.min()) && !(var5 > var1.max()) && (var3 != int.class || var5 == Math.rint(var5))) {
               if (var3 == int.class) {
                  return Integer.valueOf((int)var5);
               }
               return Double.valueOf(var5);
            } else {
               throw new NumberFormatException();
            }
         } catch (NumberFormatException var7) {
            throw new IllegalArgumentException(var1.label() + ": nhập " + var1.min() + "–" + var1.max() + (var3 == int.class ? " (số nguyên)" : ""));
         }
      }
   }

   private void save() {
      LinkedHashMap<Field, Object> var1 = new LinkedHashMap<>();
      LinkedHashMap<Field, Object> var2 = new LinkedHashMap<>();

      try {
         for (int var3 = 0; var3 < this.groups.size(); var3++) {
            for (SeaSettingsScreen.Entry var15 : this.groups.get(var3)) {
               try {
                  Object var6 = this.parse(var15, var15.key());
                  var1.put(Config.class.getField(var15.key()), var6);
                  if (var15.maxKey() != null) {
                     Object var7 = this.parse(var15, var15.maxKey());
                     if (((Number)var6).doubleValue() > ((Number)var7).doubleValue()) {
                        throw new IllegalArgumentException(var15.label() + ": Min phải nhỏ hơn hoặc bằng Max.");
                     }

                     var1.put(Config.class.getField(var15.maxKey()), var7);
                  }
               } catch (IllegalArgumentException var9) {
                  this.tab = var3;
                  this.offset = this.groups.get(var3).indexOf(var15);
                  this.rebuild();
                  throw var9;
               }
            }
         }

         for (Map.Entry var14 : var1.entrySet()) {
            var2.put((Field)var14.getKey(), ((Field)var14.getKey()).get(null));
            ((Field)var14.getKey()).set(null, var14.getValue());
         }

         ModConfig.save();
         boolean var12 = var1.keySet()
            .stream()
            .filter(var0 -> var0.getName().startsWith("cycle"))
            .anyMatch(var2x -> !Objects.equals(var2.get(var2x), var1.get(var2x)));
         if (var12) {
            SellCycleScheduler.reset();
         }

         this.method_25419();
      } catch (Exception var10) {
         for (Map.Entry var5 : var2.entrySet()) {
            try {
               ((Field)var5.getKey()).set(null, var5.getValue());
            } catch (IllegalAccessException var8) {
            }
         }

         this.error = var10.getMessage() == null ? "Không lưu được cài đặt." : var10.getMessage();
      }
   }

   public void method_25419() {
      class_437 var1 = this.parent;
      if (var1 instanceof class_465 var2 && (this.field_22787.field_1724 == null || var2.method_17577() != this.field_22787.field_1724.field_7512)) {
         var1 = null;
      }

      this.field_22787.method_1507(var1);
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25401(double var1, double var3, double var5, double var7) {
      if (var1 >= this.contentX && var1 < this.contentX + this.contentW && var3 >= this.startY && var3 < this.y + this.h - 35 && var7 != 0.0) {
         this.offset += var7 < 0.0 ? 1 : -1;
         this.rebuild();
         return true;
      } else {
         return super.method_25401(var1, var3, var5, var7);
      }
   }

   public void method_25394(class_332 var1, int var2, int var3, float var4) {
      var1.method_25294(0, 0, this.field_22789, this.field_22790, -1207431656);
      var1.method_25294(this.x + 3, this.y + 4, this.x + this.w + 3, this.y + this.h + 4, 1610612736);
      var1.method_25294(this.x, this.y, this.x + this.w, this.y + this.h, -15721685);
      var1.method_25294(this.x, this.y, this.x + this.w, this.y + 2, -12396086);
      var1.method_25294(this.x, this.y + 2, this.x + this.side + 5, this.y + this.h, -15457738);
      this.text(var1, "§lSEA SELL", this.x + 13, this.y + 15, -1379076);
      this.text(var1, "F9 · CÀI ĐẶT", this.x + 13, this.y + 29, -8415824);
      this.text(var1, "§l" + TABS[this.tab], this.contentX, this.y + 15, -1379076);
      this.text(
         var1,
         this.fit(this.tab == 0 ? "1 tick ≈ 0,05 giây · 20 tick ≈ 1 giây" : "Tùy chỉnh và lưu khi hoàn tất", this.contentW),
         this.contentX,
         this.y + 30,
         -7298118
      );
      var1.method_25294(this.contentX, this.y + 48, this.contentX + this.contentW, this.y + 71, -15060925);
      String var5 = this.error.isEmpty() ? HELP[this.tab] : this.error;
      this.text(var1, this.fit(var5, this.contentW - 12), this.contentX + 6, this.y + 56, this.error.isEmpty() ? -5973784 : -25187);
      List var6 = this.groups.get(this.tab);

      for (int var7 = this.offset; var7 < Math.min(var6.size(), this.offset + this.rows); var7++) {
         SeaSettingsScreen.Entry var8 = (SeaSettingsScreen.Entry)var6.get(var7);
         int var9 = this.startY + (var7 - this.offset) * this.rowHeight;
         var1.method_25294(this.contentX - 3, var9, this.contentX + this.contentW + 3, var9 + this.rowHeight - 2, -15260104);
         int var10 = Math.max(80, Math.min(156, this.contentW / 2));
         int var11 = this.contentW - var10 - 12;
         this.text(var1, this.fit(var8.label(), var11), this.contentX + 4, var9 + (this.tab == 0 ? 9 : 5), -2102282);
         if (this.tab != 0) {
            this.text(var1, this.fit(var8.hint(), var11), this.contentX + 4, var9 + 18, -8217933);
         }
      }

      var1.method_25294(this.contentX - 3, this.y + this.h - 35, this.contentX + this.contentW + 3, this.y + this.h - 34, -14073770);
      if (this.contentW > 295) {
         this.text(
            var1,
            this.offset + 1 + "–" + Math.min(var6.size(), this.offset + this.rows) + " / " + var6.size(),
            this.contentX + 54,
            this.y + this.h - 22,
            -7298118
         );
      }

      super.method_25394(var1, var2, var3, var4);
   }

   private String fit(String var1, int var2) {
      return this.field_22793.method_27523(var1, Math.max(10, var2));
   }

   private void text(class_332 var1, String var2, int var3, int var4, int var5) {
      var1.method_51433(this.field_22793, var2, var3, var4, var5, false);
   }

   private record Entry(String label, String key, String maxKey, double min, double max, String hint) {
   }
}
