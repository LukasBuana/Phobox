package com.photobox.session;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhotoSession {

    private final int maxPhotos;

    /*
     * Menyimpan foto berdasarkan slot template.
     *
     * Contoh:
     * slot 0 -> photo_A.jpg
     * slot 2 -> photo_B.jpg
     * slot 3 -> photo_C.jpg
     *
     * Jadi user bebas mengambil foto dengan urutan apa pun.
     */
    private final Map<Integer, Path> photosBySlot =
            new HashMap<>();

    public PhotoSession(int maxPhotos) {

        if (maxPhotos <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah foto harus lebih dari 0."
            );
        }

        this.maxPhotos = maxPhotos;
    }

    /**
     * Menyimpan foto ke slot tertentu.
     */
    public void addPhoto(int slotIndex, Path photoPath) {

        if (slotIndex < 0 || slotIndex >= maxPhotos) {
            throw new IllegalArgumentException(
                    "Slot index tidak valid: " + slotIndex
            );
        }

        if (photoPath == null) {
            throw new IllegalArgumentException(
                    "Photo path tidak boleh null."
            );
        }

        photosBySlot.put(slotIndex, photoPath);
    }

    /**
     * Mengecek apakah sebuah slot sudah memiliki foto.
     */
    public boolean isSlotFilled(int slotIndex) {
        return photosBySlot.containsKey(slotIndex);
    }

    /**
     * Mengambil foto dari slot tertentu.
     */
    public Path getPhoto(int slotIndex) {
        return photosBySlot.get(slotIndex);
    }

    /**
     * Mengembalikan jumlah slot yang sudah terisi.
     */
    public int getPhotoCount() {
        return photosBySlot.size();
    }

    public int getMaxPhotos() {
        return maxPhotos;
    }

    /**
     * Mengecek apakah seluruh slot sudah terisi.
     */
    public boolean isComplete() {
        return photosBySlot.size() >= maxPhotos;
    }

    /**
     * Mengembalikan semua foto berdasarkan slot.
     */
    public Map<Integer, Path> getPhotosBySlot() {
        return Collections.unmodifiableMap(
                photosBySlot
        );
    }

    /**
     * Mengosongkan session.
     */
    public void reset() {
        photosBySlot.clear();
    }
}