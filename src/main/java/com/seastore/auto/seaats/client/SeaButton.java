package com.seastore.auto.seaats.client;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_4185.class_4241;

final class SeaButton extends class_4185 {
   private final int bx;
   private final int by;
   private final int bw;
   private final int bh;
   private final String caption;
   private final boolean accent;
   private final boolean enabledToggle;

   SeaButton(int var1, int var2, int var3, int var4, String var5, class_4241 var6) {
      super(var1, var2, var3, var4, class_2561.method_43470(var5), var6, class_4185.field_40754);
      this.bx = var1;
      this.by = var2;
      this.bw = var3;
      this.bh = var4;
      this.caption = var5.replaceAll("§.", "");
      this.accent = var5.startsWith("§b");
      this.enabledToggle = var5.startsWith("§a");
   }

   protected void method_75752(class_332 var1, int var2, int var3, float var4) {
      boolean var5 = this.field_22763 && (var2 >= this.bx && var2 < this.bx + this.bw && var3 >= this.by && var3 < this.by + this.bh || this.method_25370());
      int var6 = !this.field_22763 ? -15194568 : (var5 ? -14005920 : (this.accent ? -14924211 : -14535093));
      var1.method_25294(this.bx, this.by, this.bx + this.bw, this.by + this.bh, var6);
      if (this.accent || var5) {
         var1.method_25294(this.bx, this.by, this.bx + 2, this.by + this.bh, -12396086);
      }

      int var7 = !this.field_22763 ? -10785407 : (this.enabledToggle ? -10362188 : (this.accent ? -8918561 : -2299402));
      class_327 var8 = class_310.method_1551().field_1772;
      String var9 = var8.method_27523(this.caption, this.bw - 8);
      var1.method_51433(var8, var9, this.bx + (this.bw - var8.method_1727(var9)) / 2, this.by + (this.bh - 8) / 2, var7, false);
   }
}
