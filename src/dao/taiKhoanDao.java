package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class taiKhoanDao {
    // Nhớ điều chỉnh lại tên database, user, pass cho đúng với máy của bạn nếu cần
    private String url = "jdbc:sqlserver://localhost:1433;databaseName=TuanTruongDB;encrypt=false";
    private String dbUser = "sa"; 
    private String dbPass = "sapassword"; 

    public boolean kiemTraDangNhap(String user, String pass) {
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            // Lệnh SQL lọc sơ bộ
            String sql = "SELECT maNV, matKhau FROM NhanVien WHERE maNV = ? AND matKhau = ? AND chucVu = 'QUANLY' AND trangThai = N'Đang làm'";
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            
            ResultSet rs = pst.executeQuery();
            
            // Nếu có kết quả trả về từ SQL, ta bắt đầu dùng Java để kiểm tra hoa/thường
            if (rs.next()) {
                String dbMaNV = rs.getString("maNV");
                String dbMatKhau = rs.getString("matKhau");
                
                // Hàm .equals() của Java sẽ bắt buộc chính xác 100% từng ký tự hoa/thường
                if (user.equals(dbMaNV) && pass.equals(dbMatKhau)) {
                    return true; // Hoàn toàn trùng khớp
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Trả về false nếu sai tài khoản, sai mật khẩu, sai chữ hoa/thường
        return false; 
    }
}