package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame;

@Environment(EnvType.CLIENT)
public final class ChatMoneyListener {
   private static final Pattern MONEY_PATTERN = Pattern.compile("(?i)\\$\\s*([0-9][0-9,.]*)\\s*([KMB])?");
   private static final Pattern SELL_WORD = Pattern.compile("(?:^|[^\\p{L}])(sold|sell|bán)(?:[^\\p{L}]|$)", 66);
   private static final Pattern AMOUNT_ONLY = Pattern.compile("(?i)\\$\\s*[0-9][0-9,.]*\\s*[KMB]?");

   private ChatMoneyListener() {
   }

   public static void register() {
      ClientReceiveMessageEvents.ALLOW_GAME.register((AllowGame)(var0, var1) -> {
         if (AutoSellLogic.handlePayConfirmation(var0.getString())) {
            return true;
         } else if (AutoSellLogic.handlePayBalanceMessage(var0.getString())) {
            return true;
         } else {
            recordIfConfirmedSell(var0.getString());
            return true;
         }
      });
   }

   static boolean recordIfConfirmedSell(String var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = normalize(var0);
         if (!isSellReceipt(var1)) {
            return false;
         } else {
            double var2 = parseMoney(var1);
            if (!(var2 > 0.0) || !Double.isFinite(var2)) {
               return false;
            } else if (!AutoSellLogic.consumeExpectedSellReceipt()) {
               return false;
            } else {
               Config.totalMoney += var2;
               Config.totalSellCount++;
               Config.lastMoneyReceivedMs = System.currentTimeMillis();
               System.out.println("[SeaATS] Confirmed /sell income: " + var2 + " from: " + var0);
               return true;
            }
         }
      }
   }

   private static boolean isSellReceipt(String var0) {
      return AMOUNT_ONLY.matcher(var0).matches() || SELL_WORD.matcher(var0).find();
   }

   static String normalize(String var0) {
      return Normalizer.normalize(var0, Form.NFKC).replaceAll("(?i)§[0-9a-fk-orx]", "").replaceAll("\\p{Cf}", "").strip();
   }

   static double parseMoney(String var0) {
      Matcher var1 = MONEY_PATTERN.matcher(var0);
      if (!var1.find()) {
         return 0.0;
      } else {
         String var2 = var1.group(1);
         String var3 = var1.group(2);
         if (var3 != null) {
            if (var2.indexOf(46) >= 0) {
               var2 = var2.replace(",", "");
            } else if (var2.indexOf(44) >= 0) {
               var2 = var2.replace(',', '.');
            }
         } else {
            var2 = var2.replace(",", "");
         }

         try {
            double var4 = Double.parseDouble(var2);
            if (var3 != null) {
               String var6 = var3.toUpperCase(Locale.ROOT);
               switch (var6) {
                  case "K":
                     var4 *= 1000.0;
                     break;
                  case "M":
                     var4 *= 1000000.0;
                     break;
                  case "B":
                     var4 *= 1.0E9;
               }
            }

            return var4;
         } catch (NumberFormatException var8) {
            return 0.0;
         }
      }
   }
}
