package dao;

import connectDB.SQLConnection;
import entity.HoaDon;
import entity.NhanVien;
import entity.Ban;
import entity.KhuyenMai;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon_DAO {

	public List<HoaDon> getAll() {
		List<HoaDon> ds = new ArrayList<>();
		String sql = "SELECT maHD, ngayLap, maKM, maNV, maBan, phuongThucTT, tongThanhTien FROM HoaDon ORDER BY ngayLap DESC";
		try (Connection con = SQLConnection.getConnection();
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sql)) {

			while (rs.next()) {
				HoaDon hd = new HoaDon();
				hd.setMaHD(rs.getString("maHD"));
				if (rs.getTimestamp("ngayLap") != null) {
					hd.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
				}
				hd.setPhuongThucTT(rs.getString("phuongThucTT"));
				hd.setTongTien(rs.getDouble("tongThanhTien"));

				// --- SỬA Ở ĐÂY ĐỂ TRÁNH LỖI CONSTRUCTOR ---
				NhanVien nv = new NhanVien();
				nv.setMaNV(rs.getString("maNV"));
				hd.setNhanVien(nv);

				Ban b = new Ban();
				b.setMaBan(rs.getString("maBan"));
				hd.setBan(b);

				if (rs.getString("maKM") != null && !rs.getString("maKM").isEmpty()) {
					KhuyenMai km = new KhuyenMai();
					km.setMaKM(rs.getString("maKM"));
					hd.setKhuyenMai(km);
				}
				// -------------------------------------------

				ds.add(hd);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ds;
	}

	// Hàm xóa hóa đơn (Xóa Chi tiết trước, xóa Hóa đơn sau)
	public boolean xoaHoaDon(String maHD) {
		String sqlCT = "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
		String sqlHD = "DELETE FROM HoaDon WHERE maHD = ?";

		try (Connection con = SQLConnection.getConnection()) {
			con.setAutoCommit(false); // Tắt auto commit để đảm bảo an toàn
			try (PreparedStatement psCT = con.prepareStatement(sqlCT);
					PreparedStatement psHD = con.prepareStatement(sqlHD)) {

				// Xóa chi tiết hóa đơn
				psCT.setString(1, maHD);
				psCT.executeUpdate();

				// Xóa hóa đơn
				psHD.setString(1, maHD);
				int ketQua = psHD.executeUpdate();

				con.commit(); // Thành công thì lưu thay đổi
				return ketQua > 0;
			} catch (Exception e) {
				con.rollback(); // Lỗi thì hoàn tác
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean themHoaDon(HoaDon hd) {
		String sql = "INSERT INTO HoaDon (maHD, ngayLap, maKM, maNV, maBan, phuongThucTT, tongThanhTien) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = SQLConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, hd.getMaHD());
			ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
			ps.setString(3, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : null);
			ps.setString(4, hd.getNhanVien().getMaNV());
			ps.setString(5, hd.getBan().getMaBan());
			ps.setString(6, hd.getPhuongThucTT());
			ps.setDouble(7, hd.getTongTien());

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}