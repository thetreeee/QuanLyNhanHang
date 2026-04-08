package dao;

import entity.*;
import java.sql.*;
import java.util.*;

import connectDB.SQLConnection;

public class ChiTietDatMon_DAO {

    public List<ChiTietDatMon> getByMaDon(String maDon) {
        List<ChiTietDatMon> list = new ArrayList<>();

        try {
            Connection con = SQLConnection.getConnection();
            
            String sql = """
                SELECT c.*, m.tenMon
                FROM ChiTietDatMon c
                JOIN MonAn m ON c.maMon = m.maMon
                WHERE c.maDonDat = ?
            """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDon);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ChiTietDatMon ct = new ChiTietDatMon(
                        rs.getString("maDonDat"),
                        rs.getString("maMon"),
                        rs.getInt("soLuong"),
                        rs.getFloat("donGia")
                );

                MonAn m = new MonAn();
                m.setTenMon(rs.getString("tenMon"));
                ct.setMonAn(m);

                list.add(ct);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    // ĐÃ NÂNG CẤP: Chống lỗi trùng lặp khóa chính (Duplicate Key)
    public void insertChiTiet(String maDon, DonGoiMon d) {
        String sql = "IF EXISTS (SELECT 1 FROM ChiTietDatMon WHERE maDonDat = ? AND maMon = ?) " +
                     "BEGIN " +
                     "    UPDATE ChiTietDatMon SET soLuong = soLuong + ? WHERE maDonDat = ? AND maMon = ? " +
                     "END " +
                     "ELSE " +
                     "BEGIN " +
                     "    INSERT INTO ChiTietDatMon(maDonDat, maMon, soLuong, donGia) VALUES (?, ?, ?, ?) " +
                     "END";
                     
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // 1. Tham số cho câu IF EXISTS
            ps.setString(1, maDon);
            ps.setString(2, d.getMaMon());
            
            // 2. Tham số cho câu UPDATE
            ps.setInt(3, d.getSoLuong());
            ps.setString(4, maDon);
            ps.setString(5, d.getMaMon());
            
            // 3. Tham số cho câu INSERT
            ps.setString(6, maDon);
            ps.setString(7, d.getMaMon());
            ps.setInt(8, d.getSoLuong());
            ps.setDouble(9, d.getGia());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ĐÃ NÂNG CẤP TƯƠNG TỰ CHO HÀM insertCt
    public void insertCt(String maDon, ChiTietDatMon d) {
        String sql = "IF EXISTS (SELECT 1 FROM ChiTietDatMon WHERE maDonDat = ? AND maMon = ?) " +
                     "BEGIN " +
                     "    UPDATE ChiTietDatMon SET soLuong = soLuong + ? WHERE maDonDat = ? AND maMon = ? " +
                     "END " +
                     "ELSE " +
                     "BEGIN " +
                     "    INSERT INTO ChiTietDatMon(maDonDat, maMon, soLuong, donGia) VALUES (?, ?, ?, ?) " +
                     "END";
                     
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // 1. Tham số cho câu IF EXISTS
            ps.setString(1, maDon);
            ps.setString(2, d.getMaMon());
            
            // 2. Tham số cho câu UPDATE
            ps.setInt(3, d.getSoLuong());
            ps.setString(4, maDon);
            ps.setString(5, d.getMaMon());
            
            // 3. Tham số cho câu INSERT
            ps.setString(6, maDon);
            ps.setString(7, d.getMaMon());
            ps.setInt(8, d.getSoLuong());
            ps.setDouble(9, d.getDonGia());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void suaDatMon(String maDon, String maMon, int soLuong, double donGia) {
        try (Connection con = SQLConnection.getConnection()) {
            String sql = "UPDATE ChiTietDatMon SET soLuong = ?, donGia = ? WHERE maDonDat = ? AND maMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, soLuong);
            ps.setDouble(2, donGia);
            ps.setString(3, maDon);
            ps.setString(4, maMon);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xoaDatMon(String maDon, String maMon) {
        try (Connection con = SQLConnection.getConnection()) {
            String sql = "DELETE FROM ChiTietDatMon WHERE maDonDat = ? AND maMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDon);
            ps.setString(2, maMon);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean xoaTatCaChiTietTheoMaDon(String maDonDat) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SQLConnection.getConnection(); 
            String sql = "DELETE FROM ChiTietDatMon WHERE maDonDat = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, maDonDat);
            
            return ps.executeUpdate() >= 0; 
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e2) {}
        }
        return false;
    }
}