package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import dev.autoominous.ProtectedItemAutoUseRuntime;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_465;
import net.minecraft.class_634;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public class AutoSellLogic {
   static int tickDelay = 0;
   private static String lastPayAmountLabel = "";
   private static String payRecipientSnapshot = "";
   static AutoSellLogic.State currentState = AutoSellLogic.State.IDLE;
   private static final Random RANDOM = new Random();
   private static AutoSellLogic.AntiPlayerState antiState = AutoSellLogic.AntiPlayerState.IDLE;
   private static int antiTimer = 0;
   private static class_1657 antiTarget = null;
   private static int pearlThrowCount = 0;
   private static int pearlFailCount = 0;
   private static boolean killCommandSent = false;
   private static boolean confirmClicked = false;
   private static boolean isPaying = false;
   private static long lastPayTimeMs = System.currentTimeMillis();
   private static boolean payCommandSent = false;
   private static boolean wasAutoSellOnBeforePay = false;
   private static boolean isGhostFixing = false;
   private static int ghostTimer = 0;
   private static int ghostStep = 0;
   private static int ghostPearlCount = 0;
   private static long lastSellCommandSentMs = 0L;
   private static int sellCommandRetryCount = 0;
   private static final int MAX_SELL_RETRY = 3;
   private static final long SELL_OPEN_TIMEOUT_NANOS = 8000000000L;
   private static long sellOpenDeadlineNanos;
   private static long sellRetryNotBeforeNanos;
   private static int sellFailureRounds;
   private static class_634 sellConnection;
   private static class_746 sellPlayer;
   private static class_1703 knownSellHandler;
   private static long sellContentsDeadlineNanos;
   private static long lastSellTimeMs = System.currentTimeMillis();
   private static long lastHomeCheckMs = 0L;
   private static boolean isHomeRecovering = false;
   private static boolean toggleHomeCmd = false;
   private static long lastProfitReportMs = System.currentTimeMillis();
   private static class_304 ghostSneakKey;
   private static boolean wasAutoSellOnBeforeAnti;
   private static int expectedSellReceiptTicks;
   private static boolean payAllRequest;
   private static BigDecimal payBalance;
   private static long payBalanceDeadlineNanos;
   private static class_634 payConnection;
   private static BigDecimal payExpectedAmount;
   private static long payConfirmationDeadlineNanos;
   private static PayReceipt payReceipt;
   private static String payFailure;

   public static void resetState() {
      releaseGhostSneak();
      resetSellControl();
      sellConnection = null;
      sellPlayer = null;
      SellCycleScheduler.reset();
      currentState = AutoSellLogic.State.IDLE;
      tickDelay = 0;
      antiState = AutoSellLogic.AntiPlayerState.IDLE;
      antiTimer = 0;
      antiTarget = null;
      wasAutoSellOnBeforeAnti = false;
      expectedSellReceiptTicks = 0;
      lastPayAmountLabel = "";
      clearPayBalanceRequest();
      pearlThrowCount = 0;
      pearlFailCount = 0;
      killCommandSent = false;
      confirmClicked = false;
      isPaying = false;
      payCommandSent = false;
      wasAutoSellOnBeforePay = false;
      isGhostFixing = false;
      ghostTimer = 0;
      ghostStep = 0;
      ghostPearlCount = 0;
      lastSellCommandSentMs = 0L;
      sellCommandRetryCount = 0;
      isHomeRecovering = false;
      lastHomeCheckMs = 0L;
      toggleHomeCmd = false;
      Config.lastMoneyReceivedMs = System.currentTimeMillis();
   }

   public static boolean hasInventoryItems(class_310 var0) {
      if (var0.field_1724 == null) {
         return false;
      } else {
         class_1661 var1 = var0.field_1724.method_31548();

         for (int var2 = 0; var2 < 36; var2++) {
            if (isSellableInventoryItem(var1.method_5438(var2), var2)) {
               return true;
            }
         }

         return false;
      }
   }

   static boolean isSellGuiOpen(class_310 var0) {
      if (var0.field_1755 instanceof class_465 var2) {
         class_1703 var3 = var2.method_17577();
         if (var0.field_1724 != null && var3 == var0.field_1724.field_7512) {
            if (var3 == knownSellHandler) {
               return true;
            } else {
               int var4 = var3.field_7761.size() - 36;
               if (var4 < 0) {
                  var4 = var3.field_7761.size();
               }

               for (int var5 = 0; var5 < var4; var5++) {
                  if (var3.method_7611(var5).method_7677().method_31574(class_1802.field_8581)) {
                     knownSellHandler = var3;
                     return true;
                  }
               }

               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static List<class_1657> getNearbyPlayers(class_310 var0, double var1) {
      return var0.field_1687
         .method_8390(class_1657.class, var0.field_1724.method_5829().method_1014(var1), var1x -> var1x != var0.field_1724 && !var1x.method_7325());
   }

   private static boolean isFriend(String var0) {
      if (Config.friendList == null) {
         return false;
      } else {
         for (String var2 : Config.friendList) {
            if (var2.trim().equalsIgnoreCase(var0.trim())) {
               return true;
            }
         }

         return false;
      }
   }

   private static void sendSellCommand(class_310 var0) {
      ProtectedItemAutoUseRuntime.forceRelease();
      if (var0.method_1562() != null && System.nanoTime() - sellRetryNotBeforeNanos >= 0L) {
         if (!hasActiveMenu(var0) && !(var0.field_1755 instanceof class_465)) {
            currentState = AutoSellLogic.State.WAITING_GUI_OPEN;
            tickDelay = 0;
            lastSellCommandSentMs = System.currentTimeMillis();
            sellOpenDeadlineNanos = System.nanoTime() + 8000000000L;
            sellCommandRetryCount++;

            try {
               ChatCommandSender.send("sell");
               System.out.println("[SeaATS] Requested /sell via chat; attempt=" + sellCommandRetryCount + ", waiting up to 8s for menu contents");
            } catch (RuntimeException var2) {
               scheduleSellRetry(var0, "command submission failed: " + var2.getClass().getSimpleName());
            }
         } else {
            closeAllGui(var0);
            sellRetryNotBeforeNanos = System.nanoTime() + 500000000L;
         }
      }
   }

   static boolean hasActiveMenu(class_310 var0) {
      return var0.field_1724 != null && var0.field_1724.field_7512 != var0.field_1724.field_7498;
   }

   static void closeAllGui(class_310 var0) {
      knownSellHandler = null;
      sellContentsDeadlineNanos = 0L;
      if (hasActiveMenu(var0)) {
         int var1 = var0.field_1724.field_7512.field_7763;
         var0.field_1724.method_7346();
         System.out.println("[SeaATS] Closed active server menu; syncId=" + var1);
      }

      if (var0.field_1755 != null) {
         var0.method_1507(null);
      }
   }

   private static void scheduleSellRetry(class_310 var0, String var1) {
      int var2 = var0.field_1724.field_7512.field_7763;
      int var3 = var0.field_1755 instanceof class_465 var4 ? var4.method_17577().field_7763 : -1;
      String var7 = var0.field_1755 == null ? "none" : var0.field_1755.getClass().getSimpleName();
      System.out
         .println(
            "[SeaATS] Sell GUI recovery: "
               + var1
               + ", attempt="
               + sellCommandRetryCount
               + ", screen="
               + var7
               + ", activeSyncId="
               + var2
               + ", visibleSyncId="
               + var3
         );
      closeAllGui(var0);
      currentState = AutoSellLogic.State.IDLE;
      tickDelay = 0;
      sellOpenDeadlineNanos = 0L;
      long var8 = 2L;
      if (sellCommandRetryCount >= 3) {
         sellFailureRounds++;
         var8 = Math.min(60L, 10L << Math.min(sellFailureRounds - 1, 3));
         sellCommandRetryCount = 0;
         var0.field_1724.method_7353(class_2561.method_43470("§e[SeaATS] GUI sell chưa sẵn sàng. Đã dọn phiên menu; thử lại sau " + var8 + " giây."), false);
      }

      sellRetryNotBeforeNanos = System.nanoTime() + var8 * 1000000000L;
   }

   private static void resetSellControl() {
      currentState = AutoSellLogic.State.IDLE;
      tickDelay = 0;
      lastSellCommandSentMs = 0L;
      sellCommandRetryCount = 0;
      sellOpenDeadlineNanos = 0L;
      sellRetryNotBeforeNanos = 0L;
      sellFailureRounds = 0;
      expectedSellReceiptTicks = 0;
      knownSellHandler = null;
      sellContentsDeadlineNanos = 0L;
   }

   private static boolean waitForSellContents(class_310 var0) {
      if (!isSellGuiOpen(var0)) {
         return false;
      } else {
         class_1703 var1 = ((class_465)var0.field_1755).method_17577();
         int var2 = Math.max(0, var1.field_7761.size() - 36);

         for (int var3 = 0; var3 < var2; var3++) {
            if (var1.method_7611(var3).method_7677().method_31574(class_1802.field_8581)) {
               sellContentsDeadlineNanos = 0L;
               return false;
            }
         }

         if (sellContentsDeadlineNanos == 0L) {
            sellContentsDeadlineNanos = System.nanoTime() + 8000000000L;
         }

         if (System.nanoTime() - sellContentsDeadlineNanos >= 0L) {
            scheduleSellRetry(var0, "confirmed sell menu contents stayed unavailable for 8s");
         }

         return true;
      }
   }

   public static void toggleFromKey(class_310 var0) {
      boolean var1 = !Config.isAutoSellEnabled;
      resetState();
      Config.isAutoSellEnabled = var1;
      closeAllGui(var0);
      sellConnection = var0.method_1562();
      sellPlayer = var0.field_1724;
      sellRetryNotBeforeNanos = System.nanoTime() + 500000000L;
      if (var1) {
         Config.startTimeMs = System.currentTimeMillis();
         Config.totalSellCount = 0;
         Config.totalMoney = 0.0;
      }

      if (var0.field_1724 != null) {
         if (var1) {
            var0.field_1724.method_7353(class_2561.method_43470("§b[SeaATS] §aĐã reset thống kê!"), false);
         }

         var0.field_1724.method_7353(class_2561.method_43470("§b[SeaATS] §fAuto Sell: " + (var1 ? "§aBẬT" : "§cTẮT")), false);
      }
   }

   public static void tick(class_310 var0) {
      if (!dev.autoominous.MasterAutomation.isEnabled()) return;
      if (expectedSellReceiptTicks > 0) {
         expectedSellReceiptTicks--;
      }

      if (var0.field_1724 != null && var0.field_1687 != null && var0.method_1562() != null) {
         if (sellConnection != var0.method_1562() || sellPlayer != var0.field_1724) {
            resetSellControl();
            sellConnection = var0.method_1562();
            sellPlayer = var0.field_1724;
         }

         if (isPaying
            && (
               !Config.enableAutoPay
                  || Config.payAll != payAllRequest
                  || !payRecipientSnapshot.equals(Config.payRecipient.trim())
                  || var0.method_1562() != payConnection
            )) {
            finishAutoPay(
               var0,
               payCommandSent
                  ? "Dừng chờ Auto Pay vì cấu hình hoặc kết nối thay đổi; lệnh đã gửi có thể vẫn được server xử lý."
                  : "Đã hủy lượt Auto Pay vì cấu hình hoặc kết nối thay đổi."
            );
         }

         if (isPaying && payCommandSent) {
            checkPayConfirmation(var0);
         }

         if (isPaying && payAllRequest && !payCommandSent && System.nanoTime() - payBalanceDeadlineNanos >= 0L) {
            finishAutoPay(var0, "Không nhận được số dư /bal hợp lệ trong 10 giây; bỏ qua lượt chuyển.");
         }

         if (!Config.enableHome || !Config.isHomeSet) {
            isHomeRecovering = false;
         }

         if (var0.field_1755 instanceof SeaSettingsScreen) {
            if (isGhostFixing) {
               finishGhostFix(var0);
            }

            Config.lastMoneyReceivedMs = System.currentTimeMillis();
         } else if (var0.field_1755 != null && !(var0.field_1755 instanceof class_465)) {
            if (isGhostFixing) {
               finishGhostFix(var0);
            }

            Config.lastMoneyReceivedMs = System.currentTimeMillis();
         } else {
            SellCycleScheduler.tick(
               Config.isAutoSellEnabled && !isPaying && !isGhostFixing && antiState == AutoSellLogic.AntiPlayerState.IDLE && !isHomeRecovering
            );
            if (var0.field_1724 != null && var0.field_1687 != null) {
               if (Config.enableGhostFix && Config.isAutoSellEnabled && !isGhostFixing) {
                  long var1 = Math.max(1, Config.ghostTimeoutMinutes) * 60L * 1000L;
                  if (System.currentTimeMillis() - Config.lastMoneyReceivedMs > var1) {
                     isGhostFixing = true;
                     ghostTimer = 0;
                     ghostStep = 0;
                     ghostPearlCount = 0;
                     Config.isAutoSellEnabled = false;
                     closeAllGui(var0);
                     ghostSneakKey = var0.field_1690.field_1832;
                     setGhostSneak(false);
                     String var14 = var0.field_1724.method_5477().getString();
                     WebhookHelper.sendAlert("**\ud83e\uddf9 Chống Ghost Item**\n" + var14 + " đang thực hiện chống ghost item...");
                     var0.field_1724
                        .method_7353(class_2561.method_43470("§e[SeaATS] Không nhận được tiền bán. Đang nhấn Shift 2 lần để đồng bộ lại..."), false);
                     System.out.println("[SeaATS] Ghost fix triggered (no money received for " + Config.ghostTimeoutMinutes + " min)");
                     return;
                  }
               }

               if (isGhostFixing) {
                  tickGhostFix(var0);
               } else {
                  if (Config.enableAutoPay && !isPaying && antiState == AutoSellLogic.AntiPlayerState.IDLE) {
                     long var8 = Config.payIntervalMinutes * 60L * 1000L;
                     if (System.currentTimeMillis() - lastPayTimeMs > var8) {
                        startAutoPay(var0);
                     }
                  }

                  if (isPaying) {
                     tickAutoPay(var0);
                  } else {
                     List<class_1657> var9;
                     if (Config.antiPlayerEnabled
                        && antiState == AutoSellLogic.AntiPlayerState.IDLE
                        && !(var9 = getNearbyPlayers(var0, 5.0)).isEmpty()
                        && !var9.stream().anyMatch(var0x -> isFriend(var0x.method_5477().getString()))) {
                        triggerAntiPlayer(var0, (class_1657)var9.get(0));
                     }

                     if (antiState != AutoSellLogic.AntiPlayerState.IDLE) {
                        tickAntiPlayer(var0);
                     } else if (Config.isAutoSellEnabled && isHomeRecovering) {
                        processHomeRecovery(var0);
                     } else if (Config.isAutoSellEnabled
                        && Config.isHomeSet
                        && Config.enableHome
                        && var0.field_1724.method_5649(Config.homeX, Config.homeY, Config.homeZ) > 4.0) {
                        handleMovedAway(var0);
                     } else if (!Config.isAutoSellEnabled) {
                        if (currentState != AutoSellLogic.State.IDLE) {
                           resetSellControl();
                        }
                     } else {
                        if (currentState != AutoSellLogic.State.WAITING_GUI_OPEN && waitForSellContents(var0)) {
                           return;
                        }

                        if (tickDelay > 0) {
                           tickDelay--;
                        } else {
                           if (currentState == AutoSellLogic.State.WAITING_GUI_OPEN) {
                              if (!isSellGuiOpen(var0)) {
                                 if (System.nanoTime() - sellOpenDeadlineNanos >= 0L) {
                                    scheduleSellRetry(var0, "menu/confirm item did not become ready in 8s");
                                 }

                                 return;
                              }

                              sellCommandRetryCount = 0;
                              sellFailureRounds = 0;
                              sellRetryNotBeforeNanos = 0L;
                              sellOpenDeadlineNanos = 0L;
                              if (currentState == AutoSellLogic.State.WAITING_GUI_OPEN) {
                                 currentState = AutoSellLogic.State.MOVING_ITEMS;
                                 tickDelay = 2;
                              }
                           }

                           switch (currentState) {
                              case IDLE:
                                 if (!isSellGuiOpen(var0)) {
                                    if (hasInventoryItems(var0)) {
                                       sendSellCommand(var0);
                                    }
                                 } else {
                                    sellCommandRetryCount = 0;
                                    sellFailureRounds = 0;
                                    sellRetryNotBeforeNanos = 0L;
                                    sellOpenDeadlineNanos = 0L;
                                    currentState = AutoSellLogic.State.MOVING_ITEMS;
                                    tickDelay = 2;
                                 }
                              case WAITING_GUI_OPEN:
                              default:
                                 break;
                              case MOVING_ITEMS:
                                 if (!isSellGuiOpen(var0)) {
                                    currentState = AutoSellLogic.State.IDLE;
                                    tickDelay = 10;
                                    sellCommandRetryCount = 0;
                                 } else {
                                    class_1703 var10 = ((class_465)var0.field_1755).method_17577();
                                    int var13 = var10.field_7761.size() - 36;
                                    if (var13 < 0) {
                                       var13 = var10.field_7761.size();
                                    }

                                    int var15 = 0;
                                    int var17 = 0;

                                    for (; var17 < var13; var17++) {
                                       class_1799 var6 = var10.method_7611(var17).method_7677();
                                       if (!var6.method_7960() && !var6.method_31574(class_1802.field_8581)) {
                                          var15++;
                                       }
                                    }

                                    if (var15 < 45 && var13 - var15 > 1) {
                                       var17 = var10.field_7761.size() - 36;
                                       if (var17 < 0) {
                                          var17 = 0;
                                       }

                                       boolean var19 = false;

                                       for (int var7 = var17; var7 < var10.field_7761.size(); var7++) {
                                          if (isSellableInventorySlot(var0, var10.method_7611(var7))) {
                                             var0.field_1761.method_2906(var10.field_7763, var7, 0, class_1713.field_7794, var0.field_1724);
                                             var19 = true;
                                          }
                                       }

                                       if (var19) {
                                          tickDelay = 2;
                                       } else {
                                          currentState = AutoSellLogic.State.CLICK_CONFIRM;
                                          tickDelay = 3;
                                       }
                                    } else {
                                       currentState = AutoSellLogic.State.CLICK_CONFIRM;
                                       tickDelay = 3;
                                    }
                                 }
                                 break;
                              case CLICK_CONFIRM:
                                 if (!isSellGuiOpen(var0)) {
                                    currentState = AutoSellLogic.State.IDLE;
                                 } else {
                                    class_1703 var2 = ((class_465)var0.field_1755).method_17577();
                                    int var3 = -1;
                                    int var4 = var2.field_7761.size() - 36;
                                    if (var4 < 0) {
                                       var4 = var2.field_7761.size();
                                    }

                                    for (int var5 = 0; var5 < var4; var5++) {
                                       if (var2.method_7611(var5).method_7677().method_31574(class_1802.field_8581)) {
                                          var3 = var5;
                                          break;
                                       }
                                    }

                                    if (var3 != -1) {
                                       expectedSellReceiptTicks = 200;
                                       var0.field_1761.method_2906(var2.field_7763, var3, 0, class_1713.field_7790, var0.field_1724);
                                       lastSellTimeMs = System.currentTimeMillis();
                                       sellCommandRetryCount = 0;
                                       tickDelay = 6;
                                       currentState = AutoSellLogic.State.CLICK_CONFIRM_DELAYED;
                                    } else {
                                       currentState = AutoSellLogic.State.IDLE;
                                       tickDelay = 10;
                                    }
                                 }
                                 break;
                              case CLICK_CONFIRM_DELAYED:
                                 closeAllGui(var0);
                                 currentState = AutoSellLogic.State.IDLE;
                                 tickDelay = 6;
                           }

                           if (Config.isAutoSellEnabled) {
                              long var11 = Config.profitIntervalMinutes * 60L * 1000L;
                              if (System.currentTimeMillis() - lastProfitReportMs > var11) {
                                 lastProfitReportMs = System.currentTimeMillis();
                                 long var16 = System.currentTimeMillis() - Config.startTimeMs;
                                 WebhookHelper.sendReport(buildReport(var0, var16));
                              }
                           }

                           if (Config.isAutoSellEnabled && System.currentTimeMillis() - lastSellTimeMs > Config.noSellIntervalMinutes * 60L * 1000L) {
                              String var12 = var0.field_1724.method_5477().getString();
                              WebhookHelper.sendAlert("**Cảnh báo:** " + var12 + " đã " + Config.noSellIntervalMinutes + " phút không bán được item nào!");
                              lastSellTimeMs = System.currentTimeMillis();
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         if (isPaying) {
            finishAutoPay(var0, "");
         }

         if (isGhostFixing) {
            resetState();
            Config.isAutoSellEnabled = true;
         }

         resetSellControl();
         sellConnection = null;
         sellPlayer = null;
      }
   }

   private static void startAutoPay(class_310 var0) {
      lastPayTimeMs = System.currentTimeMillis();
      clearPayBalanceRequest();
      isPaying = true;
      payCommandSent = false;
      lastPayAmountLabel = "";
      wasAutoSellOnBeforePay = Config.isAutoSellEnabled;
      Config.isAutoSellEnabled = false;
      payAllRequest = Config.payAll;
      payRecipientSnapshot = Config.payRecipient.trim();
      payBalance = null;
      payBalanceDeadlineNanos = 0L;
      payConnection = var0.method_1562();
      closeAllGui(var0);
      if (payRecipientSnapshot.isEmpty() || payRecipientSnapshot.matches(".*\\s.*")) {
         finishAutoPay(var0, "Chưa có tên người nhận hợp lệ; bỏ qua lượt chuyển.");
      } else if (payAllRequest) {
         payBalanceDeadlineNanos = System.nanoTime() + 10000000000L;
         payMessage(var0, "Đang đọc số dư /bal để chuyển toàn bộ money...");

         try {
            ChatCommandSender.send("bal");
         } catch (RuntimeException var7) {
            finishAutoPay(var0, "Không gửi được /bal; bỏ qua lượt chuyển.");
         }
      } else {
         double var1 = Config.payRandomMinMillions;
         double var3 = Config.payRandomMaxMillions;
         if (Double.isFinite(var1) && Double.isFinite(var3) && !(var1 < 0.0) && !(var3 < var1) && !(var3 <= 0.0)) {
            double var5 = var1 == var3 ? var1 : ThreadLocalRandom.current().nextDouble(var1, var3);
            var5 = Math.max(var1, Math.min(var3, Math.round(var5 * 100.0) / 100.0));
            if (var5 <= 0.0) {
               finishAutoPay(var0, "Số tiền được chọn bằng 0; bỏ qua lượt chuyển.");
            } else {
               sendAutoPay(var0, BigDecimal.valueOf(var5).movePointRight(6));
            }
         } else {
            finishAutoPay(var0, "Khoảng tiền ngẫu nhiên không hợp lệ; bỏ qua lượt chuyển.");
         }
      }
   }

   static boolean handlePayBalanceMessage(String var0) {
      BigDecimal var1 = PayBalanceReply.parse(var0);
      if (var1 == null) {
         return false;
      } else {
         class_310 var2 = class_310.method_1551();
         if (isCurrentPayRequest(var2) && payAllRequest && !payCommandSent && payBalance == null && System.nanoTime() - payBalanceDeadlineNanos < 0L) {
            payBalance = var1;
         }

         return true;
      }
   }

   private static void tickAutoPay(class_310 var0) {
      if (payAllRequest && !payCommandSent) {
         if (payBalance == null) {
            return;
         }

         if (payBalance.signum() <= 0) {
            finishAutoPay(var0, "Số dư /bal không lớn hơn 0; bỏ qua lượt chuyển.");
            return;
         }

         sendAutoPay(var0, payBalance);
      }
   }

   private static void sendAutoPay(class_310 var0, BigDecimal var1) {
      if (!payCommandSent && isCurrentPayRequest(var0)) {
         String var2 = var1.stripTrailingZeros().toPlainString();
         payExpectedAmount = var1;
         lastPayAmountLabel = "$" + var2;
         payCommandSent = true;
         payConfirmationDeadlineNanos = System.nanoTime() + 15000000000L;
         String var3 = "pay " + payRecipientSnapshot + " " + var2;
         payMessage(var0, "Đang gửi /" + var3 + " qua chat; chờ server xác nhận...");

         try {
            ChatCommandSender.send(var3);
         } catch (RuntimeException var5) {
            finishAutoPay(var0, "Gửi Auto Pay gặp lỗi; chưa xác nhận chuyển tiền. Không tự gửi lại lượt này.");
         }
      }
   }

   private static boolean isCurrentPayRequest(class_310 var0) {
      return isPaying
         && Config.enableAutoPay
         && Config.payAll == payAllRequest
         && payRecipientSnapshot.equals(Config.payRecipient.trim())
         && var0.field_1724 != null
         && var0.field_1687 != null
         && var0.method_1562() == payConnection;
   }

   static boolean handlePayConfirmation(String var0) {
      PayReceipt var1 = PayReceipt.parse(var0);
      boolean var2 = PayReceipt.isInvalidNumber(var0);
      if (var1 == null && !var2) {
         return false;
      } else {
         class_310 var3 = class_310.method_1551();
         if (isCurrentPayRequest(var3) && payCommandSent && payReceipt == null && payFailure == null && System.nanoTime() - payConfirmationDeadlineNanos < 0L) {
            if (var1 != null && var1.matches(payRecipientSnapshot, payExpectedAmount)) {
               payReceipt = var1;
            } else if (var2) {
               payFailure = "Server từ chối số tiền: That number is invalid.";
            }
         }

         return true;
      }
   }

   private static void checkPayConfirmation(class_310 var0) {
      if (payReceipt != null) {
         String var1 = "Server đã xác nhận chuyển " + payReceipt.displayAmount() + " cho " + payReceipt.recipient() + ".";
         String var2 = String.format(
            "**Auto Pay được server xác nhận**\n**Người gửi:** %s\n**Người nhận:** %s\n**Số tiền yêu cầu:** %s\n**Server xác nhận:** %s\n**Thời gian:** %s",
            var0.field_1724.method_5477().getString(),
            payRecipientSnapshot,
            lastPayAmountLabel,
            payReceipt.displayAmount(),
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
         );
         finishAutoPay(var0, var1);
         WebhookHelper.sendPay(var2);
      } else if (payFailure != null) {
         finishAutoPay(var0, payFailure + " Chưa xác nhận chuyển tiền; không tự gửi lại lượt này.");
      } else if (System.nanoTime() - payConfirmationDeadlineNanos >= 0L) {
         finishAutoPay(var0, "Chưa nhận được xác nhận Auto Pay từ server sau 15 giây. Không tự gửi lại lượt này để tránh chuyển trùng.");
      }
   }

   private static void finishAutoPay(class_310 var0, String var1) {
      if (wasAutoSellOnBeforePay) {
         Config.isAutoSellEnabled = true;
      }

      isPaying = false;
      payCommandSent = false;
      wasAutoSellOnBeforePay = false;
      lastPayTimeMs = System.currentTimeMillis();
      clearPayBalanceRequest();
      currentState = AutoSellLogic.State.IDLE;
      tickDelay = 10;
      lastSellCommandSentMs = 0L;
      sellCommandRetryCount = 0;
      Config.lastMoneyReceivedMs = System.currentTimeMillis();
      if (!var1.isEmpty()) {
         payMessage(var0, var1);
      }
   }

   private static void clearPayBalanceRequest() {
      payAllRequest = false;
      payRecipientSnapshot = "";
      payBalance = null;
      payBalanceDeadlineNanos = 0L;
      payConnection = null;
      payExpectedAmount = null;
      payConfirmationDeadlineNanos = 0L;
      payReceipt = null;
      payFailure = null;
   }

   private static void payMessage(class_310 var0, String var1) {
      if (var0.field_1724 != null) {
         var0.field_1724.method_7353(class_2561.method_43470("§b[SeaATS] §e" + var1), false);
      }
   }

   private static void tickGhostFix(class_310 var0) {
      if (!Config.enableGhostFix) {
         finishGhostFix(var0);
      } else {
         ghostTimer++;
         if (ghostTimer == 5 || ghostTimer == 15) {
            setGhostSneak(true);
         }

         if (ghostTimer == 10 || ghostTimer == 20) {
            setGhostSneak(false);
         }

         if (ghostTimer >= 25) {
            finishGhostFix(var0);
         }
      }
   }

   private static void setGhostSneak(boolean var0) {
      if (ghostSneakKey != null && ghostSneakKey.method_1434() != var0) {
         ghostSneakKey.method_23481(var0);
         if (!var0 && ghostSneakKey.method_1434()) {
            ghostSneakKey.method_23481(true);
         }
      }
   }

   private static void releaseGhostSneak() {
      setGhostSneak(false);
      ghostSneakKey = null;
   }

   private static void finishGhostFix(class_310 var0) {
      releaseGhostSneak();
      resetSellControl();
      isGhostFixing = false;
      ghostTimer = 0;
      ghostStep = 0;
      ghostPearlCount = 0;
      Config.lastMoneyReceivedMs = System.currentTimeMillis();
      currentState = AutoSellLogic.State.IDLE;
      tickDelay = 5;
      lastSellCommandSentMs = 0L;
      sellCommandRetryCount = 0;
      Config.isAutoSellEnabled = true;
      var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Đã đồng bộ lại, tiếp tục Auto Sell."), false);
      System.out.println("[SeaATS] Ghost fix finished: two sneak cycles; auto sell resumed");
   }

   public static String trackerStatus() {
      if (isGhostFixing) {
         return "ĐỒNG BỘ";
      } else if (isPaying) {
         return payAllRequest && !payCommandSent ? "ĐỌC SỐ DƯ" : "CHỜ PAY";
      } else if (antiState != AutoSellLogic.AntiPlayerState.IDLE) {
         return "BẢO VỆ";
      } else if (isHomeRecovering) {
         return "VỀ HOME";
      } else if (!Config.isAutoSellEnabled || currentState != AutoSellLogic.State.WAITING_GUI_OPEN && sellContentsDeadlineNanos == 0L) {
         if (Config.isAutoSellEnabled && System.nanoTime() - sellRetryNotBeforeNanos < 0L) {
            return "THỬ LẠI";
         } else {
            return Config.isAutoSellEnabled ? "ĐANG BÁN" : "ĐÃ TẮT";
         }
      } else {
         return "CHỜ GUI";
      }
   }

   static boolean consumeExpectedSellReceipt() {
      if (expectedSellReceiptTicks <= 0) {
         return false;
      } else {
         expectedSellReceiptTicks = 0;
         return true;
      }
   }

   private static void triggerAntiPlayer(class_310 var0, class_1657 var1) {
      wasAutoSellOnBeforeAnti = Config.isAutoSellEnabled;
      Config.isAutoSellEnabled = false;
      if (var0.field_1755 != null) {
         var0.field_1724.method_7346();
      }

      antiTarget = var1;
      antiState = AutoSellLogic.AntiPlayerState.THROWING_PEARL;
      antiTimer = 0;
      pearlThrowCount = 0;
      pearlFailCount = 0;
      killCommandSent = false;
      confirmClicked = false;
      String var2 = var0.field_1724.method_5477().getString();
      String var3 = var1.method_5477().getString();
      String var4 = String.format("**%s** phát hiện người lạ **%s** trong vòng 5 block! Đang kích hoạt bảo vệ...", var2, var3);
      WebhookHelper.sendAlert(var4);
      var0.field_1724.method_7353(class_2561.method_43470("§c[SeaATS] Phát hiện người lạ! Kích hoạt bảo vệ..."), false);
   }

   private static void tickAntiPlayer(class_310 var0) {
      if (antiState == AutoSellLogic.AntiPlayerState.WAITING_CLEAR) {
         boolean var8 = Config.antiPlayerEnabled && getNearbyPlayers(var0, 5.0).stream().anyMatch(var0x -> !isFriend(var0x.method_5477().getString()));
         if (!var8) {
            antiState = AutoSellLogic.AntiPlayerState.IDLE;
            if (wasAutoSellOnBeforeAnti) {
               Config.isAutoSellEnabled = true;
               currentState = AutoSellLogic.State.IDLE;
               tickDelay = 10;
               sellCommandRetryCount = 0;
               lastSellCommandSentMs = 0L;
               Config.lastMoneyReceivedMs = System.currentTimeMillis();
            }

            wasAutoSellOnBeforeAnti = false;
         }
      } else {
         if (antiTarget == null) {
            antiState = AutoSellLogic.AntiPlayerState.IDLE;
         } else {
            antiTimer++;
            switch (antiState) {
               case THROWING_PEARL:
                  if (antiTimer == 0 || antiTimer == 5) {
                     class_1799 var7 = var0.field_1724.method_31548().method_5438(40);
                     if (var7.method_7909() == class_1802.field_8634) {
                        var0.field_1761.method_2919(var0.field_1724, class_1268.field_5810);
                        class_746 var9 = var0.field_1724;
                        int var11 = ++pearlThrowCount;
                        var9.method_7353(class_2561.method_43470("§a[SeaATS] Đã ném ender pearl lần " + var11), false);
                     } else {
                        class_746 var10 = var0.field_1724;
                        int var12 = ++pearlFailCount;
                        var10.method_7353(class_2561.method_43470("§c[SeaATS] Không có ender pearl trong tay trái! (lần thử " + var12 + ")"), false);
                        if (pearlFailCount >= 2) {
                           var0.field_1724.method_7353(class_2561.method_43470("§c[SeaATS] Không có ender pearl sau 2 lần thử, dừng bảo vệ."), false);
                           antiState = AutoSellLogic.AntiPlayerState.DONE;
                           break;
                        }
                     }
                  }

                  if (antiTimer >= 10 && pearlFailCount < 2) {
                     antiState = AutoSellLogic.AntiPlayerState.WAITING_KILL;
                     antiTimer = 0;
                     var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Chờ 2 giây để /kill..."), false);
                  }
                  break;
               case WAITING_KILL:
                  if (antiTimer >= 40) {
                     if (var0.method_1562() != null) {
                        var0.method_1562().method_45730("kill");
                        killCommandSent = true;
                        var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Đã gửi lệnh /kill!"), false);
                     }

                     antiState = AutoSellLogic.AntiPlayerState.CLICK_CONFIRM;
                     antiTimer = 0;
                  }
                  break;
               case CLICK_CONFIRM:
                  if (antiTimer >= 20) {
                     if (var0.field_1755 instanceof class_465 var2) {
                        class_1703 var3 = var2.method_17577();
                        int var4 = -1;
                        int var5 = var3.field_7761.size() - 36;
                        if (var5 < 0) {
                           var5 = var3.field_7761.size();
                        }

                        for (int var6 = 0; var6 < var5; var6++) {
                           if (var3.method_7611(var6).method_7677().method_31574(class_1802.field_8581)) {
                              var4 = var6;
                              break;
                           }
                        }

                        if (var4 == -1 && var3.field_7761.size() > 44) {
                           var4 = 44;
                        }

                        if (var4 != -1) {
                           var0.field_1761.method_2906(var3.field_7763, var4, 0, class_1713.field_7790, var0.field_1724);
                           confirmClicked = true;
                           var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Đã click confirm!"), false);
                           antiState = AutoSellLogic.AntiPlayerState.DONE;
                        } else if (++antiTimer > 100) {
                           var0.field_1724.method_7353(class_2561.method_43470("§c[SeaATS] Không tìm thấy GUI confirm!"), false);
                           antiState = AutoSellLogic.AntiPlayerState.DONE;
                        }
                     } else if (antiTimer > 100) {
                        var0.field_1724.method_7353(class_2561.method_43470("§c[SeaATS] GUI confirm không xuất hiện!"), false);
                        antiState = AutoSellLogic.AntiPlayerState.DONE;
                     }
                  }
                  break;
               case DONE:
                  var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Hoàn thành bảo vệ."), false);
                  antiState = wasAutoSellOnBeforeAnti ? AutoSellLogic.AntiPlayerState.WAITING_CLEAR : AutoSellLogic.AntiPlayerState.IDLE;
                  antiTarget = null;
                  antiTimer = 0;
                  pearlThrowCount = 0;
                  pearlFailCount = 0;
                  killCommandSent = false;
                  confirmClicked = false;
            }
         }
      }
   }

   private static void handleMovedAway(class_310 var0) {
      if (var0.field_1755 != null) {
         var0.field_1724.method_7346();
      }

      tickDelay = 100;
      isHomeRecovering = true;
      lastHomeCheckMs = System.currentTimeMillis();
      String var1 = var0.field_1724.method_5477().getString();
      WebhookHelper.sendAlert("**Cảnh báo:** " + var1 + " đã di chuyển ra khỏi khu vực HomeSet!");
   }

   private static void processHomeRecovery(class_310 var0) {
      if (var0.field_1724.method_5649(Config.homeX, Config.homeY, Config.homeZ) <= 4.0) {
         isHomeRecovering = false;
         currentState = AutoSellLogic.State.IDLE;
         tickDelay = 5;
         var0.field_1724.method_7353(class_2561.method_43470("§a[SeaATS] Đã về đúng vị trí HomeSet."), false);
      } else {
         long var1 = System.currentTimeMillis();
         if (var1 - lastHomeCheckMs > Config.homeCommandIntervalMin * 60L * 1000L) {
            lastHomeCheckMs = var1;
            String var3 = toggleHomeCmd ? Config.homeCommand2 : Config.homeCommand1;
            toggleHomeCmd = !toggleHomeCmd;
            if (var0.method_1562() != null) {
               var0.method_1562().method_45730(var3.replace("/", ""));
            }
         }
      }
   }

   private static String buildReport(class_310 var0, long var1) {
      double var3 = var1 / 3600000.0;
      double var5 = Config.totalMoney;
      String var7 = var0.field_1724.method_5477().getString();
      String var8 = var0.method_1558() != null ? var0.method_1558().field_3761 : "Unknown";
      String var9 = formatTime(var1);
      double var10 = var3 > 0.0 ? var5 / var3 : 0.0;
      return String.format(
         "**⛏️ Báo Cáo Treo Máy Định Kỳ**\n**\ud83d\udc64 Người chơi:** %s\n**\ud83d\udda5️ Server:** %s\n**⏱️ Thời gian treo:** %s\n**\ud83d\udcb0 Tổng thu nhập:** %s\n**\ud83d\udcc8 Tốc độ trung bình:** %s\n**\ud83d\udce6 Số lượt bán:** %d\n**\ud83d\udfe2 Trạng thái:** Đang treo máy...",
         var7,
         var8,
         var9,
         formatMoney(var5),
         formatMoney(var10) + "/h",
         Config.totalSellCount
      );
   }

   private static String formatMoney(double var0) {
      if (var0 >= 1.0E9) {
         return String.format("$%.2fB", var0 / 1.0E9);
      } else if (var0 >= 1000000.0) {
         return String.format("$%.2fM", var0 / 1000000.0);
      } else {
         return var0 >= 1000.0 ? String.format("$%.2fK", var0 / 1000.0) : String.format("$%.0f", var0);
      }
   }

   private static String formatTime(long var0) {
      long var2 = var0 / 1000L;
      long var4 = var2 / 3600L;
      long var6 = var2 % 3600L / 60L;
      long var8 = var2 % 60L;
      return String.format("%02d:%02d:%02d", var4, var6, var8);
   }

   static boolean isSellableInventoryItem(class_1799 stack, int inventoryIndex) {
      if (stack.method_7960()) {
         return false;
      }
      if (stack.method_31574(class_1802.field_50140)) {
         // Player inventory indices 0..8 are the hotbar; 9..35 are the main inventory.
         return inventoryIndex >= 9 && inventoryIndex < 36;
      }
      return !isProtectedNeverSellItem(stack);
   }

   static boolean isSellableInventorySlot(class_310 client, class_1735 slot) {
      return client.field_1724 != null
         && slot.field_7871 == client.field_1724.method_31548()
         && isSellableInventoryItem(slot.method_7677(), slot.method_34266());
   }

   static boolean isProtectedNeverSellItem(class_1799 var0) {
      return var0.method_31574(class_1802.field_8639)
         | var0.method_31574(class_1802.field_8450)
         | var0.method_31574(class_1802.field_8091)
         | var0.method_31574(class_1802.field_61338)
         | var0.method_31574(class_1802.field_8528)
         | var0.method_31574(class_1802.field_8845)
         | var0.method_31574(class_1802.field_8371)
         | var0.method_31574(class_1802.field_8802)
         | var0.method_31574(class_1802.field_22022)
         | var0.method_31574(class_1802.field_50140)
         | var0.method_7909().getClass().getName().equals("net.minecraft.class_9462");
   }

   @Environment(EnvType.CLIENT)
   private static enum AntiPlayerState {
      IDLE,
      THROWING_PEARL,
      WAITING_KILL,
      CLICK_CONFIRM,
      DONE,
      WAITING_CLEAR;
   }

   @Environment(EnvType.CLIENT)
   static enum State {
      IDLE,
      WAITING_GUI_OPEN,
      MOVING_ITEMS,
      CLICK_CONFIRM,
      CLICK_CONFIRM_DELAYED;
   }
}
