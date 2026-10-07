package com.photobox;

import com.photobox.camera.WebcamCamera;

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

    @Override
    public void start(Stage stage) {

        camera = new WebcamCamera();

        Label title = new Label("PHOTOBOX");

        title.setStyle(
                "-fx-font-size: 28px; " +
                "-fx-font-weight: bold;"
        );

        cameraView = new ImageView();

        cameraView.setFitWidth(800);
        cameraView.setFitHeight(450);

        cameraView.setPreserveRatio(true);

        Button startButton = new Button(
                "Start Camera"
        );

        Button stopButton = new Button(
                "Stop Camera"
        );

        startButton.setOnAction(event -> startCamera());

        stopButton.setOnAction(event -> stopCamera());

        HBox controls = new HBox(
                15,
                startButton,
                stopButton
        );

        controls.setStyle(
                "-fx-alignment: center;"
        );

        BorderPane root = new BorderPane();

        root.setTop(title);

        BorderPane.setAlignment(
                title,
                javafx.geometry.Pos.CENTER
        );

        StackPane previewContainer =
                new StackPane(cameraView);

        previewContainer.setStyle(
                "-fx-background-color: black;"
        );

        root.setCenter(previewContainer);

        root.setBottom(controls);

        BorderPane.setAlignment(
                controls,
                javafx.geometry.Pos.CENTER
        );

        Scene scene = new Scene(
                root,
                1000,
                700
        );

        stage.setTitle(
                "Photobox Prototype"
        );

        stage.setScene(scene);

        stage.show();

        stage.setOnCloseRequest(event -> {
            stopCamera();
            Platform.exit();
        });
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

                BufferedImage frame =
                        camera.getImage();

                if (frame == null) {
                    return;
                }

                WritableImage fxImage =
                        SwingFXUtils.toFXImage(
                                frame,
                                null
                        );

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
