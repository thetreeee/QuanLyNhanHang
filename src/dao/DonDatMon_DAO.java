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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public DonDatMon getDonDangMoTheoBan(String maBan) {
        try {
            Connection con = SQLConnection.getConnection();
            String sql = "SELECT * FROM DonDatMon WHERE maBan = ? ";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ĐÃ THÊM: Hàm tự động phát sinh mã đơn tuần tự (D001, D002...)
    public String phatSinhMaDon() {
        int max = 0;
        try (Connection con = SQLConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT maDonDat FROM DonDatMon")) {
             
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.startsWith("D")) {
                    try {
                        // Chỉ lấy những mã có độ dài bình thường (D001...), bỏ qua các mã Timestamp dài chục tỷ cũ
                        if (ma.length() <= 10) {
                            int so = Integer.parseInt(ma.substring(1));
                            if (so > max) max = so;
                        }
                    } catch (Exception e) {
                        // Bỏ qua nếu có lỗi ép kiểu của các dữ liệu rác
                    } 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Format mã mới: Chữ D + 3 chữ số (ví dụ: D001, D015, D100...)
        return String.format("D%03d", max + 1);
    }
    
    // ĐÃ SỬA: Thay thế logic tạo mã bằng hàm phatSinhMaDon()
    public String createDon(String maBan, String maNV, String ghiChu) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
   
    public boolean updateGhiChu(String maDon, String ghiChu) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SQLConnection.getConnection();
            String sql = "UPDATE DonDatMon SET ghiChu = ? WHERE maDonDat = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, ghiChu);
            ps.setString(2, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }
    
    public boolean xoaDon(String maDonDat) {
        try (java.sql.Connection con = connectDB.SQLConnection.getConnection()) {
            String sql = "DELETE FROM DonDatMon WHERE maDonDat = ?";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDonDat);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}