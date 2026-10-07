package com.photobox.camera;

import java.awt.image.BufferedImage;

public interface Camera {

    /**
     * Membuka kamera.
     */
    void start();

    /**
     * Menutup kamera.
     */
    void stop();

    /**
     * Mengecek apakah kamera sedang aktif.
     */
    boolean isRunning();

    /**
     * Mengambil frame terbaru dari kamera.
     */
    BufferedImage getImage();
}
