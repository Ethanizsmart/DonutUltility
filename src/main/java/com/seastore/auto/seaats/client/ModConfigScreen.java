package com.seastore.auto.seaats.client;

import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;

public class ModConfigScreen {
   public static class_437 createScreen(class_437 var0) {
      class_310 var1 = class_310.method_1551();
      if (var0 instanceof class_465 || AutoSellLogic.hasActiveMenu(var1)) {
         AutoSellLogic.closeAllGui(var1);
         var0 = null;
      }

      return new SeaSettingsScreen(var0);
   }
}
