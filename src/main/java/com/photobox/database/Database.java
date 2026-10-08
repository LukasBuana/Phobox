package com.photobox.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final Path DATABASE_DIRECTORY =
            Paths.get("data");

    private static final Path DATABASE_FILE =
            DATABASE_DIRECTORY.resolve("photobox.db");

    private static final String DATABASE_URL =
            "jdbc:sqlite:" + DATABASE_FILE;

    public static Connection getConnection()
            throws SQLException {

        try {
            Files.createDirectories(DATABASE_DIRECTORY);

        } catch (IOException e) {

            throw new SQLException(
                    "Gagal membuat folder database.",
                    e
            );
        }

        Connection connection =
                DriverManager.getConnection(DATABASE_URL);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }

        return connection;
    }

    public static void initializeDatabase() {

        // ==================================================
        // DROP TABEL LAMA (MENGATASI PERUBAHAN SKEMA / KOLOM)
        // ==================================================
        String dropSlotsTable = "DROP TABLE IF EXISTS template_slots";
        String dropTemplatesTable = "DROP TABLE IF EXISTS templates";
        String dropTransactionsTable = "DROP TABLE IF EXISTS transactions";

        String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id TEXT PRIMARY KEY,
                    status TEXT NOT NULL,
                    started_at TEXT NOT NULL,
                    resolved_at TEXT
                )
                """;

        // Kolom 'type' ditambahkan untuk membedakan jenis layout (misal: "2x2", "3x3", dll)
        String createTemplatesTable = """
                CREATE TABLE IF NOT EXISTS templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    description TEXT,
                    background_path TEXT NOT NULL,
                    canvas_width INTEGER NOT NULL,
                    canvas_height INTEGER NOT NULL,
                    photo_count INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """;

        String createTemplateSlotsTable = """
                CREATE TABLE IF NOT EXISTS template_slots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    template_id INTEGER NOT NULL,
                    slot_index INTEGER NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    width INTEGER NOT NULL,
                    height INTEGER NOT NULL,
                    rotation REAL NOT NULL DEFAULT 0,

                    FOREIGN KEY (template_id)
                        REFERENCES templates(id)
                        ON DELETE CASCADE,

                    UNIQUE (template_id, slot_index)
                )
                """;

        try (Connection connection =
                     getConnection();

             Statement statement =
                     connection.createStatement()) {

            // Hapus tabel lama terlebih dahulu agar skema ter-update otomatis
            statement.execute(dropSlotsTable);
            statement.execute(dropTemplatesTable);
            statement.execute(dropTransactionsTable);

            // Buat ulang tabel dengan struktur yang baru
            statement.execute(
                    createTransactionsTable
            );

            statement.execute(
                    createTemplatesTable
            );

            statement.execute(
                    createTemplateSlotsTable
            );

            System.out.println(
                    "Database cleared and initialized successfully with new schema."
            );

        } catch (SQLException e) {

            System.err.println(
                    "Gagal initialize database:"
            );

            e.printStackTrace();
        }
    }
}