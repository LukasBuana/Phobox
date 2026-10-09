package com.photobox.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.photobox.database.Database;

public class TransactionRepository {

    public void createTransaction(String transactionId, String packageName, int price, String paymentMethod) {
        String sql = """
                INSERT INTO transactions (id, status, package_name, price, payment_method, started_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transactionId);
            pstmt.setString(2, "PAID"); // Status default
            pstmt.setString(3, packageName);
            pstmt.setInt(4, price);
            pstmt.setString(5, paymentMethod);
            pstmt.setString(6, LocalDateTime.now().toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateResolvedAt(String transactionId) {
        String sql = "UPDATE transactions SET resolved_at = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setString(2, transactionId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}