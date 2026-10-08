package com.photobox.template;

import com.photobox.database.Database;

import java.util.List;

public class TemplateSeeder {

    public static void main(String[] args) {

        Database.initializeDatabase();

        TemplateRepository repository =
                new TemplateRepository();

        // Jangan membuat template 2x2 lagi
        // jika sudah ada.
        if (!repository.findAllActive().isEmpty()) {

            System.out.println(
                    "Template sudah tersedia."
            );

            return;
        }

        // ==================================================
        // CANVAS
        // ==================================================

        int canvasWidth = 1200;
        int canvasHeight = 1800;


        // ==================================================
        // PHOTO SLOTS
        // ==================================================

        List<TemplateSlot> slots = List.of(

                new TemplateSlot(
                        0,
                        0,
                        100,
                        200,
                        500,
                        600,
                        0
                ),

                new TemplateSlot(
                        0,
                        1,
                        600,
                        200,
                        500,
                        600,
                        0
                ),

                new TemplateSlot(
                        0,
                        2,
                        100,
                        900,
                        500,
                        600,
                        0
                ),

                new TemplateSlot(
                        0,
                        3,
                        600,
                        900,
                        500,
                        600,
                        0
                )
        );


        // ==================================================
        // CREATE TEMPLATE
        // ==================================================

        int templateId =
                repository.create(
                        "2 × 2",
                        "Template 4 foto dengan layout 2 × 2",
                        "templates/2x2/background.png",
                        canvasWidth,
                        canvasHeight,
                        4,
                        slots
                );

        System.out.println(
                "Template berhasil dibuat."
        );

        System.out.println(
                "Template ID: "
                        + templateId
        );
    }
}