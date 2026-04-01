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
            // CẬP NHẬT: Thêm soDienThoai vào câu lệnh SELECT
            String sql = "SELECT maNV, hoTen, soDienThoai, gmail, chucVu, luong, matKhau, gioiTinh, trangThai FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                // Sử dụng Constructor 9 tham số (Đã có số điện thoại)
                NhanVien nv = new NhanVien(
                    rs.getString("maNV"),
                    rs.getString("hoTen"),
                    rs.getString("soDienThoai"), // Đọc số điện thoại từ DB
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
            // CẬP NHẬT: Thêm soDienThoai vào câu lệnh INSERT (Tổng cộng 11 dấu chấm hỏi)
            String sql = "INSERT INTO NhanVien (maNV, hoTen, soDienThoai, gmail, ngaySinh, gioiTinh, soCCCD, luong, matKhau, chucVu, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setString(3, nv.getSoDienThoai()); // Ghi số điện thoại xuống DB
            stmt.setString(4, nv.getGmail());
            stmt.setDate(5, java.sql.Date.valueOf("2000-01-01")); // Giá trị mặc định
            stmt.setString(6, nv.getGioiTinh());
            stmt.setString(7, "000000000000"); // Giá trị mặc định
            stmt.setDouble(8, 5000000.0); // Mặc định lương cơ bản
            stmt.setString(9, nv.getMatKhau());
            stmt.setString(10, nv.getChucVu());
            
            // Đảm bảo nhân viên mới luôn có trạng thái là Đang làm
            String trangThai = (nv.getTrangThai() != null && !nv.getTrangThai().trim().isEmpty()) ? nv.getTrangThai() : "Đang làm";
            stmt.setString(11, trangThai);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) JOptionPane.showMessageDialog(null, "Mã " + nv.getMaNV() + " đã tồn tại!");
            else e.printStackTrace();
            return false;
        }
    }

    public boolean updateNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // CẬP NHẬT: Thêm SET soDienThoai vào câu lệnh UPDATE
            String sql = "UPDATE NhanVien SET hoTen = ?, soDienThoai = ?, gmail = ?, gioiTinh = ?, matKhau = ?, chucVu = ?, trangThai = ? WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setString(2, nv.getSoDienThoai()); // Cập nhật số điện thoại
            stmt.setString(3, nv.getGmail());
            stmt.setString(4, nv.getGioiTinh());
            stmt.setString(5, nv.getMatKhau());
            stmt.setString(6, nv.getChucVu());
            stmt.setString(7, nv.getTrangThai());
            stmt.setString(8, nv.getMaNV());
            
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