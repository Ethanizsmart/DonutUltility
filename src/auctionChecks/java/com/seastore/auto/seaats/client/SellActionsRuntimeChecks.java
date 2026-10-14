package com.seastore.auto.seaats.client;

import java.nio.file.*;
import java.util.*;
import javax.tools.ToolProvider;

/** Executes production transfer/rhythm code with a predictive vanilla-click fixture. */
public final class SellActionsRuntimeChecks {
   public static void main(String[] args) throws Exception {
      Path root = Path.of("src/main/java/com/seastore/auto/seaats/client");
      Path temporary = Files.createTempDirectory(Path.of("build"), "sell-actions-checks-");
      Map<String, String> sources = new LinkedHashMap<>();
      sources.put("net/minecraft/class_1792.java", """
         package net.minecraft;
         public class class_1792 { public final String name; public class_1792(String name) { this.name = name; } }
         """);
      sources.put("net/minecraft/class_9462.java", """
         package net.minecraft;
         public class class_9462 extends class_1792 { public class_9462() { super("Ominous"); } }
         """);
      String logic = Files.readString(root.resolve("AutoSellLogic.java"));
      String policy = logic.substring(logic.indexOf("   static boolean isSellableInventoryItem("), logic.indexOf("   @Environment", logic.indexOf("   static boolean isSellableInventoryItem(")));
      String probes = logic.substring(logic.indexOf("   public static boolean hasInventoryItems("), logic.indexOf("   public static boolean isInventorySellComplete("));
      StringBuilder items = new StringBuilder("package net.minecraft; public class class_1802 { public static final class_1792 OMINOUS = new class_9462(), STONE = new class_1792(\"Stone\"), DIRT = new class_1792(\"Dirt\");");
      var fields = java.util.regex.Pattern.compile("class_1802\\.(field_\\d+)").matcher(policy + Files.readString(root.resolve("HumanizedSellRuntime.java")));
      Set<String> names = new HashSet<>();
      while (fields.find()) if (names.add(fields.group(1))) items.append("public static final class_1792 ").append(fields.group(1)).append(" = new class_1792(\"").append(fields.group(1)).append("\");");
      sources.put("net/minecraft/class_1802.java", items.append("}").toString());
      sources.put("net/minecraft/class_2561.java", """
         package net.minecraft;
         public record class_2561(String text) { public String getString() { return text; } }
         """);
      sources.put("net/minecraft/class_1799.java", """
         package net.minecraft;
         public class class_1799 {
            public class_1792 item; public int count; public String components;
            public class_1799(class_1792 item, int count, String components) { this.item=item; this.count=count; this.components=components; }
            public boolean method_7960() { return count <= 0; }
            public class_1792 method_7909() { return item; }
            public int method_7947() { return count; }
            public int method_7914() { return 64; }
            public boolean method_31574(class_1792 item) { return this.item==item; }
            public class_2561 method_7964() { return new class_2561(components); }
            public class_1799 method_7972() { return new class_1799(item,count,components); }
            public static boolean method_31577(class_1799 a, class_1799 b) { return !a.method_7960() && !b.method_7960() && a.item==b.item && a.components.equals(b.components); }
            public static class_1799 empty() { return new class_1799(class_1802.STONE,0,""); }
         }
         """);
      sources.put("net/minecraft/class_1661.java", """
         package net.minecraft;
         public class class_1661 {
            public class_1799[] items = new class_1799[36]; public int selected;
            public class_1661() { java.util.Arrays.setAll(items, i -> class_1799.empty()); }
            public class_1799 method_5438(int i) { return items[i]; }
            public int method_67532() { return selected; }
         }
         """);
      sources.put("net/minecraft/class_1735.java", """
         package net.minecraft;
         public class class_1735 {
            public Object field_7871; public int field_7874; public int index; public class_1799 stack=class_1799.empty();
            public class_1735(Object inventory,int index,int id) { field_7871=inventory; this.index=index; field_7874=id; }
            public class_1799 method_7677() { return field_7871 instanceof class_1661 inventory ? inventory.items[index] : stack; }
            public void set(class_1799 value) { if (field_7871 instanceof class_1661 inventory) inventory.items[index]=value; else stack=value; }
            public int method_34266() { return index; }
         }
         """);
      sources.put("net/minecraft/class_1703.java", """
         package net.minecraft;
         public class class_1703 {
            public java.util.List<class_1735> field_7761 = new java.util.ArrayList<>(); public int field_7763=7;
            public class_1799 cursor=class_1799.empty();
            public class_1735 method_7611(int i) { return field_7761.get(i); }
            public class_1799 method_34255() { return cursor; }
         }
         """);
      sources.put("net/minecraft/class_465.java", """
         package net.minecraft;
         public record class_465(class_1703 menu) { public class_1703 method_17577() { return menu; } }
         """);
      sources.put("net/minecraft/class_1713.java", """
         package net.minecraft;
         public enum class_1713 { field_7790, field_7794 }
         """);
      sources.put("net/minecraft/class_310.java", """
         package net.minecraft;
         public class class_310 {
            public static class_310 instance;
            public Player field_1724=new Player(); public Interaction field_1761=new Interaction(); public Object field_1755;
            public static class_310 method_1551() { return instance; }
            public static class Player {
               public class_1661 inventory=new class_1661(); public class_1703 field_7512; public int closed;
               public class_1661 method_31548() { return inventory; }
               public void method_7346() { closed++; field_7512=null; instance.field_1755=null; }
            }
            public static class Interaction {
               public record Click(int sync, int id, int button, class_1713 action) {}
               public java.util.List<Click> clicks=new java.util.ArrayList<>();
               public boolean rollback, failPlace, replaceAfterClick;
               public void method_2906(int sync,int id,int button,class_1713 action,Player player) {
                  clicks.add(new Click(sync,id,button,action));
                  class_1703 menu=player.field_7512;
                  if (replaceAfterClick) { player.field_7512=new class_1703(); instance.field_1755=new class_465(player.field_7512); return; }
                  if (rollback) return;
                  class_1735 slot=menu.field_7761.stream().filter(s -> s.field_7874==id).findFirst().orElseThrow();
                  if (id==menu.method_7611(49).field_7874) return;
                  if (action==class_1713.field_7794) {
                     for (int i=0;i<45;i++) if (menu.method_7611(i).method_7677().method_7960()) {
                        menu.method_7611(i).set(slot.method_7677()); slot.set(class_1799.empty()); break;
                     }
                  } else if (menu.cursor.method_7960()) { menu.cursor=slot.method_7677(); slot.set(class_1799.empty()); }
                  else if (button==1 && slot.method_7677().method_7960()) {
                     class_1799 one=menu.cursor.method_7972(); one.count=1; menu.cursor.count--; slot.set(one);
                  } else if (!(failPlace && !(slot.field_7871 instanceof class_1661))) {
                     class_1799 original=slot.method_7677();
                     if (original.method_7960()) { slot.set(menu.cursor); menu.cursor=class_1799.empty(); }
                     else if (class_1799.method_31577(original,menu.cursor)) { original.count+=menu.cursor.count; menu.cursor=class_1799.empty(); }
                  }
               }
            }
         }
         """);
      sources.put("com/seastore/auto/seaats/client/AutoSellLogic.java", """
         package com.seastore.auto.seaats.client;
         import net.minecraft.*;
         public class AutoSellLogic {
            static int tickDelay; static State currentState=State.MOVING_ITEMS; static class_1703 knownSellHandler;
            enum State { IDLE, WAITING_GUI_OPEN, MOVING_ITEMS, CLICK_CONFIRM, CLICK_CONFIRM_DELAYED }
            static void closeAllGui(class_310 client) { HumanizedSellRuntime.forceReset(); knownSellHandler=null; client.field_1724.method_7346(); }
         """ + policy + probes + "}");
      sources.put("com/seastore/auto/seaats/client/ActionsCheck.java", CHECKS);
      List<String> compiler = new ArrayList<>(List.of("-encoding", "UTF-8", "-d", temporary.toString()));
      for (var source : sources.entrySet()) {
         Path path = temporary.resolve(source.getKey());
         Files.createDirectories(path.getParent());
         Files.writeString(path, source.getValue());
         compiler.add(path.toString());
      }
      for (String name : List.of("HumanizedSellRuntime", "SellRhythmRuntime", "InventorySellRules")) compiler.add(root.resolve(name + ".java").toString());
      if (ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err, compiler.toArray(String[]::new)) != 0) throw new AssertionError("Fixture compilation failed");
      int result = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java.exe").toString(), "-cp", temporary.toString(), "com.seastore.auto.seaats.client.ActionsCheck").inheritIO().start().waitFor();
      if (result != 0) throw new AssertionError("Sell actions checks failed");
      if (!logic.contains("HumanizedSellRuntime.tickMoving(var0);") || !logic.contains("HumanizedSellRuntime.tickAfterConfirm(var0);")) throw new AssertionError("Coordinator must route to checked runtime");
   }

   private static final String CHECKS = """
      package com.seastore.auto.seaats.client;
      import net.minecraft.*;
      public class ActionsCheck {
         static int checks;
         static void check(boolean result,String name) { checks++; if (!result) throw new AssertionError(name); }
         static class_310 setup() {
            HumanizedSellRuntime.clearSession(); AutoSellLogic.knownSellHandler=null; AutoSellLogic.currentState=AutoSellLogic.State.MOVING_ITEMS;
            class_310 c=new class_310(); class_310.instance=c; class_1703 m=new class_1703(); c.field_1724.field_7512=m; c.field_1755=new class_465(m);
            for (int i=0;i<54;i++) m.field_7761.add(new class_1735(new Object(),i,100+i));
            for (int i=0;i<36;i++) m.field_7761.add(new class_1735(c.field_1724.inventory,i<27?i+9:i-27,154+i));
            m.method_7611(49).set(new class_1799(class_1802.field_8581,1,"Sell")); return c;
         }
         static class_1799 stack(class_1792 item,int count,String data) { return new class_1799(item,count,data); }
         static void put(class_310 c,int index,class_1792 item,int count,String data) { c.field_1724.inventory.items[index]=stack(item,count,data); }
         static void fill(class_310 c,int n) { for(int i=0;i<n;i++) c.field_1724.field_7512.method_7611(i).set(stack(class_1802.DIRT,64,"d")); }
         static void clearBatch(class_310 c) { for(int i=0;i<45;i++) c.field_1724.field_7512.method_7611(i).set(class_1799.empty()); }
         static void expire(String field) throws Exception { var f=HumanizedSellRuntime.class.getDeclaredField(field); f.setAccessible(true); f.setLong(null,System.nanoTime()-30_000_000_000L); }
         public static void main(String[] args) throws Exception {
            class_310 c=setup();
            for(int i=0;i<6;i++) put(c,9+i,class_1802.STONE,64,"a");
            HumanizedSellRuntime.tickMoving(c);
            check(c.field_1761.clicks.size()==6,"Group sends all six matching stacks in one step");
            check(c.field_1761.clicks.stream().allMatch(x -> x.action()==class_1713.field_7794 && x.button()==0 && x.sync()==7 && x.id()>=154),"Uses handler slot IDs and vanilla QUICK_MOVE");
            check(AutoSellLogic.tickDelay>=3 && AutoSellLogic.tickDelay<=6,"Group delay 3-6 inclusive");
            HumanizedSellRuntime.tickMoving(c);
            check(AutoSellLogic.currentState==AutoSellLogic.State.CLICK_CONFIRM && AutoSellLogic.tickDelay>=4 && AutoSellLogic.tickDelay<=8,"Exhausted source schedules confirm");
            check(HumanizedSellRuntime.tickConfirm(c),"Confirm dispatched");
            check(AutoSellLogic.tickDelay==6 && AutoSellLogic.currentState==AutoSellLogic.State.CLICK_CONFIRM_DELAYED,"Six ticks after confirm");
            int clicks=c.field_1761.clicks.size();
            check(!HumanizedSellRuntime.tickConfirm(c) && c.field_1761.clicks.size()==clicks,"Duplicate confirm invocation cannot click twice");
            for(int i=0;i<5;i++) HumanizedSellRuntime.tickAfterConfirm(c);
            check(c.field_1761.clicks.size()==clicks && c.field_1724.closed==0,"Pending batch polled without second confirm");
            check(AutoSellLogic.tickDelay>=2 && AutoSellLogic.tickDelay<=5,"Poll delay 2-5");
            clearBatch(c); HumanizedSellRuntime.tickAfterConfirm(c);
            check(c.field_1724.closed==1 && AutoSellLogic.currentState==AutoSellLogic.State.IDLE,"Empty batch and inventory close server menu");

            c=setup(); fill(c,43); for(int i=9;i<14;i++) put(c,i,class_1802.STONE,64,"a");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==2,"Group bounded by initial two free sell slots");
            HumanizedSellRuntime.tickMoving(c); check(AutoSellLogic.currentState==AutoSellLogic.State.CLICK_CONFIRM && AutoSellLogic.tickDelay>=2 && AutoSellLogic.tickDelay<=4,"Full batch delay 2-4");
            HumanizedSellRuntime.tickConfirm(c); clearBatch(c); HumanizedSellRuntime.tickAfterConfirm(c);
            check(c.field_1724.closed==0 && AutoSellLogic.currentState==AutoSellLogic.State.MOVING_ITEMS,"Continue remaining sources in same menu");
            check(AutoSellLogic.tickDelay>=4 && AutoSellLogic.tickDelay<=8,"Continuation delay 4-8");
            c=setup(); fill(c,44); for(int i=9;i<13;i++) put(c,i,class_1802.STONE,64,"a");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==1,"One free slot sends one stack");
            c=setup(); put(c,9,class_1802.STONE,64,"a"); put(c,10,class_1802.STONE,64,"b");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==1,"Different components never grouped");
            c=setup(); put(c,9,class_1802.STONE,1,"a"); put(c,10,class_1802.STONE,64,"a");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==2,"Counts ignored when grouping");

            c=setup(); put(c,9,class_1802.field_8639,1,"sword"); put(c,0,class_1802.field_8450,1,"protected");
            check(!AutoSellLogic.hasInventoryItems(c),"Protected items retained in main and hotbar");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.isEmpty(),"Protected items never clicked");
            for(int location : new int[]{0,9,35}) {
               c=setup(); put(c,location,class_1802.OMINOUS,8,"level1");
               check(AutoSellLogic.hasInventoryItems(c),"Surplus detected at "+location);
               HumanizedSellRuntime.tickMoving(c);
               check(c.field_1761.clicks.size()==3,"Reserve uses three clicks at "+location);
               check(c.field_1761.clicks.get(0).button()==0 && c.field_1761.clicks.get(1).button()==1 && c.field_1761.clicks.get(2).button()==0,"Left right left split sequence");
               check(c.field_1724.inventory.items[location].count==1 && c.field_1724.field_7512.cursor.method_7960(),"Exactly one bottle retained and empty cursor");
               check(c.field_1724.field_7512.method_7611(0).method_7677().count==7,"Seven surplus staged");
               check(!AutoSellLogic.hasInventoryItems(c),"Reserve alone does not keep sale/recovery pending");
            }
            c=setup(); put(c,9,class_1802.OMINOUS,1,"one");
            check(!AutoSellLogic.hasInventoryItems(c),"Single bottle in main retained");
            c=setup(); put(c,1,class_1802.OMINOUS,1,"one"); put(c,9,class_1802.OMINOUS,3,"other");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==1 && c.field_1724.inventory.items[1].count==1 && c.field_1724.inventory.items[9].method_7960(),"Hotbar bottle reserved; other level sells");
            c=setup(); c.field_1724.inventory.selected=4; put(c,1,class_1802.OMINOUS,1,"one"); put(c,4,class_1802.OMINOUS,1,"one");
            check(AutoSellLogic.reservedOminousIndex(c)==4,"Selected hotbar bottle has priority");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1724.inventory.items[4].count==1 && c.field_1724.inventory.items[1].method_7960(),"Other hotbar bottle sells");
            c=setup(); fill(c,45); put(c,0,class_1802.OMINOUS,8,"a");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.isEmpty() && c.field_1724.inventory.items[0].count==8,"No split without empty sell slot");
            c=setup(); put(c,0,class_1802.OMINOUS,8,"a"); c.field_1761.failPlace=true;
            HumanizedSellRuntime.tickMoving(c); check(c.field_1724.inventory.items[0].count==8 && c.field_1724.field_7512.cursor.method_7960(),"Failed split returns cursor only to matching source");

            c=setup(); put(c,9,class_1802.STONE,64,"a"); c.field_1724.field_7512.cursor=stack(class_1802.DIRT,4,"held");
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.isEmpty(),"Unknown cursor never clicked into arbitrary slot");
            check(!HumanizedSellRuntime.tickConfirm(c) && c.field_1761.clicks.isEmpty(),"Unknown cursor blocks confirm");
            c=setup(); put(c,9,class_1802.STONE,64,"a"); c.field_1761.rollback=true;
            HumanizedSellRuntime.tickMoving(c); check(AutoSellLogic.currentState==AutoSellLogic.State.MOVING_ITEMS,"Unaccepted move cannot advance to confirm");
            expire("softDeadlineNanos"); HumanizedSellRuntime.tickMoving(c); check(c.field_1724.closed==1,"No-progress session bounded by timeout");
            c=setup(); fill(c,1); HumanizedSellRuntime.tickMoving(c); HumanizedSellRuntime.tickConfirm(c); expire("confirmWaitStartedNanos"); HumanizedSellRuntime.tickAfterConfirm(c);
            check(c.field_1724.closed==1,"Uncleared confirmation times out");
            c=setup(); put(c,9,class_1802.STONE,64,"a"); HumanizedSellRuntime.tickMoving(c); clicks=c.field_1761.clicks.size();
            class_1703 replacement=new class_1703(); replacement.field_7761.addAll(c.field_1724.field_7512.field_7761); replacement.field_7763=8; c.field_1724.field_7512=replacement; c.field_1755=new class_465(replacement);
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==clicks && AutoSellLogic.currentState==AutoSellLogic.State.IDLE,"Handler replacement cancels old session without clicks");
            c=setup(); put(c,9,class_1802.STONE,64,"a"); put(c,10,class_1802.STONE,64,"a"); c.field_1761.replaceAfterClick=true;
            HumanizedSellRuntime.tickMoving(c); check(c.field_1761.clicks.size()==1,"Handler rechecked between grouped clicks");
            c=setup(); c.field_1724.field_7512.method_7611(0).set(stack(class_1802.field_8581,1,"ordinary dye"));
            check(HumanizedSellRuntime.findConfirmSlot(c.field_1724.field_7512)==49,"Ordinary matching item is not confirm button");
            c.field_1724.field_7512.method_7611(49).set(class_1799.empty()); AutoSellLogic.knownSellHandler=null;
            check(!AutoSellLogic.isSellGuiOpen(c),"No button means unrecognized sell menu");
            c=setup(); for(int i=9;i<20;i++) put(c,i,class_1802.STONE,64,"a");
            int selected=SellRhythmRuntime.selectSlot(c,c.field_1724.field_7512);
            check(selected>=54 && selected<=59,"First source drawn from first six candidates");
            System.out.println("Sell actions: "+checks+" offline runtime checks passed.");
         }
      }
      """;
}
