import java.nio.file.*;
import java.net.*;
import java.util.*;
import javax.tools.ToolProvider;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exercises actual Home Guard bytecode using a clock, inert Minecraft objects and isolated files. */
public class HomeGuardChecks {
   public static void main(String[] args) throws Exception {
      Path main=Path.of(args[0]),work=Path.of(args[1]);
      Path src=work.resolve("src"),bin=work.resolve("bin");Files.createDirectories(bin);
      Map<String,String> files=new LinkedHashMap<>();
      files.put("dev.autoominous.Clock","public class Clock {public static long now;}");
      files.put("dev.autoominous.MasterAutomation","public class MasterAutomation {public static boolean enabled=true;public static boolean isEnabled(){return enabled;}}");
      files.put("dev.autoominous.DeathRecoverySettings", """
         public class DeathRecoverySettings {
            public static boolean guard=true;
            public static void maybePersist(){}
            public static boolean isHomeGuardEnabled(){return guard;}
            public static double getHomeGuardPositionTolerance(){return 0.3;}
            public static double getHomeGuardLookTolerance(){return 2.0;}
            public static String getHome1CommandForSend(){return "/home 1";}
         }
         """);
      files.put("dev.autoominous.DeathRecoveryRuntime","public class DeathRecoveryRuntime {public static boolean pending;public static boolean isRecoveryPending(){return pending;}}");
      files.put("dev.autoominous.VexSafetyRuntime","public class VexSafetyRuntime {public static boolean danger;public static boolean isDangerArmed(){return danger;}public static boolean allowHomeGuardConfigured(){return !danger;}}");
      files.put("dev.autoominous.AutoTpaRuntime","public class AutoTpaRuntime {public static String pendingUser;public static boolean awaitingConfirm;}");
      files.put("dev.autoominous.BountyRuntime","public class BountyRuntime {public static boolean busy;public static boolean isBlockingSell(){return busy;}}");
      files.put("dev.autoominous.SwordRecoveryRuntime","public class SwordRecoveryRuntime {public static String state=\"CHECK_LOCAL\";public static long stateDeadlineMs;public static boolean active;public static boolean isRecoveryActive(){return active;}}");
      files.put("dev.autoominous.TransactionGate","public class TransactionGate {public static boolean sell;public static boolean isSeaSellActive(){return sell;}}");
      files.put("dev.autoominous.CommandBroker","public class CommandBroker {public static boolean backoff;public static boolean isBackoffActive(){return backoff;}}");
      files.put("dev.autoominous.SlashSellRuntime","public class SlashSellRuntime {public static int resets;public static void resetRuntime(){resets++;}}");
      files.put("dev.autoominous.ProtectedItemAutoUseRuntime","public class ProtectedItemAutoUseRuntime {public static int releases,rearms;public static void forceRelease(){releases++;}public static void requestRearm(){rearms++;}}");
      files.put("com.seastore.auto.seaats.client.HumanizedSellRuntime","public class HumanizedSellRuntime {public static void forceReset(){}}");
      files.put("dev.autoominous.ClientStatusMessage","public class ClientStatusMessage {public static String last;public static void actionbar(String s){last=s;}}");
      files.put("dev.autoominous.HumanBehaviorProfile","public class HumanBehaviorProfile {public static int homeGuardRandom(int a,int b){return 500;}}");
      files.put("net.minecraft.class_437","public class class_437 {}");
      files.put("net.minecraft.class_465","public class class_465 extends class_437 {}");
      files.put("net.minecraft.class_638","public class class_638 {public String key=\"overworld\";public String method_27983(){return key;}}");
      files.put("net.minecraft.class_634","public class class_634 {public int calls;public String sent;public void method_45730(String s){calls++;sent=s;}}");
      files.put("net.minecraft.class_746", """
         public class class_746 {
            public double x=10,y=64,z=20;public float yaw=30,pitch=15;public boolean alive=true;
            public double method_23317(){return x;}public double method_23318(){return y;}public double method_23321(){return z;}
            public float method_36454(){return yaw;}public float method_36455(){return pitch;}
            public boolean method_5805(){return alive;}public void method_7346(){}
         }
         """);
      files.put("net.minecraft.class_310", """
         public class class_310 {
            public static class_310 instance=new class_310();
            public class_746 field_1724=new class_746();public class_638 field_1687=new class_638();
            public class_437 field_1755;public class_634 connection=new class_634();
            public static class_310 method_1551(){return instance;}public class_634 method_1562(){return connection;}
            public void method_1507(class_437 s){field_1755=s;}
         }
         """);
      files.put("dev.autoominous.HomeChecks", """
         import net.minecraft.*;
         import java.nio.file.*;
         import java.util.*;
         public class HomeChecks {
            public static Path anchor;
            static int checks;
            static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
            static Object get(String name)throws Exception{var f=HomePositionGuardRuntime.class.getDeclaredField(name);f.setAccessible(true);return f.get(null);}
            static void set(String name,Object value)throws Exception{var f=HomePositionGuardRuntime.class.getDeclaredField(name);f.setAccessible(true);f.set(null,value);}
            static class_310 c(){return class_310.instance;}
            static class_746 p(){return c().field_1724;}
            static void at(long t){Clock.now=t;HomePositionGuardRuntime.onTick();}
            static void reload()throws Exception{set("loaded",false);set("anchorSet",false);set("anchorLookSet",false);HomePositionGuardRuntime.getCaptureButtonLabel();}
            static Properties props()throws Exception{var v=new Properties();try(var r=Files.newBufferedReader(anchor)){v.load(r);}return v;}
            static void write(String content)throws Exception{Files.writeString(anchor,content);reload();}
            static void setup()throws Exception{
               Clock.now=1000;MasterAutomation.enabled=true;DeathRecoverySettings.guard=true;DeathRecoveryRuntime.pending=false;VexSafetyRuntime.danger=false;
               AutoTpaRuntime.pendingUser=null;AutoTpaRuntime.awaitingConfirm=false;BountyRuntime.busy=false;SwordRecoveryRuntime.active=false;
               TransactionGate.sell=false;CommandBroker.backoff=false;class_310.instance=new class_310();HomePositionGuardRuntime.resetForMaster();
               HomePositionGuardRuntime.saveCurrentPose();c().connection.calls=0;
            }
            static void drift(){p().yaw=40;at(2000);at(2499);at(2500);}
            static boolean blocking(){return HomePositionGuardRuntime.isBlockingAutomation();}
            public static void main(String[] args)throws Exception{
               anchor=Path.of(args[0]);Files.createDirectories(anchor.getParent());
               setup();check(c().connection.calls==0,"manual calibration sends no command");
               check(props().getProperty("yaw").equals("30.0") && props().getProperty("pitch").equals("15.0"),"manual yaw/pitch persisted");
               check(props().getProperty("captureMode").equals("manual"),"manual mode explicit");reload();check((boolean)get("anchorLookSet"),"manual pose reloads");
               MasterAutomation.enabled=false;p().yaw=70;at(5000);at(6000);check(c().connection.calls==0,"Master OFF prevents Home");
               HomePositionGuardRuntime.saveCurrentPose();check((double)get("anchorYaw")==70,"explicit calibration allowed with Master OFF");
               setup();at(2000);at(5000);check(c().connection.calls==0,"correct pose stays idle");
               p().x+=0.1;p().yaw+=1;p().pitch+=1;at(6000);at(8000);check(c().connection.calls==0,"small noise tolerated");
               setup();p().yaw=40;at(2000);at(2499);check(c().connection.calls==0,"yaw debounce not early");at(2500);
               check(c().connection.calls==1,"yaw alone triggers home");check(c().connection.sent.equals("home 1"),"normalized Home 1 command");
               check(blocking(),"return owns automation");at(3000);at(9000);check(c().connection.calls==1,"no repeated command during transit");
               p().yaw=30;p().pitch=25;at(9100);at(9900);check(blocking(),"position restored but pitch wrong does not finish");
               p().pitch=15;at(10000);at(10649);check(blocking(),"pose must settle 650ms");at(10650);check(!blocking(),"correct pose releases automation");
               check(ProtectedItemAutoUseRuntime.rearms>0,"input rearm requested");
               setup();p().pitch=20;at(2000);at(2500);check(c().connection.calls==1,"pitch alone triggers");
               setup();p().yaw=179;HomePositionGuardRuntime.saveCurrentPose();p().yaw=-179;at(2000);at(2500);check(c().connection.calls==0,"yaw wrap boundary accepted");
               p().yaw=-175;at(3000);at(3500);check(c().connection.calls==1,"yaw wrap larger drift triggers");
               for(int axis=0;axis<3;axis++){setup();if(axis==0)p().x+=1;else if(axis==1)p().y+=1;else p().z+=1;at(2000);at(2500);check(c().connection.calls==1,"XYZ axis drift "+axis);}
               setup();c().field_1687.key="nether";at(2000);at(2500);check(c().connection.calls==1,"world mismatch triggers");
               setup();drift();at(12500);check(c().connection.calls==2,"one retry at ten seconds");at(22500);check(!blocking(),"failure stops transit after second timeout");
               check((boolean)get("returnFailureLatched"),"failure latch set");at(25000);at(100000);check(c().connection.calls==2,"failed home cannot spam");
               p().yaw=30;at(101000);check(!(boolean)get("returnFailureLatched"),"correct pose clears failure latch");p().yaw=40;at(102000);at(102500);check(c().connection.calls==3,"new drift after correct pose may return");
               setup();drift();DeathRecoverySettings.guard=false;at(2600);check(!blocking(),"individual OFF cancels return");at(13000);check(c().connection.calls==1,"individual OFF cancels retry");
               setup();drift();DeathRecoveryRuntime.pending=true;at(2600);check(!blocking(),"Death Recovery cancels return");
               setup();drift();VexSafetyRuntime.danger=true;at(2600);check(!blocking(),"Vex cancels return");
               setup();drift();MasterAutomation.enabled=false;HomePositionGuardRuntime.resetForMaster();at(13000);check(!blocking() && c().connection.calls==1,"Master reset cancels return and retry");
               setup();drift();c().field_1755=new class_465();at(12500);check(c().connection.calls==1,"retry does not send while manual GUI open");check(c().field_1755!=null && !blocking(),"retry defers and preserves manual GUI");
               setup();drift();CommandBroker.backoff=true;at(12500);check(c().connection.calls==1 && !blocking(),"retry respects command backoff");
               setup();drift();AutoTpaRuntime.awaitingConfirm=true;at(12500);check(c().connection.calls==1 && !blocking(),"retry respects newly pending TPA");
               for(int busy=0;busy<8;busy++){
                  setup();p().yaw=40;
                  switch(busy){case 0:c().field_1755=new class_465();break;case 1:BountyRuntime.busy=true;break;case 2:SwordRecoveryRuntime.active=true;break;case 3:TransactionGate.sell=true;break;case 4:CommandBroker.backoff=true;break;case 5:AutoTpaRuntime.pendingUser="friend";break;case 6:DeathRecoveryRuntime.pending=true;break;case 7:VexSafetyRuntime.danger=true;}
                  at(2000);at(3000);check(c().connection.calls==0,"busy operation defers Home "+busy);
               }
               setup();p().yaw=40;AutoTpaRuntime.awaitingConfirm=true;at(2000);AutoTpaRuntime.awaitingConfirm=false;at(3000);at(4999);check(c().connection.calls==0,"TPA grace respected");at(5000);at(5500);check(c().connection.calls==1,"guard resumes after TPA grace and debounce");
               setup();p().alive=false;p().yaw=40;at(2000);at(3000);check(c().connection.calls==0,"dead player not teleported");
               var saved=Files.readString(anchor);HomePositionGuardRuntime.saveCurrentPose();check(Files.readString(anchor).equals(saved),"dead pose not saved");
               setup();p().pitch=Float.NaN;var oldYaw=get("anchorYaw");HomePositionGuardRuntime.saveCurrentPose();check(get("anchorYaw").equals(oldYaw),"invalid angles not saved");
               setup();HomePositionGuardRuntime.requestCapture();c().field_1755=new class_437();at(2000);check(c().connection.calls==0,"capture waits for GUI");c().field_1755=null;at(2100);check(c().connection.calls==1 && blocking(),"capture begins when safe");
               p().x=100;p().yaw=80;p().pitch=-30;at(2200);at(2849);check(blocking(),"capture waits for stable pose");at(2850);check(!blocking(),"capture finishes after teleport and settle");
               check((double)get("anchorX")==100 && (double)get("anchorYaw")==80 && (double)get("anchorPitch")==-30,"captured XYZ yaw pitch match");
               check(props().getProperty("verifiedTeleportCapture").equals("true"),"capture verified marker saved");
               setup();HomePositionGuardRuntime.requestCapture();at(2000);at(12000);check(!blocking(),"capture without teleport times out");check((double)get("anchorX")==10,"failed capture preserves saved pose");
               setup();write("set=true\\nverifiedTeleportCapture=true\\nx=10\\ny=64\\nz=20\\nworld=overworld\\n");check((boolean)get("anchorSet") && !(boolean)get("anchorLookSet"),"legacy verified XYZ anchor supported without invented angles");
               p().yaw=70;at(2000);at(2500);check(c().connection.calls==0,"legacy anchor waits for look calibration");p().x=11;at(3000);at(3500);check(c().connection.calls==1,"legacy position check preserved");
               HomePositionGuardRuntime.resetForMaster();write("set=true\\nverifiedTeleportCapture=false\\nx=10\\ny=64\\nz=20\\n");check(!(boolean)get("anchorSet"),"unverified legacy anchor ignored");
               write("set=true\\ncaptureMode=manual\\nx=NaN\\ny=64\\nz=20\\nlookSet=true\\nyaw=30\\npitch=15\\n");check(!(boolean)get("anchorSet"),"nonfinite stored position rejected");
               write("set=true\\ncaptureMode=manual\\nx=10\\ny=64\\nz=20\\nlookSet=true\\nyaw=30\\npitch=91\\n");check((boolean)get("anchorSet") && !(boolean)get("anchorLookSet"),"invalid stored pitch rejected while retaining valid XYZ");
               System.out.println("PASS "+checks+" Home Guard runtime checks; inert objects, isolated files, no Minecraft/network.");
            }
         }
         """);
      List<String> command=new ArrayList<>(List.of("-d",bin.toString(),"-classpath",main.toString()));
      for(var entry:files.entrySet()){
         String name=entry.getKey();Path file=src.resolve(name.replace('.','/')+".java");Files.createDirectories(file.getParent());
         Files.writeString(file,"package "+name.substring(0,name.lastIndexOf('.'))+";\n"+entry.getValue());command.add(file.toString());
      }
      int result=ToolProvider.getSystemJavaCompiler().run(null,System.out,System.err,command.toArray(String[]::new));
      if(result!=0)throw new AssertionError("fixture compile "+result);
      for(String name:List.of("HomePositionGuardRuntime","HomePositionGuardRuntime$Transit","HomeAnchorRules")){
         Path relative=Path.of("dev/autoominous/"+name+".class");ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(main.resolve(relative))).accept(c,0);
         for(MethodNode m:c.methods){
            for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode call && call.owner.equals("java/lang/System") && call.name.equals("currentTimeMillis"))
               m.instructions.set(i,new FieldInsnNode(Opcodes.GETSTATIC,"dev/autoominous/Clock","now","J"));
            if(m.name.equals("anchorPath")){
               m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
               m.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,"dev/autoominous/HomeChecks","anchor","Ljava/nio/file/Path;"));m.instructions.add(new InsnNode(Opcodes.ARETURN));m.maxStack=1;m.maxLocals=0;
            }
         }
         ClassWriter writer=new ClassWriter(0);c.accept(writer);Files.write(bin.resolve(relative),writer.toByteArray());
      }
      try(URLClassLoader loader=new URLClassLoader(new URL[]{bin.toUri().toURL()},ClassLoader.getPlatformClassLoader())){
         loader.loadClass("dev.autoominous.HomeChecks").getMethod("main",String[].class).invoke(null,(Object)new String[]{work.resolve("config/home-anchor.properties").toString()});
      }
   }
}
