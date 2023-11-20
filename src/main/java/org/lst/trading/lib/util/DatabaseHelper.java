package org.lst.trading.lib.util;

import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:stockdata.db";

    public DatabaseHelper() {
        initialize();
    }

    private void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS stock_data (" +
                    "symbol TEXT PRIMARY KEY," +
                    "data TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isDataPresent(String symbol) {
        String sql = "SELECT 1 FROM stock_data WHERE symbol = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertData(String symbol, String data) {
        String sql = "INSERT INTO stock_data (symbol, data) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setString(2, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getData(String symbol) {
        String sql = "SELECT data FROM stock_data WHERE symbol = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}