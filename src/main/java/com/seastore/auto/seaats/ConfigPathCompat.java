package com.seastore.auto.seaats;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigPathCompat {
   private ConfigPathCompat() {
   }

   public static Path getConfigDir() {
      return Paths.get(System.getProperty("user.dir"), "config");
   }

   public static Path getConfigDirCompat() {
      return getConfigDir();
   }
}
