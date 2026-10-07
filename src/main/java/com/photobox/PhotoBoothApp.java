package com.photobox;

import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.nio.file.Path;
import javafx.animation.AnimationTimer;
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
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class PhotoBoothApp extends Application {

    private WebcamCamera camera;

    private ImageView cameraView;

    private AnimationTimer previewTimer;

    private PhotoCapture photoCapture;

    private Label countdownLabel;

    private Button takePhotoButton;

    @Override
    public void start(Stage stage) {

        camera = new WebcamCamera();

        photoCapture = new PhotoCapture(camera);

        Label title = new Label("PHOTOBOX");

        title.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold;");

        cameraView = new ImageView();

        cameraView.setFitWidth(800);
        cameraView.setFitHeight(450);

        cameraView.setPreserveRatio(true);

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

        HBox controls = new HBox(
                15,
                startButton,
                takePhotoButton,
                stopButton);

        controls.setStyle(
                "-fx-alignment: center;");

        BorderPane root = new BorderPane();

        root.setTop(title);

        BorderPane.setAlignment(
                title,
                javafx.geometry.Pos.CENTER);

        StackPane previewContainer = new StackPane(
                cameraView,
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

    private void capturePhoto() {

        try {

            Path photoPath = photoCapture.capture();

            System.out.println(
                    "Captured photo: "
                            + photoPath.toAbsolutePath());

        } catch (Exception e) {

            System.err.println(
                    "Failed to capture photo:");

            e.printStackTrace();

        } finally {

            countdownLabel.setVisible(false);

            takePhotoButton.setDisable(false);
        }
    }

    private void takePhoto() {

        if (!camera.isRunning()) {
            System.out.println(
                    "Camera belum aktif.");
            return;
        }

        takePhotoButton.setDisable(true);

        countdownLabel.setVisible(true);

        Timeline countdown = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        event -> countdownLabel
                                .setText("3")),

                new KeyFrame(
                        Duration.seconds(1),
                        event -> countdownLabel
                                .setText("2")),

                new KeyFrame(
                        Duration.seconds(2),
                        event -> countdownLabel
                                .setText("1")),

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
