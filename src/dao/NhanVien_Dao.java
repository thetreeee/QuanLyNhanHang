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
            String sql = "SELECT maNV, hoTen, gmail, chucVu, luong, matKhau, gioiTinh FROM NhanVien";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ds.add(new NhanVien(rs.getString(1), rs.getString(2), rs.getString(3), 
                                   rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean insertNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO NhanVien (maNV, hoTen, gmail, ngaySinh, gioiTinh, soCCCD, luong, matKhau, chucVu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) JOptionPane.showMessageDialog(null, "Mã " + nv.getMaNV() + " đã tồn tại!");
            else e.printStackTrace();
            return false;
        }
    }

    public boolean updateNhanVien(NhanVien nv) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String sql = "UPDATE NhanVien SET hoTen = ?, gmail = ?, gioiTinh = ?, matKhau = ?, chucVu = ? WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setString(2, nv.getGmail());
            stmt.setString(3, nv.getGioiTinh());
            stmt.setString(4, nv.getMatKhau());
            stmt.setString(5, nv.getChucVu());
            stmt.setString(6, nv.getMaNV());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean deleteNhanVien(String maNV) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String sql = "DELETE FROM NhanVien WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Nếu vướng khóa ngoại (nhân viên đã lập hóa đơn), SQL sẽ báo lỗi
            e.printStackTrace();
            return false;
        }
    }
}