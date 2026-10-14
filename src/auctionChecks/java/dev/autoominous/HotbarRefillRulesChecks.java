package dev.autoominous;

import java.nio.file.*;
import java.util.*;
import javax.tools.ToolProvider;

public final class HotbarRefillRulesChecks {
   public static void main(String[] args) throws Exception {
      Path root = Path.of("src/main/java/dev/autoominous");
      Path temporary = Files.createTempDirectory(Path.of("build"), "hotbar-refill-checks-");
      Map<String, String> sources = new LinkedHashMap<>();

      sources.put("net/minecraft/class_2960.java", """
         package net.minecraft;
         public record class_2960(String id) {
            public static class_2960 method_60654(String id) { return new class_2960(id); }
            public boolean method_40226(class_2960 other) { return other != null && id.equals(other.id); }
            public String toString() { return id; }
         }
         """);

      sources.put("net/minecraft/class_7923.java", """
         package net.minecraft;
         public class class_7923 {
            public static final Registry field_41178 = new Registry();
            public static class Registry {
               public class_2960 method_10221(class_1792 item) { return new class_2960(item.id); }
            }
         }
         """);

      sources.put("net/minecraft/class_1792.java", """
         package net.minecraft;
         public class class_1792 {
            public final String id;
            public class_1792(String id) { this.id = id; }
         }
         """);

      sources.put("net/minecraft/class_9462.java", """
         package net.minecraft;
         public class class_9462 extends class_1792 {
            public class_9462() { super("minecraft:ominous_bottle"); }
         }
         """);

      sources.put("net/minecraft/class_1802.java", """
         package net.minecraft;
         public class class_1802 {
            public static final class_1792 field_8802 = new class_1792("minecraft:diamond_sword");
            public static final class_1792 field_22022 = new class_1792("minecraft:netherite_sword");
            public static final class_1792 field_8371 = new class_1792("minecraft:iron_sword");
            public static final class_1792 field_8845 = new class_1792("minecraft:golden_sword");
            public static final class_1792 field_8528 = new class_1792("minecraft:stone_sword");
            public static final class_1792 field_8091 = new class_1792("minecraft:wooden_sword");
            public static final class_1792 field_61338 = new class_1792("minecraft:copper_sword");
            public static final class_1792 field_50140 = new class_9462();
            public static final class_1792 field_8639 = new class_1792("minecraft:firework_rocket");
            public static final class_1792 DIRT = new class_1792("minecraft:dirt");
            public static final class_1792 COBBLE = new class_1792("minecraft:cobblestone");
         }
         """);

      sources.put("net/minecraft/class_1799.java", """
         package net.minecraft;
         import java.util.*;
         public class class_1799 {
            public class_1792 item;
            public int count;
            public Map<class_2960, Integer> enchants = new HashMap<>();

            public class_1799(class_1792 item, int count) { this.item = item; this.count = count; }
            public class_1799(class_1792 item, int count, int lootingLevel) {
               this(item, count);
               if (lootingLevel > 0) enchants.put(new class_2960("minecraft:looting"), lootingLevel);
            }
            public boolean method_7960() { return count <= 0 || item == null; }
            public class_1792 method_7909() { return item; }
            public int method_7947() { return count; }
            public boolean method_31574(class_1792 other) { return this.item == other; }
            public static class_1799 empty() { return new class_1799(null, 0); }

            public EnchantmentsComponent method_58657() {
               return new EnchantmentsComponent(enchants);
            }

            public static class EnchantmentsComponent {
               public final Map<class_2960, Integer> map;
               public EnchantmentsComponent(Map<class_2960, Integer> map) { this.map = map; }
               public Set<Map.Entry<class_2960, Integer>> method_57539() {
                  Set<Map.Entry<class_2960, Integer>> res = new HashSet<>();
                  for (var e : map.entrySet()) {
                     res.add(new EntryStub(e.getKey(), e.getValue()));
                  }
                  return res;
               }
            }
            public static class EntryStub implements Map.Entry<class_2960, Integer> {
               final class_2960 k; final Integer v;
               public EntryStub(class_2960 k, Integer v) { this.k=k; this.v=v; }
               public class_2960 getKey() { return k; }
               public Integer getValue() { return v; }
               public int getIntValue() { return v; }
               public Integer setValue(Integer val) { return v; }
            }
         }
         """);

      sources.put("net/minecraft/class_1661.java", """
         package net.minecraft;
         public class class_1661 {
            public class_1799[] items = new class_1799[36];
            public class_1661() {
               for (int i=0; i<36; i++) items[i] = class_1799.empty();
            }
            public class_1799 method_5438(int i) { return items[i]; }
            public void set(int i, class_1799 stack) { items[i] = stack; }
         }
         """);

      sources.put("net/minecraft/class_1657.java", """
         package net.minecraft;
         public class class_1657 {
            public class_1661 inventory = new class_1661();
            public class_1661 method_31548() { return inventory; }
         }
         """);

      sources.put("net/minecraft/class_1735.java", """
         package net.minecraft;
         public class class_1735 {
            public Object field_7871;
            public int field_7874;
            public int index;
            public class_1735(Object inv, int index, int id) {
               this.field_7871 = inv;
               this.index = index;
               this.field_7874 = id;
            }
            public int method_34266() { return index; }
         }
         """);

      sources.put("net/minecraft/class_1703.java", """
         package net.minecraft;
         import java.util.*;
         public class class_1703 {
            public int field_7763 = 0;
            public List<class_1735> field_7761 = new ArrayList<>();
         }
         """);

      sources.put("net/minecraft/class_1713.java", """
         package net.minecraft;
         public enum class_1713 {
            field_7790,
            field_7794,
            field_7791
         }
         """);

      sources.put("net/minecraft/class_437.java", """
         package net.minecraft;
         public class class_437 {}
         """);

      sources.put("net/minecraft/class_490.java", """
         package net.minecraft;
         public class class_490 extends class_437 {
            public class_490(class_1657 player) {}
         }
         """);

      sources.put("net/minecraft/class_310.java", """
         package net.minecraft;
         import java.util.*;
         public class class_310 {
            public static class_310 instance;
            public Player field_1724;
            public Object field_1687 = new Object();
            public Interaction field_1761;
            public Object field_1755;
            public Options field_1690 = new Options();

            public static class_310 method_1551() { return instance; }
            public void method_1507(Object screen) { this.field_1755 = screen; }

            public static class Options {
               public Key useKey = new Key();
               public Key field_1904 = useKey;
            }
            public static class Key {
               public boolean pressed;
               public void method_23481(boolean p) { this.pressed = p; }
            }
            public static class Player extends class_1657 {
               public class_1703 field_7512;
               public boolean alive = true;
               public int closed = 0;
               public boolean method_5805() { return alive; }
               public void method_7346() { closed++; }
            }
            public static class Interaction {
               public record Click(int sync, int slot, int button, class_1713 action) {}
               public List<Click> clicks = new ArrayList<>();
               public void method_2906(int sync, int slot, int button, class_1713 action, Player player) {
                  clicks.add(new Click(sync, slot, button, action));
                  // Vanilla SWAP logic in test fixture
                  if (action == class_1713.field_7791) {
                     int invIndex = -1;
                     for (class_1735 s : player.field_7512.field_7761) {
                        if (s.field_7874 == slot) { invIndex = s.index; break; }
                     }
                     if (invIndex >= 0 && button >= 0 && button < 9) {
                        class_1799 temp = player.inventory.items[button];
                        player.inventory.items[button] = player.inventory.items[invIndex];
                        player.inventory.items[invIndex] = temp;
                     }
                  }
               }
            }
         }
         """);

      sources.put("dev/autoominous/MasterAutomation.java", """
         package dev.autoominous;
         public final class MasterAutomation {
            public static boolean enabled = true;
            public static boolean isEnabled() { return enabled; }
         }
         """);

      sources.put("dev/autoominous/ProtectedItemAutoUseRuntime.java", """
         package dev.autoominous;
         public final class ProtectedItemAutoUseRuntime {
            public static boolean released = false;
            public static boolean rearmed = false;
            public static void forceRelease() { released = true; }
            public static void requestRearm() { rearmed = true; }
         }
         """);

      sources.put("dev/autoominous/DeathRecoveryRuntime.java", """
         package dev.autoominous;
         public final class DeathRecoveryRuntime {
            public static boolean pending = false;
            public static boolean isRecoveryPending() { return pending; }
            public static boolean isPostDeathSellPending() { return false; }
         }
         """);

      sources.put("dev/autoominous/SwordRecoveryRuntime.java", """
         package dev.autoominous;
         public final class SwordRecoveryRuntime {
            public static boolean active = false;
            public static boolean isRecoveryActive() { return active; }
         }
         """);

      sources.put("dev/autoominous/HomePositionGuardRuntime.java", """
         package dev.autoominous;
         public final class HomePositionGuardRuntime {
            public static boolean isBlockingAutomation() { return false; }
         }
         """);

      sources.put("dev/autoominous/BountyRuntime.java", """
         package dev.autoominous;
         public final class BountyRuntime {
            public static boolean isBlockingSell() { return false; }
         }
         """);

      sources.put("com/seastore/auto/seaats/client/AutoSellLogic.java", """
         package com.seastore.auto.seaats.client;
         public final class AutoSellLogic {
            public static boolean isBusy() { return false; }
         }
         """);

      sources.put("com/seastore/auto/seaats/client/HumanizedSellRuntime.java", """
         package com.seastore.auto.seaats.client;
         public final class HumanizedSellRuntime {
            public static boolean isBusy() { return false; }
         }
         """);

      sources.put("dev/autoominous/RefillChecks.java", CHECKS);

      List<String> compiler = new ArrayList<>(List.of("-encoding", "UTF-8", "-d", temporary.toString()));
      for (var source : sources.entrySet()) {
         Path path = temporary.resolve(source.getKey());
         Files.createDirectories(path.getParent());
         Files.writeString(path, source.getValue());
         compiler.add(path.toString());
      }
      compiler.add(root.resolve("HotbarRefillRules.java").toString());
      compiler.add(root.resolve("HotbarRefillRuntime.java").toString());

      if (ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err, compiler.toArray(String[]::new)) != 0) {
         throw new AssertionError("Fixture compilation failed for HotbarRefillRulesChecks");
      }

