package com.photobox.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramNotifier {

    private static final String BOT_TOKEN = "8867051914:AAE0knG8ZQx7D5bkt4b5qzf5BAtraRLzDk0";
    private static final String CHAT_ID = "1533477445";

    public static void sendNotification(String message) {
        if (BOT_TOKEN.equals("MASUKKAN_BOT_TOKEN_DISINI")) {
            return;
        }

        new Thread(() -> {
            try {
                String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
                
                // Hilangkan mode Markdown untuk sementara jika teks mengandung karakter khusus, atau gunakan teks biasa
                String jsonPayload = String.format(
                    "{\"chat_id\": \"%s\", \"text\": \"%s\"}",
                    CHAT_ID,
                    message.replace("\n", "\\n") // Escape karakter enter agar valid dalam JSON
                );

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlString))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Cetak respon untuk debugging jika masih gagal
                System.out.println("Telegram Response Code: " + response.statusCode());
                System.out.println("Telegram Response Body: " + response.body());

            } catch (Exception e) {
                System.err.println("Gagal mengirim notifikasi Telegram: " + e.getMessage());
            }
        }).start();
    }
}