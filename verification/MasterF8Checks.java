import java.nio.file.*;
import java.net.*;
import java.util.*;
import javax.tools.ToolProvider;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exercises actual Master/settings/key callback bytecode against inert runtime substitutes. */
public class MasterF8Checks {
   public static void main(String[] args) throws Exception {
      Path main=Path.of(args[0]),work=Path.of(args[1]),src=work.resolve("src"),bin=work.resolve("bin");
      Files.createDirectories(bin);
      Map<String,String> files=new LinkedHashMap<>();
      files.put("net.minecraft.class_437","public class class_437 {}");
      files.put("net.minecraft.class_465","public class class_465 extends class_437 {}");
      files.put("net.minecraft.class_1703","public class class_1703 {}");
      files.put("net.minecraft.class_1723","public class class_1723 extends class_1703 {}");
      files.put("net.minecraft.class_746","public class class_746 { public int closes;public class_1723 field_7498=new class_1723();public class_1703 field_7512=field_7498;public void method_7346(){closes++;field_7512=field_7498;class_310.instance.field_1755=null;} }");
      files.put("net.minecraft.class_310","public class class_310 {public static class_310 instance=new class_310();public class_746 field_1724=new class_746();public class_437 field_1755;public static class_310 method_1551(){return instance;} }");
      files.put("dev.autoominous.gui.AutoOminousGuiConfigs","public class AutoOminousGuiConfigs extends net.minecraft.class_437 {public int refreshes;public void initGui(){refreshes++;} }");
      files.put("fi.dy.masa.malilib.gui.GuiBase","public class GuiBase {public static void openGui(net.minecraft.class_437 gui){net.minecraft.class_310.instance.field_1755=gui;} }");
      files.put("fi.dy.masa.malilib.hotkeys.KeyAction","public enum KeyAction {PRESS,RELEASE,BOTH}");
      files.put("fi.dy.masa.malilib.hotkeys.KeybindSettings","public class KeybindSettings {public enum Context {ANY,GUI,INGAME} public Context context;public KeyAction action;public boolean allowExtra;public static KeybindSettings create(Context c,KeyAction a,boolean e,boolean o,boolean x,boolean cancel){KeybindSettings s=new KeybindSettings();s.context=c;s.action=a;s.allowExtra=e;return s;} }");
      files.put("fi.dy.masa.malilib.hotkeys.IHotkeyCallback","public interface IHotkeyCallback {boolean onKeyAction(KeyAction action,IKeybind keybind);}");
      files.put("fi.dy.masa.malilib.hotkeys.IKeybind","public interface IKeybind {void setCallback(IHotkeyCallback callback);}");
      files.put("fi.dy.masa.malilib.hotkeys.IKeybindManager","public interface IKeybindManager {void addKeybindToMap(IKeybind keybind);void addHotkeysForCategory(String name,String title,java.util.List<?> hotkeys);}");
      files.put("fi.dy.masa.malilib.hotkeys.IKeybindProvider","public interface IKeybindProvider {void addKeysToMap(IKeybindManager m);void addHotkeys(IKeybindManager m);}");
      files.put("fi.dy.masa.malilib.config.options.ConfigHotkey","public class ConfigHotkey {public final String name,key;public final fi.dy.masa.malilib.hotkeys.KeybindSettings settings;public final Binding binding=new Binding();public ConfigHotkey(String n,String k){this(n,k,null);}public ConfigHotkey(String n,String k,fi.dy.masa.malilib.hotkeys.KeybindSettings s){name=n;key=k;settings=s;}public fi.dy.masa.malilib.hotkeys.IKeybind getKeybind(){return binding;}public static class Binding implements fi.dy.masa.malilib.hotkeys.IKeybind {public fi.dy.masa.malilib.hotkeys.IHotkeyCallback callback;public void setCallback(fi.dy.masa.malilib.hotkeys.IHotkeyCallback c){callback=c;}public boolean fire(fi.dy.masa.malilib.hotkeys.KeyAction a){return callback.onKeyAction(a,this);} } }");
      files.put("fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed","public class ConfigBooleanHotkeyed {public final fi.dy.masa.malilib.config.options.ConfigHotkey.Binding binding=new fi.dy.masa.malilib.config.options.ConfigHotkey.Binding();public fi.dy.masa.malilib.hotkeys.IKeybind getKeybind(){return binding;} }");
      files.put("com.seastore.auto.seaats.Config","public class Config {public static boolean isAutoSellEnabled;}");
      files.put("dev.autoominous.SlashSellSettings","public class SlashSellSettings {public static boolean enabled=true;public static final fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed AUTO_SLASH_SELL=new fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed();}");
      files.put("dev.autoominous.BountySettings","public class BountySettings {public static boolean enabled=true;public static final fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed AUTO_BOUNTY=new fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed();}");
      files.put("dev.autoominous.TransactionGate","public class TransactionGate {public static boolean sell;public static boolean isSeaSellActive(){return sell;} }");
      files.put("dev.autoominous.SwordRecoveryRuntime","public class SwordRecoveryRuntime {public static boolean active;public static boolean isRecoveryActive(){return active;}public static void stopForMaster(){active=false;} }");
      files.put("dev.autoominous.HomePositionGuardRuntime","public class HomePositionGuardRuntime {public static boolean active;public static boolean isBlockingAutomation(){return active;}public static void resetForMaster(){active=false;} }");
      files.put("dev.autoominous.BountyRuntime","public class BountyRuntime {public static boolean active;public static boolean isBlockingSell(){return active;}public static void resetForMaster(){active=false;} }");
      files.put("dev.autoominous.AutoTpaRuntime","public class AutoTpaRuntime {public static boolean awaitingConfirm;public static void resetForMaster(){awaitingConfirm=false;} }");
      files.put("dev.autoominous.AutoReconnectRuntime","public class AutoReconnectRuntime {public static long deadline;public static void resetForMaster(){deadline=0;} }");
      files.put("dev.autoominous.DisconnectRuntime","public class DisconnectRuntime {public static boolean triggered;public static void resetForMaster(){triggered=false;} }");
      files.put("dev.autoominous.VexSafetyRuntime","public class VexSafetyRuntime {public static boolean waiting;public static void resetForMaster(){waiting=false;} }");
      files.put("dev.autoominous.G232SafeRuntime","public class G232SafeRuntime {public static boolean pending;public static void resetForMaster(){pending=false;} }");
      files.put("dev.autoominous.AutomationCoordinator","public class AutomationCoordinator {public static int resets;public static boolean held,recovery;public static void resetForMaster(){resets++;held=false;recovery=false;} }");
      files.put("dev.autoominous.ConsumableRecoveryRuntime","public class ConsumableRecoveryRuntime {public static void resetOmiBaseline(){}public static void resetFireworkBaseline(){} }");
      files.put("dev.autoominous.SlashSellRuntime","public class SlashSellRuntime {public static int resets;public static void resetRuntime(){resets++;TransactionGate.sell=false;} }");
      files.put("dev.autoominous.ReflectionSupport","public class ReflectionSupport {public static int farmResets;public static boolean staticBoolean(String c,String f,boolean fallback){return AutoTpaRuntime.awaitingConfirm;}public static void callStaticVoid(String c,String m){if(c.equals(\"dev.autoominous.FarmCycleRuntime\")&&m.equals(\"resetSoft\"))farmResets++;} }");
      files.put("dev.autoominous.ClientStatusMessage","public class ClientStatusMessage {public static String text;public static void actionbarState(String name,boolean enabled){text=name+\": \"+(enabled?\"ON\":\"OFF\");} }");
      files.put("dev.autoominous.MasterChecks","""
         import net.minecraft.*;import fi.dy.masa.malilib.hotkeys.*;import dev.autoominous.event.AutoOminousInputHandler;import dev.autoominous.gui.AutoOminousGuiConfigs;
         public class MasterChecks {
            static int checks;static void check(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
            static void dirty(){TransactionGate.sell=true;SwordRecoveryRuntime.active=true;HomePositionGuardRuntime.active=true;BountyRuntime.active=true;AutoTpaRuntime.awaitingConfirm=true;AutoReconnectRuntime.deadline=12345;DisconnectRuntime.triggered=true;VexSafetyRuntime.waiting=true;G232SafeRuntime.pending=true;AutomationCoordinator.held=true;AutomationCoordinator.recovery=true;com.seastore.auto.seaats.Config.isAutoSellEnabled=true;}
            public static void main(String[] args){
               check(!MasterAutomation.isEnabled(),"new game starts OFF");
               var key=AutoOminousSettings.MASTER_TOGGLE;check(key.key.equals("F8"),"F8 binding");check(key.settings.context==KeybindSettings.Context.ANY,"F8 works with GUI");check(key.settings.action==KeyAction.PRESS,"press activation only");check(key.settings.allowExtra,"F8 still works while other keys held");
               AutoOminousInputHandler.initCallbacks();
               class Manager implements IKeybindManager {java.util.List<IKeybind> bindings=new java.util.ArrayList<>();java.util.List<?> hotkeys;public void addKeybindToMap(IKeybind b){bindings.add(b);}public void addHotkeysForCategory(String n,String t,java.util.List<?> h){hotkeys=h;}}
               Manager manager=new Manager();AutoOminousInputHandler.getInstance().addKeysToMap(manager);AutoOminousInputHandler.getInstance().addHotkeys(manager);check(manager.bindings.contains(key.binding),"registered key map");check(manager.hotkeys.contains(key),"registered hotkey category");
               var gui=new AutoOminousGuiConfigs();class_310.instance.field_1755=gui;
               check(key.binding.fire(KeyAction.PRESS),"F8 consumed");check(MasterAutomation.isEnabled(),"F8 enables");check(gui.refreshes==1,"F8 refreshes GUI label");check(ClientStatusMessage.text.equals("Master (F8): ON"),"ON actionbar");
               int resets=AutomationCoordinator.resets;check(!key.binding.fire(KeyAction.RELEASE),"release ignored");check(MasterAutomation.isEnabled(),"release cannot toggle again");check(resets==AutomationCoordinator.resets,"release does not reset runtime");
               MasterAutomation.setEnabled(true);check(resets==AutomationCoordinator.resets,"same state is idempotent");
               dirty();class_310.instance.field_1755=new class_465();class_310.instance.field_1724.field_7512=new class_1703();key.binding.fire(KeyAction.PRESS);
               check(!MasterAutomation.isEnabled(),"second F8 disables");check(!AutomationCoordinator.held,"held input released");check(!AutomationCoordinator.recovery,"recovery cancelled");check(!TransactionGate.sell,"sell state reset");check(!SwordRecoveryRuntime.active,"buyer cancelled");check(!HomePositionGuardRuntime.active,"Home transit cancelled");check(!BountyRuntime.active,"Bounty pending cancelled");check(!AutoTpaRuntime.awaitingConfirm,"TPA pending cancelled");check(AutoReconnectRuntime.deadline==0,"reconnect countdown cancelled");check(!DisconnectRuntime.triggered,"old reconnect trigger cancelled");check(!VexSafetyRuntime.waiting,"Vex wait reset");check(!G232SafeRuntime.pending,"inventory recovery reset");check(!com.seastore.auto.seaats.Config.isAutoSellEnabled,"seller runtime OFF");check(class_310.instance.field_1724.closes==1,"owned server menu closed without confirming");check(SlashSellSettings.enabled&&BountySettings.enabled,"individual ON settings preserved");check(ClientStatusMessage.text.equals("Master (F8): OFF"),"OFF actionbar");check(ReflectionSupport.farmResets==2,"farm session reset per toggle");
               SlashSellSettings.enabled=false;BountySettings.enabled=false;MasterAutomation.setEnabled(true);check(!SlashSellSettings.enabled&&!BountySettings.enabled,"individual OFF settings preserved");check(class_310.instance.field_1724.closes==1,"unowned GUI not closed");check(MasterAutomation.isEnabled(),"can resume");class_310.instance=null;MasterAutomation.toggle();check(!MasterAutomation.isEnabled(),"toggle works disconnected");
               System.out.println("PASS "+checks+" Master/F8 checks with inert fixtures; no Minecraft/network/config.");
            }
         }
         """);
      List<String> javac=new ArrayList<>(List.of("--release","21","-cp",main.toString(),"-d",bin.toString()));
      for(var e:files.entrySet()) {int p=e.getKey().lastIndexOf('.');Path path=src.resolve(e.getKey().replace('.','/')+".java");Files.createDirectories(path.getParent());Files.writeString(path,"package "+e.getKey().substring(0,p)+";\n"+e.getValue());javac.add(path.toString());}
      int exit=ToolProvider.getSystemJavaCompiler().run(null,null,null,javac.toArray(String[]::new));if(exit!=0)throw new AssertionError("fixture compile "+exit);
      for(String type:List.of("MasterAutomation","AutoOminousSettings","event/AutoOminousInputHandler")){Path relative=Path.of("dev/autoominous/"+type+".class");Files.createDirectories(bin.resolve(relative).getParent());Files.copy(main.resolve(relative),bin.resolve(relative),StandardCopyOption.REPLACE_EXISTING);}
      try(URLClassLoader loader=new URLClassLoader(new URL[]{bin.toUri().toURL()},ClassLoader.getPlatformClassLoader())) {loader.loadClass("dev.autoominous.MasterChecks").getMethod("main",String[].class).invoke(null,(Object)new String[0]);}
      String[][] guards={{"AutomationCoordinator","onTick"},{"DisconnectRuntime","onTick"},{"AutoReconnectRuntime","onTick"},{"AutoTpaRuntime","onTick"},{"AutoTpaRuntime","onGameMessage"},{"BountyRuntime","onTick"},{"BountyRuntime","onGameMessageObject"},{"DeathRecoveryRuntime","onTick"},{"SwordRecoveryRuntime","onTick"},{"HomePositionGuardRuntime","onTick"},{"RecoverySellBridge","tickCurrentSeller"},{"mixin/SellRelogWatchdogMixin","donututility$sellRelogWatchdog"}};
      for(String[] guard:guards){ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(main.resolve("dev/autoominous/"+guard[0]+".class"))).accept(c,0);MethodNode m=c.methods.stream().filter(x->x.name.equals(guard[1])).findFirst().orElseThrow();MethodInsnNode first=null;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode call){first=call;break;}if(first==null||!first.owner.equals("dev/autoominous/MasterAutomation")||!first.name.equals("isEnabled"))throw new AssertionError("Ungated entry: "+Arrays.toString(guard));}
      System.out.println("PASS "+guards.length+" bytecode checks: Master guard precedes action calls at automation entry points.");
   }
}
