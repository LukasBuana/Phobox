package com.photobox.template;

import com.photobox.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TemplateRepository {

    public List<Template> findAllActive() {
        return findByTypeActive(null);
    }

    /**
     * Mengambil semua template aktif berdasarkan tipe layout (misal: "2x2", "3x3").
     * Jika tipe bernilai null atau kosong, maka mengembalikan seluruh template aktif.
     */
    public List<Template> findByTypeActive(String type) {
        String sql;
        boolean filterByType = (type != null && !type.trim().isEmpty());

        if (filterByType) {
            sql = """
                    SELECT
                        id,
                        name,
                        type,
                        description,
                        background_path,
                        canvas_width,
                        canvas_height,
                        photo_count,
                        is_active
                    FROM templates
                    WHERE is_active = 1 AND type = ?
                    ORDER BY id
                    """;
        } else {
            sql = """
                    SELECT
                        id,
                        name,
                        type,
                        description,
                        background_path,
                        canvas_width,
                        canvas_height,
                        photo_count,
                        is_active
                    FROM templates
                    WHERE is_active = 1
                    ORDER BY id
                    """;
        }

        List<Template> templates = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (filterByType) {
                statement.setString(1, type);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Template template = mapTemplate(connection, resultSet);
                    templates.add(template);
                }
            }

            return templates;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Gagal mengambil template aktif.",
                    e
            );
        }
    }

    public Template findById(int templateId) {
        String sql = """
                SELECT
                    id,
                    name,
                    type,
                    description,
                    background_path,
                    canvas_width,
                    canvas_height,
                    photo_count,
                    is_active
                FROM templates
                WHERE id = ?
                """;

        try (Connection connection =
                     Database.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, templateId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapTemplate(
                        connection,
                        resultSet
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal mengambil template.",
                    e
            );
        }
    }

    public int create(
            String name,
            String type,
            String description,
            String backgroundPath,
            int canvasWidth,
            int canvasHeight,
            int photoCount,
            List<TemplateSlot> slots
    ) {

        String sql = """
                INSERT INTO templates (
                    name,
                    type,
                    description,
                    background_path,
                    canvas_width,
                    canvas_height,
                    photo_count,
                    is_active,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                """;

        try (Connection connection =
                     Database.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             java.sql.Statement.RETURN_GENERATED_KEYS
                     )) {

            String now =
                    LocalDateTime.now().toString();

            statement.setString(1, name);
            statement.setString(2, type);
            statement.setString(3, description);
            statement.setString(4, backgroundPath);
            statement.setInt(5, canvasWidth);
            statement.setInt(6, canvasHeight);
            statement.setInt(7, photoCount);
            statement.setString(8, now);
            statement.setString(9, now);

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {

                    throw new SQLException(
                            "Gagal mendapatkan ID template."
                    );
                }

                int templateId =
                        keys.getInt(1);

                saveSlots(
                        connection,
                        templateId,
                        slots
                );

                return templateId;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal membuat template.",
                    e
            );
        }
    }

    private void saveSlots(
            Connection connection,
            int templateId,
            List<TemplateSlot> slots
    ) throws SQLException {

        String sql = """
                INSERT INTO template_slots (
                    template_id,
                    slot_index,
                    x,
                    y,
                    width,
                    height,
                    rotation
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (TemplateSlot slot : slots) {

                statement.setInt(1, templateId);
                statement.setInt(2, slot.getSlotIndex());
                statement.setInt(3, slot.getX());
                statement.setInt(4, slot.getY());
                statement.setInt(5, slot.getWidth());
                statement.setInt(6, slot.getHeight());
                statement.setDouble(7, slot.getRotation());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private Template mapTemplate(
            Connection connection,
            ResultSet resultSet
    ) throws SQLException {

        int templateId =
                resultSet.getInt("id");

        List<TemplateSlot> slots =
                findSlots(
                        connection,
                        templateId
                );

        return new Template(
                templateId,
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("background_path"),
                resultSet.getInt("canvas_width"),
                resultSet.getInt("canvas_height"),
                resultSet.getInt("photo_count"),
                resultSet.getInt("is_active") == 1,
                slots
        );
    }

    private List<TemplateSlot> findSlots(
            Connection connection,
            int templateId
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    slot_index,
                    x,
                    y,
                    width,
                    height,
                    rotation
                FROM template_slots
                WHERE template_id = ?
                ORDER BY slot_index
                """;

        List<TemplateSlot> slots =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, templateId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    TemplateSlot slot =
                            new TemplateSlot(
                                    resultSet.getInt("id"),
                                    resultSet.getInt("slot_index"),
                                    resultSet.getInt("x"),
                                    resultSet.getInt("y"),
                                    resultSet.getInt("width"),
                                    resultSet.getInt("height"),
                                    resultSet.getDouble("rotation")
                            );

                    slots.add(slot);
                }
            }
        }

        return slots;
    }

    public void deactivate(int templateId) {
        String sql = """
                UPDATE templates
                SET
                    is_active = 0,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     Database.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    LocalDateTime.now().toString()
            );

            statement.setInt(
                    2,
                    templateId
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Gagal menonaktifkan template.",
                    e
            );
        }
    }
}