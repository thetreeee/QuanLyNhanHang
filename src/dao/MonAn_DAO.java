package dao;

import connectDB.SQLConnection;

import entity.MonAnTT;
import entity.MonAn;
import entity.MonAnTT;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonAn_DAO {

    /**
     * HÀM BỔ TRỢ: Ánh xạ dữ liệu từ ResultSet sang Object MonAn
     * (Đã cập nhật để tự động đọc đúng mọi cột, bao gồm donViTinh và trangThai)
     */
    private MonAn mapMonAn(ResultSet rs) throws SQLException {
        MonAn m = new MonAn();
        m.setMaMon(rs.getString("maMon"));
        m.setTenMon(rs.getString("tenMon"));
        m.setHinhAnh(rs.getString("hinhAnh"));
        m.setLoaiMon(rs.getString("loaiMon"));
        m.setDonViTinh(rs.getString("donViTinh")); // <--- KHẮC PHỤC LỖI THIẾU ĐƠN VỊ TÍNH
        m.setTrangThai(rs.getString("trangThai")); // <--- KHẮC PHỤC LỖI THIẾU TRẠNG THÁI
        
        // Lấy giá bán (Alias trong SQL phải là giaBan)
        double gia = rs.getDouble("giaBan");
        if (rs.wasNull() || gia <= 0) {
            m.setGiaBan(-1); // Không có giá hợp lệ -> Mờ đi
        } else {
            m.setGiaBan(gia); // Có bảng giá 'Đang áp dụng' -> Sáng lên
        }
        return m;
    }

    /**
     * Lấy tất cả món ăn kèm giá đang có hiệu lực 
     * ĐIỀU KIỆN: Trạng thái 'Đang áp dụng' VÀ Ngày hiện tại nằm trong [ngayBatDau, ngayKetThuc]
     */
    public List<MonAn> getAllMonAnWithActivePrice() {
        List<MonAn> ds = new ArrayList<>();
        // Câu SQL đảm bảo: Với mỗi món, chỉ lấy giá trị Đang áp dụng và Đúng ngày nhất
        String sql = "SELECT m.*, ( " +
                     "    SELECT TOP 1 ct.giaBan " +
                     "    FROM ChiTietBangGia ct " +
                     "    JOIN BangGia b ON ct.maBangGia = b.maBangGia " +
                     "    WHERE ct.maMon = m.maMon " +
                     "    AND b.trangThai = N'Đang áp dụng' " +
                     "    AND CAST(GETDATE() AS DATE) BETWEEN b.ngayBatDau AND b.ngayKetThuc " +
                     "    ORDER BY b.ngayBatDau DESC " + 
                     ") AS giaBan " + // Alias là giaBan để khớp với hàm mapMonAn
                     "FROM MonAn m";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // Tận dụng hàm mapMonAn để code gọn và không bao giờ sót cột
                ds.add(mapMonAn(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Tìm kiếm món ăn theo tên (Đồng bộ logic thông minh chống trùng lặp giá)
     */
    public List<MonAn> searchMonAn(String keyword) {
        List<MonAn> ds = new ArrayList<>();
        String sql = "SELECT m.*, ( " +
                     "    SELECT TOP 1 ct.giaBan " +
                     "    FROM ChiTietBangGia ct " +
                     "    JOIN BangGia b ON ct.maBangGia = b.maBangGia " +
                     "    WHERE ct.maMon = m.maMon " +
                     "    AND b.trangThai = N'Đang áp dụng' " +
                     "    AND CAST(GETDATE() AS DATE) BETWEEN b.ngayBatDau AND b.ngayKetThuc " +
                     "    ORDER BY b.ngayBatDau DESC " + 
                     ") AS giaBan " +
                     "FROM MonAn m " +
                     "WHERE m.tenMon LIKE ?";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapMonAn(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Thêm mới một món ăn
     */
    public boolean insertMonAn(MonAn mon) {
        if (mon.getMaMon() == null || !mon.getMaMon().matches("^M\\d{3}$")) return false;

        String sql = "INSERT INTO MonAn (maMon, tenMon, donViTinh, trangThai, loaiMon, hinhAnh) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mon.getMaMon());
            pst.setString(2, mon.getTenMon());
            pst.setString(3, mon.getDonViTinh());
            pst.setString(4, mon.getTrangThai());
            pst.setString(5, mon.getLoaiMon());
            pst.setString(6, mon.getHinhAnh());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin món ăn
     */
    public boolean updateMonAn(MonAn mon) {
        String sql = "UPDATE MonAn SET tenMon = ?, donViTinh = ?, trangThai = ?, loaiMon = ?, hinhAnh = ? WHERE maMon = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mon.getTenMon());
            pst.setString(2, mon.getDonViTinh());
            pst.setString(3, mon.getTrangThai());
            pst.setString(4, mon.getLoaiMon());
            pst.setString(5, mon.getHinhAnh());
            pst.setString(6, mon.getMaMon());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa món ăn
     */
    public boolean deleteMonAn(String maMon) throws SQLException {
        String sql = "DELETE FROM MonAn WHERE maMon = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maMon);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean exists(String maMon) {
        String sql = "SELECT COUNT(*) FROM MonAn WHERE maMon = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maMon);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public List<MonAnTT> getMenuHienTai() {
        List<MonAnTT> list = new ArrayList<>();

        String sql = 
        		"SELECT m.maMon, m.tenMon, m.hinhAnh, ct.giaBan " +
        	    "FROM MonAn m " +
        	    "JOIN ChiTietBangGia ct ON m.maMon = ct.maMon " +
        	    "JOIN BangGia bg ON bg.maBangGia = ct.maBangGia " +
        	    "WHERE bg.trangThai = N'Đang áp dụng'";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MonAnTT(
                        rs.getString("maMon"),
                        rs.getString("tenMon"),
                        rs.getString("hinhAnh"),
                        rs.getDouble("giaBan")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}