package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connectDB.SQLConnection;

public class taiKhoanDao {
    private String url = "jdbc:sqlserver://localhost:1433;databaseName=TuanTruongDB;encrypt=false";
    private String dbUser = "sa"; 
    private String dbPass = "sapassword"; 

    public boolean kiemTraDangNhap(String user, String pass) {
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            // ĐÃ CẬP NHẬT: chucVu = N'Quản lý' để khớp chính xác với lệnh SQL của bạn
            String sql = "SELECT maNV, matKhau FROM NhanVien WHERE maNV = ? AND matKhau = ? AND chucVu = N'Quản lý' AND trangThai = N'Đang làm'";
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // Dùng .trim() để cắt sạch mọi khoảng trắng thừa (rất hay gặp nếu DB dùng kiểu CHAR)
                String dbMaNV = rs.getString("maNV").trim();
                String dbMatKhau = rs.getString("matKhau").trim();
                
                // Kiểm tra chính xác chữ hoa/chữ thường trong Java
                if (user.equals(dbMaNV) && pass.equals(dbMatKhau)) {
                    return true; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false; 
    }
    public String getRole(String user, String pass) {
        String sql = "SELECT chucVu FROM NhanVien WHERE maNV = ? AND matKhau = ?";
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("chucVu");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}