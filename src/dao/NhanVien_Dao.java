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
            String sql = "SELECT maNV, hoTen, gmail, chucVu, luong, matKhau, gioiTinh, trangThai FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                // ĐỒNG BỘ: Dùng Constructor 8 tham số đã tạo bên entity.NhanVien
                // Dùng tên cột (Column Name) thay vì số thứ tự để code an toàn và dễ đọc hơn
                NhanVien nv = new NhanVien(
                    rs.getString("maNV"),
                    rs.getString("hoTen"),
                    rs.getString("gmail"),
                    rs.getString("chucVu"),
                    rs.getDouble("luong"),
                    rs.getString("matKhau"),
                    rs.getString("gioiTinh"),
                    rs.getString("trangThai") 
                );
                ds.add(nv);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean insertNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO NhanVien (maNV, hoTen, gmail, ngaySinh, gioiTinh, soCCCD, luong, matKhau, chucVu, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setString(3, nv.getGmail());
            stmt.setDate(4, java.sql.Date.valueOf("2000-01-01"));
            stmt.setString(5, nv.getGioiTinh());
            stmt.setString(6, "000000000000");
            stmt.setDouble(7, 5000000.0); // Mặc định lương cơ bản
            stmt.setString(8, nv.getMatKhau());
            stmt.setString(9, nv.getChucVu());
            
            // Đảm bảo nhân viên mới luôn có trạng thái là Đang làm
            String trangThai = (nv.getTrangThai() != null && !nv.getTrangThai().trim().isEmpty()) ? nv.getTrangThai() : "Đang làm";
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
            String sql = "UPDATE NhanVien SET hoTen = ?, gmail = ?, gioiTinh = ?, matKhau = ?, chucVu = ?, trangThai = ? WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setString(2, nv.getGmail());
            stmt.setString(3, nv.getGioiTinh());
            stmt.setString(4, nv.getMatKhau());
            stmt.setString(5, nv.getChucVu());
            stmt.setString(6, nv.getTrangThai()); // Cập nhật trạng thái mới
            stmt.setString(7, nv.getMaNV());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // XÓA MỀM (Soft Delete)
    public boolean deleteNhanVien(String maNV) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // Đổi trạng thái thành "Nghỉ việc" thay vì xóa dữ liệu
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