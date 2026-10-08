package com.seastore.auto.seaats.client;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PayBalanceReply {
   private static final String NUMBER = "-?(?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]+)?";
   private static final Pattern REPLY = Pattern.compile("^You\\s+have\\s+\\$\\s*(-?(?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]+)?)\\s*[.!]?$", 2);

   private PayBalanceReply() {
   }

   static BigDecimal parse(String var0) {
      if (var0 == null) {
         return null;
      } else {
         Matcher var1 = REPLY.matcher(ChatMoneyListener.normalize(var0));
         return !var1.matches() ? null : new BigDecimal(var1.group(1).replace(",", ""));
      }
   }
}
