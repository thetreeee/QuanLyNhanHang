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
            String sql = "SELECT maNV, hoTen, ngaySinh, soDienThoai, gmail, chucVu, matKhau, gioiTinh, trangThai FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("ngaySinh");
                java.time.LocalDate ns = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                
                NhanVien nv = new NhanVien(
                    rs.getString("maNV"),
                    rs.getString("hoTen"),
                    ns,
                    rs.getString("soDienThoai"), // Đọc số điện thoại từ DB
                    rs.getString("gmail"),
                    rs.getString("chucVu"),
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
            // ĐÃ SỬA: 9 cột tương ứng với 9 dấu chấm hỏi (?)
            String sql = "INSERT INTO NhanVien (maNV, hoTen, soDienThoai, gmail, ngaySinh, gioiTinh, matKhau, chucVu, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            
            // ĐÃ SỬA: Đánh lại số thứ tự liền mạch từ 1 đến 9
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setString(3, nv.getSoDienThoai()); 
            stmt.setString(4, nv.getGmail());
            stmt.setDate(5, (nv.getNgaySinh() != null) ? java.sql.Date.valueOf(nv.getNgaySinh()) : null); 
            stmt.setString(6, nv.getGioiTinh());
            stmt.setString(7, nv.getMatKhau());
            stmt.setString(8, nv.getChucVu());
            
            String trangThai = (nv.getTrangThai() != null && !nv.getTrangThai().trim().isEmpty()) ? nv.getTrangThai() : "Đang làm";
            stmt.setString(9, trangThai);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) {
                JOptionPane.showMessageDialog(null, "Mã " + nv.getMaNV() + " đã tồn tại!");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean updateNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String sql = "UPDATE NhanVien SET hoTen = ?, ngaySinh = ?, soDienThoai = ?, gmail = ?, gioiTinh = ?, matKhau = ?, chucVu = ?, trangThai = ? WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setDate(2, (nv.getNgaySinh() != null) ? java.sql.Date.valueOf(nv.getNgaySinh()) : null);
            stmt.setString(3, nv.getSoDienThoai()); // Cập nhật số điện thoại
            stmt.setString(4, nv.getGmail());
            stmt.setString(5, nv.getGioiTinh());
            stmt.setString(6, nv.getMatKhau());
            stmt.setString(7, nv.getChucVu());
            stmt.setString(8, nv.getTrangThai());
            stmt.setString(9, nv.getMaNV());
            
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