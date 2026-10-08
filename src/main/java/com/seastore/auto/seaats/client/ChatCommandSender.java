package com.seastore.auto.seaats.client;

import net.minecraft.class_408;

final class ChatCommandSender {
   private ChatCommandSender() {
   }

   static void send(String var0) {
      new class_408("", false).method_44056("/" + var0, true);
   }
}
