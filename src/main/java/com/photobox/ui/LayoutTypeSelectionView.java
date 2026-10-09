package com.photobox.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LayoutTypeSelectionView extends StackPane {

    private final Consumer<String> onLayoutSelected;

    public LayoutTypeSelectionView(Consumer<String> onLayoutSelected) {
        this.onLayoutSelected = onLayoutSelected;
        createView();
    }

    private void createView() {
        // ==================================================
        // BACKGROUND BERMODEL GRADIASI NATAL & EFEK SALJU
        // ==================================================
        this.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #ffffff);" +
                "-fx-padding: 40;"
        );

        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(800);

        // ==================================================
        // HEADER / JUDUL BERTEMA NATAL
        // ==================================================
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Label subtitle = new Label("🎅 MERRY CHRISTMAS & HAPPY HOLIDAYS 🎄");
        subtitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #FFD700;" + // Warna emas
                "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.5), 10, 0, 0, 0);"
        );

        Label title = new Label("PILIH JENIS LAYOUT FOTO");
        title.setStyle(
                "-fx-font-size: 38px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.3), 10, 0, 0, 0);"
        );

        Label hint = new Label("✨ Abadikan momen seru Natal bersama Santa & Rusa kesayanganmu! 🦌");
        hint.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #A3E4D7;"
        );

        headerBox.getChildren().addAll(subtitle, title, hint);

        // ==================================================
        // KARTU PILIHAN LAYOUT (CARD SELECTION)
        // ==================================================
        HBox cardContainer = new HBox(40);
        cardContainer.setAlignment(Pos.CENTER);

        // Card 1: Layout 2x2
        VBox card2x2 = createLayoutCard(
                "🎁",
                "LAYOUT 2x2",
                "4 Foto • Klasik & Elegan",
                "#C0392B", // Merah Natal
                e -> onLayoutSelected.accept("2x2")
        );

        // Card 2: Layout 3x3
        VBox card3x3 = createLayoutCard(
                "🦌",
                "LAYOUT 3x3",
                "9 Foto • Seru & Ramai",
                "#16A085", // Hijau Pinus
                e -> onLayoutSelected.accept("3x3")
        );

        cardContainer.getChildren().addAll(card2x2, card3x3);

        // ==================================================
        // FOOTER / DEKORASI TAMBAHAN
        // ==================================================
        Label footer = new Label("❄️ Photobox Special Edition - Powered by JavaFX ❄️");
        footer.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7F8C8D;" +
                "-fx-padding: 20 0 0 0;"
        );

        mainContainer.getChildren().addAll(headerBox, cardContainer, footer);
        this.getChildren().add(mainContainer);
    }

    private VBox createLayoutCard(String iconEmoji, String layoutName, String description, String accentColor, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 25, 30, 25));
        card.setPrefWidth(280);
        card.setPrefHeight(320);

        // Gaya Kartu Kaca (Glassmorphism / Festive Card)
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-color: " + accentColor + ";" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 20px;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
        );

        Label icon = new Label(iconEmoji);
        icon.setStyle("-fx-font-size: 48px;");

        Label name = new Label(layoutName);
        name.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #FFFFFF;"
        );

        Label desc = new Label(description);
        desc.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #BDC3C7;"
        );
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Button selectBtn = new Button("PILIH LAYOUT");
        selectBtn.setPrefWidth(200);
        selectBtn.setStyle(
                "-fx-background-color: " + accentColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 20;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;"
        );

        selectBtn.setOnAction(action);

        // Efek Hover interaktif pada Kartu
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.15);" +
                "-fx-border-color: #FFD700;" + // Berubah jadi emas saat disentuh
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 20px;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-color: " + accentColor + ";" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 20px;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
        ));

        // Klik pada seluruh card juga memicu tombol
        card.setOnMouseClicked(e -> selectBtn.fire());

        card.getChildren().addAll(icon, name, desc, selectBtn);
        return card;
    }
}