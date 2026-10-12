package com.photobox;

import com.photobox.camera.Camera;
import com.photobox.camera.WebcamCamera;
import com.photobox.capture.PhotoCapture;
import com.photobox.session.PhotoSession;
import com.photobox.template.Template;
import com.photobox.template.TemplateRepository;
import com.photobox.template.TemplateSlot;
import com.photobox.transaction.TransactionRepository;
import com.photobox.ui.AttractScreenView; // Layar Idle Baru
import com.photobox.ui.PhotoCaptureView;
import com.photobox.ui.TemplateSelectionView;
import com.photobox.ui.TemplateSessionView;
import com.photobox.template.TemplateRenderer;
import com.photobox.ui.FinalPhotoView;
import com.photobox.ui.LayoutTypeSelectionView;
import com.photobox.ui.PaymentScreenView;

import javafx.animation.PauseTransition; // Import untuk Timer
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent; // Import untuk memantau sentuhan/klik
import javafx.stage.Stage;
import javafx.util.Duration; // Import untuk Durasi

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

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

    // ==========================================
    // VARIABLE IDLE TIMER
    // ==========================================
    private PauseTransition idleTimer;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        initializeServices();

        // 1. Inisialisasi dan jalankan Idle Timer
        setupGlobalIdleTimer();

        // 2. Alur selalu dimulai dari Attract Screen
        showAttractScreen();

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
    // FITUR GLOBAL IDLE TIMER (3 MENIT TIMEOUT)
    // ==========================================================
    private void setupGlobalIdleTimer() {
        // Set durasi timeout (misal: 3 menit)
        idleTimer = new PauseTransition(Duration.minutes(3));

        // Apa yang terjadi jika waktu habis? Panggil resetToIdleState()
        idleTimer.setOnFinished(event -> resetToIdleState());

        // Pantau SETIAP interaksi (Sentuhan layar, Mouse, Keyboard) di seluruh aplikasi
        stage.addEventFilter(InputEvent.ANY, event -> {
            // Jika ada interaksi, reset timer ke awal (kembali ke 3 menit)
            idleTimer.playFromStart();
        });

        // Mulai timer sekarang
        idleTimer.play();
    }

    private void resetToIdleState() {
        System.out.println("⏳ Mesin IDLE selama 3 menit. Mereset seluruh sesi untuk privasi...");

        // 1. Buang tampilan kamera yang sedang aktif (jika ada)
        disposeCurrentCaptureView();

        // 2. Bersihkan sesi foto sementara agar tidak bocor ke orang berikutnya
        if (photoSession != null) {
            photoSession.reset();
        }

        // 3. Batalkan status template dan transaksi yang menggantung
        selectedTemplate = null;
        currentTransactionId = null; 

        // 4. Kembali panggil layar animasi awal
        showAttractScreen();
    }

    // ==========================================================
    // TAHAP 0: ATTRACT SCREEN (IDLE MODE)
    // ==========================================================
    private void showAttractScreen() {
        disposeCurrentCaptureView();

        AttractScreenView view = new AttractScreenView(
                // Jika layar ditekan, pindah ke tahap 1 (Pilih Layout)
                this::showLayoutTypeSelection 
        );

        showView(view);
    }

    // ==========================================================
    // TAHAP 1: PILIH JENIS LAYOUT
    // ==========================================================
    private void showLayoutTypeSelection() {
        disposeCurrentCaptureView();

        LayoutTypeSelectionView view = new LayoutTypeSelectionView(
                this::showTemplateSelection
        );

        showView(view);
    }

    // ==========================================================
    // TAHAP 2: PILIH TEMPLATE
    // ==========================================================
    private void showTemplateSelection(String layoutType) {
        disposeCurrentCaptureView();

        List<Template> templates = templateRepository.findByTypeActive(layoutType);

        TemplateSelectionView view = new TemplateSelectionView(
                templates,
                this::showPaymentScreen, // Pindah ke layar pembayaran sebelum foto
                this::showAttractScreen 
        );

        showView(view);
    }

   // ==========================================================
    // TAHAP 3: LAYAR PEMBAYARAN QRIS
    // ==========================================================
   // ==========================================================
    // TAHAP 3: LAYAR PEMBAYARAN QRIS
    // ==========================================================
    private void showPaymentScreen(Template template) {
        selectedTemplate = template;
        disposeCurrentCaptureView();

        // Buat ID Transaksi Unik untuk Xendit
        currentTransactionId = "TX-" + System.currentTimeMillis();

        PaymentScreenView view = new PaymentScreenView(
                currentTransactionId,
                // Jika Berhasil Bayar (Atau di-klik untuk bypass)
                () -> {
                    // ==========================================
                    // 1. INISIALISASI SESI FOTO (AGAR TIDAK NULL)
                    // ==========================================
                    photoSession = new PhotoSession(selectedTemplate.getPhotoCount());

                    // 2. Catat ke Database Lokal
                    transactionRepository.createTransaction(
                            "#" + currentTransactionId,
                            template.getName(),
                            25000,
                            "QRIS"
                    );
                    System.out.println("Transaksi Dicatat: " + currentTransactionId);

                    // 3. Kirim Notifikasi (Opsional: Telegram/WA)
                    String message = String.format(
                        "🚨 *PEMBAYARAN BERHASIL!*\n\n" +
                        "🆔 ID Transaksi: `%s`\n" +
                        "📦 Paket: *%s*\n" +
                        "💰 Nominal: *Rp 25.000*",
                        currentTransactionId,
                        template.getName()
                    );
                    com.photobox.service.TelegramNotifier.sendNotification(message);

                    // 4. Mulai Sesi Foto Berurutan
                    startSequentialCapture(0);
                },
                // Jika Batal / Timeout
                this::backToTemplateSelection
        );

        showView(view);
    }

    // ==========================================================
    // TAHAP 4: AUTO-CAPTURE BERURUTAN
    // ==========================================================
    private void startSequentialCapture(int slotIndex) {
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

                // ON PHOTO ACCEPTED
                photoPath -> {
                    photoSession.addPhoto(slotIndex, photoPath);
                    startSequentialCapture(slotIndex + 1);
                },

                // ON BACK/BATAL
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

                // ON PHOTO ACCEPTED (Menimpa)
                photoPath -> {
                    photoSession.addPhoto(slotIndex, photoPath);
                    showTemplateSession();
                },

                // ON BACK
                this::showTemplateSession
        );

        showView(photoCaptureView);
    }

    // ==========================================================
    // TAHAP 6: LAYAR REVIEW
    // ==========================================================
    private void showTemplateSession() {
        disposeCurrentCaptureView();

        TemplateSessionView view = new TemplateSessionView(
                selectedTemplate,
                photoSession,

                this::backToTemplateSelection,
                this::startRetakePhoto,
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

                // RETAKE SESI
                this::showTemplateSelectionForCurrentType,

                // PRINT (Setelah selesai ngeprint, biasanya mesin kembali ke layar Attract)
                () -> {
                    System.out.println("PRINT belum diimplementasikan.");
                    System.out.println("File: " + finalPhoto.toAbsolutePath());
                    resetToIdleState(); // Selesai print = reset mesin untuk orang berikutnya
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
        if (idleTimer != null) {
            idleTimer.stop();
        }

        disposeCurrentCaptureView();

        if (camera != null && camera.isRunning()) {
            camera.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}