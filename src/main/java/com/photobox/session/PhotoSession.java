package com.photobox.session;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotoSession {
    private final int maxPhotos;
    private final List<Path> photos = new ArrayList<>();

    public PhotoSession(int maxPhotos) {
        if (maxPhotos <= 0) {
            throw new IllegalArgumentException("Jumlah foto harus lebih dari 0.");
        }
        this.maxPhotos = maxPhotos;
    }

    /** * Menambahkan foto yang sudah dipilih * oleh user ke dalam session. */
    public void addPhoto(Path photoPath) {
        if (isComplete()) {
            throw new IllegalStateException("Photo session sudah penuh.");
        }
        if (photoPath == null) {
            throw new IllegalArgumentException("Photo path tidak boleh null.");
        }
        photos.add(photoPath);
    }

    /** * Mengambil jumlah foto yang sudah * diterima user. */
    public int getPhotoCount() {
        return photos.size();
    }

    /** * Mengambil jumlah maksimum foto. */
    public int getMaxPhotos() {
        return maxPhotos;
    }

    /** * Mengecek apakah session sudah lengkap. */
    public boolean isComplete() {
        return photos.size() >= maxPhotos;
    }

    /** * Mengambil foto berdasarkan index. */
    public Path getPhoto(int index) {
        return photos.get(index);
    }

    /**
     * * Mengambil seluruh foto. * * Collections.unmodifiableList digunakan * agar
     * list internal tidak dapat diubah * secara langsung dari luar class.
     */
    public List<Path> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    /** * Menghapus seluruh foto dari session. */
    public void reset() {
        photos.clear();
    }
}