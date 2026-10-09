package com.photobox.ui;

import com.photobox.camera.Camera;
import com.photobox.capture.PhotoCapture;
import com.photobox.template.TemplateSlot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

public class PhotoCaptureView extends BorderPane {

    private final Camera camera;
    private final PhotoCapture photoCapture;
    private final TemplateSlot targetSlot;

    private final Consumer<Path> onPhotoAccepted;
    private final Runnable onBack;

    private final ImageView cameraView;
    private final Label countdownLabel;

    private boolean viewportInitialized = false;
    private Timeline countdownTimeline;
    private int countdown;

    public PhotoCaptureView(
            Camera camera,
            PhotoCapture photoCapture,
            TemplateSlot targetSlot,
            Consumer<Path> onPhotoAccepted,
            Runnable onBack
    ) {

        this.camera = camera;
        this.photoCapture = photoCapture;
        this.targetSlot = targetSlot;

        this.onPhotoAccepted = onPhotoAccepted;
        this.onBack = onBack;

        cameraView = new ImageView();
        countdownLabel = new Label();

        createView();
        startCameraPreview();

        // ==================================================
        // OTOMATIS MULAI HITUNG MUNDUR SAAT KAMERA DIBUKA
        // ==================================================
        startCountdown();
    }

    private void createView() {

        setPadding(new Insets(30));

        Label title = new Label("Get Ready! Photo " + (targetSlot.getSlotIndex() + 1));
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        BorderPane.setAlignment(title, Pos.CENTER);
        setTop(title);

        cameraView.setFitWidth(900);
        cameraView.setFitHeight(600);
        cameraView.setPreserveRatio(true);
        cameraView.setSmooth(true);

        StackPane previewContainer = new StackPane();

        countdownLabel.setStyle(
                "-fx-font-size: 120px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );

        previewContainer.getChildren().addAll(cameraView, countdownLabel);
        StackPane.setAlignment(countdownLabel, Pos.CENTER);

        setCenter(previewContainer);

    }

    private void updateViewport(double camWidth, double camHeight) {
        double slotRatio = (double) targetSlot.getWidth() / (double) targetSlot.getHeight();
        double camRatio = camWidth / camHeight;

        double cropWidth, cropHeight, xOffset, yOffset;

        if (camRatio > slotRatio) {
            cropHeight = camHeight;
            cropWidth = camHeight * slotRatio;
            xOffset = (camWidth - cropWidth) / 2;
            yOffset = 0;
        } else {
            cropWidth = camWidth;
            cropHeight = camWidth / slotRatio;
            xOffset = 0;
            yOffset = (camHeight - cropHeight) / 2;
        }

        cameraView.setViewport(new Rectangle2D(xOffset, yOffset, cropWidth, cropHeight));
    }


    private void startCameraPreview() {
        Thread previewThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (!camera.isRunning()) {
                    break;
                }

                BufferedImage frame = camera.getImage();
                if (frame != null) {
                    try {
                        var output = new java.io.ByteArrayOutputStream();
                        ImageIO.write(frame, "JPG", output);
                        byte[] bytes = output.toByteArray();
                        Image image = new Image(new ByteArrayInputStream(bytes));

                        Platform.runLater(() -> {
                            cameraView.setImage(image);

                            if (!viewportInitialized && image.getWidth() > 0) {
                                updateViewport(image.getWidth(), image.getHeight());
                                viewportInitialized = true;
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(33); // Sekitar 30 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        previewThread.setDaemon(true);
        previewThread.start();
    }

    private void startCountdown() {
        stopCountdown();
        countdown = 3; // Timer 3 detik

        countdownLabel.setText(String.valueOf(countdown));

        countdownTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> {
                            countdown--;

                            if (countdown <= 0) {
                                countdownLabel.setText("");
                                stopCountdown();
                                takePhoto(); // Jepret!
                            } else {
                                countdownLabel.setText(String.valueOf(countdown));
                            }
                        }
                )
        );

        countdownTimeline.setCycleCount(3);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        countdownLabel.setText("");
    }

    private void takePhoto() {
        try {
            // Efek Flash sederhana bisa ditambahkan di sini jika mau
            
            // 1. Ambil foto menggunakan layanan PhotoCapture
            Path savedPhoto = photoCapture.capture();
            
            // 2. Langsung lempar hasilnya (Bypass layar preview)
            onPhotoAccepted.accept(savedPhoto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        stopCountdown();
    }
}