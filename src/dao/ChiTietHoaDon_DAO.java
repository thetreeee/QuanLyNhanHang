package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import entity.MonAn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDon_DAO {

    public List<ChiTietHoaDon> getByMaHD(String maHD) {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        // Khớp với cấu trúc có cột thanhTien
        String sql = "SELECT ct.maHD, ct.maMon, ct.soLuong, ct.thanhTien, m.tenMon " +
                     "FROM ChiTietHoaDon ct JOIN MonAn m ON ct.maMon = m.maMon WHERE ct.maHD = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setMaHD(rs.getString("maHD"));
                ct.setMaMon(rs.getString("maMon"));
                
                int sl = rs.getInt("soLuong");
                ct.setSoLuong(sl);
                
                // Tính ngược lại đơn giá từ thanh tiền để hiển thị
                double thanhTien = rs.getDouble("thanhTien");
                ct.setDonGia(sl > 0 ? (thanhTien / sl) : 0);
                
                MonAn mon = new MonAn();
                mon.setTenMon(rs.getString("tenMon"));
                ct.setMonAn(mon);
                
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean themChiTietHienTai(String maHD, String maMon, int sl, double thanhTien) {
        String sql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setString(2, maMon);
            ps.setInt(3, sl);
            ps.setDouble(4, thanhTien);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}