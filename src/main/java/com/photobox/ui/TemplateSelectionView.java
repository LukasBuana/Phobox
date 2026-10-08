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
import javafx.scene.layout.StackPane;
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

        setPadding(new Insets(30));

        // ==================================================
        // HEADER / TITLE & BACK BUTTON
        // ==================================================
        BorderPane header = new BorderPane();
        
        Button backButton = new Button("KEMBALI");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");
        backButton.setOnAction(event -> onBack.run());

        Label title = new Label("Pilih Template Background");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        header.setLeft(backButton);
        header.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        setTop(header);

        // ==================================================
        // TEMPLATE CONTAINER
        // ==================================================
        FlowPane templateContainer =
                new FlowPane();

        templateContainer.setHgap(25);
        templateContainer.setVgap(25);
        templateContainer.setPadding(
                new Insets(30, 0, 0, 0)
        );

        templateContainer.setAlignment(
                Pos.TOP_CENTER
        );

        if (templates.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "Belum ada template tersedia untuk layout ini."
                    );

            emptyLabel.setStyle(
                    "-fx-font-size: 18px;"
            );

            templateContainer
                    .getChildren()
                    .add(emptyLabel);

        } else {

            for (Template template : templates) {

                VBox card =
                        createTemplateCard(template);

                templateContainer
                        .getChildren()
                        .add(card);
            }
        }

        setCenter(templateContainer);
    }

    private VBox createTemplateCard(
            Template template
    ) {

        VBox card = new VBox(10);

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));

        card.setPrefWidth(220);
        card.setPrefHeight(340);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dddddd;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        );

        Pane previewPane =
                createPreview(template);

        Label name =
                new Label(template.getName());

        name.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        Label photoCount =
                new Label(
                        template.getPhotoCount()
                                + " Photos"
                );

        photoCount.setStyle(
                "-fx-text-fill: #666666;" +
                "-fx-font-size: 12px;"
        );

        Button selectButton =
                new Button("Pilih Template");

        selectButton.setMaxWidth(
                Double.MAX_VALUE
        );

        selectButton.setOnAction(event ->
                onTemplateSelected.accept(template)
        );

        card.getChildren().addAll(
                previewPane,
                name,
                photoCount,
                selectButton
        );

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
        double previewWidth = 180;
        double previewHeight = 210;

        double scale = Math.min(
                previewWidth / canvasWidth,
                previewHeight / canvasHeight
        );

        double displayWidth = canvasWidth * scale;
        double displayHeight = canvasHeight * scale;

        Pane pane = new Pane();
        pane.setPrefSize(displayWidth, displayHeight);
        pane.setMaxSize(displayWidth, displayHeight);
        pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

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