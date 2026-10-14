package dev.autoominous;

import java.util.List;

/** Offline regression checks for the auction spending boundary and active-filter detection. */
public final class AuctionSwordRulesChecks {
   private static int checks;

   public static void main(String[] args) {
      MissingSwordTimer timer = new MissingSwordTimer();
      check(!timer.update(true, 1000L), "Missing sword starts observation");
      check(!timer.update(true, 15999L), "Do not buy before fifteen seconds");
      check(timer.update(true, 16000L), "Buy at fifteen seconds of continuous absence");
      check(!timer.update(false, 16100L), "Sword arriving cancels observation");
      check(!timer.update(true, 16200L), "New absence starts fresh");
      timer.reset();
      check(!timer.update(true, 20000L), "Death/disconnect/toggle reset requires fresh window");
      check(!timer.update(true, 19000L), "Clock rollback restarts observation");
      check(timer.update(true, 34000L), "Fresh window completes after rollback");
      expectPrice(5_000_000L, "Price: $5,000,000");
      expectPrice(5_000_000L, "§aPrice: §f$5M");
      expectPrice(4_750_000L, "Price: $4.75M");
      expectPrice(950_000L, "Cost: $950K");
      expectPrice(1_200_000_000L, "Buy for: $1.2B");
      expectPrice(3_000_000L, "$3,000,000");
      expectPrice(4_000_000L, "Diamond Sword", "Seller: player123", "Price: $4M", "Expires: 12 hours");
      check(AuctionSwordRules.affordable(5_000_000L), "5M is inclusive");
      check(!AuctionSwordRules.affordable(5_000_001L), "Reject just over 5M");
      check(!AuctionSwordRules.affordable(0L), "Reject zero/missing price");
      check(!AuctionSwordRules.affordable(-1L), "Reject negative price");
      rejectPrice("Price: $5,1M");
      rejectPrice("Price: $5.000.000");
      rejectPrice("Price: $0");
      rejectPrice("Price: $-5M");
      rejectPrice("Price: $1.5");
      rejectPrice("Price: $9999999999999999999999999B");
      rejectPrice("Seller: player123", "Looting III", "Expires: 5 minutes");
      rejectPrice("Price: $4M", "Cost: $6M");
      expectPrice(4_000_000L, "Price: $4M", "Cost: $4M");
      selected(AuctionSwordRules.FilterMode.RECENTLY_LISTED, 0x555555, 0x555555, 0xFFFFFF);
      selected(AuctionSwordRules.FilterMode.HIGHEST_PRICE, 0x555555, 0xFFFFFF, 0x555555);
      selected(AuctionSwordRules.FilterMode.LOWEST_PRICE, 0xFFFFFF, 0x555555, 0x555555);
      selected(AuctionSwordRules.FilterMode.LOWEST_PRICE, 0xAAAAAA, 0x555555, 0x555555);
      selected(AuctionSwordRules.FilterMode.LOWEST_PRICE, 0xDDEEFF, 0x444444, 0x555555);
      selected(AuctionSwordRules.FilterMode.UNKNOWN, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF);
      selected(AuctionSwordRules.FilterMode.UNKNOWN, 0x555555, 0x555555, 0x555555);
      selected(AuctionSwordRules.FilterMode.UNKNOWN, 0xFFFFFF, 0xFFFFFF, 0x555555);
      selected(AuctionSwordRules.FilterMode.UNKNOWN, 0xAAAAAA, 0xA0A0A0, 0x555555);
      var splitLabel = AuctionSwordRules.filterOption(List.of(
         new AuctionSwordRules.ColoredRun("▪ ", 0x555555),
         new AuctionSwordRules.ColoredRun("Lowest ", 0xFFFFFF),
         new AuctionSwordRules.ColoredRun("Price", 0xFFFFFF)));
      check(splitLabel.mode() == AuctionSwordRules.FilterMode.LOWEST_PRICE && splitLabel.brightness() == 255,
         "Read the label's color across nested text, not its dim bullet");
      var brightBullet = AuctionSwordRules.filterOption(List.of(
         new AuctionSwordRules.ColoredRun("▪ ", 0xFFFFFF),
         new AuctionSwordRules.ColoredRun("Lowest Price", 0x555555)));
      check(brightBullet.brightness() == 85, "Bright bullet must not make a dark option selected");
      check(AuctionSwordRules.filterOption(List.of(new AuctionSwordRules.ColoredRun("Click to change", 0xFFFFFF)))
         .mode() == AuctionSwordRules.FilterMode.UNKNOWN, "Ignore filter instructions");
      check(AuctionSwordRules.selectedFilter(List.of(splitLabel)) == AuctionSwordRules.FilterMode.UNKNOWN,
         "Do not guess from incomplete menu contents");
      // Simulate cycling the real menu: Recently Listed -> Highest Price -> Lowest Price.
      // A click is needed only for the first two frames; repeated Lowest Price frames stay selected.
      int clicks = 0;
      for (int active : new int[]{2, 1, 0, 0, 0}) {
         var mode = filterMode(active == 0 ? 0xFFFFFF : 0x555555,
            active == 1 ? 0xFFFFFF : 0x555555, active == 2 ? 0xFFFFFF : 0x555555);
         if (mode == AuctionSwordRules.FilterMode.HIGHEST_PRICE || mode == AuctionSwordRules.FilterMode.RECENTLY_LISTED) clicks++;
      }
      check(clicks == 2, "Stop cycling as soon as Lowest Price is bright");
      check(AuctionSwordRules.confirmationTitle("Confirm Purchase"), "Recognize purchase menu");
      check(!AuctionSwordRules.confirmationTitle("Confirm Bounty"), "Do not buy in bounty menu");
      check(AuctionSwordRules.buyButton("§aConfirm Purchase"), "Recognize confirm button");
      check(!AuctionSwordRules.buyButton("Cancel"), "Never click cancellation as purchase");
      check(AuctionSwordRules.confirmationTitle("Are you sure you want to buy this?"), "Recognize Yes/No purchase dialog");
      check(!AuctionSwordRules.confirmationTitle("Are you sure you want to delete this?"), "Reject unrelated Yes/No dialog");
      check(AuctionSwordRules.confirmationPriceMatches(1_500_000L, List.of("$ 1.5M")), "Read separate dialog price");
      check(AuctionSwordRules.confirmationPriceMatches(5_000_000L, List.of("$ 5M")), "Allow exact 5M confirmation");
      check(!AuctionSwordRules.confirmationPriceMatches(1_500_000L, List.of("$ 2M")), "Reject changed confirmation price");
      check(!AuctionSwordRules.confirmationPriceMatches(6_000_000L, List.of("$ 6M")), "Reject over-budget confirmation");
      check(!AuctionSwordRules.confirmationPriceMatches(1_500_000L, List.of()), "Wait for missing dialog price");
      check(!AuctionSwordRules.confirmationPriceMatches(1_500_000L, List.of("$ 1.5M", "$ 7M")), "Reject ambiguous dialog price");
      System.out.println("Auction sword rules: " + checks + " checks passed.");
   }

   private static void expectPrice(long expected, String... lines) {
      var value = AuctionSwordRules.price(List.of(lines));
      check(value.isPresent() && value.getAsLong() == expected, "Price: " + List.of(lines));
   }

   private static void rejectPrice(String... lines) {
      check(AuctionSwordRules.price(List.of(lines)).isEmpty(), "Reject price: " + List.of(lines));
   }

   private static AuctionSwordRules.FilterMode filterMode(int lowest, int highest, int recent) {
      return AuctionSwordRules.selectedFilter(List.of(
         AuctionSwordRules.filterOption(List.of(new AuctionSwordRules.ColoredRun("▪ Lowest Price", lowest))),
         AuctionSwordRules.filterOption(List.of(new AuctionSwordRules.ColoredRun("▪ Highest Price", highest))),
         AuctionSwordRules.filterOption(List.of(new AuctionSwordRules.ColoredRun("▪ Recently Listed", recent)))));
   }

   private static void selected(AuctionSwordRules.FilterMode expected, int lowest, int highest, int recent) {
      check(filterMode(lowest, highest, recent) == expected, "Selected filter: " + expected);
   }

   private static void check(boolean condition, String message) {
      checks++;
      if (!condition) throw new AssertionError(message);
   }
}
