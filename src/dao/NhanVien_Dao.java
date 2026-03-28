package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import entity.NhanVien;
import javax.swing.JOptionPane;

public class NhanVien_Dao {
    private String url = "jdbc:sqlserver://localhost:1433;databaseName=TuanTruongDB;encrypt=false";
    private String user = "sa"; 
    private String pass = "sapassword"; 

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // CẬP NHẬT: Thêm cột trangThai vào câu lệnh SELECT
            String sql = "SELECT maNV, hoTen, gmail, chucVu, luong, matKhau, gioiTinh, trangThai FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien(
                    rs.getString(1), // maNV
                    rs.getString(2), // hoTen
                    rs.getString(3), // gmail
                    rs.getString(4), // chucVu
                    rs.getDouble(5), // luong
                    rs.getString(6), // matKhau
                    rs.getString(7)  // gioiTinh
                );
                // Gán thêm trạng thái cho đối tượng nhân viên
                nv.setTrangThai(rs.getString(8));
                
                ds.add(nv);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean insertNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // CẬP NHẬT: Thêm cột trangThai vào lệnh INSERT
            String sql = "INSERT INTO NhanVien (maNV, hoTen, gmail, ngaySinh, gioiTinh, soCCCD, luong, matKhau, chucVu, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setString(3, nv.getGmail());
            stmt.setDate(4, java.sql.Date.valueOf("2000-01-01"));
            stmt.setString(5, nv.getGioiTinh());
            stmt.setString(6, "000000000000");
            stmt.setDouble(7, 5000000.0);
            stmt.setString(8, nv.getMatKhau());
            stmt.setString(9, nv.getChucVu());
            
            // Mặc định khi thêm mới là "Đang làm"
            String trangThai = (nv.getTrangThai() != null && !nv.getTrangThai().isEmpty()) ? nv.getTrangThai() : "Đang làm";
            stmt.setString(10, trangThai);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) JOptionPane.showMessageDialog(null, "Mã " + nv.getMaNV() + " đã tồn tại!");
            else e.printStackTrace();
            return false;
        }
    }

    public boolean updateNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // CẬP NHẬT: Thêm việc cập nhật trangThai
            String sql = "UPDATE NhanVien SET hoTen = ?, gmail = ?, gioiTinh = ?, matKhau = ?, chucVu = ?, trangThai = ? WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setString(2, nv.getGmail());
            stmt.setString(3, nv.getGioiTinh());
            stmt.setString(4, nv.getMatKhau());
            stmt.setString(5, nv.getChucVu());
            stmt.setString(6, nv.getTrangThai()); // Cập nhật trạng thái
            stmt.setString(7, nv.getMaNV());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // CẬP NHẬT QUAN TRỌNG: XÓA MỀM (Soft Delete)
    public boolean deleteNhanVien(String maNV) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // Thay vì DELETE, ta dùng UPDATE để đổi trạng thái thành "Nghỉ việc"
            String sql = "UPDATE NhanVien SET trangThai = N'Nghỉ việc' WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}