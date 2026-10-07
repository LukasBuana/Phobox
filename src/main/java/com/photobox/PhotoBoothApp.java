package com.photobox;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;
import com.photobox.session.PhotoSession;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PhotoBoothApp extends Application {

    private WebcamCamera camera;

    private ImageView cameraView;

    private AnimationTimer previewTimer;

    private PhotoCapture photoCapture;

    private Label countdownLabel;

    private Button takePhotoButton;

    private Path lastPhotoPath;

    private ImageView resultView;

    private Button retakeButton;

    private Button usePhotoButton;

    private PhotoSession photoSession;

    private Label sessionLabel;

    @Override
    public void start(Stage stage) {

        camera = new WebcamCamera();

        photoSession = new PhotoSession(4);

        photoCapture = new PhotoCapture(camera);

        Label title = new Label("PHOTOBOX");

        title.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold;");

        sessionLabel = new Label("Photo 1 / 4");
        sessionLabel.setStyle("-fx-font-size: 20px;" + "-fx-font-weight: bold;");

        VBox header = new VBox(5, title, sessionLabel);
        header.setStyle("-fx-alignment: center;" + "-fx-padding: 15;");

        cameraView = new ImageView();

        cameraView.setFitWidth(800);
        cameraView.setFitHeight(450);

        cameraView.setPreserveRatio(true);

        resultView = new ImageView();

        resultView.setFitWidth(800);
        resultView.setFitHeight(500);

        resultView.setPreserveRatio(true);

        resultView.setVisible(false);

        countdownLabel = new Label();

        countdownLabel.setStyle(
                "-fx-font-size: 100px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;");

        countdownLabel.setVisible(false);

        Button startButton = new Button(
                "Start Camera");

        Button stopButton = new Button(
                "Stop Camera");

        startButton.setOnAction(event -> startCamera());

        stopButton.setOnAction(event -> stopCamera());

        takePhotoButton = new Button("Take Photo");

        takePhotoButton.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-padding: 10 25;");

        takePhotoButton.setOnAction(
                event -> takePhoto());

        retakeButton = new Button("Retake");

        usePhotoButton = new Button("Use Photo");

        retakeButton.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-padding: 10 25;");

        usePhotoButton.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-padding: 10 25;");

        retakeButton.setVisible(false);
        usePhotoButton.setVisible(false);

        retakeButton.setOnAction(
                event -> retakePhoto());

        usePhotoButton.setOnAction(
                event -> usePhoto());

        HBox controls = new HBox(
                15,
                startButton,
                takePhotoButton,
                retakeButton,
                usePhotoButton,
                stopButton);

        controls.setStyle(
                "-fx-alignment: center;");

        BorderPane root = new BorderPane();

        root.setTop(header);

        BorderPane.setAlignment(
                title,
                javafx.geometry.Pos.CENTER);

        StackPane previewContainer = new StackPane(
                cameraView,
                resultView,
                countdownLabel);

        previewContainer.setStyle(
                "-fx-background-color: black;");

        root.setCenter(previewContainer);

        root.setBottom(controls);

        BorderPane.setAlignment(
                controls,
                javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(
                root,
                1000,
                700);

        stage.setTitle(
                "Photobox Prototype");

        stage.setScene(scene);

        stage.show();

        stage.setOnCloseRequest(event -> {
            stopCamera();
            Platform.exit();
        });
    }

    private void usePhoto() {
        if (lastPhotoPath == null) {
            return;
        }

        try {
            // Masukkan foto ke session
            photoSession.addPhoto(lastPhotoPath);

            System.out.println("Photo accepted: " + lastPhotoPath.toAbsolutePath());
            System.out
                    .println("Session progress: " + photoSession.getPhotoCount() + " / " + photoSession.getMaxPhotos());

            // Cek apakah session sudah selesai
            if (photoSession.isComplete()) {
                finishSession();
                return;
            }

            // Masih ada foto berikutnya
            prepareNextPhoto();
        } catch (Exception e) {
            System.err.println("Failed to accept photo:");
            e.printStackTrace();
        }
    }

    private void prepareNextPhoto() {
        int nextPhotoNumber = photoSession.getPhotoCount() + 1;

        sessionLabel.setText("Photo " + nextPhotoNumber + " / " + photoSession.getMaxPhotos());

        // Bersihkan hasil sebelumnya
        resultView.setImage(null);
        resultView.setVisible(false);

        // Tampilkan live camera
        cameraView.setVisible(true);

        // Reset countdown
        countdownLabel.setText("");
        countdownLabel.setVisible(false);

        // Reset tombol
        retakeButton.setVisible(false);
        usePhotoButton.setVisible(false);

        takePhotoButton.setVisible(true);
        takePhotoButton.setDisable(false);

        System.out.println("Ready for photo " + nextPhotoNumber + " / " + photoSession.getMaxPhotos());
    }

    private void finishSession() {
        System.out.println("================================");
        System.out.println("PHOTO SESSION COMPLETE");
        System.out.println("================================");

        for (int i = 0; i < photoSession.getPhotoCount(); i++) {
            System.out.println("Photo " + (i + 1) + ": " + photoSession.getPhoto(i).toAbsolutePath());
        }

        // Sembunyikan tombol
        takePhotoButton.setVisible(false);
        retakeButton.setVisible(false);
        usePhotoButton.setVisible(false);

        // Tampilkan informasi sementara
        sessionLabel.setText("Session Complete!");
        countdownLabel.setVisible(false);

        System.out.println("Ready for template processing.");
    }

    private void retakePhoto() {

        System.out.println("Retaking photo...");

        // Bersihkan hasil foto sebelumnya
        resultView.setImage(null);
        resultView.setVisible(false);

        // Tampilkan kembali live camera
        cameraView.setVisible(true);

        // Reset countdown
        countdownLabel.setText("");
        countdownLabel.setVisible(false);

        // Reset tombol
        retakeButton.setVisible(false);
        usePhotoButton.setVisible(false);

        takePhotoButton.setVisible(true);
        takePhotoButton.setDisable(false);

        retakeButton.setDisable(false);
        usePhotoButton.setDisable(false);

        System.out.println("Ready to take another photo.");
    }

    private void capturePhoto() {

        try {

            lastPhotoPath = photoCapture.capture();

            System.out.println(
                    "Captured photo: "
                            + lastPhotoPath.toAbsolutePath());

            // Load foto hasil capture
            javafx.scene.image.Image resultImage = new javafx.scene.image.Image(
                    lastPhotoPath.toUri().toString());

            resultView.setImage(resultImage);

            // Ganti dari live camera
            // menjadi hasil foto
            cameraView.setVisible(false);
            resultView.setVisible(true);

            // Tombol
            takePhotoButton.setVisible(false);

            retakeButton.setVisible(true);
            usePhotoButton.setVisible(true);

        } catch (Exception e) {

            System.err.println(
                    "Failed to capture photo:");

            e.printStackTrace();

            takePhotoButton.setVisible(true);
            takePhotoButton.setDisable(false);

        } finally {

            countdownLabel.setVisible(false);
        }
    }

    private void takePhoto() {

        if (!camera.isRunning()) {
            System.out.println("Camera belum aktif.");
            return;
        }

        takePhotoButton.setDisable(true);

        countdownLabel.setVisible(true);
        countdownLabel.setText("3");

        Timeline countdown = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        event -> countdownLabel.setText("3")),
                new KeyFrame(
                        Duration.seconds(1),
                        event -> countdownLabel.setText("2")),
                new KeyFrame(
                        Duration.seconds(2),
                        event -> countdownLabel.setText("1")),
                new KeyFrame(
                        Duration.seconds(3),
                        event -> capturePhoto()));

        countdown.setCycleCount(1);
        countdown.play();
    }

    private void startCamera() {

        if (camera.isRunning()) {
            return;
        }

        try {

            camera.start();

            if (photoSession.isComplete()) {
                photoSession.reset();
            }

            sessionLabel.setText(
                    "Photo 1 / "
                            + photoSession.getMaxPhotos());

            cameraView.setVisible(true);
            resultView.setVisible(false);

            takePhotoButton.setVisible(true);

            retakeButton.setVisible(false);
            usePhotoButton.setVisible(false);

            takePhotoButton.setDisable(false);
            retakeButton.setDisable(false);
            usePhotoButton.setDisable(false);

            startPreview();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void stopCamera() {

        if (previewTimer != null) {

            previewTimer.stop();

            previewTimer = null;
        }

        if (camera != null) {

            camera.stop();
        }

        cameraView.setImage(null);
    }

    private void startPreview() {

        previewTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (!camera.isRunning()) {
                    return;
                }

                BufferedImage frame = camera.getImage();

                if (frame == null) {
                    return;
                }

                WritableImage fxImage = SwingFXUtils.toFXImage(
                        frame,
                        null);

                cameraView.setImage(fxImage);
            }
        };

        previewTimer.start();
    }

    @Override
    public void stop() {

        stopCamera();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
