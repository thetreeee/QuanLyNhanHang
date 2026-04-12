package dao;

import connectDB.SQLConnection;
import entity.DonDatBan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonDatBanDAO {

    /**
     * 1. Lấy tất cả đơn đặt bàn (JOIN 3 bảng: DonDatBan, ChiTietDatBan, KhachHang)
     * ĐÃ CẬP NHẬT: Lọc bỏ các đơn trong quá khứ, chỉ lấy đơn từ ngày hiện tại trở đi và sắp xếp thời gian.
     */
    public List<DonDatBan> getAllDonDat() {
        List<DonDatBan> ds = new ArrayList<>();
        
        // Thêm WHERE d.ngayDat >= CONVERT(DATE, GETDATE()) để chặn đơn cũ
        // Thêm ORDER BY để danh sách hiển thị theo thứ tự thời gian từ sớm đến muộn
        String sql = "SELECT d.maDon, d.ngayDat, d.thoiGian, d.trangThai, c.maBan, c.soLuongKhach, " +
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
                DonDatBan don = new DonDatBan(
                    rs.getString("maDon"),
                    rs.getString("maBan"), 
                    rs.getDate("ngayDat").toLocalDate(),
                    rs.getTimestamp("thoiGian").toLocalDateTime().toLocalTime(),
                    rs.getInt("soLuongKhach"),
                    "", // Ghi chú mặc định trống do SQL chưa có cột này
                    // ĐÃ SỬA: Mặc định hiển thị là "Đã đặt" nếu rỗng
                    rs.getString("trangThai") != null ? rs.getString("trangThai") : "Đã đặt"
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

            // --- BƯỚC B: Lưu vào bảng DonDatBan ---
            // ĐÃ SỬA: Bổ sung thêm cột trangThai vào câu lệnh INSERT và gán cứng là "Đã đặt"
            String sqlDon = "INSERT INTO DonDatBan (maDon, ngayDat, thoiGian, maKhachHang, maNV, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psDon = con.prepareStatement(sqlDon)) {
                psDon.setString(1, don.getMaDon());
                psDon.setDate(2, Date.valueOf(don.getNgayDat()));
                psDon.setTimestamp(3, Timestamp.valueOf(don.getNgayDat().atTime(don.getThoiGian())));
                psDon.setString(4, maKH); 
                psDon.setString(5, null); // maNV để null tạm thời
                psDon.setString(6, "Đã đặt"); // <--- MỚI: Thêm trực tiếp trạng thái "Đã đặt" vào DB
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

    /**
     * 4. CẬP NHẬT TRẠNG THÁI CỦA ĐƠN ĐẶT BÀN VÀO CSDL (Thêm mới)
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
     * 5. KIỂM TRA TRÙNG LỊCH ĐẶT BÀN (Khoảng cách giữa 2 đơn < 2 tiếng)
     */
    public boolean kiemTraTrungLich(String maBan, java.time.LocalDate ngayDat, java.time.LocalTime thoiGian, String maDonNgoaiLe) {
        String sql = "SELECT d.thoiGian FROM DonDatBan d " +
                     "JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                     "WHERE c.maBan = ? AND d.ngayDat = ? AND d.trangThai = N'Đã đặt' AND d.maDon != ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBan);
            ps.setDate(2, java.sql.Date.valueOf(ngayDat));
            ps.setString(3, maDonNgoaiLe); // Loại trừ chính cái đơn đang được thao tác
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalTime timeDB = rs.getTimestamp("thoiGian").toLocalDateTime().toLocalTime();
                    // Tính độ chênh lệch thời gian giữa 2 đơn
                    long diffMinutes = java.time.Duration.between(timeDB, thoiGian).abs().toMinutes();
                    if (diffMinutes < 120) { // Nhỏ hơn 120 phút (2 tiếng) là bị trùng
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
     * - Nếu còn > 2 tiếng hoặc là đơn của ngày mai: Ép bàn về màu XANH (Trống)
     * - Nếu <= 2 tiếng (và không bị trễ quá 30p): Tự động lên màu VÀNG (Đã đặt)
     */
    public boolean autoUpdateMauBan() {
        boolean hasChange = false;
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement()) {
             
            // 1. Trả bàn về màu XANH (Trống) nếu: Thời gian chờ > 2 tiếng HOẶC là đơn ngày mai
            String sqlToXanh = "UPDATE Ban SET trangThai = N'Trống' " +
                               "WHERE trangThai = N'Đã đặt' AND maBan NOT IN (" +
                               "    SELECT c.maBan FROM DonDatBan d JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                               "    WHERE d.trangThai = N'Đã đặt' " +
                               "    AND DATEDIFF(MINUTE, GETDATE(), d.thoiGian) BETWEEN -30 AND 120" +
                               ")";
            int countXanh = stmt.executeUpdate(sqlToXanh);
            
            // 2. Lên màu VÀNG (Đã đặt) cho các bàn: Trống VÀ Còn <= 2 tiếng tới giờ khách nhận bàn
            String sqlToVang = "UPDATE Ban SET trangThai = N'Đã đặt' " +
                               "WHERE trangThai = N'Trống' AND maBan IN (" +
                               "    SELECT c.maBan FROM DonDatBan d JOIN ChiTietDatBan c ON d.maDon = c.maDon " +
                               "    WHERE d.trangThai = N'Đã đặt' " +
                               "    AND DATEDIFF(MINUTE, GETDATE(), d.thoiGian) BETWEEN -30 AND 120" +
                               ")";
            int countVang = stmt.executeUpdate(sqlToVang);
            
            if (countXanh > 0 || countVang > 0) {
                hasChange = true; // Cờ báo hiệu sơ đồ bàn cần được làm mới
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasChange;
    }

    /**
     * 7. TỰ ĐỘNG PHÁT SINH MÃ ĐƠN MỚI (Tránh lỗi trùng mã ngày cũ)
     * Quét toàn bộ CSDL để tìm mã Dxxx lớn nhất.
     */
    public String getMaDonTiepTheo() {
        String sql = "SELECT maDon FROM DonDatBan WHERE maDon LIKE 'D%'";
        int maxSo = 0;
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.length() > 1) {
                    try {
                        int so = Integer.parseInt(ma.substring(1));
                        if (so > maxSo) maxSo = so;
                    } catch (Exception ex) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("D%03d", maxSo + 1);
    }
}