      int result = new ProcessBuilder(
         Path.of(System.getProperty("java.home"), "bin/java.exe").toString(),
         "-cp", temporary.toString(),
         "dev.autoominous.RefillChecks"
      ).inheritIO().start().waitFor();

      if (result != 0) {
         throw new AssertionError("Hotbar refill checks failed");
      }
   }

   private static final String CHECKS = """
      package dev.autoominous;

      import net.minecraft.*;
      import java.util.*;

      public class RefillChecks {
         static int checks = 0;
         static void check(boolean cond, String msg) {
            checks++;
            if (!cond) throw new AssertionError("Check failed: " + msg);
         }

         static class_310 setupClient() {
            class_310 c = new class_310();
            class_310.instance = c;
            c.field_1724 = new class_310.Player();
            c.field_1761 = new class_310.Interaction();
            c.field_1755 = null;
            class_1703 handler = new class_1703();
            c.field_1724.field_7512 = handler;
            // Slots 0-8: hotbar (indices 0-8, ids 36-44)
            for (int i=0; i<9; i++) handler.field_7761.add(new class_1735(c.field_1724.inventory, i, 36+i));
            // Slots 9-35: bag (indices 9-35, ids 9-35)
            for (int i=9; i<36; i++) handler.field_7761.add(new class_1735(c.field_1724.inventory, i, i));
            HotbarRefillRuntime.forceReset();
            MasterAutomation.enabled = true;
            ProtectedItemAutoUseRuntime.released = false;
            ProtectedItemAutoUseRuntime.rearmed = false;
            DeathRecoveryRuntime.pending = false;
            SwordRecoveryRuntime.active = false;
            return c;
         }

         public static void main(String[] args) throws Exception {
            // 1. Scoring checks
            class_1799 diaLoot3 = new class_1799(class_1802.field_8802, 1, 3);
            class_1799 nethLoot3 = new class_1799(class_1802.field_22022, 1, 3);
            class_1799 ironLoot3 = new class_1799(class_1802.field_8371, 1, 3);
            class_1799 goldLoot3 = new class_1799(class_1802.field_8845, 1, 3);
            class_1799 stoneLoot3 = new class_1799(class_1802.field_8528, 1, 3);
            class_1799 woodLoot3 = new class_1799(class_1802.field_8091, 1, 3);
            class_1799 copperLoot3 = new class_1799(class_1802.field_61338, 1, 3);

            class_1799 diaNoLoot = new class_1799(class_1802.field_8802, 1, 0);
            class_1799 nethNoLoot = new class_1799(class_1802.field_22022, 1, 0);
            class_1799 ironNoLoot = new class_1799(class_1802.field_8371, 1, 0);
            class_1799 woodNoLoot = new class_1799(class_1802.field_8091, 1, 0);
            class_1799 dirt = new class_1799(class_1802.DIRT, 64);
            class_1799 empty = class_1799.empty();

            check(HotbarRefillRules.getSwordScore(diaLoot3) == 100, "Diamond + Looting III = 100");
            check(HotbarRefillRules.getSwordScore(nethLoot3) == 100, "Netherite + Looting III = 100");
            check(HotbarRefillRules.getSwordScore(ironLoot3) == 80, "Iron + Looting III = 80");
            check(HotbarRefillRules.getSwordScore(goldLoot3) == 80, "Gold + Looting III = 80");
            check(HotbarRefillRules.getSwordScore(stoneLoot3) == 80, "Stone + Looting III = 80");
            check(HotbarRefillRules.getSwordScore(woodLoot3) == 80, "Wood + Looting III = 80");
            check(HotbarRefillRules.getSwordScore(copperLoot3) == 80, "Copper + Looting III = 80");
            check(HotbarRefillRules.getSwordScore(diaNoLoot) == 60, "Diamond without Looting = 60");
            check(HotbarRefillRules.getSwordScore(nethNoLoot) == 60, "Netherite without Looting = 60");
            check(HotbarRefillRules.getSwordScore(ironNoLoot) == 20, "Iron without Looting = 20");
            check(HotbarRefillRules.getSwordScore(woodNoLoot) == 20, "Wood without Looting = 20");
            check(HotbarRefillRules.getSwordScore(dirt) == 0, "Dirt is not a sword = 0");
            check(HotbarRefillRules.getSwordScore(empty) == 0, "Empty stack = 0");

            // 2. Type recognition
            check(HotbarRefillRules.isSword(diaLoot3), "Diamond sword is recognized");
            check(HotbarRefillRules.isDiamondOrNetherite(diaLoot3), "Diamond is diamond/netherite");
            check(HotbarRefillRules.isDiamondOrNetherite(nethNoLoot), "Netherite is diamond/netherite");
            check(!HotbarRefillRules.isDiamondOrNetherite(ironLoot3), "Iron is not diamond/netherite");
            check(HotbarRefillRules.hasLootingThree(diaLoot3), "Looting III detected");
            check(!HotbarRefillRules.hasLootingThree(diaNoLoot), "No looting detected");

            class_1799 ominous = new class_1799(class_1802.field_50140, 1);
            class_1799 firework = new class_1799(class_1802.field_8639, 64);
            check(HotbarRefillRules.isOminous(ominous), "Ominous bottle recognized");
            check(!HotbarRefillRules.isOminous(dirt), "Dirt is not ominous");
            check(HotbarRefillRules.isFirework(firework), "Firework rocket recognized");
            check(!HotbarRefillRules.isFirework(ominous), "Ominous is not firework");

            // 3. Inventory rules testing
            class_1661 inv = new class_1661();
            inv.set(0, diaLoot3);
            inv.set(1, ominous);
            inv.set(2, firework);
            check(!HotbarRefillRules.hasAnyRefillNeed(inv), "All 3 hotbar slots optimal -> no refill needed");

            // Missing sword, bag has iron and diamond looting 3
            inv.set(0, dirt);
            inv.set(10, ironNoLoot);
            inv.set(15, diaLoot3);
            check(HotbarRefillRules.hasAnyRefillNeed(inv), "Missing sword in hotbar 0 -> refill needed");
            int bestSlot = HotbarRefillRules.findBestSwordBagSlot(inv, 0, -1);
            check(bestSlot == 15, "Picks diamond looting 3 (slot 15) over iron (slot 10)");

            // Hotbar 0 has iron sword (20), bag has diamond looting 3 (100) -> upgrades!
            inv.set(0, ironNoLoot);
            check(HotbarRefillRules.hasAnyRefillNeed(inv), "Inferior sword in hotbar -> upgrade needed");
            check(HotbarRefillRules.findBestSwordBagSlot(inv, 20, -1) == 15, "Finds upgrade candidate");

            // Hotbar 0 has diamond looting 3 (100), bag has diamond looting 3 (100) -> no swap!
            inv.set(0, diaLoot3);
            check(HotbarRefillRules.findBestSwordBagSlot(inv, 100, -1) == -1, "Equal best sword in bag does not trigger swap");

            // Ominous bottle selection
            inv.set(1, dirt);
            inv.set(12, ominous);
            inv.set(18, ominous);
            check(HotbarRefillRules.hasAnyRefillNeed(inv), "Hotbar 1 missing ominous -> refill needed");
            check(HotbarRefillRules.findFirstOminousBagSlot(inv, -1, -1) == 12, "Finds first ominous in bag");
            check(HotbarRefillRules.findFirstOminousBagSlot(inv, 12, -1) == 18, "Skips excluded slot 12");

            // Firework selection
            inv.set(1, ominous);
            inv.set(2, dirt);
            inv.set(20, firework);
            check(HotbarRefillRules.hasAnyRefillNeed(inv), "Hotbar 2 missing firework -> refill needed");
            check(HotbarRefillRules.findFirstFireworkBagSlot(inv, -1, -1) == 20, "Finds firework in bag");

            // Missing item in hotbar but bag has none -> no refill needed!
            inv.set(2, dirt);
            inv.set(20, empty);
            check(!HotbarRefillRules.hasAnyRefillNeed(inv), "Hotbar 2 missing firework but bag has none -> do not refill");

            // 4. Full 2-step simulation of HotbarRefillRuntime
            class_310 client = setupClient();
            client.field_1724.inventory.set(0, dirt);
            client.field_1724.inventory.set(1, dirt);
            client.field_1724.inventory.set(2, dirt);
            client.field_1724.inventory.set(10, ironNoLoot);
            client.field_1724.inventory.set(15, diaLoot3);
            client.field_1724.inventory.set(20, ominous);
            client.field_1724.inventory.set(25, firework);

            // Tick 1: Step 1 (Mở túi đồ)
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.AWAIT_SYNC, "State transitioned to AWAIT_SYNC");
            check(client.field_1755 instanceof class_490, "InventoryScreen opened");
            check(ProtectedItemAutoUseRuntime.released, "Mouse right-click released");

            // Immediate tick (<50ms): Should wait for sync
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.AWAIT_SYNC, "Still awaiting sync < 50ms");
            check(client.field_1761.clicks.isEmpty(), "No clicks performed yet");

            // Wait 55ms: Tick 2: Step 2 (Tráo đồ rồi đóng)
            Thread.sleep(55L);
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "State returned to IDLE");
            check(client.field_1755 == null, "InventoryScreen closed immediately");
            check(ProtectedItemAutoUseRuntime.rearmed, "AutoUse re-armed");
            check(client.field_1761.clicks.size() == 3, "Sent 3 swap clicks for sword, ominous, firework");

            // Verify click arguments
            var click0 = client.field_1761.clicks.get(0);
            var click1 = client.field_1761.clicks.get(1);
            var click2 = client.field_1761.clicks.get(2);
            check(click0.action() == class_1713.field_7791 && click0.button() == 0 && click0.slot() == 15, "Sword swap to button 0 from slot 15");
            check(click1.action() == class_1713.field_7791 && click1.button() == 1 && click1.slot() == 20, "Ominous swap to button 1 from slot 20");
            check(click2.action() == class_1713.field_7791 && click2.button() == 2 && click2.slot() == 25, "Firework swap to button 2 from slot 25");

            // Verify items in player inventory
            check(HotbarRefillRules.getSwordScore(client.field_1724.inventory.method_5438(0)) == 100, "Hotbar 0 now has Diamond Looting 3");
            check(HotbarRefillRules.isOminous(client.field_1724.inventory.method_5438(1)), "Hotbar 1 now has Ominous Bottle");
            check(HotbarRefillRules.isFirework(client.field_1724.inventory.method_5438(2)), "Hotbar 2 now has Firework");

            // Verify original dirt from hotbar 0, 1, 2 went into bag slots 15, 20, 25 (no items lost)
            check(client.field_1724.inventory.method_5438(15).item == class_1802.DIRT, "Bag slot 15 got old hotbar item");
            check(client.field_1724.inventory.method_5438(20).item == class_1802.DIRT, "Bag slot 20 got old hotbar item");
            check(client.field_1724.inventory.method_5438(25).item == class_1802.DIRT, "Bag slot 25 got old hotbar item");

            // Next tick within 400ms: Cooldown throttles
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "Idle during cooldown");

            // 5. Safety gates
            MasterAutomation.enabled = false;
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "Disabled when MasterAutomation is OFF");
            MasterAutomation.enabled = true;

            DeathRecoveryRuntime.pending = true;
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "Disabled when DeathRecovery is pending");
            DeathRecoveryRuntime.pending = false;

            SwordRecoveryRuntime.active = true;
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "Disabled when SwordRecovery is active");
            SwordRecoveryRuntime.active = false;

            // Chest screen open: Must not touch!
            client.field_1755 = new class_437();
            HotbarRefillRuntime.onTick();
            check(HotbarRefillRuntime.getState() == HotbarRefillRuntime.State.IDLE, "Disabled when another screen is open");

            System.out.println("Hotbar refill rules: " + checks + " checks passed.");
         }
      }
      """;
}
