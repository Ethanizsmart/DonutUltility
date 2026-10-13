import java.nio.file.*;
import java.net.*;
import java.util.*;
import javax.tools.ToolProvider;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Compiles inert substitutes; tests only the actual DeathRecovery/bridge bytecode. */
public class RuntimePortChecks {
   public static void main(String[] args) throws Exception {
      Path main = Path.of(args[0]), work = Path.of(args[1]);
      Path src = work.resolve("fixtures-src"), bin = work.resolve("fixtures-bin");
      Files.createDirectories(bin);
      Map<String, String> files = new LinkedHashMap<>();
      files.put("dev.autoominous.Clock", "public class Clock { public static long now; }");
      files.put("dev.autoominous.DeathRecoverySettings", """
         public class DeathRecoverySettings {
            public static boolean enabled=true;
            public static void maybePersist() {}
            public static boolean isEnabled(){return enabled;}
            public static int getRespawnDelayMs(){return 100;}
            public static int getHomeDelayMs(){return 200;}
            public static String getHome1CommandForSend(){return "/home 1";}
         }
         """);
      files.put("dev.autoominous.SwordRecoveryRuntime", "public class SwordRecoveryRuntime { public static int resets; public static void forceReset(){resets++;} public static boolean isRecoveryActive(){return false;} public static boolean allowAutoSellDuringRecovery(){return false;} }");
      files.put("dev.autoominous.HomePositionGuardRuntime", "public class HomePositionGuardRuntime { public static void cancelForDeathRecovery(){} }");
      files.put("dev.autoominous.G232SafeRuntime", "public class G232SafeRuntime { public static void resetForDeathRecovery(){} }");
      files.put("dev.autoominous.VexSafetyRuntime", "public class VexSafetyRuntime { public static boolean filterDeathForRecovery(boolean dead){return dead;} }");
      files.put("dev.autoominous.ProtectedItemAutoUseRuntime", "public class ProtectedItemAutoUseRuntime { public static void forceRelease(){} }");
      files.put("dev.autoominous.SlashSellSettings", "public class SlashSellSettings { public static boolean enabled; public static boolean isEnabled(){return enabled;} }");
      files.put("dev.autoominous.SlashSellRuntime", """
         public class SlashSellRuntime {
            public static int resets,syncs;
            public static void resetRuntime(){resets++;com.seastore.auto.seaats.client.AutoSellLogic.currentState="IDLE";}
            private static void syncSeaConfig(){syncs++;}
         }
         """);
      files.put("dev.autoominous.ReflectionSupport", """
         public class ReflectionSupport {
            public static Object staticField(String cls,String field){try{return Class.forName(cls).getField(field).get(null);}catch(Exception e){return null;}}
         }
         """);
      files.put("com.seastore.auto.seaats.Config", "public class Config { public static boolean isAutoSellEnabled; }");
      files.put("com.seastore.auto.seaats.client.AutoSellLogic", """
         public class AutoSellLogic {
            public static String currentState="IDLE";
            public static boolean items=true,throwOnTick,sawEnabled;
            public static int ticks;
            public static void tick(net.minecraft.class_310 c){ticks++;sawEnabled=com.seastore.auto.seaats.Config.isAutoSellEnabled;if(throwOnTick)throw new IllegalStateException("fixture tick");}
            public static boolean hasInventoryItems(net.minecraft.class_310 c){return items;}
         }
         """);
      files.put("net.minecraft.class_437", "public class class_437 {}");
      files.put("net.minecraft.class_418", "public class class_418 extends class_437 {}");
      files.put("net.minecraft.class_465", "public class class_465 extends class_437 {}");
      files.put("net.minecraft.class_638", "public class class_638 {}");
      files.put("net.minecraft.class_1703", "public class class_1703 {}");
      files.put("net.minecraft.class_1723", "public class class_1723 extends class_1703 {}");
      files.put("net.minecraft.class_634", "public class class_634 { public String sent=" + "\"\"" + "; public int calls; public void method_45730(String cmd){sent=cmd;calls++;} }");
      files.put("net.minecraft.class_746", """
         public class class_746 {
            public boolean dead;public int respawns;
            public class_1723 field_7498=new class_1723();
            public class_1703 field_7512=field_7498;
            public boolean method_29504(){return dead;}
            public boolean method_5805(){return !dead;}
            public void method_7331(){respawns++;dead=false;}
            public void method_7346(){field_7512=field_7498;}
         }
         """);
      files.put("net.minecraft.class_310", """
         public class class_310 {
            public static class_310 instance=new class_310();
            public class_746 field_1724=new class_746();public class_638 field_1687=new class_638();
            public class_437 field_1755;public class_634 connection=new class_634();
            public static class_310 method_1551(){return instance;}
            public class_634 method_1562(){return connection;}
            public void method_1507(class_437 screen){field_1755=screen;}
         }
         """);
      files.put("dev.autoominous.FlowChecks", """
         import net.minecraft.*;
         import com.seastore.auto.seaats.Config;
         import com.seastore.auto.seaats.client.AutoSellLogic;
         public class FlowChecks {
            static int checks;
            static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
            static class_310 client(){return class_310.instance;}
            static void at(long time){Clock.now=time;DeathRecoveryRuntime.onTick();}
            static void setup(){DeathRecoveryRuntime.resetForVexDeathWait();class_310.instance=new class_310();DeathRecoverySettings.enabled=true;SlashSellSettings.enabled=false;Config.isAutoSellEnabled=false;AutoSellLogic.items=true;AutoSellLogic.throwOnTick=false;AutoSellLogic.currentState="IDLE";}
            static void reachSale(long base){client().field_1724.dead=true;at(base);at(base+100);at(base+101);at(base+301);at(base+8301);}
            public static void main(String[] args){
               setup();at(1000);check(!DeathRecoveryRuntime.isRecoveryPending(),"alive idle");
               client().field_1724.dead=true;at(2000);check(DeathRecoveryRuntime.isRecoveryPending(),"death arms");check(DeathRecoveryRuntime.isBlockingAutoSell(),"death blocks sell");check(client().field_1724.respawns==0,"respawn delay");
               at(2099);check(client().field_1724.respawns==0,"not early respawn");at(2100);check(client().field_1724.respawns==1,"respawn at configured delay");
               at(2101);at(2300);check(client().connection.calls==0,"home delay");at(2301);check(client().connection.calls==1,"one home command");check(client().connection.sent.equals("home 1"),"Home 1 normalized");
               at(10300);check(!DeathRecoveryRuntime.isPostDeathSellPending(),"hold eight seconds");at(10301);check(DeathRecoveryRuntime.isPostDeathSellPending(),"sale at eight seconds");check(!DeathRecoveryRuntime.isBlockingAutoSell(),"priority seller allowed");check(DeathRecoveryRuntime.isRecoveryPending(),"other automation remains blocked");
               RecoverySellBridge.onTick();check(AutoSellLogic.sawEnabled,"runtime seller enabled with user OFF");check(!SlashSellSettings.enabled,"user OFF preserved");check(!Config.isAutoSellEnabled,"runtime restored OFF");check(SlashSellRuntime.syncs>0,"original timing sync used");
               AutoSellLogic.throwOnTick=true;try{RecoverySellBridge.onTick();throw new AssertionError("expected fixture exception");}catch(IllegalStateException expected){}check(!Config.isAutoSellEnabled,"finally restores runtime on tick failure");AutoSellLogic.throwOnTick=false;
               SlashSellSettings.enabled=true;Config.isAutoSellEnabled=true;RecoverySellBridge.onTick();check(Config.isAutoSellEnabled,"regular enabled state restored");check(SlashSellSettings.enabled,"user ON preserved");
               SlashSellSettings.enabled=false;RecoverySellBridge.onTick();check(!Config.isAutoSellEnabled,"OFF during recovery honored after tick");
               AutoSellLogic.items=false;client().field_1755=new class_465();at(10302);check(DeathRecoveryRuntime.isRecoveryPending(),"wait for screen to close");client().field_1755=null;client().field_1724.field_7512=new class_1703();at(10303);check(DeathRecoveryRuntime.isRecoveryPending(),"wait for server container to close");client().field_1724.field_7512=client().field_1724.field_7498;at(10304);check(!DeathRecoveryRuntime.isRecoveryPending(),"sale complete releases automation");check(!DeathRecoveryRuntime.isPostDeathSellPending(),"sale flag cleared");
               setup();AutoSellLogic.items=false;reachSale(20000);at(28302);check(!DeathRecoveryRuntime.isRecoveryPending(),"nothing sellable skips phase");
               setup();reachSale(30000);check(DeathRecoveryRuntime.isPostDeathSellPending(),"second cycle armed");client().field_1724.dead=true;at(38302);check(!DeathRecoveryRuntime.isPostDeathSellPending(),"repeat death clears previous sale");check(DeathRecoveryRuntime.isRecoveryPending(),"repeat death remains recovery");
               setup();reachSale(40000);client().connection=null;at(48302);check(!DeathRecoveryRuntime.isRecoveryPending(),"disconnect resets");
               setup();reachSale(50000);client().connection=new class_634();at(58302);check(!DeathRecoveryRuntime.isRecoveryPending(),"new connection cancels old sale");
               setup();reachSale(60000);client().field_1724=new class_746();at(68302);check(!DeathRecoveryRuntime.isRecoveryPending(),"new player cancels old sale");
               setup();reachSale(70000);DeathRecoverySettings.enabled=false;at(78302);check(!DeathRecoveryRuntime.isRecoveryPending(),"toggle OFF resets");
               setup();reachSale(80000);DeathRecoveryRuntime.resetForVexDeathWait();check(!DeathRecoveryRuntime.isRecoveryPending(),"Vex reset");check(!DeathRecoveryRuntime.isPostDeathSellPending(),"Vex sale reset");
               System.out.println("PASS "+checks+" death/recovery bridge checks with inert fixtures; no Minecraft/network/config.");
            }
         }
         """);
      List<String> command = new ArrayList<>(List.of("-d",bin.toString(),"-classpath",main.toString()));
      for (var entry : files.entrySet()) {
         String name=entry.getKey();int dot=name.lastIndexOf('.');
         Path file=src.resolve(name.replace('.','/')+".java");Files.createDirectories(file.getParent());
         Files.writeString(file,"package "+name.substring(0,dot)+";\n"+entry.getValue());command.add(file.toString());
      }
      int exit=ToolProvider.getSystemJavaCompiler().run(null,System.out,System.err,command.toArray(String[]::new));
      if(exit!=0)throw new AssertionError("fixture compile "+exit);
      for(String name:List.of("DeathRecoveryRuntime","RecoverySellBridge")) {
         Path relative=Path.of("dev/autoominous/"+name+".class");ClassNode c=new ClassNode();
         new ClassReader(Files.readAllBytes(main.resolve(relative))).accept(c,0);
         for(MethodNode m:c.methods)for(AbstractInsnNode i:m.instructions.toArray())
            if(i instanceof MethodInsnNode call && call.owner.equals("java/lang/System") && call.name.equals("currentTimeMillis"))
               m.instructions.set(i,new FieldInsnNode(Opcodes.GETSTATIC,"dev/autoominous/Clock","now","J"));
         ClassWriter w=new ClassWriter(0);c.accept(w);Files.write(bin.resolve(relative),w.toByteArray());
      }
      try(URLClassLoader loader=new URLClassLoader(new URL[]{bin.toUri().toURL()},ClassLoader.getPlatformClassLoader())) {
         loader.loadClass("dev.autoominous.FlowChecks").getMethod("main",String[].class).invoke(null,(Object)new String[0]);
      }
   }
}
