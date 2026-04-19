package dao;

import entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import connectDB.SQLConnection;

public class DonDatMon_DAO {

    public List<DonDatMon> getAll() {
        List<DonDatMon> list = new ArrayList<>();
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM DonDatMon ORDER BY thoiGianDat DESC")) {
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
        String sql = "SELECT TOP 1 * FROM DonDatMon WHERE maBan = ? ORDER BY thoiGianDat DESC";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DonDatMon(
                        rs.getString("maDonDat"),
                        rs.getTimestamp("thoiGianDat").toLocalDateTime(),
                        rs.getString("ghiChu"),
                        rs.getString("maNV"),
                        rs.getString("maBan")
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String phatSinhMaDon() {
        int max = 0;
        String sql = "SELECT maDonDat FROM DonDatMon WHERE maDonDat LIKE 'D%'";
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maDonDat");
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
        String maDon = phatSinhMaDon();
        String sql = "INSERT INTO DonDatMon(maDonDat, thoiGianDat, ghiChu, maNV, maBan) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonDatMon d = new DonDatMon();
                    d.setMaDonDat(rs.getString("maDonDat"));
                    d.setThoiGianDat(rs.getTimestamp("thoiGianDat").toLocalDateTime());
                    d.setGhiChu(rs.getString("ghiChu"));
                    d.setMaNV(rs.getString("maNV"));
                    d.setMaBan(rs.getString("maBan"));
                    return d;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
   
    public boolean updateGhiChu(String maDon, String ghiChu) {
        String sql = "UPDATE DonDatMon SET ghiChu = ? WHERE maDonDat = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ghiChu);
            ps.setString(2, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); } 
        return false;
    }

    public boolean xoaDon(String maDon) {
        String sql = "DELETE FROM DonDatMon WHERE maDonDat = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ==============================================================
    // HÀM GỘP ĐƠN SIÊU TỐI ƯU (ĐỔI CHỦ SỞ HỮU HÓA ĐƠN)
    // ==============================================================
    public boolean gopDonDatMon(String maBanChinh, List<String> listBanPhu) {
        try (Connection con = SQLConnection.getConnection()) {
            if (listBanPhu == null || listBanPhu.isEmpty()) return true;

            // Bước 1: Sang tên đổi chủ. Đổi "maBan" của TẤT CẢ các phiếu thuộc các Bàn Phụ thành Bàn Chính.
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < listBanPhu.size(); i++) {
                inClause.append("'").append(listBanPhu.get(i).toUpperCase()).append("'");
                if (i < listBanPhu.size() - 1) inClause.append(",");
            }

            String sqlUpdateMaBan = "UPDATE DonDatMon SET maBan = ? WHERE maBan IN (" + inClause.toString() + ")";
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateMaBan)) {
                psUpdate.setString(1, maBanChinh);
                psUpdate.executeUpdate();
            }

            // Bước 2: Cập nhật dòng chữ (Gồm: ...) vào phiếu mới nhất của Bàn Chính để khu vực Bếp dễ nhìn
            String sqlFind = "SELECT TOP 1 maDonDat, ghiChu FROM DonDatMon WHERE maBan = ? ORDER BY thoiGianDat DESC";
            try (PreparedStatement psFind = con.prepareStatement(sqlFind)) {
                psFind.setString(1, maBanChinh);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) {
                        String maDon = rs.getString("maDonDat");
                        String noteCu = rs.getString("ghiChu");
                        if (noteCu == null) noteCu = "";
                        
                        // Lọc bỏ rác "(Gồm:...)" cũ
                        noteCu = noteCu.replaceAll("\\(Gồm:[^)]+\\)", "").trim();
                        String strGop = "(Gồm: " + String.join(", ", listBanPhu) + ")";
                        String newNote = noteCu.isEmpty() ? strGop : noteCu + " " + strGop;
                        
                        // Ghi đè ghi chú mới
                        try (PreparedStatement psNote = con.prepareStatement("UPDATE DonDatMon SET ghiChu = ? WHERE maDonDat = ?")) {
                            psNote.setString(1, newNote);
                            psNote.setString(2, maDon);
                            psNote.executeUpdate();
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // HÀM CHUYỂN BÀN (CẬP NHẬT TRONG ĐƠN ĐẶT MÓN)
    // ==============================================================
    // Hàm này sẽ tìm Phiếu gọi món MỚI NHẤT của bàn cũ và đổi nó sang bàn mới
 // ==============================================================
    // HÀM CHUYỂN BÀN (DỜI TOÀN BỘ ĐƠN HÀNG CỦA BÀN CŨ)
    // ==============================================================
    public boolean chuyenBan(String maBanCu, String maBanMoi) {
        // SQL: Quét toàn bộ các đơn đang gắn với bàn cũ và đổi thành mã bàn mới
        String sql = "UPDATE DonDatMon SET maBan = ? WHERE maBan = ?"; 
        
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, maBanMoi); // Bàn đích đến
            ps.setString(2, maBanCu);  // Bàn cũ đang ngồi
            
            int n = ps.executeUpdate();
            return n > 0; // Trả về true nếu có ít nhất 1 đơn đặt món được chuyển
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}