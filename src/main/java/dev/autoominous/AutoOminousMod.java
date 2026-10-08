package dev.autoominous;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;

public final class AutoOminousMod implements ModInitializer {
   public void onInitialize() {
      InitializationHandler.getInstance().registerInitializationHandler(new AutoOminousInitHandler());
   }
}
