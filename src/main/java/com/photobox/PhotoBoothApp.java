package com.photobox;

import com.photobox.camera.Camera;
import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;
import com.photobox.session.PhotoSession;
import com.photobox.template.Template;
import com.photobox.template.TemplateRepository;
import com.photobox.template.TemplateSlot;
import com.photobox.transaction.TransactionRepository;
import com.photobox.ui.PhotoCaptureView;
import com.photobox.ui.TemplateSelectionView;
import com.photobox.ui.TemplateSessionView;
import com.photobox.template.TemplateRenderer;
import com.photobox.ui.FinalPhotoView;
import com.photobox.ui.LayoutTypeSelectionView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDateTime;
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

    private TransactionRepository transactionRepository = new TransactionRepository();
    private String currentTransactionId;

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

        // Menggunakan UI bertema Natal yang sudah dipisah ke package ui
        LayoutTypeSelectionView view = new LayoutTypeSelectionView(
                layoutType -> showTemplateSelection(layoutType)
        );

        showView(view);
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
    // TAHAP 3: MULAI SESI & TRANSAKSI
    // ==========================================================
    private void startTemplateSession(Template template) {
        selectedTemplate = template;
        photoSession = new PhotoSession(template.getPhotoCount());

        // Generate ID Transaksi unik berbasis tanggal/waktu
        currentTransactionId = "#" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        
        // Simpan transaksi ke database
        transactionRepository.createTransaction(
                currentTransactionId,
                template.getName(),
                25000,
                "QRIS"
        );

        System.out.println("Transaksi Dicatat: " + currentTransactionId + " | Paket: " + template.getName());

        // Langsung mulai proses auto-capture dari slot pertama (0)
        startSequentialCapture(0);
    }

    // ==========================================================
    // TAHAP 4: AUTO-CAPTURE BERURUTAN (SEQUENTIAL)
    // ==========================================================
    private void startSequentialCapture(int slotIndex) {
        // Jika index sudah mencapai total foto yang dibutuhkan, tampilkan layar review
        if (slotIndex >= selectedTemplate.getPhotoCount()) {
            showTemplateSession();
            return;
        }

        disposeCurrentCaptureView();

        TemplateSlot targetSlot = selectedTemplate.getSlots().get(slotIndex);

        photoCaptureView = new PhotoCaptureView(
                camera,
                photoCapture,
                targetSlot,

                // ON PHOTO ACCEPTED: Simpan foto lalu otomatis jepret slot berikutnya
                photoPath -> {
                    photoSession.addPhoto(slotIndex, photoPath);
                    startSequentialCapture(slotIndex + 1);
                },

                // ON BACK: Batalkan sesi, kembali ke layar pilih template
                this::backToTemplateSelection
        );

        showView(photoCaptureView);
    }

    // ==========================================================
    // TAHAP 5: RETAKE DARI LAYAR REVIEW
    // ==========================================================
    private void startRetakePhoto(int slotIndex) {
        disposeCurrentCaptureView();

        TemplateSlot targetSlot = selectedTemplate.getSlots().get(slotIndex);

        photoCaptureView = new PhotoCaptureView(
                camera,
                photoCapture,
                targetSlot,

                // ON PHOTO ACCEPTED: Timpa foto lama, lalu kembali ke layar review
                photoPath -> {
                    photoSession.addPhoto(slotIndex, photoPath);
                    showTemplateSession();
                },

                // ON BACK: Batal retake, kembali ke layar review
                this::showTemplateSession
        );

        showView(photoCaptureView);
    }

    // ==========================================================
    // TAHAP 6: LAYAR REVIEW (TEMPLATE SESSION VIEW)
    // ==========================================================
    private void showTemplateSession() {
        disposeCurrentCaptureView();

        TemplateSessionView view = new TemplateSessionView(
                selectedTemplate,
                photoSession,

                // BACK
                this::backToTemplateSelection,

                // SLOT SELECTED -> Memicu Retake
                this::startRetakePhoto,

                // CONTINUE -> Selesai dan render
                this::finishSession
        );

        showView(view);
    }

    // ==========================================================
    // TAHAP 7: FINISH SESSION & RENDER
    // ==========================================================
    private void finishSession() {
        try {
            Path finalPhoto = templateRenderer.render(
                    selectedTemplate,
                    photoSession
            );

            if (currentTransactionId != null) {
                transactionRepository.updateResolvedAt(currentTransactionId);
            }

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

                // RETAKE SESI (Mulai ulang template ini)
                this::showTemplateSelectionForCurrentType,

                // PRINT
                () -> {
                    System.out.println("PRINT belum diimplementasikan.");
                    System.out.println("File: " + finalPhoto.toAbsolutePath());
                }
        );

        showView(view);
    }

    // ==========================================================
    // NAVIGATION HELPERS & CLEANUP
    // ==========================================================
    private void showTemplateSelectionForCurrentType() {
        if (selectedTemplate != null) {
            showTemplateSelection(selectedTemplate.getName());
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