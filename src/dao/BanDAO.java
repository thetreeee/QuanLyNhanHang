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
                Integer maKhoi = null;
                if (rs.getObject("maKhoi") != null) {
                    maKhoi = rs.getInt("maKhoi");
                }
                String maBanChinh = rs.getString("maBanChinh");

                Ban ban = new Ban(
                    rs.getString("maBan"),
                    rs.getString("tenBan"),
                    rs.getInt("soChoNgoi"), 
                    rs.getString("trangThai"),
                    rs.getString("viTri"),
                    maKhoi,
                    maBanChinh
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
        String sql = "INSERT INTO Ban (maBan, soChoNgoi, trangThai, viTri, tenBan, maKhoi, maBanChinh) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, ban.getMaBan());
            ps.setInt(2, ban.getSoGhe());
            ps.setString(3, ban.getTrangThai());
            ps.setString(4, ban.getViTri());
            ps.setString(5, ban.getTenBan());
            
            if (ban.getMaKhoi() != null) {
                ps.setInt(6, ban.getMaKhoi());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            if (ban.getMaBanChinh() != null) {
                ps.setString(7, ban.getMaBanChinh());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin bàn
    public boolean updateBan(Ban ban) {
        String sql = "UPDATE Ban SET soChoNgoi = ?, trangThai = ?, viTri = ?, tenBan = ?, maKhoi = ?, maBanChinh = ? WHERE maBan = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, ban.getSoGhe());
            ps.setString(2, ban.getTrangThai());
            ps.setString(3, ban.getViTri());
            ps.setString(4, ban.getTenBan());
            
            if (ban.getMaKhoi() != null) {
                ps.setInt(5, ban.getMaKhoi());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (ban.getMaBanChinh() != null) {
                ps.setString(6, ban.getMaBanChinh());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            
            ps.setString(7, ban.getMaBan());
            
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

    // Tự động phát sinh mã bàn Bxxx
    public String phatSinhMaBanTuDong() {
        String maMoi = "B001";
        String sql = "SELECT MAX(maBan) AS MaxMa FROM Ban WHERE maBan LIKE 'B%'";
        
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next() && rs.getString("MaxMa") != null) {
                String maxMa = rs.getString("MaxMa"); 
                int soHienTai = Integer.parseInt(maxMa.substring(1)); 
                maMoi = String.format("B%03d", soHienTai + 1); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maMoi;
    }

    // Lấy thông tin 1 bàn theo mã
    public Ban getBanByMa(String maBan) {
        String sql = "SELECT * FROM Ban WHERE maBan = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer maKhoi = rs.getObject("maKhoi") != null ? rs.getInt("maKhoi") : null;
                    String maBanChinh = rs.getString("maBanChinh");

                    return new Ban(
                        rs.getString("maBan"),
                        rs.getString("tenBan"),
                        rs.getInt("soChoNgoi"),
                        rs.getString("trangThai"),
                        rs.getString("viTri"),
                        maKhoi,
                        maBanChinh
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Cập nhật trạng thái bàn nhanh
    public boolean updateTrangThaiBan(String maBan, String trangThaiMoi) {
        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maBan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================================
    // CÁC HÀM MỚI BỔ SUNG ĐỂ QUẢN LÝ KHỐI BÀN (TABLE BLOCKS)
    // =====================================================================

    // 1. Sinh tự động ID Khối tiếp theo
    public int getMaKhoiTiepTheo() {
        String sql = "SELECT MAX(maKhoi) AS maxKhoi FROM Ban";
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getObject("maxKhoi") != null) {
                return rs.getInt("maxKhoi") + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; 
    }

    // 2. Gom danh sách các bàn thành 1 Khối mới
    // ĐÃ FIX: Thêm chữ N'Đang dùng'
    public boolean taoKhoiBan(List<String> danhSachMaBan, String maBanChinh) {
        if (danhSachMaBan == null || danhSachMaBan.isEmpty()) return false;
        
        int maKhoiMoi = getMaKhoiTiepTheo();
        String sql = "UPDATE Ban SET maKhoi = ?, maBanChinh = ?, trangThai = N'Đang dùng' WHERE maBan = ?";
        
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            con.setAutoCommit(false); 
            
            for (String maBan : danhSachMaBan) {
                ps.setInt(1, maKhoiMoi);
                ps.setString(2, maBanChinh);
                ps.setString(3, maBan);
                ps.addBatch();
            }
            
            ps.executeBatch();
            con.commit(); 
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Giải tán toàn bộ Khối khi có lệnh "Về Trống"
    // ĐÃ FIX: Thêm chữ N'Trống'
    public boolean giaiTanKhoi(int maKhoi) {
        String sql = "UPDATE Ban SET maKhoi = NULL, maBanChinh = NULL, trangThai = N'Trống' WHERE maKhoi = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, maKhoi);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}