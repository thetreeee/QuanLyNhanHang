package dao;

import connectDB.SQLConnection;
import entity.DonDatBan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonDatBanDAO {

    /**
     * 1. LẤY TẤT CẢ ĐƠN ĐẶT BÀN (ĐÃ GỘP NHIỀU BÀN THÀNH 1 DÒNG)
     * Dùng LinkedHashMap để tự động gộp các đơn trùng nhau mà vẫn giữ nguyên thứ tự thời gian
     */
    public List<DonDatBan> getAllDonDat() {
        java.util.Map<String, DonDatBan> mapDon = new java.util.LinkedHashMap<>();
        
        String sql = "SELECT d.maDon, d.ngayDat, d.thoiGian, d.trangThai, d.ghiChu, d.maNV, c.maBan, c.soLuongKhach, " +
                     "k.hoTen, k.soDienThoai " +
                     "FROM DonDatBan d " +
                     "JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                     "LEFT JOIN KhachHang k ON d.maKhachHang = k.maKhachHang " +
                     "WHERE d.ngayDat >= CONVERT(DATE, GETDATE()) " + 
                     "ORDER BY d.ngayDat ASC, d.thoiGian ASC";
        
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                String maDon = rs.getString("maDon");
                String maBan = rs.getString("maBan");
                int soKhachCuaBan = rs.getInt("soLuongKhach");

                if (mapDon.containsKey(maDon)) {
                    // NẾU ĐƠN ĐÃ CÓ TRONG MAP -> Lấy ra, cộng dồn mã bàn và số khách
                    DonDatBan donCu = mapDon.get(maDon);
                    donCu.setMaBan(donCu.getMaBan() + ", " + maBan);
                    donCu.setSoLuongKhach(donCu.getSoLuongKhach() + soKhachCuaBan);
                } else {
                    // NẾU LÀ ĐƠN MỚI -> Tạo đối tượng và đưa vào Map
                    DonDatBan donMoi = new DonDatBan(
                        maDon,
                        maBan, 
                        rs.getDate("ngayDat").toLocalDate(),
                        rs.getTimestamp("thoiGian").toLocalDateTime().toLocalTime(),
                        soKhachCuaBan,
                        rs.getString("ghiChu") != null ? rs.getString("ghiChu") : "", 
                        rs.getString("trangThai") != null ? rs.getString("trangThai") : "Đã đặt"
                    );
                    
                    donMoi.setTenKhachHang(rs.getString("hoTen") != null ? rs.getString("hoTen") : "Khách vãng lai");
                    donMoi.setSoDienThoai(rs.getString("soDienThoai") != null ? rs.getString("soDienThoai") : "");
                    donMoi.setMaNV(rs.getString("maNV") != null ? rs.getString("maNV") : ""); 
                    
                    mapDon.put(maDon, donMoi);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new ArrayList<>(mapDon.values());
    }

    /**
     * 2. TẠO ĐƠN ĐẶT BÀN MỚI
     * Sử dụng Transaction để lưu vào 3 bảng: KhachHang -> DonDatBan -> ChiTietDatBan
     */
    public boolean insertDonDat(DonDatBan don, List<String> danhSachMaBan) {
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false); // Bật Transaction

            // ==============================================================
            // BƯỚC A: XỬ LÝ KHÁCH HÀNG THÔNG MINH
            // ==============================================================
            String maKH = null;
            String sdtKhach = don.getSoDienThoai();
            
            // 1. Dò tìm xem khách này đã từng ăn ở quán chưa (Dựa vào SĐT)
            String sqlCheckKH = "SELECT maKhachHang FROM KhachHang WHERE soDienThoai = ?";
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheckKH)) {
                psCheck.setString(1, sdtKhach);
                try (ResultSet rsKH = psCheck.executeQuery()) {
                    if (rsKH.next()) {
                        maKH = rsKH.getString("maKhachHang"); // Nếu có thì lấy lại Mã cũ
                    }
                }
            }
            
            // 2. Nếu SĐT lạ (Khách mới hoàn toàn) -> Tạo mã mới và INSERT vào DB
            if (maKH == null) {
                // ĐÃ ĐỒNG BỘ: Sử dụng hàm sinh mã tuần tự từ KhachHang_DAO
                maKH = new KhachHang_DAO().tuDongPhatSinhMa(); 
                
                String sqlKH = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai) VALUES (?, ?, ?)";
                try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                    psKH.setString(1, maKH);
                    psKH.setString(2, don.getTenKhachHang());
                    psKH.setString(3, sdtKhach);
                    psKH.executeUpdate();
                }
            }
            // ==============================================================

            // --- BƯỚC B: Lưu DonDatBan (Chỉ tạo DUY NHẤT 1 DÒNG) ---
            String sqlDon = "INSERT INTO DonDatBan (maDon, ngayDat, thoiGian, maKhachHang, maNV, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psDon = con.prepareStatement(sqlDon)) {
                psDon.setString(1, don.getMaDon());
                psDon.setDate(2, Date.valueOf(don.getNgayDat()));
                psDon.setTimestamp(3, Timestamp.valueOf(don.getNgayDat().atTime(don.getThoiGian())));
                psDon.setString(4, maKH); 
                psDon.setString(5, don.getMaNV()); 
                psDon.setString(6, don.getTrangThai() != null ? don.getTrangThai() : "Đã đặt");
                psDon.setString(7, don.getGhiChu());
                
                psDon.executeUpdate();
            }

            // --- BƯỚC C: Lưu ChiTietDatBan (Lưu NHIỀU BÀN cùng lúc bằng AddBatch) ---
            int khachMoiBan = don.getSoLuongKhach() / danhSachMaBan.size();
            int khachLe = don.getSoLuongKhach() % danhSachMaBan.size();
            
            String sqlCT = "INSERT INTO ChiTietDatBan (maDon, maBan, soLuongKhach) VALUES (?, ?, ?)";
            try (PreparedStatement psCT = con.prepareStatement(sqlCT)) {
                for (int i = 0; i < danhSachMaBan.size(); i++) {
                    psCT.setString(1, don.getMaDon());
                    psCT.setString(2, danhSachMaBan.get(i));
                    // Bàn đầu tiên sẽ gánh số khách lẻ
                    psCT.setInt(3, i == 0 ? (khachMoiBan + khachLe) : khachMoiBan); 
                    psCT.addBatch(); 
                }
                psCT.executeBatch(); 
            }

            con.commit(); 
            return true;

        } catch (SQLException e) {
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

    public boolean insertDonDat(DonDatBan don) {
        List<String> danhSachMaBan = new ArrayList<>();
        danhSachMaBan.add(don.getMaBan());
        return insertDonDat(don, danhSachMaBan);
    }

    /**
     * 3. Cập nhật trạng thái của BÀN khi Hủy Đơn
     */
    public boolean updateTrangThaiDon(String maDon, String trangThaiMoi) {
        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan IN (SELECT maBan FROM ChiTietDatBan WHERE maDon = ?)";
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

    /**
     * 4. CẬP NHẬT TRẠNG THÁI CỦA ĐƠN ĐẶT BÀN VÀO CSDL
     */
    public boolean updateTrangThaiCuaDon(String maDon, String trangThaiMoi) {
        String sql = "UPDATE DonDatBan SET trangThai = ? WHERE maDon = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 5. KIỂM TRA TRÙNG LỊCH ĐẶT BÀN
     */
    public boolean kiemTraTrungLich(String maBan, java.time.LocalDate ngayDat, java.time.LocalTime thoiGian, String maDonNgoaiLe) {
        String sql = "SELECT d.thoiGian FROM DonDatBan d " +
                     "JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                     "WHERE c.maBan = ? AND d.ngayDat = ? AND d.trangThai = N'Đã đặt' AND d.maDon != ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            ps.setDate(2, java.sql.Date.valueOf(ngayDat));
            ps.setString(3, maDonNgoaiLe); 
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalTime timeDB = rs.getTimestamp("thoiGian").toLocalDateTime().toLocalTime();
                    long diffMinutes = java.time.Duration.between(timeDB, thoiGian).abs().toMinutes();
                    if (diffMinutes < 120) { 
                        return true; 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 6. AUTO UPDATE MÀU SƠ ĐỒ BÀN (Hệ thống chạy ngầm)
     */
    public boolean autoUpdateMauBan() {
        boolean hasChange = false;
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement()) {
             
            String sqlToXanh = "UPDATE Ban SET trangThai = N'Trống' " +
                               "WHERE trangThai = N'Đã đặt' AND maBan NOT IN (" +
                               "    SELECT c.maBan FROM DonDatBan d JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                               "    WHERE d.trangThai = N'Đã đặt' " +
                               "    AND DATEDIFF(MINUTE, GETDATE(), d.thoiGian) BETWEEN -30 AND 120" +
                               ")";
            int countXanh = stmt.executeUpdate(sqlToXanh);
            
            String sqlToVang = "UPDATE Ban SET trangThai = N'Đã đặt' " +
                               "WHERE trangThai = N'Trống' AND maBan IN (" +
                               "    SELECT c.maBan FROM DonDatBan d JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                               "    WHERE d.trangThai = N'Đã đặt' " +
                               "    AND DATEDIFF(MINUTE, GETDATE(), d.thoiGian) BETWEEN -30 AND 120" +
                               ")";
            int countVang = stmt.executeUpdate(sqlToVang);
            
            if (countXanh > 0 || countVang > 0) hasChange = true;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasChange;
    }

    /**
     * 7. TỰ ĐỘNG PHÁT SINH MÃ ĐƠN MỚI TỐI ƯU
     */
    public String getMaDonTiepTheo() {
        String sql = "SELECT MAX(CAST(SUBSTRING(maDon, 2, LEN(maDon)) AS INT)) FROM DonDatBan WHERE maDon LIKE 'D%'";
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) {
                int maxSo = rs.getInt(1);
                return String.format("D%03d", maxSo + 1); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "D001";
    }

    /**
     * 8. HÀM MỚI: LẤY CHI TIẾT BÀN CỦA 1 ĐƠN (Dùng cho Popup Form Chi Tiết)
     * Trả về Object[] chứa: [Mã bàn, Số lượng khách dự kiến]
     */
    public List<Object[]> getChiTietBanCuaDon(String maDon) {
        List<Object[]> listCT = new ArrayList<>();
        String sql = "SELECT maBan, soLuongKhach FROM ChiTietDatBan WHERE maDon = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maDon);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                listCT.add(new Object[]{ 
                    rs.getString("maBan"), 
                    rs.getInt("soLuongKhach") 
                });
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return listCT;
    }
}