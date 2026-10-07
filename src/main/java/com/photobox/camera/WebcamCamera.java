package com.photobox.camera;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.sarxos.webcam.Webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class WebcamCamera implements Camera {

    private Webcam webcam;

    @Override
    public void start() {

        if (isRunning()) {
            return;
        }

        System.out.println("Starting webcam...");

        // Gunakan native Windows camera driver
        Webcam.setDriver(new NativeDriver());

        webcam = Webcam.getDefault();

        if (webcam == null) {
            throw new IllegalStateException(
                    "Tidak ada webcam yang terdeteksi."
            );
        }

        System.out.println(
                "Camera detected: " + webcam.getName()
        );

        // Resolusi awal untuk prototype
        webcam.setViewSize(
                new Dimension(1920, 1080)
        );

        webcam.open();

        System.out.println("Webcam started.");
    }

    @Override
    public void stop() {

        if (webcam != null && webcam.isOpen()) {

            System.out.println("Stopping webcam...");

            webcam.close();

            System.out.println("Webcam stopped.");
        }
    }

    @Override
    public boolean isRunning() {
        return webcam != null && webcam.isOpen();
    }

    /**
     * Mengambil frame terbaru dari kamera.
     */
    public BufferedImage getImage() {

        if (!isRunning()) {
            return null;
        }

        return webcam.getImage();
    }
}
