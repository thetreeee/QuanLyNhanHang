package dao;

import connectDB.SQLConnection;
import entity.DonDatBan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonDatBanDAO {

    /**
     * 1. Lấy tất cả đơn đặt bàn (JOIN 3 bảng: DonDatBan, ChiTietDatBan, KhachHang)
     * Mục đích: Lấy được cả Họ tên và Số điện thoại khách hàng để hiển thị lên bảng chi tiết
     */
    public List<DonDatBan> getAllDonDat() {
        List<DonDatBan> ds = new ArrayList<>();
        String sql = "SELECT d.maDon, d.ngayDat, d.thoiGian, c.maBan, c.soLuongKhach, " +
                     "k.hoTen, k.soDienThoai " +
                     "FROM DonDatBan d " +
                     "JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                     "LEFT JOIN KhachHang k ON d.maKhachHang = k.maKhachHang";
        
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                DonDatBan don = new DonDatBan(
                    rs.getString("maDon"),
                    rs.getString("maBan"), 
                    rs.getDate("ngayDat").toLocalDate(),
                    rs.getTimestamp("thoiGian").toLocalDateTime().toLocalTime(),
                    rs.getInt("soLuongKhach"),
                    "", // Ghi chú mặc định trống do SQL chưa có cột này
                    "Đang chờ" // Trạng thái mặc định
                );
                
                // Gán thông tin khách hàng vào đối tượng để hiển thị lên JTable
                don.setTenKhachHang(rs.getString("hoTen") != null ? rs.getString("hoTen") : "Khách vãng lai");
                don.setSoDienThoai(rs.getString("soDienThoai") != null ? rs.getString("soDienThoai") : "");
                
                ds.add(don);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * 2. Thêm đơn đặt mới (Dùng Transaction để lưu đồng thời vào KhachHang, DonDatBan, ChiTietDatBan)
     */
    public boolean insertDonDat(DonDatBan don) {
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            // --- BƯỚC A: Tạo và lưu Khách Hàng mới ---
            String maKH = "KH" + (System.currentTimeMillis() % 1000000); 
            String sqlKH = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai) VALUES (?, ?, ?)";
            try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                psKH.setString(1, maKH);
                psKH.setString(2, don.getTenKhachHang());
                psKH.setString(3, don.getSoDienThoai());
                psKH.executeUpdate();
            }

            // --- BƯỚC B: Lưu vào bảng DonDatBan (Sử dụng mã khách hàng vừa tạo) ---
            String sqlDon = "INSERT INTO DonDatBan (maDon, ngayDat, thoiGian, maKhachHang, maNV) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psDon = con.prepareStatement(sqlDon)) {
                psDon.setString(1, don.getMaDon());
                psDon.setDate(2, Date.valueOf(don.getNgayDat()));
                psDon.setTimestamp(3, Timestamp.valueOf(don.getNgayDat().atTime(don.getThoiGian())));
                psDon.setString(4, maKH); 
                psDon.setString(5, null); // maNV để null tạm thời
                psDon.executeUpdate();
            }

            // --- BƯỚC C: Lưu vào bảng ChiTietDatBan ---
            String sqlCT = "INSERT INTO ChiTietDatBan (maDon, maBan, soLuongKhach) VALUES (?, ?, ?)";
            try (PreparedStatement psCT = con.prepareStatement(sqlCT)) {
                psCT.setString(1, don.getMaDon());
                psCT.setString(2, don.getMaBan());
                psCT.setInt(3, don.getSoLuongKhach());
                psCT.executeUpdate();
            }

            // Hoàn tất Transaction
            con.commit(); 
            return true;

        } catch (SQLException e) {
            // Nếu có lỗi ở bất kỳ bước nào, rollback (hoàn tác) lại toàn bộ
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return false;
    }

    /**
     * 3. Cập nhật trạng thái (Hàm này dùng để thay đổi trạng thái của BÀN khi Hủy Đơn)
     */
    public boolean updateTrangThaiDon(String maDon, String trangThaiMoi) {
        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = (SELECT maBan FROM ChiTietDatBan WHERE maDon = ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maDon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}