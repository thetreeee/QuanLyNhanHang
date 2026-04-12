package dao;

import entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import connectDB.SQLConnection;

public class DonDatMon_DAO {

    public List<DonDatMon> getAll() {
        List<DonDatMon> list = new ArrayList<>();
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "SELECT * FROM DonDatMon ORDER BY thoiGianDat DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                DonDatMon d = new DonDatMon(
                        rs.getString("maDonDat"),
                        rs.getTimestamp("thoiGianDat").toLocalDateTime(),
                        rs.getString("ghiChu"),
                        rs.getString("maNV"),
                        rs.getString("maBan")
                );
                list.add(d);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public DonDatMon getDonDangMoTheoBan(String maBan) {
        try {
            Connection con = SQLConnection.getConnection();
            // Lấy đơn mới nhất của bàn đó
            String sql = "SELECT * FROM DonDatMon WHERE maBan = ? ORDER BY thoiGianDat DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new DonDatMon(
                    rs.getString("maDonDat"),
                    rs.getTimestamp("thoiGianDat").toLocalDateTime(),
                    rs.getString("ghiChu"),
                    rs.getString("maNV"),
                    rs.getString("maBan")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    // --- HÀM TẠO MÃ ĐƠN ĐẸP (D001, D002...) ---
    public String phatSinhMaDon() {
        int max = 0;
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "SELECT maDonDat FROM DonDatMon WHERE maDonDat LIKE 'D%'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String ma = rs.getString("maDonDat");
                // Thông minh: Chỉ đếm các mã ngắn (D004), bỏ qua các mã rác do System.currentTimeMillis tạo ra
                if (ma.length() <= 6) {
                    try {
                        int so = Integer.parseInt(ma.substring(1));
                        if (so > max) max = so;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return String.format("D%03d", max + 1);
    }
    
    public String createDon(String maBan, String maNV, String ghiChu) {
        // Sử dụng hàm tạo mã đẹp thay vì lấy thời gian thực
        String maDon = phatSinhMaDon();
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "INSERT INTO DonDatMon(maDonDat, thoiGianDat, ghiChu, maNV, maBan) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDon);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); 
            ps.setString(3, ghiChu);
            ps.setString(4, maNV);
            ps.setString(5, maBan);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
        return maDon;
    }
    
    public DonDatMon getById(String maDon) {
        String sql = "SELECT * FROM DonDatMon WHERE maDonDat = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DonDatMon d = new DonDatMon();
                d.setMaDonDat(rs.getString("maDonDat"));
                d.setThoiGianDat(rs.getTimestamp("thoiGianDat").toLocalDateTime());
                d.setGhiChu(rs.getString("ghiChu"));
                d.setMaNV(rs.getString("maNV"));
                d.setMaBan(rs.getString("maBan"));
                return d;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
   
    public boolean updateGhiChu(String maDon, String ghiChu) {
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "UPDATE DonDatMon SET ghiChu = ? WHERE maDonDat = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ghiChu);
            ps.setString(2, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); } 
        return false;
    }

    public boolean xoaDon(String maDon) {
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "DELETE FROM DonDatMon WHERE maDonDat = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // --- HÀM GỘP ĐƠN ĐÃ FIX LỖI TRÙNG MÓN VÀ LỖI PRIMARY KEY ---
 // ==============================================================
    // HÀM GỘP ĐƠN ĐẶT MÓN (CẬP NHẬT TỰ ĐỘNG GHI CHÚ)
    // ==============================================================
    public boolean gopDonDatMon(String maBanChinh, List<String> listBanPhu) {
        Connection con = null; 
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = SQLConnection.getConnection(); 
            
            // Tìm Mã Đơn của bàn (lấy đơn mới nhất của bàn đó)
            String sqlFind = "SELECT TOP 1 maDonDat FROM DonDatMon WHERE maBan = ? ORDER BY thoiGianDat DESC";

            stmt = con.prepareStatement(sqlFind);
            stmt.setString(1, maBanChinh);
            rs = stmt.executeQuery();
            
            String maDonChinh = null;
            if (rs.next()) {
                maDonChinh = rs.getString("maDonDat");
            }

            for (String banPhu : listBanPhu) {
                stmt = con.prepareStatement(sqlFind);
                stmt.setString(1, banPhu);
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String maDonPhu = rs.getString("maDonDat");

                    if (maDonChinh == null) {
                        String updateDonToMain = "UPDATE DonDatMon SET maBan = ? WHERE maDonDat = ?";
                        stmt = con.prepareStatement(updateDonToMain);
                        stmt.setString(1, maBanChinh);
                        stmt.setString(2, maDonPhu);
                        stmt.executeUpdate();
                        maDonChinh = maDonPhu; 
                    } else {
                        // 1. Cộng dồn số lượng
                        String addQty = "UPDATE t1 SET t1.soLuong = t1.soLuong + t2.soLuong " +
                                        "FROM ChiTietDatMon t1 INNER JOIN ChiTietDatMon t2 ON t1.maMon = t2.maMon " +
                                        "WHERE t1.maDonDat = ? AND t2.maDonDat = ?";
                        stmt = con.prepareStatement(addQty);
                        stmt.setString(1, maDonChinh); stmt.setString(2, maDonPhu);
                        stmt.executeUpdate();

                        // 2. Xóa món trùng
                        String delDup = "DELETE FROM ChiTietDatMon WHERE maDonDat = ? AND maMon IN (SELECT maMon FROM ChiTietDatMon WHERE maDonDat = ?)";
                        stmt = con.prepareStatement(delDup);
                        stmt.setString(1, maDonPhu); stmt.setString(2, maDonChinh);
                        stmt.executeUpdate();

                        // 3. Đẩy món sang bàn chính
                        String moveItems = "UPDATE ChiTietDatMon SET maDonDat = ? WHERE maDonDat = ?";
                        stmt = con.prepareStatement(moveItems);
                        stmt.setString(1, maDonChinh); stmt.setString(2, maDonPhu);
                        stmt.executeUpdate();
                        
                        // 4. Xóa phiếu phụ
                        String deleteDonPhu = "DELETE FROM DonDatMon WHERE maDonDat = ?";
                        stmt = con.prepareStatement(deleteDonPhu);
                        stmt.setString(1, maDonPhu);
                        stmt.executeUpdate();
                    }
                }
            }

            // --- ĐÃ THÊM: TỰ ĐỘNG GHI CHÚ CÁC BÀN VỪA GỘP VÀO ĐƠN CHÍNH ---
            if (maDonChinh != null && !listBanPhu.isEmpty()) {
                String strGop = " (Gồm: " + String.join(", ", listBanPhu) + ")";
                // Cấu trúc ISNULL giúp: Nếu ghi chú rỗng thì thêm mới, nếu đã có chữ thì nối tiếp vào đuôi
                String sqlNote = "UPDATE DonDatMon SET ghiChu = ISNULL(ghiChu, '') + ? WHERE maDonDat = ?";
                stmt = con.prepareStatement(sqlNote);
                stmt.setString(1, strGop);
                stmt.setString(2, maDonChinh);
                stmt.executeUpdate();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}