package dao;

import connectDB.SQLConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThongKeDAO {

    // =================================================================================
    // 1. TỔNG QUAN: Lấy Tổng Doanh Thu và Tổng Số Đơn trong khoảng thời gian
    // Trả về mảng double: [0] = Tổng Doanh Thu, [1] = Tổng Số Đơn
    // =================================================================================
    public double[] getTongQuanHoaDon(LocalDate tuNgay, LocalDate denNgay) {
        double[] ketQua = new double[]{0.0, 0.0};
        String sql = "SELECT SUM(tongThanhTien) AS DoanhThu, COUNT(maHD) AS SoDon " +
                     "FROM HoaDon WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ?";
                     
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ketQua[0] = rs.getDouble("DoanhThu");
                    ketQua[1] = rs.getDouble("SoDon");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ketQua;
    }

    // =================================================================================
    // 1b. KPI HÔM NAY (Cố định): Trả về Doanh Thu, Tên Món Bán Chạy Nhất, Số Lượng Món Đang Bán
    // =================================================================================
    public Object[] getKPIHomNay() {
        Object[] kpi = new Object[]{0.0, "Chưa có", 0};
        
        String sql1 = "SELECT SUM(tongThanhTien) AS DoanhThu FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
        String sql2 = "SELECT TOP 1 m.tenMon FROM ChiTietHoaDon c JOIN MonAn m ON c.maMon = m.maMon JOIN HoaDon h ON c.maHD = h.maHD WHERE CAST(h.ngayLap AS DATE) = CAST(GETDATE() AS DATE) GROUP BY m.tenMon ORDER BY SUM(c.soLuong) DESC";
        String sql3 = "SELECT COUNT(maMon) AS SoMon FROM MonAn WHERE trangThai = N'Đang bán'";
        
        try (Connection con = SQLConnection.getConnection()) {
            try (PreparedStatement ps1 = con.prepareStatement(sql1); ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) kpi[0] = rs1.getDouble("DoanhThu");
            }
            try (PreparedStatement ps2 = con.prepareStatement(sql2); ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) kpi[1] = rs2.getString("tenMon");
            }
            try (PreparedStatement ps3 = con.prepareStatement(sql3); ResultSet rs3 = ps3.executeQuery()) {
                if (rs3.next()) kpi[2] = rs3.getInt("SoMon");
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        return kpi;
    }

    // =================================================================================
    // 2. BIỂU ĐỒ TRÒN: Lấy Top 5 Món Ăn Bán Chạy Nhất trong khoảng thời gian
    // =================================================================================
    public Map<String, Double> getTopMonAnBanChay(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT TOP 5 m.tenMon, SUM(c.thanhTien) as TongTien " +
                     "FROM ChiTietHoaDon c " +
                     "JOIN MonAn m ON c.maMon = m.maMon " +
                     "JOIN HoaDon h ON c.maHD = h.maHD " +
                     "WHERE CAST(h.ngayLap AS DATE) >= ? AND CAST(h.ngayLap AS DATE) <= ? " +
                     "GROUP BY m.tenMon " +
                     "ORDER BY TongTien DESC";
                     
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("tenMon"), rs.getDouble("TongTien"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // =================================================================================
    // 3. BIỂU ĐỒ CỘT/ĐƯỜNG: Lấy Doanh Thu linh hoạt theo (Ngày / Tháng / Quý)
    // =================================================================================
    public Map<String, Double> getDoanhThuBieuDo(LocalDate tuNgay, LocalDate denNgay, String loaiThongKe) {
        Map<String, Double> data = new LinkedHashMap<>();
        
        // BƯỚC 1: Dựng sẵn trục X (Để những ngày/tháng không có khách mua vẫn hiện 0đ)
        taoKhungThoiGian(data, tuNgay, denNgay, loaiThongKe);

        // BƯỚC 2: Chọn câu lệnh SQL tương ứng để Group By
        String sql = "";
        if (loaiThongKe.equalsIgnoreCase("Ngày")) {
            sql = "SELECT FORMAT(ngayLap, 'dd/MM') as Nhan, SUM(tongThanhTien) as DoanhThu " +
                  "FROM HoaDon WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                  "GROUP BY FORMAT(ngayLap, 'dd/MM'), CAST(ngayLap AS DATE) ORDER BY CAST(ngayLap AS DATE)";
        } 
        else if (loaiThongKe.equalsIgnoreCase("Tháng")) {
            sql = "SELECT FORMAT(ngayLap, 'MM/yyyy') as Nhan, SUM(tongThanhTien) as DoanhThu " +
                  "FROM HoaDon WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                  "GROUP BY FORMAT(ngayLap, 'MM/yyyy'), YEAR(ngayLap), MONTH(ngayLap) ORDER BY YEAR(ngayLap), MONTH(ngayLap)";
        } 
        else if (loaiThongKe.equalsIgnoreCase("Quý")) {
            sql = "SELECT 'Q' + CAST(DATEPART(QUARTER, ngayLap) AS VARCHAR) + '/' + CAST(YEAR(ngayLap) AS VARCHAR) as Nhan, " +
                  "SUM(tongThanhTien) as DoanhThu " +
                  "FROM HoaDon WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                  "GROUP BY DATEPART(QUARTER, ngayLap), YEAR(ngayLap) ORDER BY YEAR(ngayLap), DATEPART(QUARTER, ngayLap)";
        }

        // BƯỚC 3: Đổ dữ liệu thật từ Database vào Khung
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nhan = rs.getString("Nhan");
                    double doanhThu = rs.getDouble("DoanhThu");
                    if (data.containsKey(nhan)) {
                        data.put(nhan, doanhThu); // Đè số tiền thật lên số 0
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return data;
    }

    // =================================================================================
    // 4. BẢNG THỐNG KÊ MÓN ĂN: Lấy Danh sách món ăn và số lượng bán
    // =================================================================================
    public java.util.List<Object[]> getThongKeMonAnBang(LocalDate tuNgay, LocalDate denNgay) {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        String sql = "SELECT m.maMon, m.tenMon, SUM(c.soLuong) as SoLuongBan " +
                     "FROM ChiTietHoaDon c " +
                     "JOIN MonAn m ON c.maMon = m.maMon " +
                     "JOIN HoaDon h ON c.maHD = h.maHD " +
                     "WHERE CAST(h.ngayLap AS DATE) >= ? AND CAST(h.ngayLap AS DATE) <= ? " +
                     "GROUP BY m.maMon, m.tenMon " +
                     "ORDER BY SoLuongBan DESC";
                     
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[]{
                        rs.getString("maMon"),
                        rs.getString("tenMon"),
                        rs.getInt("SoLuongBan")
                    };
                    list.add(row);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // --- Hàm bổ trợ: Dựng trục X (Nhãn thời gian) ---
    private void taoKhungThoiGian(Map<String, Double> data, LocalDate tuNgay, LocalDate denNgay, String loaiThongKe) {
        if (loaiThongKe.equalsIgnoreCase("Ngày")) {
            // Giới hạn chống treo máy nếu người dùng lỡ chọn khoảng cách quá xa (giới hạn 31 ngày để vẽ biểu đồ)
            long khoangCach = ChronoUnit.DAYS.between(tuNgay, denNgay);
            if (khoangCach > 31) tuNgay = denNgay.minusDays(31); 
            
            LocalDate temp = tuNgay;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            while (!temp.isAfter(denNgay)) {
                data.put(temp.format(fmt), 0.0);
                temp = temp.plusDays(1);
            }
        } 
        else if (loaiThongKe.equalsIgnoreCase("Tháng")) {
            YearMonth startMonth = YearMonth.from(tuNgay);
            YearMonth endMonth = YearMonth.from(denNgay);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
            while (!startMonth.isAfter(endMonth)) {
                data.put(startMonth.format(fmt), 0.0);
                startMonth = startMonth.plusMonths(1);
            }
        } 
        else if (loaiThongKe.equalsIgnoreCase("Quý")) {
            LocalDate temp = tuNgay;
            while (!temp.isAfter(denNgay)) {
                int quy = (temp.getMonthValue() - 1) / 3 + 1;
                String nhanQuy = "Q" + quy + "/" + temp.getYear();
                data.putIfAbsent(nhanQuy, 0.0);
                temp = temp.plusMonths(3); // Bước nhảy theo Quý
            }
        }
    }
}