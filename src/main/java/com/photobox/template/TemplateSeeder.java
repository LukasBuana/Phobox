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
        // 1. TEMPLATE TYPE: "2x2" (4 Foto - Potrait Center Balanced)
        // ==================================================
        List<TemplateSlot> slots2x2 = List.of(
                new TemplateSlot(0, 0, 75, 200, 500, 600, 0),
                new TemplateSlot(0, 1, 625, 200, 500, 600, 0),
                new TemplateSlot(0, 2, 75, 900, 500, 600, 0),
                new TemplateSlot(0, 3, 625, 900, 500, 600, 0)
        );

        repository.create(
                "2 × 2 Classic",
                "2x2",
                "Template 4 foto dengan layout 2 × 2",
                "templates/2x2/background.png",
                canvasWidth,
                canvasHeight,
                4,
                slots2x2
        );

        // ==================================================
        // 2. TEMPLATE TYPE: "3x3" (9 Foto - Square Grid)
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

        repository.create(
                "3 × 3 Grid",
                "3x3",
                "Template 9 foto persegi dengan layout 3 × 3",
                "templates/3x3/background.png",
                canvasWidth,
                canvasHeight,
                9,
                slots3x3
        );

        // ==================================================
        // 3. TEMPLATE TYPE: "strip" (Dual Strip 1x4 / 8 Slot)
        // ==================================================
        List<TemplateSlot> slotsStrip = List.of(
                // Strip Kiri (4 Foto Vertikal)
                new TemplateSlot(0, 0, 60, 120, 480, 360, 0),
                new TemplateSlot(0, 1, 60, 510, 480, 360, 0),
                new TemplateSlot(0, 2, 60, 900, 480, 360, 0),
                new TemplateSlot(0, 3, 60, 1290, 480, 360, 0),
                // Strip Kanan (4 Foto Duplikat)
                new TemplateSlot(0, 4, 660, 120, 480, 360, 0),
                new TemplateSlot(0, 5, 660, 510, 480, 360, 0),
                new TemplateSlot(0, 6, 660, 900, 480, 360, 0),
                new TemplateSlot(0, 7, 660, 1290, 480, 360, 0)
        );

        repository.create(
                "Classic Strip 1x4",
                "strip",
                "Template photostrip ganda vertikal 4 foto",
                "templates/strip/background.png",
                canvasWidth,
                canvasHeight,
                8,
                slotsStrip
        );

        // ==================================================
        // 4. TEMPLATE TYPE: "polaroid" (3 Foto Gaya Polaroid Vertikal)
        // ==================================================
        List<TemplateSlot> slotsPolaroid = List.of(
                new TemplateSlot(0, 0, 210, 150, 780, 480, 0),
                new TemplateSlot(0, 1, 210, 680, 780, 480, 0),
                new TemplateSlot(0, 2, 210, 1210, 780, 480, 0)
        );

        repository.create(
                "Polaroid Stack",
                "polaroid",
                "Template 3 foto bertumpuk ala gaya polaroid",
                "templates/polaroid/background.png",
                canvasWidth,
                canvasHeight,
                3,
                slotsPolaroid
        );

        // ==================================================
        // 5. TEMPLATE TYPE: "single_strip" (1 Strip Tunggal 4 Foto)
        // ==================================================
        List<TemplateSlot> slotsSingleStrip = List.of(
                new TemplateSlot(0, 0, 360, 100, 480, 360, 0),
                new TemplateSlot(0, 1, 360, 490, 480, 360, 0),
                new TemplateSlot(0, 2, 360, 880, 480, 360, 0),
                new TemplateSlot(0, 3, 360, 1270, 480, 360, 0)
        );

        repository.create(
                "Single Strip 1x4",
                "single_strip",
                "Template satu strip vertikal isi 4 foto di tengah",
                "templates/single_strip/background.png",
                canvasWidth,
                canvasHeight,
                4,
                slotsSingleStrip
        );

        System.out.println("Semua template (2x2, 3x3, strip, polaroid, single_strip) berhasil di-seed!");
    }
}