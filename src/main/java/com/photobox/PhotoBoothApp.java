package com.photobox;

import com.photobox.camera.Camera;
import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;
import com.photobox.session.PhotoSession;
import com.photobox.template.Template;
import com.photobox.template.TemplateRepository;
import com.photobox.template.TemplateSlot;
import com.photobox.ui.PhotoCaptureView;
import com.photobox.ui.TemplateSelectionView;
import com.photobox.ui.TemplateSessionView;
import com.photobox.template.TemplateRenderer;
import com.photobox.ui.FinalPhotoView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class PhotoBoothApp extends Application {

    private Stage stage;

    private Camera camera;
    private PhotoCapture photoCapture;

    private TemplateRepository templateRepository;

    private Template selectedTemplate;
    private PhotoSession photoSession;

    private PhotoCaptureView photoCaptureView;
    private TemplateRenderer templateRenderer;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        initializeServices();

        // Alur dimulai dengan memilih jenis layout terlebih dahulu
        showLayoutTypeSelection();

        stage.setTitle("PHOTOBOX");

        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");

        stage.show();
    }

    private void initializeServices() {

        camera = new WebcamCamera();
        camera.start();

        photoCapture = new PhotoCapture(camera);
        templateRepository = new TemplateRepository();
        templateRenderer = new TemplateRenderer();
    }

    // ==========================================================
    // TAHAP 1: PILIH JENIS LAYOUT (2x2, 3x3, dll)
    // ==========================================================
    private void showLayoutTypeSelection() {
        disposeCurrentCaptureView();

        VBox layoutRoot = new VBox(25);
        layoutRoot.setAlignment(Pos.CENTER);
        layoutRoot.setStyle("-fx-background-color: #111111; -fx-padding: 40;");

        Label title = new Label("PILIH JENIS LAYOUT");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button btn2x2 = new Button("LAYOUT 2x2 (4 Foto)");
        btn2x2.setPrefWidth(300);
        btn2x2.setStyle("-fx-font-size: 18px; -fx-padding: 15;");
        btn2x2.setOnAction(e -> showTemplateSelection("2x2"));

        Button btn3x3 = new Button("LAYOUT 3x3 (9 Foto)");
        btn3x3.setPrefWidth(300);
        btn3x3.setStyle("-fx-font-size: 18px; -fx-padding: 15;");
        btn3x3.setOnAction(e -> showTemplateSelection("3x3"));

        layoutRoot.getChildren().addAll(title, btn2x2, btn3x3);

        showView(layoutRoot);
    }

    // ==========================================================
    // TAHAP 2: PILIH TEMPLATE BERDASARKAN TIPE
    // ==========================================================
    private void showTemplateSelection(String layoutType) {
        disposeCurrentCaptureView();

        List<Template> templates = templateRepository.findByTypeActive(layoutType);

        TemplateSelectionView view = new TemplateSelectionView(
                templates,
                this::startTemplateSession,
                this::showLayoutTypeSelection // Tombol kembali untuk ganti jenis layout
        );

        showView(view);
    }

    // ==========================================================
    // TEMPLATE SESSION
    // ==========================================================
    private void startTemplateSession(Template template) {
        selectedTemplate = template;

        photoSession = new PhotoSession(
                template.getPhotoCount());

        showTemplateSession();
    }

    private void showTemplateSession() {

        disposeCurrentCaptureView();

        TemplateSessionView view = new TemplateSessionView(
                selectedTemplate,
                photoSession,

                // BACK
                this::backToTemplateSelection,

                // SLOT SELECTED
                this::startPhotoCapture,

                // CONTINUE
                this::finishSession);

        showView(view);
    }

    // ==========================================================
    // PHOTO CAPTURE
    // ==========================================================
    private void startPhotoCapture(int slotIndex) {

        disposeCurrentCaptureView();

        TemplateSlot targetSlot =
                selectedTemplate.getSlots().get(slotIndex);

        photoCaptureView = new PhotoCaptureView(
                camera,
                photoCapture,
                targetSlot,

                // USE PHOTO
                photoPath -> photoAccepted(
                        slotIndex,
                        photoPath),

                // BACK
                this::showTemplateSession);

        showView(photoCaptureView);
    }

    private void photoAccepted(
            int slotIndex,
            Path photoPath) {

        photoSession.addPhoto(
                slotIndex,
                photoPath);

        showTemplateSession();
    }

    // ==========================================================
    // FINISH SESSION
    // ==========================================================
    private void finishSession() {

        try {

            Path finalPhoto = templateRenderer.render(
                    selectedTemplate,
                    photoSession);

            System.out.println("================================");
            System.out.println("SESSION FINISHED");
            System.out.println("Final photo: " + finalPhoto.toAbsolutePath());
            System.out.println("================================");

            showFinalPhoto(finalPhoto);

        } catch (Exception e) {
            System.err.println("Gagal membuat final photo:");
            e.printStackTrace();
        }
    }

    private void showFinalPhoto(Path finalPhoto) {

        FinalPhotoView view = new FinalPhotoView(
                finalPhoto,

                // RETAKE
                this::showTemplateSelectionForCurrentType,

                // PRINT
                () -> {
                    System.out.println("PRINT belum diimplementasikan.");
                    System.out.println("File: " + finalPhoto.toAbsolutePath());
                });

        showView(view);
    }

    // ==========================================================
    // NAVIGATION HELPERS
    // ==========================================================
    private void showTemplateSelectionForCurrentType() {
        if (selectedTemplate != null) {
            showTemplateSelection(selectedTemplate.getName()); // atau ambil type dari selectedTemplate
        } else {
            showLayoutTypeSelection();
        }
    }

    private void backToTemplateSelection() {

        if (photoSession != null) {
            photoSession.reset();
        }

        selectedTemplate = null;

        showLayoutTypeSelection();
    }

    private void showView(javafx.scene.Parent view) {
        Scene scene = stage.getScene();

        if (scene == null) {
            scene = new Scene(view);
            stage.setScene(scene);
        } else {
            scene.setRoot(view);
        }
    }

    private void disposeCurrentCaptureView() {
        if (photoCaptureView != null) {
            photoCaptureView.dispose();
            photoCaptureView = null;
        }
    }

    @Override
    public void stop() {
        disposeCurrentCaptureView();

        if (camera != null && camera.isRunning()) {
            camera.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}