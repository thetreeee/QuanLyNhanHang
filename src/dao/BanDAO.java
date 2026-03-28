package dao;

import connectDB.SQLConnection;
import entity.Ban;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanDAO {

    // Lấy danh sách tất cả các bàn
    public List<Ban> getAllBan() {
        List<Ban> dsBan = new ArrayList<>();
        String sql = "SELECT * FROM Ban";
        
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                // Lưu ý DB tên cột là soChoNgoi, ta map vào soGhe của Entity
                Ban ban = new Ban(
                    rs.getString("maBan"),
                    rs.getString("tenBan"),
                    rs.getInt("soChoNgoi"), 
                    rs.getString("trangThai"),
                    rs.getString("viTri")
                );
                dsBan.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsBan;
    }

    // Thêm bàn mới
    public boolean insertBan(Ban ban) {
        String sql = "INSERT INTO Ban (maBan, soChoNgoi, trangThai, viTri, tenBan) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, ban.getMaBan());
            ps.setInt(2, ban.getSoGhe());
            ps.setString(3, ban.getTrangThai());
            ps.setString(4, ban.getViTri());
            ps.setString(5, ban.getTenBan());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin bàn
    public boolean updateBan(Ban ban) {
        String sql = "UPDATE Ban SET soChoNgoi = ?, trangThai = ?, viTri = ?, tenBan = ? WHERE maBan = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, ban.getSoGhe());
            ps.setString(2, ban.getTrangThai());
            ps.setString(3, ban.getViTri());
            ps.setString(4, ban.getTenBan());
            ps.setString(5, ban.getMaBan());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa bàn
    public boolean deleteBan(String maBan) {
        String sql = "DELETE FROM Ban WHERE maBan = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Tự động phát sinh mã bàn Bxxx (VD: B001, B002)
    public String phatSinhMaBanTuDong() {
        String maMoi = "B001";
        String sql = "SELECT MAX(maBan) AS MaxMa FROM Ban WHERE maBan LIKE 'B%'";
        
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next() && rs.getString("MaxMa") != null) {
                String maxMa = rs.getString("MaxMa"); // VD: B015
                int soHienTai = Integer.parseInt(maxMa.substring(1)); // Cắt chữ B, lấy 15
                maMoi = String.format("B%03d", soHienTai + 1); // Format lại thành B016
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maMoi;
    }
}