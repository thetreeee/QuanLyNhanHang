package dao;

import connectDB.SQLConnection;
import entity.KhuyenMai;
import entity.DoiTuongKM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai_DAO {

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY maKM DESC"; // Sắp xếp để cái mới nhất lên đầu

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKM(rs.getString("maKM"));
                km.setTenKM(rs.getString("tenKM"));
                km.setLoaiKM(rs.getString("loaiKM"));
                km.setGiaTri(rs.getDouble("giaTri"));
                km.setNgayBatDau(rs.getDate("ngayBatDau") != null ? rs.getDate("ngayBatDau").toLocalDate() : null);
                km.setNgayKetThuc(rs.getDate("ngayKetThuc") != null ? rs.getDate("ngayKetThuc").toLocalDate() : null);
                km.setTrangThai(rs.getString("trangThai"));

                DoiTuongKM dt = DoiTuongKM.fromLabel(rs.getString("doiTuongApDung"));
                km.setDoiTuongApDung(dt != null ? dt : DoiTuongKM.TAT_CA);

                list.add(km);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đổi thành boolean để báo cho Form biết có lưu thành công hay không
    public boolean insert(KhuyenMai km) {
        // PHẢI GHI RÕ TÊN CỘT ĐỂ TRÁNH LỖI LỆCH THỨ TỰ TRONG SQL SERVER
        String sql = "INSERT INTO KhuyenMai (maKM, tenKM, loaiKM, giaTri, ngayBatDau, ngayKetThuc, doiTuongApDung, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getMaKM());
            ps.setString(2, km.getTenKM());
            ps.setString(3, km.getLoaiKM());
            ps.setDouble(4, km.getGiaTri());
            ps.setDate(5, km.getNgayBatDau() != null ? Date.valueOf(km.getNgayBatDau()) : null);
            ps.setDate(6, km.getNgayKetThuc() != null ? Date.valueOf(km.getNgayKetThuc()) : null);
            ps.setString(7, km.getDoiTuongApDung() != null ? km.getDoiTuongApDung().toString() : null);
            ps.setString(8, km.getTrangThai());

            return ps.executeUpdate() > 0; // Trả về true nếu Insert thành công

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi (trùng mã, sai kiểu dữ liệu...)
        }
    }

    // Đổi thành boolean
    public boolean update(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM=?, loaiKM=?, giaTri=?, ngayBatDau=?, ngayKetThuc=?, doiTuongApDung=?, trangThai=? WHERE maKM=?";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getLoaiKM());
            ps.setDouble(3, km.getGiaTri());
            ps.setDate(4, km.getNgayBatDau() != null ? Date.valueOf(km.getNgayBatDau()) : null);
            ps.setDate(5, km.getNgayKetThuc() != null ? Date.valueOf(km.getNgayKetThuc()) : null);
            ps.setString(6, km.getDoiTuongApDung() != null ? km.getDoiTuongApDung().toString() : null);
            ps.setString(7, km.getTrangThai());
            ps.setString(8, km.getMaKM());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Đổi thành boolean
    public boolean stop(String maKM) {
        String sql = "UPDATE KhuyenMai SET trangThai = N'Ngừng áp dụng' WHERE maKM=?";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKM);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String generateMaKM() {
        String sql = "SELECT TOP 1 maKM FROM KhuyenMai ORDER BY maKM DESC";

        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastMa = rs.getString("maKM");
                int number = Integer.parseInt(lastMa.substring(2));
                number++;
                return String.format("KM%03d", number);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "KM001";
    }
}