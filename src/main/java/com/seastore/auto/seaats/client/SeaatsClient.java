package com.seastore.auto.seaats.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.seastore.auto.seaats.Config;
import com.seastore.auto.seaats.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_746;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;

@Environment(EnvType.CLIENT)
public class SeaatsClient implements ClientModInitializer {
   private static class_304 toggleKey;
   private static class_304 settingsKey;
   private static final class_11900 SEAATS_CATEGORY = class_11900.method_74698(class_2960.method_60655("seaats", "category"));

   public void onInitializeClient() {
      ModConfig.load();
      System.out.println("[SeaATS] V1.08.4 SellGuiRecovery loaded (Fabric 1.21.11)");
      toggleKey = KeyBindingHelper.registerKeyBinding(new class_304("key.seaats.toggle", class_307.field_1668, 297, SEAATS_CATEGORY));
      settingsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.seaats.settings", class_307.field_1668, 298, SEAATS_CATEGORY));
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(var0, var1) -> var0.register(
               (LiteralArgumentBuilder)ClientCommandManager.literal("homeset")
                  .executes(
                     var0x -> {
                        class_746 var1x = ((FabricClientCommandSource)var0x.getSource()).getPlayer();
                        Config.homeX = var1x.method_23317();
                        Config.homeY = var1x.method_23318();
                        Config.homeZ = var1x.method_23321();
                        Config.isHomeSet = true;
                        ModConfig.save();
                        ((FabricClientCommandSource)var0x.getSource())
                           .sendFeedback(
                              class_2561.method_43470(
                                 "§a[SeaATS] Đã lưu toạ độ HomeSet tại: " + String.format("%.2f, %.2f, %.2f", Config.homeX, Config.homeY, Config.homeZ)
                              )
                           );
                        return 1;
                     }
                  )
            )
         );
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         while (toggleKey.method_1436()) {
            AutoSellLogic.toggleFromKey(var0);
         }

         while (settingsKey.method_1436()) {
            var0.execute(() -> {
               try {
                  var0.method_1507(ModConfigScreen.createScreen(var0.field_1755));
               } catch (Exception var2) {
                  var2.printStackTrace();
                  if (var0.field_1724 != null) {
                     var0.field_1724.method_7353(class_2561.method_43470("§cLỗi mở GUI cài đặt: " + var2.getMessage()), false);
                  }
               }
            });
         }

         AutoSellLogic.tick(var0);
      });
      ChatMoneyListener.register();
      HudTracker.register();
   }
}
