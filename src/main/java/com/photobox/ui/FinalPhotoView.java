package com.photobox.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FinalPhotoView extends BorderPane {

    private final Path finalPhoto;

    private final Runnable onRetake;
    private final Runnable onPrint;

    public FinalPhotoView(
            Path finalPhoto,
            Runnable onRetake,
            Runnable onPrint
    ) {

        this.finalPhoto = finalPhoto;
        this.onRetake = onRetake;
        this.onPrint = onPrint;

        createView();
    }

    private void createView() {

        setPadding(
                new Insets(20)
        );

        // ==================================================
        // TITLE
        // ==================================================

        Label title =
                new Label("FINAL PHOTO");

        title.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;"
        );

        BorderPane.setAlignment(
                title,
                Pos.CENTER
        );

        setTop(title);

        // ==================================================
        // IMAGE
        // ==================================================

        ImageView imageView =
                new ImageView();

        imageView.setPreserveRatio(true);

        imageView.setSmooth(true);

        imageView.setCache(true);

        try {

            Image image =
                    new Image(
                            Files.newInputStream(
                                    finalPhoto
                            )
                    );

            imageView.setImage(image);

        } catch (IOException e) {

            e.printStackTrace();
        }

        /*
         * Jangan menggunakan ukuran fixed seperti:
         *
         * setFitWidth(700)
         * setFitHeight(700)
         *
         * Karena ukuran layar photobox bisa berbeda.
         *
         * Preview akan mengikuti ukuran area yang tersedia.
         */

        VBox preview =
                new VBox(
                        imageView
                );

        preview.setAlignment(
                Pos.CENTER
        );

        preview.setPadding(
                new Insets(10)
        );

        /*
         * Berikan ruang fleksibel untuk preview.
         */
        VBox.setVgrow(
                imageView,
                Priority.ALWAYS
        );

        /*
         * Ukuran maksimal preview mengikuti ukuran window.
         *
         * Lebar maksimal = 70% dari area.
         * Tinggi maksimal = 65% dari area.
         */
        imageView.fitWidthProperty().bind(
                widthProperty()
                        .multiply(0.70)
        );

        imageView.fitHeightProperty().bind(
                heightProperty()
                        .multiply(0.65)
        );

        setCenter(preview);

        // ==================================================
        // BUTTONS
        // ==================================================

        Button retakeButton =
                new Button("RETAKE");

        retakeButton.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-padding: 12px 30px;"
        );

        retakeButton.setOnAction(
                event -> onRetake.run()
        );

        Button printButton =
                new Button("PRINT");

        printButton.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-padding: 12px 30px;"
        );

        printButton.setOnAction(
                event -> onPrint.run()
        );

        HBox controls =
                new HBox(
                        25,
                        retakeButton,
                        printButton
                );

        controls.setAlignment(
                Pos.CENTER
        );

        controls.setPadding(
                new Insets(15, 0, 10, 0)
        );

        setBottom(controls);
    }
}
