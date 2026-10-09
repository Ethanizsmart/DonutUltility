package dev.autoominous;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Text rules kept separate from Minecraft so auction metadata can be verified offline. */
final class AuctionSwordRules {
   static final long MAX_PRICE = 5_000_000L;
   private static final Pattern PRICE = Pattern.compile(
      "(?i)(?:price|cost|buy for|purchase for)\\s*:?\\s*\\$?\\s*([0-9][0-9,.]*)(?:\\s*([kmb]))?(?:\\s*(?:coins?|dollars?))?\\s*$"
   );
   private static final Pattern MONEY_ONLY = Pattern.compile("(?i)^\\$\\s*([0-9][0-9,.]*)(?:\\s*([kmb]))?\\s*$");

   private AuctionSwordRules() {}

   static String normalize(String text) {
      return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
         .replaceAll("(?i)§[0-9a-fk-orx]", "").replaceAll("\\p{Cf}", "")
         .replace('\u00a0', ' ').trim().toLowerCase(Locale.ROOT);
   }

   static OptionalLong price(List<String> lines) {
      Long found = null;
      for (String raw : lines) {
         String line = normalize(raw);
         Matcher matcher = PRICE.matcher(line);
         if (!matcher.find()) {
            matcher = MONEY_ONLY.matcher(line);
            if (!matcher.matches()) continue;
         }
         OptionalLong parsed = amount(matcher.group(1), matcher.group(2));
         if (parsed.isEmpty()) return OptionalLong.empty();
         long value = parsed.getAsLong();
         if (found != null && found != value) return OptionalLong.empty();
         found = value;
      }
      return found == null ? OptionalLong.empty() : OptionalLong.of(found);
   }

   private static OptionalLong amount(String number, String suffix) {
      try {
         // Commas are accepted only as groups of three digits, never guessed as a decimal separator.
         if (number.contains(",") && !number.matches("[0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]+)?")) {
            return OptionalLong.empty();
         }
         BigDecimal value = new BigDecimal(number.replace(",", ""));
         if (suffix != null) {
            value = value.multiply(BigDecimal.valueOf(switch (suffix.toLowerCase(Locale.ROOT)) {
               case "k" -> 1_000L;
               case "m" -> 1_000_000L;
               case "b" -> 1_000_000_000L;
               default -> throw new IllegalArgumentException("Unknown suffix");
            }));
         }
         long result = value.longValueExact();
         return result > 0 ? OptionalLong.of(result) : OptionalLong.empty();
      } catch (ArithmeticException | IllegalArgumentException ex) {
         return OptionalLong.empty();
      }
   }

   static boolean affordable(long price) {
      return price > 0 && price <= MAX_PRICE;
   }

   enum FilterMode { LOWEST_PRICE, HIGHEST_PRICE, RECENTLY_LISTED, UNKNOWN }
   record ColoredRun(String text, int rgb) {}
   record FilterOption(FilterMode mode, int brightness) {}

   static FilterOption filterOption(List<ColoredRun> runs) {
      String text = runs.stream().map(ColoredRun::text).collect(java.util.stream.Collectors.joining())
         .toLowerCase(Locale.ROOT);
      String[] labels = {"lowest price", "highest price", "recently listed"};
      FilterMode[] modes = {FilterMode.LOWEST_PRICE, FilterMode.HIGHEST_PRICE, FilterMode.RECENTLY_LISTED};
      int labelStart = -1;
      int labelEnd = -1;
      FilterMode mode = FilterMode.UNKNOWN;
      for (int i = 0; i < labels.length; i++) {
         int index = text.indexOf(labels[i]);
         if (index < 0) continue;
         if (mode != FilterMode.UNKNOWN || text.contains("click") || text.contains("next:")) {
            return new FilterOption(FilterMode.UNKNOWN, 0);
         }
         mode = modes[i];
         labelStart = index;
         labelEnd = index + labels[i].length();
      }
      if (mode == FilterMode.UNKNOWN) return new FilterOption(mode, 0);
      int offset = 0;
      int total = 0;
      int count = 0;
      for (ColoredRun run : runs) {
         int rgb = run.rgb();
         int brightness = (299 * ((rgb >> 16) & 255) + 587 * ((rgb >> 8) & 255) + 114 * (rgb & 255)) / 1000;
         for (int i = 0; i < run.text().length(); i++) {
            if (offset + i >= labelStart && offset + i < labelEnd && !Character.isWhitespace(run.text().charAt(i))) {
               total += brightness;
               count++;
            }
         }
         offset += run.text().length();
      }
      return new FilterOption(mode, count == 0 ? 0 : total / count);
   }

   static FilterMode selectedFilter(List<FilterOption> options) {
      FilterOption brightest = null;
      int secondBrightness = -1;
      int count = 0;
      for (FilterOption option : options) {
         if (option.mode() == FilterMode.UNKNOWN) continue;
         count++;
         if (brightest == null || option.brightness() > brightest.brightness()) {
            if (brightest != null) secondBrightness = Math.max(secondBrightness, brightest.brightness());
            brightest = option;
         } else {
            secondBrightness = Math.max(secondBrightness, option.brightness());
         }
      }
      // In this menu the active label is white/light gray; inactive labels are dark gray.
      // Require a clear difference. Unknown or equally colored options must never cause a click.
      return count >= 2 && brightest.brightness() >= 140 && brightest.brightness() - secondBrightness >= 24
         ? brightest.mode() : FilterMode.UNKNOWN;
   }

   static boolean confirmationTitle(String title) {
      String text = normalize(title);
      return text.contains("confirm purchase") || text.contains("purchase confirmation")
         || text.contains("confirm buy") || text.contains("confirm auction")
         || text.equals("are you sure you want to buy this?");
   }

   static boolean confirmationPriceMatches(long selectedPrice, List<String> lines) {
      OptionalLong displayedPrice = price(lines);
      return affordable(selectedPrice) && displayedPrice.isPresent()
         && displayedPrice.getAsLong() == selectedPrice && affordable(displayedPrice.getAsLong());
   }

   static boolean buyButton(String name) {
      String text = normalize(name);
      return text.equals("confirm") || text.equals("confirm purchase") || text.equals("purchase")
         || text.equals("buy") || text.equals("buy item") || text.equals("confirm buy");
   }
}
