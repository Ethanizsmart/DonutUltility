package dev.autoominous;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

final class BountyMinecraftBridge {
   private final BountyMappingBridge map = new BountyMappingBridge();
   private final Class<?> mcClass = this.load("net.minecraft.class_310");
   private final Class<?> networkClass = this.load("net.minecraft.class_634");
   private final Class<?> screenClass = this.load("net.minecraft.class_437");
   private final Class<?> handledScreenClass = this.load("net.minecraft.class_465");
   private final Class<?> screenHandlerProviderClass = this.load("net.minecraft.class_3936");
   private final Class<?> screenHandlerClass = this.load("net.minecraft.class_1703");
   private final Class<?> slotClass = this.load("net.minecraft.class_1735");
   private final Class<?> itemStackClass = this.load("net.minecraft.class_1799");
   private final Class<?> textClass = this.load("net.minecraft.class_2561");
   private final Class<?> interactionManagerClass = this.load("net.minecraft.class_636");
   private final Class<?> slotActionTypeClass = this.load("net.minecraft.class_1713");
   private final Class<?> playerEntityClass = this.load("net.minecraft.class_1657");
   private final Method mcGetInstance = this.mcClass.getMethod(this.m("net.minecraft.class_310", "method_1551", "()Lnet/minecraft/class_310;"));
   private final Method mcGetNetworkHandler = this.mcClass.getMethod(this.m("net.minecraft.class_310", "method_1562", "()Lnet/minecraft/class_634;"));
   private final Method networkSendCommand = this.networkClass
      .getMethod(this.m("net.minecraft.class_634", "method_45730", "(Ljava/lang/String;)V"), String.class);
   private final Field mcCurrentScreen = getField(this.mcClass, this.f("net.minecraft.class_310", "field_1755", "Lnet/minecraft/class_437;"));
   private final Field mcInteractionManager = getField(this.mcClass, this.f("net.minecraft.class_310", "field_1761", "Lnet/minecraft/class_636;"));
   private final Field mcPlayer = getField(this.mcClass, this.f("net.minecraft.class_310", "field_1724", "Lnet/minecraft/class_746;"));
   private final Method screenGetTitle = this.screenClass.getMethod(this.m("net.minecraft.class_437", "method_25440", "()Lnet/minecraft/class_2561;"));
   private final Method screenGetTooltipFromItem = this.screenClass
      .getMethod(
         this.m("net.minecraft.class_437", "method_25408", "(Lnet/minecraft/class_310;Lnet/minecraft/class_1799;)Ljava/util/List;"),
         this.mcClass,
         this.itemStackClass
      );
   private final Method screenClose = this.screenClass.getMethod(this.m("net.minecraft.class_437", "method_25419", "()V"));
   private final Method textAsString = this.textClass.getMethod(this.m("net.minecraft.class_2561", "method_10858", "(I)Ljava/lang/String;"), int.class);
   private final Method providerGetHandler = this.screenHandlerProviderClass
      .getMethod(this.m("net.minecraft.class_3936", "method_17577", "()Lnet/minecraft/class_1703;"));
   private final Field handlerSlots = getField(this.screenHandlerClass, this.f("net.minecraft.class_1703", "field_7761", "Lnet/minecraft/class_2371;"));
   private final Field handlerSyncId = getField(this.screenHandlerClass, this.f("net.minecraft.class_1703", "field_7763", "I"));
   private final Field slotId;
   private final Method slotHasStack = this.slotClass.getMethod(this.m("net.minecraft.class_1735", "method_7681", "()Z"));
   private final Method slotGetStack = this.slotClass.getMethod(this.m("net.minecraft.class_1735", "method_7677", "()Lnet/minecraft/class_1799;"));
   private final Method stackGetName;
   private final Method clickSlot;
   private final Field pickupAction;

   BountyMinecraftBridge() throws Exception {
      this.slotId = getField(this.slotClass, this.f("net.minecraft.class_1735", "field_7874", "I"));
      this.stackGetName = this.itemStackClass.getMethod(this.m("net.minecraft.class_1799", "method_7964", "()Lnet/minecraft/class_2561;"));
      this.pickupAction = getField(this.slotActionTypeClass, this.f("net.minecraft.class_1713", "field_7790", "Lnet/minecraft/class_1713;"));
      this.clickSlot = this.interactionManagerClass
         .getMethod(
            this.m("net.minecraft.class_636", "method_2906", "(IIILnet/minecraft/class_1713;Lnet/minecraft/class_1657;)V"),
            int.class,
            int.class,
            int.class,
            this.slotActionTypeClass,
            this.playerEntityClass
         );
   }

   Object getClient() throws Exception {
      return this.mcGetInstance.invoke(null);
   }

