package dao;

import connectDB.SQLConnection;
import entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DonDatMon_DAO {

    public List<DonDatMon> getAll() {
        List<DonDatMon> list = new ArrayList<>();
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM DonDatMon ORDER BY thoiGianDat DESC")) {
            while (rs.next()) {
                DonDatMon d = new DonDatMon();
                d.setMaDonDat(rs.getString("maDonDat"));
                d.setThoiGianDat(rs.getTimestamp("thoiGianDat").toLocalDateTime());
                d.setGhiChu(rs.getString("ghiChu"));
                d.setMaNV(rs.getString("maNV"));
                d.setMaBan(rs.getString("maBan"));
                d.setMaKhachHang(rs.getString("maKhachHang")); 
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
                    DonDatMon d = new DonDatMon();
                    d.setMaDonDat(rs.getString("maDonDat"));
                    d.setThoiGianDat(rs.getTimestamp("thoiGianDat").toLocalDateTime());
                    d.setGhiChu(rs.getString("ghiChu"));
                    d.setMaNV(rs.getString("maNV"));
                    d.setMaBan(rs.getString("maBan"));
                    d.setMaKhachHang(rs.getString("maKhachHang")); 
                    return d;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String phatSinhMaDon() {
        int max = 0;
        // Lưu ý: Nếu Đơn món của bạn dùng tiền tố D thì đổi chữ M thành D nhé
        String sql = "SELECT maDonDat FROM DonDatMon WHERE maDonDat LIKE 'M%'"; 
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
        return String.format("M%03d", max + 1); 
    }
    
    // ==============================================================
    // HÀM TẠO ĐƠN GỌI MÓN (CÓ XỬ LÝ MÃ KHÁCH HÀNG / VÃNG LAI)
    // ==============================================================
    public String createDon(String maBan, String maNV, String ghiChu, String maKhachHang) {
        String maDon = phatSinhMaDon();
        String sql = "INSERT INTO DonDatMon(maDonDat, thoiGianDat, ghiChu, maNV, maBan, maKhachHang) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDon);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); 
            ps.setString(3, ghiChu);
            ps.setString(4, maNV);
            ps.setString(5, maBan);
            
            // Xử lý Khách vãng lai (không truyền mã)
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                ps.setNull(6, java.sql.Types.VARCHAR);
            } else {
                ps.setString(6, maKhachHang);
            }
            
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
        return maDon;
    }
    
    // Nạp chồng phương thức cho tiện dùng khi chắc chắn là Khách vãng lai
    public String createDon(String maBan, String maNV, String ghiChu) {
        return createDon(maBan, maNV, ghiChu, null);
    }

    // ==============================================================
    // CÁC HÀM CẬP NHẬT / XÓA / KIỂM TRA
    // ==============================================================
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
                    d.setMaKhachHang(rs.getString("maKhachHang")); 
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

    public boolean kiemTraBanCoDonChuaThanhToan(String maBan) {
        String sql = "SELECT COUNT(*) FROM DonDatMon WHERE maBan = ?"; 
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; 
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean gopDonDatMon(String maBanChinh, List<String> listBanPhu) {
        try (Connection con = SQLConnection.getConnection()) {
            if (listBanPhu == null || listBanPhu.isEmpty()) return true;

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

            String sqlFind = "SELECT TOP 1 maDonDat, ghiChu FROM DonDatMon WHERE maBan = ? ORDER BY thoiGianDat DESC";
            try (PreparedStatement psFind = con.prepareStatement(sqlFind)) {
                psFind.setString(1, maBanChinh);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) {
                        String maDon = rs.getString("maDonDat");
                        String noteCu = rs.getString("ghiChu");
                        if (noteCu == null) noteCu = "";
                        
                        noteCu = noteCu.replaceAll("\\(Gồm:[^)]+\\)", "").trim();
                        String strGop = "(Gồm: " + String.join(", ", listBanPhu) + ")";
                        String newNote = noteCu.isEmpty() ? strGop : noteCu + " " + strGop;
                        
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

    public boolean chuyenBan(String maBanCu, String maBanMoi) {
        String sql = "UPDATE DonDatMon SET maBan = ? WHERE maBan = ?"; 
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBanMoi); 
            ps.setString(2, maBanCu);  
            int n = ps.executeUpdate();
            return n > 0; 
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ==============================================================
    // XỬ LÝ TRƯỜNG HỢP KHÁCH VÀO NGỒI NHƯNG ĐI VỀ KHÔNG GỌI MÓN
    // ==============================================================
    public boolean kiemTraDonCoMonAnChua(String maBan) {
        // Kết hợp với bảng ChiTietDatMon để xem bàn này đã order món nào chưa
        String sql = "SELECT COUNT(*) FROM ChiTietDatMon c JOIN DonDatMon d ON c.maDonDat = d.maDonDat WHERE d.maBan = ?"; 
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Trả về true nếu đã có ít nhất 1 món
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public void xoaDonRongCuaBan(String maBan) {
        // Lệnh này cực kỳ an toàn: CHỈ XÓA những Đơn chưa có chi tiết món ăn (rỗng)
        String sql = "DELETE FROM DonDatMon WHERE maBan = ? AND maDonDat NOT IN (SELECT maDonDat FROM ChiTietDatMon)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}