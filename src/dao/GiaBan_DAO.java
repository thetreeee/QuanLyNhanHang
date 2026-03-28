package dao;

import connectDB.SQLConnection;
import java.sql.*;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class GiaBan_DAO {

    /**
     * Lấy danh sách bảng giá đã gom nhóm (Mặc định load tất cả)
     */
    public Vector<Vector<Object>> getAllGiaBanGomNhom() {
        return searchGiaBanByMa(""); // Gọi lại hàm search với từ khóa rỗng
    }

    /**
     * TÌM KIẾM BẢNG GIÁ THEO MÃ (Dùng cho nút Lọc dữ liệu)
     */
    public Vector<Vector<Object>> searchGiaBanByMa(String maSearch) {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT b.maBangGia, " +
                     "       STRING_AGG(m.tenMon, ', ') AS dsMon, " +
                     "       MAX(ct.giaBan) AS gia, " +
                     "       b.ngayBatDau, b.ngayKetThuc, b.trangThai " +
                     "FROM BangGia b " +
                     "JOIN ChiTietBangGia ct ON b.maBangGia = ct.maBangGia " +
                     "JOIN MonAn m ON ct.maMon = m.maMon " +
                     "WHERE b.maBangGia LIKE ? " +
                     "GROUP BY b.maBangGia, b.ngayBatDau, b.ngayKetThuc, b.trangThai " +
                     "ORDER BY b.ngayBatDau DESC";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, "%" + maSearch + "%");
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("dsMon"));
                row.add(String.format("%,.0f", rs.getDouble("gia")));
                row.add(rs.getDate("ngayBatDau") != null ? sdf.format(rs.getDate("ngayBatDau")) : "");
                row.add(rs.getDate("ngayKetThuc") != null ? sdf.format(rs.getDate("ngayKetThuc")) : "");
                row.add(rs.getString("trangThai"));
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    /**
     * KIỂM TRA TRÙNG MÃ BẢNG GIÁ
     */
    public boolean kiemTraTrungMa(String maBG) {
        String sql = "SELECT COUNT(*) FROM BangGia WHERE maBangGia = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maBG);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * PHÁT SINH MÃ TỰ ĐỘNG (BG001, BG002...)
     */
    public String tuDongPhatSinhMa() {
        String sql = "SELECT MAX(maBangGia) FROM BangGia";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString(1);
                if (maxMa == null) return "BG001";
                int so = Integer.parseInt(maxMa.substring(2)) + 1;
                return String.format("BG%03d", so);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "BG001";
    }

    /**
     * CẬP NHẬT TOÀN BỘ BẢNG GIÁ (Dùng Transaction)
     */
    public boolean updateGiaBanFull(String maBG, String moTa, Date ngayBD, Date ngayKT, String trangThai, double giaMoi, List<String> dsMaMon) {
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Update bảng chính
            String sql1 = "UPDATE BangGia SET moTa=?, ngayBatDau=?, ngayKetThuc=?, trangThai=? WHERE maBangGia=?";
            PreparedStatement pst1 = con.prepareStatement(sql1);
            pst1.setString(1, moTa);
            pst1.setDate(2, new java.sql.Date(ngayBD.getTime()));
            pst1.setDate(3, ngayKT != null ? new java.sql.Date(ngayKT.getTime()) : null);
            pst1.setString(4, trangThai);
            pst1.setString(5, maBG);
            pst1.executeUpdate();

            // 2. Refresh chi tiết (Xóa đi chèn lại)
            String sqlDel = "DELETE FROM ChiTietBangGia WHERE maBangGia=?";
            PreparedStatement pstDel = con.prepareStatement(sqlDel);
            pstDel.setString(1, maBG);
            pstDel.executeUpdate();

            String sqlIns = "INSERT INTO ChiTietBangGia (maBangGia, maMon, giaBan) VALUES (?, ?, ?)";
            PreparedStatement pstIns = con.prepareStatement(sqlIns);
            for (String maMon : dsMaMon) {
                pstIns.setString(1, maBG);
                pstIns.setString(2, maMon);
                pstIns.setDouble(3, giaMoi);
                pstIns.addBatch();
            }
            pstIns.executeBatch();

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }

    /**
     * KIỂM TRA CHỒNG LẤN THỜI GIAN (Overlap)
     */
    public String checkMonDaCoGia(List<String> dsMaMon, String maBGCurrent, Date ngayBDMoi, Date ngayKTMoi) {
        String sql = "SELECT TOP 1 m.tenMon, b.maBangGia " +
                     "FROM ChiTietBangGia ct " +
                     "JOIN BangGia b ON ct.maBangGia = b.maBangGia " +
                     "JOIN MonAn m ON ct.maMon = m.maMon " +
                     "WHERE ct.maMon = ? AND b.maBangGia != ? AND b.trangThai = N'Đang áp dụng' " +
                     "AND ( " +
                     "    (? BETWEEN b.ngayBatDau AND b.ngayKetThuc) OR " +
                     "    (? BETWEEN b.ngayBatDau AND b.ngayKetThuc) OR " +
                     "    (b.ngayBatDau BETWEEN ? AND ?) " +
                     ")";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            java.sql.Date sqlBD = new java.sql.Date(ngayBDMoi.getTime());
            java.sql.Date sqlKT = (ngayKTMoi != null) ? new java.sql.Date(ngayKTMoi.getTime()) : java.sql.Date.valueOf("9999-12-31");

            for (String maMon : dsMaMon) {
                pst.setString(1, maMon);
                pst.setString(2, maBGCurrent == null ? "" : maBGCurrent);
                pst.setDate(3, sqlBD);
                pst.setDate(4, sqlKT);
                pst.setDate(5, sqlBD);
                pst.setDate(6, sqlKT);
                
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return "Món '" + rs.getString("tenMon") + "' đã có giá trong khoảng này (Bảng giá: " + rs.getString("maBangGia") + ")";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * TRUY VẤN MÃ BẢNG GIÁ TỪ THÔNG TIN TRÊN TABLE (Sửa lại logic so sánh ngày chuẩn)
     */
    public String getMaBangGiaByInfo(String tenMon, String ngayBDStr) {
        String sql = "SELECT TOP 1 b.maBangGia FROM BangGia b " +
                     "JOIN ChiTietBangGia ct ON b.maBangGia = ct.maBangGia " +
                     "JOIN MonAn m ON ct.maMon = m.maMon " +
                     "WHERE m.tenMon = ? AND CONVERT(VARCHAR, b.ngayBatDau, 103) = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenMon);
            pst.setString(2, ngayBDStr);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("maBangGia");
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    /**
     * XÓA BẢNG GIÁ
     */
    public boolean deleteGiaBan(String maBG) {
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false);

            PreparedStatement pst1 = con.prepareStatement("DELETE FROM ChiTietBangGia WHERE maBangGia = ?");
            pst1.setString(1, maBG);
            pst1.executeUpdate();

            PreparedStatement pst2 = con.prepareStatement("DELETE FROM BangGia WHERE maBangGia = ?");
            pst2.setString(1, maBG);
            pst2.executeUpdate();

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        }
    }
    /**
     * Dùng cho QuanLyGiaBanPanel (Load danh sách Master/Header)
     * Tìm kiếm theo Mã Bảng Giá HOẶC Mô Tả (Tên đợt giá)
     */
    public Vector<Vector<Object>> searchBangGiaHeader(String keyword) {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT maBangGia, moTa, ngayBatDau, ngayKetThuc, trangThai " +
                     "FROM BangGia " +
                     "WHERE maBangGia LIKE ? OR moTa LIKE ? " +
                     "ORDER BY ngayBatDau DESC";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("maBangGia")); // Cột 0
                row.add(rs.getString("moTa"));      // Cột 1
                row.add(rs.getDate("ngayBatDau") != null ? sdf.format(rs.getDate("ngayBatDau")) : ""); // Cột 2
                row.add(rs.getDate("ngayKetThuc") != null ? sdf.format(rs.getDate("ngayKetThuc")) : ""); // Cột 3
                row.add(rs.getString("trangThai")); // Cột 4
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
}