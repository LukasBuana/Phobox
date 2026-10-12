package com.photobox.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;

public class AttractScreenView extends StackPane {

    private final Runnable onScreenTapped;

    public AttractScreenView(Runnable onScreenTapped) {
        this.onScreenTapped = onScreenTapped;
        createView();
    }

    private void createView() {

        // ==================================================
        // 1. BACKGROUND TEMA NATAL
        // ==================================================
        File bgFile = new File("assets/background.png"); 
        
        if (bgFile.exists()) {
            Image bgImage = new Image(bgFile.toURI().toString());
            
            BackgroundSize bgSize = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO, 
                    false, false, false, true
            );
            
            BackgroundImage bgi = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    bgSize
            );
            
            this.setBackground(new Background(bgi));
        } else {
            this.setStyle("-fx-background-color: #111111;");
        }

        // ==================================================
        // 2. KONTEN TENGAH (JUDUL & HARGA)
        // ==================================================
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        Label mainTitle = new Label("MERRY CHRISTMAS\nPHOTOBOOTH");
        mainTitle.setStyle(
                "-fx-font-size: 64px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-text-alignment: center;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 0);"
        );

        Label priceLabel = new Label("Rp 25.000 / Sesi");
        priceLabel.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #FFD700;" + // Emas
                "-fx-background-color: rgba(0, 0, 0, 0.5);" +
                "-fx-padding: 10 30;" +
                "-fx-background-radius: 20px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);"
        );

        // ==================================================
        // 3. TEKS BERKEDIP (TOUCH TO START)
        // ==================================================
        Label touchText = new Label("👉 SENTUH LAYAR UNTUK MULAI 👈");
        touchText.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );

        // Memberikan jarak (margin) atas yang jauh agar tulisan ini berada agak di bawah
        VBox.setMargin(touchText, new Insets(80, 0, 0, 0));

        // Animasi Berkedip (Fade Transition)
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.2), touchText);
        fadeTransition.setFromValue(1.0);   // Transparansi penuh
        fadeTransition.setToValue(0.1);     // Hampir hilang
        fadeTransition.setCycleCount(Animation.INDEFINITE); // Mengulang terus menerus
        fadeTransition.setAutoReverse(true); // Bolak-balik (Terang -> Gelap -> Terang)
        fadeTransition.play();

        contentBox.getChildren().addAll(mainTitle, priceLabel, touchText);
        this.getChildren().add(contentBox);

        // ==================================================
        // 4. EVENT LISTENER KLIK SELURUH LAYAR
        // ==================================================
        this.setStyle("-fx-cursor: hand;");
        this.setOnMouseClicked(event -> {
            // Hentikan animasi saat layar ditekan untuk menghemat memori
            fadeTransition.stop();
            onScreenTapped.run();
        });
    }
}