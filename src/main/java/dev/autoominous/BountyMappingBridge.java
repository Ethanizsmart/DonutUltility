package dev.autoominous;

import java.lang.reflect.Method;

final class BountyMappingBridge {
   private final Object resolver;
   private final Method mapClassName;
   private final Method mapMethodName;
   private final Method mapFieldName;

   BountyMappingBridge() throws Exception {
      Class var1 = Class.forName("net.fabricmc.loader.api.FabricLoader");
      Object var2 = var1.getMethod("getInstance").invoke(null);
      this.resolver = var1.getMethod("getMappingResolver").invoke(var2);
      Class var3 = this.resolver.getClass();
      this.mapClassName = var3.getMethod("mapClassName", String.class, String.class);
      this.mapMethodName = var3.getMethod("mapMethodName", String.class, String.class, String.class, String.class);
      this.mapFieldName = var3.getMethod("mapFieldName", String.class, String.class, String.class, String.class);
   }

   String cls(String var1) throws Exception {
      return (String)this.mapClassName.invoke(this.resolver, "intermediary", var1);
   }

   String method(String var1, String var2, String var3) throws Exception {
      return (String)this.mapMethodName.invoke(this.resolver, "intermediary", var1, var2, var3);
   }

   String field(String var1, String var2, String var3) throws Exception {
      return (String)this.mapFieldName.invoke(this.resolver, "intermediary", var1, var2, var3);
   }
}