   boolean isConnected(Object var1) {
      try {
         return var1 != null && this.mcGetNetworkHandler.invoke(var1) != null && this.mcPlayer.get(var1) != null;
      } catch (Throwable var3) {
         return false;
      }
   }

   void sendCommand(Object var1, String var2) throws Exception {
      Object var3 = this.mcGetNetworkHandler.invoke(var1);
      if (var3 == null) {
         throw new IllegalStateException("No server connection");
      }

      this.networkSendCommand.invoke(var3, var2);
   }

   String textToString(Object var1) {
      if (var1 == null) {
         return "";
      }

      try {
         return (String)this.textAsString.invoke(var1, 32767);
      } catch (Throwable var3) {
         return String.valueOf(var1);
      }
   }

   BountyMinecraftBridge.ConfirmCandidate findConfirm(Object var1) {
      try {
         Object var2 = this.mcCurrentScreen.get(var1);
         if (var2 == null || !this.handledScreenClass.isInstance(var2) || !this.screenHandlerProviderClass.isInstance(var2)) {
            return null;
         }

         String var3 = this.textToString(this.screenGetTitle.invoke(var2)).trim();
         if (!var3.equalsIgnoreCase("Confirm Bounty")) {
            return null;
         }

         Object var4 = this.providerGetHandler.invoke(var2);
         if (!(this.handlerSlots.get(var4) instanceof Iterable var6)) {
            return null;
         }

         for (Object var8 : var6) {
            if (var8 != null && (Boolean)this.slotHasStack.invoke(var8)) {
               Object var9 = this.slotGetStack.invoke(var8);
               String var10 = this.textToString(this.stackGetName.invoke(var9)).trim();
               if (var10.equalsIgnoreCase("Confirm")) {
                  boolean var11 = false;

                  try {
                     List var12 = (List)this.screenGetTooltipFromItem.invoke(null, var1, var9);
                     if (var12 != null) {
                        for (Object var14 : var12) {
                           if (this.textToString(var14).toLowerCase(Locale.ROOT).contains("click to add bounty")) {
                              var11 = true;
                              break;
                           }
                        }
                     }
                  } catch (Throwable var15) {
                     var11 = true;
                  }

                  if (var11) {
                     return new BountyMinecraftBridge.ConfirmCandidate(
                        ((Number)this.slotId.get(var8)).intValue(), ((Number)this.handlerSyncId.get(var4)).intValue()
                     );
                  }
               }
            }
         }
      } catch (Throwable var16) {
         System.err.println("[DonutUtility/Bounty] Confirm scan failed: " + var16);
      }

      return null;
   }

   boolean clickConfirm(Object var1, BountyMinecraftBridge.ConfirmCandidate var2) {
      try {
         BountyMinecraftBridge.ConfirmCandidate var3 = this.findConfirm(var1);
         if (var3 != null && var2 != null && var3.slotId == var2.slotId && var3.syncId == var2.syncId) {
            Object var4 = this.mcInteractionManager.get(var1);
            Object var5 = this.mcPlayer.get(var1);
            Object var6 = this.pickupAction.get(null);
            if (var4 != null && var5 != null && var6 != null) {
               this.clickSlot.invoke(var4, var3.syncId, var3.slotId, 0, var6, var5);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } catch (Throwable var7) {
         System.err.println("[DonutUtility/Bounty] Confirm click failed: " + var7);
         return false;
      }
   }

   void closeConfirmIfOpen(Object var1) {
      try {
         Object var2 = this.mcCurrentScreen.get(var1);
         if (var2 == null || !this.handledScreenClass.isInstance(var2)) {
            return;
         }

         String var3 = this.textToString(this.screenGetTitle.invoke(var2)).trim();
         if (var3.equalsIgnoreCase("Confirm Bounty")) {
            this.screenClose.invoke(var2);
         }
      } catch (Throwable var4) {
      }
   }

   boolean closeAnyHandledScreen(Object var1) {
      try {
         Object var2 = this.mcCurrentScreen.get(var1);
         if (var2 != null && this.handledScreenClass.isInstance(var2)) {
            this.screenClose.invoke(var2);
            return true;
         } else {
            return false;
         }
      } catch (Throwable var3) {
         System.err.println("[DonutUtility/Bounty] Failed to close handled screen: " + var3);
         return false;
      }
   }

   private Class<?> load(String var1) throws Exception {
      return Class.forName(this.map.cls(var1));
   }

   private String m(String var1, String var2, String var3) throws Exception {
      return this.map.method(var1, var2, var3);
   }

   private String f(String var1, String var2, String var3) throws Exception {
      return this.map.field(var1, var2, var3);
   }

   private static Field getField(Class<?> var0, String var1) throws Exception {
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

   static final class ConfirmCandidate {
      final int slotId;
      final int syncId;

      ConfirmCandidate(int var1, int var2) {
         this.slotId = var1;
         this.syncId = var2;
      }
   }
}
