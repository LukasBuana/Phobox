package com.photobox.ui;

import com.photobox.template.Template;
import com.photobox.template.TemplateRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class TemplateSelectionView extends BorderPane {

    private final TemplateRepository templateRepository;
    private final Consumer<Template> onTemplateSelected;

    public TemplateSelectionView(
            TemplateRepository templateRepository,
            Consumer<Template> onTemplateSelected
    ) {
        this.templateRepository = templateRepository;
        this.onTemplateSelected = onTemplateSelected;

        createView();
    }

    private void createView() {

        setPadding(new Insets(30));

        Label title = new Label("Pilih Template");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        BorderPane.setAlignment(
                title,
                Pos.CENTER
        );

        setTop(title);

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

        List<Template> templates =
                templateRepository.findAllActive();

        if (templates.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "Belum ada template tersedia."
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
        card.setPrefHeight(320);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dddddd;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        );

        ImageView preview =
                createPreview(template);

        Label name =
                new Label(template.getName());

        name.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        template.getDescription() != null
                                ? template.getDescription()
                                : ""
                );

        description.setWrapText(true);
        description.setMaxWidth(190);

        Label photoCount =
                new Label(
                        template.getPhotoCount()
                                + " Photos"
                );

        photoCount.setStyle(
                "-fx-text-fill: #666666;"
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
                preview,
                name,
                description,
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

    private ImageView createPreview(
            Template template
    ) {

        ImageView imageView =
                new ImageView();

        imageView.setFitWidth(180);
        imageView.setFitHeight(210);

        imageView.setPreserveRatio(true);

        String backgroundPath =
                template.getBackgroundPath();

        if (backgroundPath != null) {

            File file =
                    new File(backgroundPath);

            if (file.exists()) {

                Image image =
                        new Image(
                                file.toURI().toString()
                        );

                imageView.setImage(image);
            }
        }

        return imageView;
    }
}