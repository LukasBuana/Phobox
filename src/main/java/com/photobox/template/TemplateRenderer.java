package com.photobox.template;

import com.photobox.session.PhotoSession;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemplateRenderer {

    private static final Path OUTPUT_DIRECTORY =
            Paths.get("outputs", "final");

    private static final DateTimeFormatter FILE_NAME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd_HHmmss_SSS"
            );

    /**
     * Render seluruh foto dalam PhotoSession
     * ke dalam template dan menghasilkan
     * satu file JPG final.
     */
    public Path render(
            Template template,
            PhotoSession session
    ) throws IOException {

        if (template == null) {
            throw new IllegalArgumentException(
                    "Template tidak boleh null."
            );
        }

        if (session == null) {
            throw new IllegalArgumentException(
                    "PhotoSession tidak boleh null."
            );
        }

        if (!session.isComplete()) {
            throw new IllegalStateException(
                    "PhotoSession belum lengkap."
            );
        }

        // ==================================================
        // BUAT CANVAS
        // ==================================================

        int canvasWidth =
                template.getCanvasWidth();

        int canvasHeight =
                template.getCanvasHeight();

        BufferedImage canvas =
                new BufferedImage(
                        canvasWidth,
                        canvasHeight,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                canvas.createGraphics();

        try {

            configureRenderingQuality(
                    graphics
            );

            // ==================================================
            // BACKGROUND
            // ==================================================

            drawBackground(
                    graphics,
                    template
            );

            // ==================================================
            // PHOTO SLOTS
            // ==================================================

            for (TemplateSlot slot :
                    template.getSlots()) {

                Path photoPath =
                        session.getPhoto(
                                slot.getSlotIndex()
                        );

                if (photoPath == null) {
                    throw new IllegalStateException(
                            "Foto untuk slot "
                                    + slot.getSlotIndex()
                                    + " belum tersedia."
                    );
                }

                drawPhoto(
                        graphics,
                        photoPath,
                        slot
                );
            }

        } finally {

            graphics.dispose();
        }

        // ==================================================
        // SAVE FINAL IMAGE
        // ==================================================

        Files.createDirectories(
                OUTPUT_DIRECTORY
        );

        String fileName =
                "final_"
                        + LocalDateTime.now()
                        .format(FILE_NAME_FORMAT)
                        + ".jpg";

        Path outputPath =
                OUTPUT_DIRECTORY.resolve(
                        fileName
                );

        boolean success =
                ImageIO.write(
                        canvas,
                        "JPG",
                        outputPath.toFile()
                );

        if (!success) {
            throw new IOException(
                    "Gagal menyimpan hasil render JPG."
            );
        }

        System.out.println(
                "Final image saved: "
                        + outputPath
                                .toAbsolutePath()
        );

        return outputPath;
    }

    // ==========================================================
    // BACKGROUND
    // ==========================================================

    private void drawBackground(
            Graphics2D graphics,
            Template template
    ) throws IOException {

        String backgroundPath =
                template.getBackgroundPath();

        if (backgroundPath == null ||
                backgroundPath.isBlank()) {

            /*
             * Jika template tidak memiliki
             * background, gunakan putih.
             */
            graphics.setColor(
                    java.awt.Color.WHITE
            );

            graphics.fillRect(
                    0,
                    0,
                    template.getCanvasWidth(),
                    template.getCanvasHeight()
            );

            return;
        }

        File backgroundFile =
                new File(backgroundPath);

        if (!backgroundFile.exists()) {

            throw new IOException(
                    "Background template tidak ditemukan: "
                            + backgroundFile
                                    .getAbsolutePath()
            );
        }

        BufferedImage background =
                ImageIO.read(backgroundFile);

        if (background == null) {

            throw new IOException(
                    "Background template tidak dapat dibaca: "
                            + backgroundFile
                                    .getAbsolutePath()
            );
        }

        graphics.drawImage(
                background,
                0,
                0,
                template.getCanvasWidth(),
                template.getCanvasHeight(),
                null
        );
    }

    // ==========================================================
    // PHOTO
    // ==========================================================

    private void drawPhoto(
        Graphics2D graphics,
        Path photoPath,
        TemplateSlot slot
) throws IOException {

    File photoFile =
            photoPath.toFile();

    if (!photoFile.exists()) {

        throw new IOException(
                "File foto tidak ditemukan: "
                        + photoFile.getAbsolutePath()
        );
    }

    BufferedImage photo =
            ImageIO.read(photoFile);

    if (photo == null) {

        throw new IOException(
                "File foto tidak dapat dibaca: "
                        + photoFile.getAbsolutePath()
        );
    }

    int targetX = slot.getX();
    int targetY = slot.getY();

    int targetWidth = slot.getWidth();
    int targetHeight = slot.getHeight();

    BufferedImage croppedPhoto =
            cropToFill(
                    photo,
                    targetWidth,
                    targetHeight
            );

    graphics.drawImage(
            croppedPhoto,
            targetX,
            targetY,
            targetWidth,
            targetHeight,
            null
    );
}

private BufferedImage cropToFill(
        BufferedImage source,
        int targetWidth,
        int targetHeight
) {

    double sourceRatio =
            (double) source.getWidth()
                    / source.getHeight();

    double targetRatio =
            (double) targetWidth
                    / targetHeight;

    int cropWidth;
    int cropHeight;

    int cropX;
    int cropY;

    if (sourceRatio > targetRatio) {

        /*
         * Foto lebih lebar daripada slot.
         *
         * Potong bagian kiri dan kanan.
         */

        cropHeight =
                source.getHeight();

        cropWidth =
                (int) (
                        cropHeight
                                * targetRatio
                );

        cropX =
                (source.getWidth()
                        - cropWidth) / 2;

        cropY = 0;

    } else {

        /*
         * Foto lebih tinggi daripada slot.
         *
         * Potong bagian atas dan bawah.
         */

        cropWidth =
                source.getWidth();

        cropHeight =
                (int) (
                        cropWidth
                                / targetRatio
                );

        cropX = 0;

        cropY =
                (source.getHeight()
                        - cropHeight) / 2;
    }

    return source.getSubimage(
            cropX,
            cropY,
            cropWidth,
            cropHeight
    );
}

    // ==========================================================
    // RENDER QUALITY
    // ==========================================================

    private void configureRenderingQuality(
            Graphics2D graphics
    ) {

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
}