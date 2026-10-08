package com.photobox;

import com.photobox.camera.Camera;
import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;
import com.photobox.session.PhotoSession;
import com.photobox.template.Template;
import com.photobox.template.TemplateRepository;
import com.photobox.ui.PhotoCaptureView;
import com.photobox.ui.TemplateSelectionView;
import com.photobox.ui.TemplateSessionView;
import com.photobox.template.TemplateRenderer;
import com.photobox.ui.FinalPhotoView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

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

        showTemplateSelection();

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
    // TEMPLATE SELECTION
    // ==========================================================

    private void showTemplateSelection() {

        disposeCurrentCaptureView();

        TemplateSelectionView view = new TemplateSelectionView(
                templateRepository,
                this::startTemplateSession);

        showView(view);
    }

    // ==========================================================
    // TEMPLATE SESSION
    // ==========================================================

    private void startTemplateSession(
            Template template) {

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

    private void startPhotoCapture(
            int slotIndex) {

        disposeCurrentCaptureView();

        photoCaptureView = new PhotoCaptureView(
                camera,
                photoCapture,
                slotIndex,

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

            System.out.println(
                    "================================");

            System.out.println(
                    "SESSION FINISHED");

            System.out.println(
                    "Final photo:");

            System.out.println(
                    finalPhoto.toAbsolutePath());

            System.out.println(
                    "================================");

            showFinalPhoto(finalPhoto);

        } catch (Exception e) {

            System.err.println(
                    "Gagal membuat final photo:");

            e.printStackTrace();
        }
    }

    private void showFinalPhoto(
            Path finalPhoto) {

        FinalPhotoView view = new FinalPhotoView(

                finalPhoto,

                // RETAKE
                this::showTemplateSession,

                // PRINT
                () -> {

                    System.out.println(
                            "PRINT belum diimplementasikan.");

                    System.out.println(
                            "File:");

                    System.out.println(
                            finalPhoto.toAbsolutePath());
                });

        showView(view);
    }

    // ==========================================================
    // NAVIGATION
    // ==========================================================

    private void backToTemplateSelection() {

        if (photoSession != null) {
            photoSession.reset();
        }

        selectedTemplate = null;

        showTemplateSelection();
    }

    private void showView(
            javafx.scene.Parent view) {

        Scene scene = stage.getScene();

        if (scene == null) {

            scene = new Scene(
                    view);

            stage.setScene(scene);

        } else {

            scene.setRoot(view);
        }
    }

    // ==========================================================
    // CAMERA / VIEW CLEANUP
    // ==========================================================

    private void disposeCurrentCaptureView() {

        if (photoCaptureView != null) {

            photoCaptureView.dispose();

            photoCaptureView = null;
        }
    }

    @Override
    public void stop() {

        disposeCurrentCaptureView();

        if (camera != null &&
                camera.isRunning()) {

            camera.stop();
        }
    }

    // ==========================================================
    // MAIN
    // ==========================================================

    public static void main(String[] args) {

        launch(args);
    }
}