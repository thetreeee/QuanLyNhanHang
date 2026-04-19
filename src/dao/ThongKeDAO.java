package dao;

import connectDB.SQLConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThongKeDAO {

    // 1. Lấy Doanh Thu Hôm Nay (Tính tổng cột tongThanhTien của ngày hôm nay)
    public double getDoanhThuHomNay() {
        double doanhThu = 0;
        String sql = "SELECT SUM(tongThanhTien) AS Total FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                doanhThu = rs.getDouble("Total");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return doanhThu;
    }

    // 2. Lấy số lượng bàn đang phục vụ (Trạng thái = Đang dùng) / Tổng số bàn
    public int[] getThongKeBan() {
        int dangPhucVu = 0;
        int tongSoBan = 0;
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) AS DangDung FROM Ban WHERE trangThai LIKE N'%dùng%' OR trangThai LIKE N'%sử dụng%'");
            if (rs1.next()) dangPhucVu = rs1.getInt("DangDung");
            
            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) AS Tong FROM Ban");
            if (rs2.next()) tongSoBan = rs2.getInt("Tong");
        } catch (Exception e) { e.printStackTrace(); }
        return new int[]{dangPhucVu, tongSoBan};
    }

    // 3. Lấy dữ liệu biểu đồ (Doanh thu 7 ngày gần nhất)
    // Dùng LinkedHashMap để giữ đúng thứ tự ngày
 // 3. Lấy dữ liệu biểu đồ (Doanh thu 7 ngày gần nhất)
    public Map<String, Double> getDoanhThu7NgayQua() {
        Map<String, Double> data = new LinkedHashMap<>();
        
        // BƯỚC 1: Tạo sẵn khung 7 ngày (từ 6 ngày trước đến hôm nay), mặc định doanh thu = 0
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(formatter);
            data.put(dateStr, 0.0);
        }

        // BƯỚC 2: Quét SQL để đắp dữ liệu thật vào những ngày có bán được hàng
        String sql = "SELECT FORMAT(ngayLap, 'dd/MM') as Ngay, SUM(tongThanhTien) as DoanhThu " +
                     "FROM HoaDon " +
                     "WHERE CAST(ngayLap AS DATE) >= DATEADD(day, -6, CAST(GETDATE() AS DATE)) " +
                     "GROUP BY FORMAT(ngayLap, 'dd/MM'), CAST(ngayLap AS DATE) " +
                     "ORDER BY CAST(ngayLap AS DATE)";
                     
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ngay = rs.getString("Ngay");
                double doanhThu = rs.getDouble("DoanhThu");
                
                // Nếu ngày SQL trả về có trong khung 7 ngày thì cập nhật lại số tiền
                if (data.containsKey(ngay)) {
                    data.put(ngay, doanhThu);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        return data;
    }

    // 4. Lấy Top 4 món bán chạy nhất hôm nay (Bạn cần sửa tên bảng ChiTietHoaDon, MonAn cho đúng với DB của bạn)
    public Map<String, Double> getTopMonAnBanChay() {
        Map<String, Double> data = new LinkedHashMap<>();
        // LƯU Ý: Chỗ này tôi đang giả định bạn có bảng ChiTietHoaDon và MonAn
        String sql = "SELECT TOP 4 m.tenMon, SUM(c.thanhTien) as TongTien " +
                     "FROM ChiTietHoaDon c " +
                     "JOIN MonAn m ON c.maMon = m.maMon " +
                     "JOIN HoaDon h ON c.maHD = h.maHD " +
                     "WHERE CAST(h.ngayLap AS DATE) = CAST(GETDATE() AS DATE) " +
                     "GROUP BY m.tenMon " +
                     "ORDER BY TongTien DESC";
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("tenMon"), rs.getDouble("TongTien"));
            }
        } catch (Exception e) { 
            // Nếu chưa có bảng thì trả về dữ liệu mẫu để giao diện không bị trống
            data.put("Chưa có dữ liệu (Cần SQL)", 0.0); 
        }
        return data;
    }
}