package com.seastore.auto.seaats.client;

import com.seastore.auto.seaats.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WebhookHelper {
   private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

   public static void sendProfit(String message) {
   }

   public static void sendReport(String message) {
      System.out.println("[SeaATS] Sending report to profit webhook. URL: " + Config.webhookUrlProfit);
      send(Config.webhookUrlProfit, message);
   }

   public static void sendAlert(String message) {
      String content = message;
      if (Config.discordId != null && !Config.discordId.trim().isEmpty()) {
         content = content + "\n<@" + Config.discordId.trim() + ">";
      }

      System.out.println("[SeaATS] Sending alert to alert webhook. URL: " + Config.webhookUrlAlert);
      send(Config.webhookUrlAlert, content);
   }

   public static void sendPay(String message) {
      System.out.println("[SeaATS] Sending pay notification to pay webhook. URL: " + Config.payWebhookUrl);
      String finalMessage = "**\ud83e\ude99 Sea Auto Pay**\n" + message;
      send(Config.payWebhookUrl, finalMessage);
   }

   private static void send(String url, String content) {
      if (url != null && !url.trim().isEmpty()) {
         try {
            String json = "{\"content\": " + jsonEscape(content) + "}";
            HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(url))
               .header("Content-Type", "application/json")
               .POST(BodyPublishers.ofString(json))
               .build();
            HTTP_CLIENT.sendAsync(request, BodyHandlers.ofString()).thenAccept(response -> {
               int code = response.statusCode();
               if (code >= 200 && code < 300) {
                  System.out.println("[SeaATS] Webhook sent successfully.");
               } else {
                  System.err.println("[SeaATS] Webhook error: " + code + " - " + response.body());
               }
            }).exceptionally(ex -> {
               System.err.println("[SeaATS] Webhook send failed: " + ex.getMessage());
               return null;
            });
         } catch (Exception e) {
            System.err.println("[SeaATS] Webhook exception: " + e.getMessage());
         }
      } else {
         System.out.println("[SeaATS] Webhook URL empty, skipping.");
      }
   }

   private static String jsonEscape(String s) {
      return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
   }
}
