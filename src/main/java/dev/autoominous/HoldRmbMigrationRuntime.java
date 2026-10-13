package dev.autoominous;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class HoldRmbMigrationRuntime {
   private static boolean done;

   private HoldRmbMigrationRuntime() {
   }

   public static void ensure() {
      if (!done) {
         done = true;

         try {
            if (ReflectionSupport.callStaticBoolean("dev.autoominous.SlashSellSettings", "isAutoUseProtectedEnabled", false)) {
               return;
            }

            Class var0 = Class.forName("fi.dy.masa.malilib.util.FileUtils");
            Object var1 = var0.getMethod("getConfigDirectoryAsPath").invoke(null);
            if (!(var1 instanceof Path)) {
               return;
            }

            Path var2 = ((Path)var1).resolve("autoominous-slash-sell.json");
            if (!Files.isReadable(var2)) {
               return;
            }

            String var3 = Files.readString(var2);
            Pattern var4 = Pattern.compile("\\\"Auto Use Protected Items\\\"\\s*:\\s*(?:\\{[^}]{0,300}?\\\"value\\\"\\s*:\\s*)?true", 34);
            if (!var4.matcher(var3).find()) {
               return;
            }

            ReflectionSupport.callStatic("dev.autoominous.SlashSellSettings", "setAutoUseProtectedEnabled", new Class[]{boolean.class}, true);
            ReflectionSupport.callStaticVoid("dev.autoominous.SlashSellSettings", "saveNow");
            System.out.println("[Auto Ominous] FIX13: migrated legacy 'Auto Use Protected Items=ON' to 'Hold RMB=ON'.");
         } catch (Throwable var5) {
         }
      }
   }
}
