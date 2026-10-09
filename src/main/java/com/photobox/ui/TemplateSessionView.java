package com.photobox.ui;

import com.photobox.session.PhotoSession;
import com.photobox.template.Template;
import com.photobox.template.TemplateSlot;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class TemplateSessionView extends BorderPane {

    private final Template template;
    private final PhotoSession session;

    private final Runnable onBack;
    private final Consumer<Integer> onSlotSelected;
    private final Runnable onContinue;

    private Pane templateCanvas;
    private StackPane centerContainer; // Tambahan: Kontainer untuk menumpuk Pop-up
    private Button continueButton;

    public TemplateSessionView(
            Template template,
            PhotoSession session,
            Runnable onBack,
            Consumer<Integer> onSlotSelected,
            Runnable onContinue
    ) {

        this.template = template;
        this.session = session;

        this.onBack = onBack;
        this.onSlotSelected = onSlotSelected;
        this.onContinue = onContinue;

        createView();
    }

    private void createView() {

        setPadding(new Insets(20));

        Label title =
                new Label(
                        template.getName()
                );

        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        BorderPane.setAlignment(
                title,
                Pos.CENTER
        );

        setTop(title);

        templateCanvas =
                createTemplateCanvas();

        // ==================================================
        // BUNGKUS CANVAS DENGAN STACKPANE AGAR BISA ADA POPUP
        // ==================================================
        centerContainer = new StackPane(templateCanvas);
        setCenter(centerContainer);

        setBottom(
                createBottomBar()
        );
    }

    private Pane createTemplateCanvas() {

        int canvasWidth =
                template.getCanvasWidth();

        int canvasHeight =
                template.getCanvasHeight();

        // Sesuaikan nilai maxWidth dan maxHeight agar tidak terlalu besar dan pas di layar
        double maxWidth = 650;
        double maxHeight = 550;

        double scale =
                Math.min(
                        maxWidth / canvasWidth,
                        maxHeight / canvasHeight
                );

        double displayWidth =
                canvasWidth * scale;

        double displayHeight =
                canvasHeight * scale;

        Pane canvas =
                new Pane();

        canvas.setPrefSize(
                displayWidth,
                displayHeight
        );

        canvas.setMaxSize(
                displayWidth,
                displayHeight
        );

        // ==================================================
        // AMANKAN PEMUATAN BACKGROUND DENGAN TRY-CATCH
        // ==================================================
        String backgroundPath =
                template.getBackgroundPath();

        if (backgroundPath != null && !backgroundPath.trim().isEmpty()) {
            try {
                File backgroundFile = new File(backgroundPath);

                if (backgroundFile.exists()) {
                    Image background =
                            new Image(
                                    backgroundFile
                                            .toURI()
                                            .toString()
                            );

                    ImageView backgroundView =
                            new ImageView(background);

                    backgroundView.setFitWidth(
                            displayWidth
                    );

                    backgroundView.setFitHeight(
                            displayHeight
                    );

                    backgroundView.setPreserveRatio(
                            false
                    );

                    canvas.getChildren()
                            .add(backgroundView);
                }
            } catch (Exception e) {
                System.err.println("Gagal memuat background, melewati background: " + e.getMessage());
            }
        }

        // ==================================================
        // SLOT FOTO DIJAMIN SELALU DIGAMBAR KE KANVAS
        // ==================================================
        for (TemplateSlot slot :
                template.getSlots()) {

            createSlotView(
                    canvas,
                    slot,
                    scale
            );
        }

        return canvas;
    }

    private void createSlotView(
            Pane canvas,
            TemplateSlot slot,
            double scale
    ) {

        int slotIndex =
                slot.getSlotIndex();

        double x =
                slot.getX() * scale;

        double y =
                slot.getY() * scale;

        double targetWidth =
                slot.getWidth() * scale;

        double targetHeight =
                slot.getHeight() * scale;

        StackPane slotContainer =
                new StackPane();

        slotContainer.setLayoutX(x);
        slotContainer.setLayoutY(y);

        slotContainer.setPrefSize(
                targetWidth,
                targetHeight
        );

        Path photoPath =
                session.getPhoto(slotIndex);

        if (photoPath != null) {

            Image image =
                    new Image(
                            photoPath
                                    .toFile()
                                    .toURI()
                                    .toString()
                    );

            ImageView imageView =
                    new ImageView(image);

            // ==================================================
            // CENTER CROP LOGIC
            // ==================================================
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            double targetRatio = targetWidth / targetHeight;
            double imageRatio = imageWidth / imageHeight;

            double cropWidth, cropHeight, xOffset, yOffset;

            if (imageRatio > targetRatio) {
                cropHeight = imageHeight;
                cropWidth = imageHeight * targetRatio;
                xOffset = (imageWidth - cropWidth) / 2;
                yOffset = 0;
            } else {
                cropWidth = imageWidth;
                cropHeight = imageWidth / targetRatio;
                xOffset = 0;
                yOffset = (imageHeight - cropHeight) / 2;
            }

            imageView.setViewport(
                    new Rectangle2D(xOffset, yOffset, cropWidth, cropHeight)
            );

            imageView.setFitWidth(targetWidth);
            imageView.setFitHeight(targetHeight);
            
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            slotContainer.getChildren().add(imageView);

            // ==================================================
            // EVENT LISTENER: KLIK FOTO UNTUK MUNCULKAN POPUP
            // ==================================================
            slotContainer.setStyle("-fx-cursor: hand;");
            slotContainer.setOnMouseClicked(event -> showRetakePopup(slotIndex));

        } else {

            createEmptySlot(
                    slotContainer,
                    slotIndex,
                    targetWidth,
                    targetHeight
            );
        }

        canvas.getChildren()
                .add(slotContainer);
    }

    private void createEmptySlot(
            StackPane container,
            int slotIndex,
            double width,
            double height
    ) {

        Label label =
                new Label(
                        "PHOTO " + (slotIndex + 1)
                );

        label.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #666666;"
        );

        container.setStyle(
                "-fx-background-color: rgba(240,240,240,0.85);" +
                "-fx-border-color: #999999;" +
                "-fx-border-width: 2;" +
                "-fx-border-style: dashed;" +
                "-fx-cursor: hand;"
        );

        container.getChildren()
                .add(label);

        container.setOnMouseClicked(event ->
                onSlotSelected.accept(slotIndex)
        );
    }

    // ==================================================
    // POP-UP KONFIRMASI RETAKE (IN-APP OVERLAY)
    // ==================================================
    private void showRetakePopup(int slotIndex) {
        
        // 1. Background Overlay (Hitam Semi-Transparan)
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");

        // 2. Dialog Box Putih
        VBox dialogBox = new VBox(25);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(30, 40, 30, 40));
        dialogBox.setMaxWidth(400);
        dialogBox.setMaxHeight(200);
        dialogBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 0);"
        );

        Label message = new Label("Ingin mengambil ulang (retake) foto ini?");
        message.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #333333;"
        );
        message.setWrapText(true);

        // 3. Tombol Aksi
        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(
                "-fx-background-color: #bdc3c7; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        
        Button btnRetake = new Button("Retake Foto");
        btnRetake.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;"
        );

        // Event Tombol
        btnBatal.setOnAction(e -> centerContainer.getChildren().remove(overlay)); // Tutup pop-up
        btnRetake.setOnAction(e -> {
            centerContainer.getChildren().remove(overlay); // Tutup pop-up
            onSlotSelected.accept(slotIndex); // Buka kamera untuk slot ini
        });

        HBox buttons = new HBox(15, btnBatal, btnRetake);
        buttons.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(message, buttons);
        overlay.getChildren().add(dialogBox);

        // Tampilkan overlay di dalam layar
        centerContainer.getChildren().add(overlay);
    }

    private BorderPane createBottomBar() {

        BorderPane bottom =
                new BorderPane();

        bottom.setPadding(
                new Insets(20, 0, 0, 0)
        );

        Button backButton =
                new Button("BACK");

        backButton.setOnAction(
                event -> onBack.run()
        );

        continueButton =
                new Button("CONTINUE");

        continueButton.setDisable(
                !session.isComplete()
        );

        continueButton.setOnAction(
                event -> onContinue.run()
        );

        bottom.setLeft(backButton);
        bottom.setRight(continueButton);

        return bottom;
    }

    public void refresh() {

        templateCanvas =
                createTemplateCanvas();

        // Refresh juga membutuhkan inisialisasi ulang StackPane centerContainer
        centerContainer = new StackPane(templateCanvas);
        setCenter(centerContainer);

        if (continueButton != null) {

            continueButton.setDisable(
                    !session.isComplete()
            );
        }
    }
}