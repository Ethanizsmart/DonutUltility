package com.seastore.auto.seaats.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record PayReceipt(String recipient, BigDecimal value, int unitPower, String displayAmount) {
   private static final String NUMBER = "(?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]+)?";
   private static final Pattern REPLY = Pattern.compile(
      "^You\\s+paid\\s+(\\S+)\\s+\\$\\s*((?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]+)?)\\s*([KMB])?\\s*[.!]?$", 2
   );

   static PayReceipt parse(String var0) {
      if (var0 == null) {
         return null;
      } else {
         Matcher var1 = REPLY.matcher(ChatMoneyListener.normalize(var0));
         if (!var1.matches()) {
            return null;
         } else {
            BigDecimal var2 = new BigDecimal(var1.group(2).replace(",", ""));
            if (var2.signum() <= 0) {
               return null;
            } else {
               String var3 = var1.group(3) == null ? "" : var1.group(3).toUpperCase(Locale.ROOT);

               byte var4 = switch (var3) {
                  case "K" -> 3;
                  case "M" -> 6;
                  case "B" -> 9;
                  default -> 0;
               };
               return new PayReceipt(var1.group(1), var2, var4, "$" + var1.group(2) + var3);
            }
         }
      }
   }

   boolean matches(String var1, BigDecimal var2) {
      if (!this.recipient.equalsIgnoreCase(var1) || var2 == null) {
         return false;
      } else if (this.unitPower == 0) {
         return this.value.compareTo(var2) == 0;
      } else {
         BigDecimal var3 = var2.movePointLeft(this.unitPower);
         return var3.setScale(this.value.scale(), RoundingMode.HALF_UP).compareTo(this.value) == 0
            || var3.setScale(this.value.scale(), RoundingMode.DOWN).compareTo(this.value) == 0;
      }
   }

   static boolean isInvalidNumber(String var0) {
      return var0 != null && ChatMoneyListener.normalize(var0).equalsIgnoreCase("That number is invalid.");
   }
}
