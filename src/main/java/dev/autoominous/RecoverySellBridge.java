package dev.autoominous;

import com.seastore.auto.seaats.Config;
import com.seastore.auto.seaats.client.AutoSellLogic;
import java.lang.reflect.Method;
import net.minecraft.class_310;

/** Runs the existing CoreV2 seller for recovery without changing its algorithm or user toggle. */
public final class RecoverySellBridge {
   private RecoverySellBridge() {}

   public static void onTick() {
      if (!DeathRecoveryRuntime.isPostDeathSellPending()) return;
      tickCurrentSeller();
   }

   public static void onSwordFreeSpaceTick() {
      if (!SwordRecoveryRuntime.isRecoveryActive() || !SwordRecoveryRuntime.allowAutoSellDuringRecovery()) return;
      tickCurrentSeller();
   }

   private static void tickCurrentSeller() {
      class_310 client = class_310.method_1551();
      if (client == null || client.field_1724 == null || client.field_1687 == null) return;
      boolean previousSeaEnabled = Config.isAutoSellEnabled;
      try {
         Method sync = SlashSellRuntime.class.getDeclaredMethod("syncSeaConfig");
         sync.setAccessible(true);
         sync.invoke(null);
         Config.isAutoSellEnabled = true;
         AutoSellLogic.tick(client);
      } catch (ReflectiveOperationException error) {
         System.err.println("[CoreV2] Recovery sell bridge: " + error.getClass().getSimpleName());
      } finally {
         Config.isAutoSellEnabled = SlashSellSettings.isEnabled() && previousSeaEnabled;
      }
   }

   public static boolean isComplete(class_310 client) {
      Object state = ReflectionSupport.staticField("com.seastore.auto.seaats.client.AutoSellLogic", "currentState");
      return client != null && client.field_1724 != null
         && "IDLE".equals(String.valueOf(state))
         && client.field_1755 == null
         && client.field_1724.field_7512 == client.field_1724.field_7498
         && !AutoSellLogic.hasInventoryItems(client);
   }
}
