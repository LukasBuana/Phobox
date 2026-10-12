package com.photobox.ui;

import com.photobox.template.Template;
import com.photobox.template.TemplateSlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class TemplateSelectionView extends BorderPane {

    private final List<Template> templates;
    private final Consumer<Template> onTemplateSelected;
    private final Runnable onBack;

    public TemplateSelectionView(
            List<Template> templates,
            Consumer<Template> onTemplateSelected,
            Runnable onBack
    ) {
        this.templates = templates;
        this.onTemplateSelected = onTemplateSelected;
        this.onBack = onBack;

        createView();
    }

    private void createView() {

        // ==================================================
        // MEMASANG BACKGROUND TEMA NATAL
        // ==================================================
        // Pastikan file "Cozy Christmas Photo Booth Backdrop.png" berada 
        // di root folder project Anda (sejajar dengan src atau pom.xml).
        // Jika berada di dalam folder assets, ubah menjadi url('file:assets/Cozy Christmas Photo Booth Backdrop.png')
        this.setStyle(
                "-fx-background-image: url('file:assets/background.png');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center center;" +
                "-fx-background-repeat: no-repeat;"
        );

        setPadding(new Insets(40));

        // ==================================================
        // HEADER / TITLE & BACK BUTTON
        // ==================================================
        BorderPane header = new BorderPane();
        
        Button backButton = new Button("⬅ KEMBALI");
        backButton.setStyle(
                "-fx-background-color: rgba(192, 57, 43, 0.9);" + // Merah Natal
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" + 
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 20;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
        );
        backButton.setOnAction(event -> onBack.run());

        Label title = new Label("Pilih Template Background");
        title.setStyle(
                "-fx-font-size: 36px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);" // Shadow pekat agar terbaca di background bergambar
        );

        header.setLeft(backButton);
        header.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(backButton, Pos.CENTER_LEFT);

        setTop(header);

        // ==================================================
        // TEMPLATE CONTAINER
        // ==================================================
        FlowPane templateContainer = new FlowPane();
        templateContainer.setHgap(35);
        templateContainer.setVgap(35);
        templateContainer.setPadding(new Insets(40, 0, 0, 0));
        templateContainer.setAlignment(Pos.TOP_CENTER);

        if (templates.isEmpty()) {
            Label emptyLabel = new Label("Belum ada template tersedia untuk layout ini.");
            emptyLabel.setStyle(
                    "-fx-font-size: 20px;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 0);"
            );
            templateContainer.getChildren().add(emptyLabel);
        } else {
            for (Template template : templates) {
                VBox card = createTemplateCard(template);
                templateContainer.getChildren().add(card);
            }
        }

        setCenter(templateContainer);
    }

    private VBox createTemplateCard(Template template) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));

        card.setPrefWidth(240);
        card.setPrefHeight(380);

        // ==================================================
        // EFEK FROSTED GLASS PADA KARTU (Putih Transparan 85%)
        // ==================================================
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85);" +
                "-fx-border-color: #27ae60;" + // Hijau Natal
                "-fx-border-width: 3px;" +
                "-fx-border-radius: 15px;" +
                "-fx-background-radius: 15px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 0);" +
                "-fx-cursor: hand;"
        );

        Pane previewPane = createPreview(template);

        Label name = new Label(template.getName());
        name.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
        );

        Label photoCount = new Label(template.getPhotoCount() + " Photos");
        photoCount.setStyle(
                "-fx-text-fill: #e74c3c;" + // Aksen tulisan Merah Natal
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        Button selectButton = new Button("PILIH TEMPLATE");
        selectButton.setMaxWidth(Double.MAX_VALUE);
        selectButton.setStyle(
                "-fx-background-color: #27ae60;" + // Tombol Hijau
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
        );

        selectButton.setOnAction(event -> onTemplateSelected.accept(template));

        card.getChildren().addAll(
                previewPane,
                name,
                photoCount,
                selectButton
        );

        // Efek Hover Interaktif Pada Kartu
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" + // Sedikit lebih pekat saat disentuh
                "-fx-border-color: #e74c3c;" + // Border berubah Merah
                "-fx-border-width: 3px;" +
                "-fx-border-radius: 15px;" +
                "-fx-background-radius: 15px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);" +
                "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85);" +
                "-fx-border-color: #27ae60;" +
                "-fx-border-width: 3px;" +
                "-fx-border-radius: 15px;" +
                "-fx-background-radius: 15px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 0);" +
                "-fx-cursor: hand;"
        ));

        card.setOnMouseClicked(event -> {
            if (event.getTarget() != selectButton) {
                onTemplateSelected.accept(template);
            }
        });

        return card;
    }

    private Pane createPreview(Template template) {
        int canvasWidth = template.getCanvasWidth();
        int canvasHeight = template.getCanvasHeight();

        // Ukuran mini preview pada kartu
        double previewWidth = 190;
        double previewHeight = 220;

        double scale = Math.min(
                previewWidth / canvasWidth,
                previewHeight / canvasHeight
        );

        double displayWidth = canvasWidth * scale;
        double displayHeight = canvasHeight * scale;

        Pane pane = new Pane();
        pane.setPrefSize(displayWidth, displayHeight);
        pane.setMaxSize(displayWidth, displayHeight);
        pane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        // 1. Muat Background jika ada
        String backgroundPath = template.getBackgroundPath();
        if (backgroundPath != null && !backgroundPath.trim().isEmpty()) {
            try {
                File file = new File(backgroundPath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(displayWidth);
                    imageView.setFitHeight(displayHeight);
                    imageView.setPreserveRatio(false);
                    pane.getChildren().add(imageView);
                }
            } catch (Exception e) {
                // Abaikan jika background gagal dimuat
            }
        }

        // 2. Render Kotak Slot Mini secara Proporsional
        for (TemplateSlot slot : template.getSlots()) {
            double x = slot.getX() * scale;
            double y = slot.getY() * scale;
            double w = slot.getWidth() * scale;
            double h = slot.getHeight() * scale;

            Pane slotBox = new Pane();
            slotBox.setLayoutX(x);
            slotBox.setLayoutY(y);
            slotBox.setPrefSize(w, h);
            slotBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                    "-fx-border-color: #666666;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-style: dashed;"
            );

            pane.getChildren().add(slotBox);
        }

        return pane;
    }
}