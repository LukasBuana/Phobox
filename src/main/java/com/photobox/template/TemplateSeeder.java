package com.photobox.template;

import com.photobox.database.Database;

import java.util.List;

public class TemplateSeeder {

    public static void main(String[] args) {

        Database.initializeDatabase();

        TemplateRepository repository =
                new TemplateRepository();

        if (!repository.findAllActive().isEmpty()) {

            System.out.println(
                    "Template sudah tersedia di database."
            );

            return;
        }

        int canvasWidth = 1200;
        int canvasHeight = 1800;

        // ==================================================
        // LAYOUT TYPE: "2x2" (3 Sampel Pilihan)
        // ==================================================
        List<TemplateSlot> slots2x2 = List.of(
                new TemplateSlot(0, 0, 75, 200, 500, 600, 0),
                new TemplateSlot(0, 1, 625, 200, 500, 600, 0),
                new TemplateSlot(0, 2, 75, 900, 500, 600, 0),
                new TemplateSlot(0, 3, 625, 900, 500, 600, 0)
        );

        // Sampel 1: 2x2 Classic
        repository.create(
                "2 × 2 Classic",
                "2x2",
                "Template 4 foto standar dengan gaya klasik",
                "templates/2x2/classic.png",
                canvasWidth,
                canvasHeight,
                4,
                slots2x2
        );

        // Sampel 2: 2x2 Minimalist
        repository.create(
                "2 × 2 Minimalist",
                "2x2",
                "Template 4 foto dengan sentuhan minimalis",
                "templates/2x2/minimalist.png",
                canvasWidth,
                canvasHeight,
                4,
                slots2x2
        );

        // Sampel 3: 2x2 Neon Vibes
        repository.create(
                "2 × 2 Neon Vibes",
                "2x2",
                "Template 4 foto dengan nuansa warna neon",
                "templates/2x2/neon.png",
                canvasWidth,
                canvasHeight,
                4,
                slots2x2
        );

        // ==================================================
        // LAYOUT TYPE: "3x3" (3 Sampel Pilihan)
        // ==================================================
        List<TemplateSlot> slots3x3 = List.of(
                // Baris 1
                new TemplateSlot(0, 0, 90, 300, 310, 310, 0),
                new TemplateSlot(0, 1, 445, 300, 310, 310, 0),
                new TemplateSlot(0, 2, 800, 300, 310, 310, 0),
                // Baris 2
                new TemplateSlot(0, 3, 90, 655, 310, 310, 0),
                new TemplateSlot(0, 4, 445, 655, 310, 310, 0),
                new TemplateSlot(0, 5, 800, 655, 310, 310, 0),
                // Baris 3
                new TemplateSlot(0, 6, 90, 1010, 310, 310, 0),
                new TemplateSlot(0, 7, 445, 1010, 310, 310, 0),
                new TemplateSlot(0, 8, 800, 1010, 310, 310, 0)
        );

        // Sampel 1: 3x3 Grid Standard
        repository.create(
                "3 × 3 Grid Standard",
                "3x3",
                "Template 9 foto persegi dengan grid standar",
                "templates/3x3/standard.png",
                canvasWidth,
                canvasHeight,
                9,
                slots3x3
        );

        // Sampel 2: 3x3 Retro Polaroid
        repository.create(
                "3 × 3 Retro Polaroid",
                "3x3",
                "Template 9 foto dengan nuansa retro",
                "templates/3x3/retro.png",
                canvasWidth,
                canvasHeight,
                9,
                slots3x3
        );

        // Sampel 3: 3x3 Pastel Dream
        repository.create(
                "3 × 3 Pastel Dream",
                "3x3",
                "Template 9 foto dengan tema warna pastel",
                "templates/3x3/pastel.png",
                canvasWidth,
                canvasHeight,
                9,
                slots3x3
        );

        System.out.println("Berhasil men-seed 3 sampel untuk layout 2x2 dan 3x3!");
    }
}