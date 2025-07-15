package latihan;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/latihan";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<Map<String, String>> getAllSiswa() {
        List<Map<String, String>> dataSiswa = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM siswa")) {

            while (rs.next()) {
                Map<String, String> siswa = new HashMap<>();
                siswa.put("nis", rs.getString("nis"));
                siswa.put("nama", rs.getString("nama"));
                siswa.put("alamat", rs.getString("alamat"));
                siswa.put("tempat_lahir", rs.getString("tempat_lahir"));
                siswa.put("tanggal_lahir", rs.getString("tanggal_lahir"));
                dataSiswa.add(siswa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSiswa;
    }

    public Map<String, String> addSiswa(Map<String, String> siswa) {
        String sql = "INSERT INTO siswa (nis, nama, alamat, tempat_lahir, tanggal_lahir) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, siswa.get("nis"));
            pstmt.setString(2, siswa.get("nama"));
            pstmt.setString(3, siswa.get("alamat"));
            pstmt.setString(4, siswa.get("tempat_lahir"));
            pstmt.setString(5, siswa.get("tanggal_lahir"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return siswa;
    }

    public Map<String, String> updateSiswa(String nis, Map<String, String> siswa) {
        String sql = "UPDATE siswa SET nama = ?, alamat = ?, tempat_lahir = ?, tanggal_lahir = ? WHERE nis = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, siswa.get("nama"));
            pstmt.setString(2, siswa.get("alamat"));
            pstmt.setString(3, siswa.get("tempat_lahir"));
            pstmt.setString(4, siswa.get("tanggal_lahir"));
            pstmt.setString(5, nis);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return siswa;
    }

    public void deleteSiswa(String nis) {
        String sql = "DELETE FROM siswa WHERE nis = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nis);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
