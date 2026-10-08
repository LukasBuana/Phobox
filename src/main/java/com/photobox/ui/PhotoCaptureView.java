package com.photobox.ui;

import com.photobox.camera.Camera;
import com.photobox.capture.PhotoCapture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    private final int slotIndex;

    private final Consumer<Path> onPhotoAccepted;
    private final Runnable onBack;

    private final ImageView cameraView;
    private final Label countdownLabel;

    private Timeline countdownTimeline;

    private Path lastPhotoPath;

    private int countdown;

    /*
     * TRUE ketika sedang menampilkan hasil foto.
     *
     * Ketika TRUE, thread kamera tidak boleh
     * mengganti ImageView dengan live camera preview.
     */
    private volatile boolean showingPhotoResult = false;

    /*
     * Menandakan apakah proses countdown sedang berjalan.
     */
    private boolean countdownRunning = false;

    public PhotoCaptureView(
            Camera camera,
            PhotoCapture photoCapture,
            int slotIndex,
            Consumer<Path> onPhotoAccepted,
            Runnable onBack
    ) {

        this.camera = camera;
        this.photoCapture = photoCapture;

        this.slotIndex = slotIndex;

        this.onPhotoAccepted =
                onPhotoAccepted;

        this.onBack = onBack;

        cameraView =
                new ImageView();

        countdownLabel =
                new Label();

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
                                + (slotIndex + 1)
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

        cameraView.setFitWidth(900);
        cameraView.setFitHeight(600);

        cameraView.setPreserveRatio(true);

        StackPane previewContainer =
                new StackPane();

        previewContainer
                .getChildren()
                .add(cameraView);

        countdownLabel.setStyle(
                "-fx-font-size: 100px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        previewContainer
                .getChildren()
                .add(countdownLabel);

        StackPane.setAlignment(
                countdownLabel,
                Pos.CENTER
        );

        setCenter(previewContainer);

        setBottom(
                createControls()
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

                        /*
                         * Jangan membaca/update preview
                         * jika camera sudah tidak aktif.
                         */
                        if (!camera.isRunning()) {
                            break;
                        }

                        /*
                         * Jika sedang menampilkan hasil foto,
                         * jangan timpa ImageView dengan
                         * frame kamera.
                         */
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

                                        /*
                                         * Cek lagi karena bisa saja
                                         * status berubah ketika
                                         * update sedang menunggu
                                         * JavaFX Application Thread.
                                         */
                                        if (!showingPhotoResult) {

                                            cameraView
                                                    .setImage(image);
                                        }

                                    });

                                } catch (IOException e) {

                                    e.printStackTrace();
                                }
                            }
                        }

                        try {

                            Thread.sleep(33);

                        } catch (
                                InterruptedException e
                        ) {

                            Thread.currentThread()
                                    .interrupt();

                            break;
                        }
                    }

                });

        previewThread.setDaemon(true);

        previewThread.start();
    }

    private void startCountdown() {

        stopCountdown();

        /*
         * Pastikan kita kembali ke live camera.
         */
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

                                        countdownLabel
                                                .setText("");

                                        countdownRunning = false;

                                        if (countdownTimeline != null) {

                                            countdownTimeline
                                                    .stop();
                                        }

                                        countdownTimeline = null;

                                        takePhoto();

                                    } else {

                                        countdownLabel
                                                .setText(
                                                        String.valueOf(
                                                                countdown
                                                        )
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

            /*
             * Tandai bahwa kita akan masuk ke
             * mode hasil foto.
             *
             * Ini dilakukan sebelum capture supaya
             * live preview tidak menimpa hasil capture.
             */
            showingPhotoResult = true;

            lastPhotoPath =
                    photoCapture.capture();

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

            /*
             * Tampilkan hasil foto yang baru saja
             * diambil.
             */
            cameraView.setImage(image);

            /*
             * Ganti tombol:
             *
             * BACK + TAKE PHOTO
             *
             * menjadi:
             *
             * RETAKE + USE PHOTO
             */
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

        /*
         * Buang hasil foto sebelumnya dari state.
         *
         * File fisiknya tidak perlu langsung dihapus.
         * Untuk tahap prototype ini lebih aman dibiarkan.
         */
        lastPhotoPath = null;

        /*
         * Kembali ke live camera.
         */
        showingPhotoResult = false;

        cameraView.setImage(null);

        /*
         * Kembalikan tombol:
         *
         * BACK + TAKE PHOTO
         */
        setBottom(
                createControls()
        );
    }

    private void usePhoto() {

        if (lastPhotoPath == null) {
            return;
        }

        Path acceptedPhoto =
                lastPhotoPath;

        /*
         * Reset state sebelum berpindah view.
         */
        lastPhotoPath = null;

        showingPhotoResult = false;

        /*
         * Foto baru dianggap diterima
         * setelah user menekan USE PHOTO.
         */
        onPhotoAccepted.accept(
                acceptedPhoto
        );
    }

    public void dispose() {

        stopCountdown();

        showingPhotoResult = true;
    }
}
