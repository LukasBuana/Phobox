package com.photobox.capture;

import com.photobox.camera.Camera;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhotoCapture {

    private final Camera camera;

    private static final Path PHOTO_DIRECTORY =
            Paths.get("outputs", "photos");

    private static final DateTimeFormatter FILE_NAME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public PhotoCapture(Camera camera) {
        this.camera = camera;
    }

    /**
     * Mengambil frame terbaru dari kamera
     * dan menyimpannya sebagai JPG.
     *
     * @return Path file foto yang berhasil disimpan
     */
    public Path capture() throws IOException {

        if (!camera.isRunning()) {
            throw new IllegalStateException(
                    "Camera belum aktif."
            );
        }

        BufferedImage image = camera.getImage();

        if (image == null) {
            throw new IllegalStateException(
                    "Tidak mendapatkan frame dari kamera."
            );
        }

        // Pastikan folder output tersedia
        Files.createDirectories(PHOTO_DIRECTORY);

        // Nama file unik berdasarkan waktu
        String fileName =
                "photo_"
                + LocalDateTime.now()
                    .format(FILE_NAME_FORMAT)
                + ".jpg";

        Path outputPath =
                PHOTO_DIRECTORY.resolve(fileName);

        File outputFile =
                outputPath.toFile();

        boolean success =
                ImageIO.write(
                        image,
                        "JPG",
                        outputFile
                );

        if (!success) {
            throw new IOException(
                    "Gagal menyimpan gambar sebagai JPG."
            );
        }

        System.out.println(
                "Photo saved: "
                + outputPath.toAbsolutePath()
        );

        return outputPath;
    }
}
