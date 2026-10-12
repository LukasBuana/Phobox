package com.photobox.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.photobox.service.QrisPaymentService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class PaymentScreenView extends StackPane {

    private final String transactionId;
    private final Runnable onSuccess;
    private final Runnable onCancel;

    private Timeline timeoutTimeline;
    private Timeline pollingTimeline;
    private int timeRemaining = 120; // 2 Menit batas waktu
    private Label countdownLabel;
    private StackPane qrContainer;

    public PaymentScreenView(String transactionId, Runnable onSuccess, Runnable onCancel) {
        this.transactionId = transactionId;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        createView();
        initializeQrisAndPolling();
    }

    private void createView() {
        // 1. Background Tema Natal
        File bgFile = new File("assets/background.png");
        if (bgFile.exists()) {
            Image bgImage = new Image(bgFile.toURI().toString());
            BackgroundSize bgSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
            this.setBackground(new Background(new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSize)));
        } else {
            this.setStyle("-fx-background-color: #2c3e50;");
        }

        // 2. Kotak Konten Utama
        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxWidth(480);
        dialogBox.setMaxHeight(620);
        dialogBox.setPadding(new Insets(35));
        dialogBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                "-fx-background-radius: 20px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 0);"
        );

        Label title = new Label("Selesaikan Pembayaran");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label price = new Label("Rp 25.000");
        price.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label instruction = new Label("Scan QRIS menggunakan M-Banking\natau e-Wallet pilihan Anda.");
        instruction.setStyle("-fx-font-size: 15px; -fx-text-fill: #555555; -fx-text-alignment: center;");
        instruction.setWrapText(true);

        // 3. Container QR Code
        qrContainer = new StackPane();
        qrContainer.setPrefSize(230, 230);
        qrContainer.setMaxSize(230, 230);
        qrContainer.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 2px; -fx-border-radius: 10px;");
        
        Label loadingLabel = new Label("Memuat QRIS...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        qrContainer.getChildren().add(loadingLabel);

        // FITUR BYPASS: Klik pada kotak QR untuk simulasi sukses instan jika offline/testing
        qrContainer.setStyle(qrContainer.getStyle() + "-fx-cursor: hand;");
        qrContainer.setOnMouseClicked(event -> handlePaymentSuccess());

        countdownLabel = new Label("Sisa Waktu: 02:00");
        countdownLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        Button btnCancel = new Button("Batal & Kembali");
        btnCancel.setStyle(
                "-fx-background-color: #bdc3c7; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnCancel.setOnAction(event -> cancelPayment());

        dialogBox.getChildren().addAll(title, price, qrContainer, instruction, countdownLabel, btnCancel);
        this.getChildren().add(dialogBox);
    }

    private void initializeQrisAndPolling() {
        // Jalankan pemanggilan API secara Asinkron (di background thread agar UI tidak macet)
        new Thread(() -> {
            // 1. Generate QRIS ke Xendit
            String responseJson = QrisPaymentService.createDynamicQris(transactionId, 25000);
            
            // Simulasi parsing string QR sederhana (ambil teks string QRIS dari field respon)
            // Catatan: Di implementasi nyata, gunakan JSON Parser untuk mengambil field "qr_string"
            String qrString = parseQrStringFromResponse(responseJson);

            Platform.runLater(() -> {
                if (qrString != null) {
                    renderQrCodeImage(qrString);
                } else {
                    // Fallback teks jika gagal terhubung ke internet/API
                    Label errLabel = new Label("Gagal Memuat QRIS\n(Klik di sini untuk Bypass)");
                    errLabel.setStyle("-fx-text-fill: red; -fx-text-alignment: center;");
                    qrContainer.getChildren().setAll(errLabel);
                }
            });
        }).start();

        // 2. Mulai Hitung Mundur Waktu Habis (Timeout)
        timeoutTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining--;
            int minutes = timeRemaining / 60;
            int seconds = timeRemaining % 60;
            countdownLabel.setText(String.format("Sisa Waktu: %02d:%02d", minutes, seconds));

            if (timeRemaining <= 0) {
                cancelPayment();
            }
        }));
        timeoutTimeline.setCycleCount(Timeline.INDEFINITE);
        timeoutTimeline.play();

        // 3. Mulai Polling Status Pembayaran Setiap 3 Detik ke Server Xendit
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            new Thread(() -> {
                boolean isPaid = QrisPaymentService.checkPaymentStatus(transactionId);
                if (isPaid) {
                    Platform.runLater(this::handlePaymentSuccess);
                }
            }).start();
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    private String parseQrStringFromResponse(String json) {
        if (json == null || !json.contains("qr_string")) return null;
        try {
            // Ekstraksi manual string JSON tanpa library tambahan
            int start = json.indexOf("qr_string") + 12;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private void renderQrCodeImage(String qrText) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrText, BarcodeFormat.QR_CODE, 210, 210);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            
            Image qrImage = new Image(new ByteArrayInputStream(out.toByteArray()));
            ImageView qrView = new ImageView(qrImage);
            
            qrContainer.getChildren().setAll(qrView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePaymentSuccess() {
        stopTimers();
        System.out.println("✅ Pembayaran Berhasil Dikonfirmasi!");
        onSuccess.run();
    }

    private void cancelPayment() {
        stopTimers();
        System.out.println("❌ Pembayaran Dibatalkan / Waktu Habis.");
        onCancel.run();
    }

    private void stopTimers() {
        if (timeoutTimeline != null) timeoutTimeline.stop();
        if (pollingTimeline != null) pollingTimeline.stop();
    }

    public void dispose() {
        stopTimers();
    }
}