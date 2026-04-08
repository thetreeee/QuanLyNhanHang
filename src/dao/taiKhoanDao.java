package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import entity.NhanVien;

public class taiKhoanDao {
    private String url = "jdbc:sqlserver://localhost:1433;databaseName=TuanTruongDB;encrypt=false";
    private String dbUser = "sa"; 
    private String dbPass = "sapassword";

    public static NhanVien nvDangNhap = null;

    public boolean kiemTraDangNhap(String user, String pass) {
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT maNV, matKhau FROM NhanVien WHERE maNV = ? AND matKhau = ? AND trangThai = N'Đang làm'";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false; 
    }

    public String getRole(String user, String pass) {
        String sql = "SELECT chucVu FROM NhanVien WHERE maNV = ? AND matKhau = ? AND trangThai = N'Đang làm'";
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("chucVu"); 
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public NhanVien getNV(String user, String pass) {
        String sql = "SELECT maNV, hoTen FROM NhanVien WHERE maNV = ? AND matKhau = ? AND trangThai = N'Đang làm'";
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                return new NhanVien(rs.getString("maNV"), rs.getString("hoTen")); 
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}