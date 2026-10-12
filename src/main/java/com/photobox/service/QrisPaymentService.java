package com.photobox.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class QrisPaymentService {

    // Ganti dengan API Key/Secret Key Xendit Anda (Gunakan mode Development/Sandbox dulu)
    private static final String API_KEY = "xnd_development_YOUR_SECRET_KEY_HERE";

    /**
     * Meminta string QRIS dinamis ke server Xendit
     */
    public static String createDynamicQris(String externalId, int amount) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String jsonBody = String.format(
                "{\"external_id\": \"%s\", \"type\": \"DYNAMIC\", \"amount\": %d, \"currency\": \"IDR\"}",
                externalId, amount
            );

            String auth = API_KEY + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.xendit.co/qr_codes"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                // Mengembalikan respons body mentah (berisi JSON dari Xendit)
                return response.body();
            } else {
                System.err.println("Gagal membuat QRIS: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mengecek status pembayaran QRIS (Polling) ke server Xendit
     */
    public static boolean checkPaymentStatus(String externalId) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String auth = API_KEY + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.xendit.co/qr_codes/" + externalId))
                    .header("Authorization", "Basic " + encodedAuth)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Cek sederhana apakah respons mengandung status COMPLETED
                // (Bisa disempurnakan dengan JSON parser seperti Gson/Jackson jika ingin lebih presisi)
                return response.body().contains("COMPLETED") || response.body().contains("SUCCEEDED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}