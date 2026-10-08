package com.photobox.ui;

import com.photobox.camera.Camera;
import com.photobox.capture.PhotoCapture;
import com.photobox.template.TemplateSlot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D; // <-- Menggunakan Viewport untuk live crop
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
import java.nio.file.Files;
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

    // Untuk memastikan viewport hanya dihitung satu kali
    private boolean viewportInitialized = false;

    private Timeline countdownTimeline;
    private Path lastPhotoPath;
    private int countdown;

    private volatile boolean showingPhotoResult = false;
    private boolean countdownRunning = false;

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
    }

    private void createView() {

        setPadding(
                new Insets(30)
        );

        Label title =
                new Label(
                        "Take Photo "
                                + (targetSlot.getSlotIndex() + 1)
                );

        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        BorderPane.setAlignment(
                title,
                Pos.CENTER
        );

        setTop(title);

        // ==================================================
        // UKURAN MAKSIMAL KAMERA DI LAYAR
        // ==================================================
        cameraView.setFitWidth(900);
        cameraView.setFitHeight(600);
        cameraView.setPreserveRatio(true);
        cameraView.setSmooth(true);

        StackPane previewContainer =
                new StackPane();

        countdownLabel.setStyle(
                "-fx-font-size: 100px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );

        previewContainer
                .getChildren()
                .addAll(cameraView, countdownLabel);

        StackPane.setAlignment(
                countdownLabel,
                Pos.CENTER
        );

        setCenter(previewContainer);

        setBottom(
                createControls()
        );
    }

    // ==================================================
    // LIVE CROP (VIEWPORT)
    // ==================================================
    // ==================================================
    // LIVE CROP (VIEWPORT)
    // ==================================================
    private void updateViewport(double camWidth, double camHeight) {

        // PERBAIKAN: Gunakan (double) agar tidak menjadi 0 (Integer Division)
        double slotRatio = (double) targetSlot.getWidth() / (double) targetSlot.getHeight();
        double camRatio = camWidth / camHeight;

        double cropWidth, cropHeight, xOffset, yOffset;

        if (camRatio > slotRatio) {
            // Kamera lebih lebar dari template -> Potong Kiri & Kanan
            cropHeight = camHeight;
            cropWidth = camHeight * slotRatio;
            xOffset = (camWidth - cropWidth) / 2;
            yOffset = 0;
        } else {
            // Kamera lebih tinggi dari template -> Potong Atas & Bawah
            cropWidth = camWidth;
            cropHeight = camWidth / slotRatio;
            xOffset = 0;
            yOffset = (camHeight - cropHeight) / 2;
        }

        // Terapkan batas crop secara langsung ke kamera
        cameraView.setViewport(
                new Rectangle2D(xOffset, yOffset, cropWidth, cropHeight)
        );
    }

    private HBox createControls() {

        Button backButton =
                new Button("BACK");

        backButton.setOnAction(
                event -> {
                    stopCountdown();
                    onBack.run();
                }
        );

        Button takeButton =
                new Button("TAKE PHOTO");

        takeButton.setOnAction(
                event -> {
                    if (!countdownRunning) {
                        startCountdown();
                    }
                }
        );

        HBox controls =
                new HBox(
                        20,
                        backButton,
                        takeButton
                );

        controls.setAlignment(
                Pos.CENTER
        );

        controls.setPadding(
                new Insets(20, 0, 0, 0)
        );

        return controls;
    }

    private void startCameraPreview() {

        Thread previewThread =
                new Thread(() -> {

                    while (!Thread.currentThread()
                            .isInterrupted()) {

                        if (!camera.isRunning()) {
                            break;
                        }

                        if (!showingPhotoResult) {

                            BufferedImage frame =
                                    camera.getImage();

                            if (frame != null) {

                                try {

                                    var output =
                                            new java.io.ByteArrayOutputStream();

                                    ImageIO.write(
                                            frame,
                                            "JPG",
                                            output
                                    );

                                    byte[] bytes =
                                            output.toByteArray();

                                    Image image =
                                            new Image(
                                                    new ByteArrayInputStream(
                                                            bytes
                                                    )
                                            );

                                    Platform.runLater(() -> {

                                        if (!showingPhotoResult) {

                                            cameraView.setImage(image);

                                            // Inisiasi Viewport (Crop) di frame pertama
                                            if (!viewportInitialized && image.getWidth() > 0) {
                                                updateViewport(image.getWidth(), image.getHeight());
                                                viewportInitialized = true;
                                            }
                                        }

                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        try {
                            Thread.sleep(33);
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
        showingPhotoResult = false;
        countdownRunning = true;
        countdown = 3;

        countdownLabel.setText(
                String.valueOf(countdown)
        );

        countdownTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                event -> {

                                    countdown--;

                                    if (countdown <= 0) {
                                        countdownLabel.setText("");
                                        countdownRunning = false;

                                        if (countdownTimeline != null) {
                                            countdownTimeline.stop();
                                        }
                                        countdownTimeline = null;
                                        takePhoto();

                                    } else {
                                        countdownLabel.setText(
                                                String.valueOf(countdown)
                                        );
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

        countdownRunning = false;
        countdownLabel.setText("");
    }

    private void takePhoto() {

        try {
            showingPhotoResult = true;
            lastPhotoPath = photoCapture.capture();
            showPhotoPreview();

        } catch (Exception e) {
            showingPhotoResult = false;
            e.printStackTrace();
        }
    }

    private void showPhotoPreview() {

        stopCountdown();

        if (lastPhotoPath == null) {
            showingPhotoResult = false;
            return;
        }

        try {
            Image image =
                    new Image(
                            Files.newInputStream(
                                    lastPhotoPath
                            )
                    );

            // Karena setViewport masih aktif, foto statis ini
            // juga akan terlihat tercrop dengan sempurna seperti live preview
            cameraView.setImage(image);

            setBottom(
                    createPhotoPreviewControls()
            );

        } catch (IOException e) {
            showingPhotoResult = false;
            e.printStackTrace();
        }
    }

    private HBox createPhotoPreviewControls() {

        Button retakeButton =
                new Button("RETAKE");

        retakeButton.setOnAction(
                event -> retakePhoto()
        );

        Button useButton =
                new Button("USE PHOTO");

        useButton.setOnAction(
                event -> usePhoto()
        );

        HBox controls =
                new HBox(
                        20,
                        retakeButton,
                        useButton
                );

        controls.setAlignment(
                Pos.CENTER
        );

        controls.setPadding(
                new Insets(20, 0, 0, 0)
        );

        return controls;
    }

    private void retakePhoto() {

        lastPhotoPath = null;
        showingPhotoResult = false;
        cameraView.setImage(null);

        setBottom(
                createControls()
        );
    }

    private void usePhoto() {

        if (lastPhotoPath == null) {
            return;
        }

        Path acceptedPhoto = lastPhotoPath;
        lastPhotoPath = null;
        showingPhotoResult = false;

        onPhotoAccepted.accept(acceptedPhoto);
    }

    public void dispose() {
        stopCountdown();
        showingPhotoResult = true;
    }
}