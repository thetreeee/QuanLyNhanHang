package dao;

import connectDB.SQLConnection;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHang_DAO {

    // ==============================================================
    // 1. LẤY TẤT CẢ DANH SÁCH KHÁCH HÀNG (Hiển thị lên bảng)
    // ==============================================================
    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        try (Connection con = SQLConnection.getConnection(); 
             Statement st = con.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new KhachHang(
                    rs.getString("maKhachHang"), 
                    rs.getString("hoTen"), 
                    rs.getString("soDienThoai"), 
                    rs.getDouble("tongChiTieu")
                ));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return ds;
    }

    // ==============================================================
    // 2. TÌM KHÁCH HÀNG THEO SỐ ĐIỆN THOẠI (Dùng cho Auto-fill)
    // ==============================================================
    public KhachHang getKhachHangBySDT(String sdt) {
        String sql = "SELECT * FROM KhachHang WHERE soDienThoai = ?";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new KhachHang(
                    rs.getString("maKhachHang"), 
                    rs.getString("hoTen"), 
                    rs.getString("soDienThoai"), 
                    rs.getDouble("tongChiTieu")
                );
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // ==============================================================
    // 3. TÌM KHÁCH HÀNG THEO MÃ
    // ==============================================================
    public KhachHang getKhachHangByMa(String maKH) {
        String sql = "SELECT * FROM KhachHang WHERE maKhachHang = ?";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new KhachHang(
                    rs.getString("maKhachHang"), 
                    rs.getString("hoTen"), 
                    rs.getString("soDienThoai"), 
                    rs.getDouble("tongChiTieu")
                );
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // ==============================================================
    // 4. THÊM KHÁCH HÀNG MỚI (Dùng cho form Thêm Khách Hàng)
    // ==============================================================
    public boolean insertKhachHang(KhachHang kh) {
        // Lưu ý: Cột tongChiTieu, diemHienTai, hangThanhVien trong SQL đã được set DEFAULT
        // nên chúng ta chỉ cần INSERT 3 thông tin cơ bản là đủ.
        String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai) VALUES (?, ?, ?)";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getHoTen());
            ps.setString(3, kh.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    // ==============================================================
    // 5. CẬP NHẬT THÔNG TIN KHÁCH HÀNG
    // ==============================================================
    public boolean updateKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? WHERE maKhachHang = ?";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSoDienThoai());
            ps.setString(3, kh.getMaKH());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return false;
    }

 // ==============================================================
    // 6. TỰ ĐỘNG PHÁT SINH MÃ KHÁCH HÀNG TIẾP THEO (KH001, KH002...)
    // ==============================================================
    public String tuDongPhatSinhMa() {
        // Cắt bỏ chữ "KH", lấy phần số chuyển thành INT và tìm giá trị lớn nhất
        String sql = "SELECT MAX(CAST(SUBSTRING(maKhachHang, 3, LEN(maKhachHang)) AS INT)) FROM KhachHang WHERE maKhachHang LIKE 'KH%'";
        try (Connection con = SQLConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            // Nếu Database đã có dữ liệu (có khách hàng)
            if (rs.next() && rs.getString(1) != null) {
                int maxSo = rs.getInt(1);
                // Sinh mã mới bằng cách cộng 1, định dạng %03d để giữ số 0 ở đầu (VD: 001, 015)
                return String.format("KH%03d", maxSo + 1); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Trả về mã đầu tiên nếu bảng đang trống trơn
        return "KH001"; 
    }

    // ==============================================================
    // 7. XÓA KHÁCH HÀNG
    // ==============================================================
    public boolean xoaMemKhachHang(String maKH) {
        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            // Sẽ nhảy vào đây nếu khách hàng này bị vướng khóa ngoại (đã từng đặt bàn/hóa đơn)
            return false; 
        }
    }
    
    // ==============================================================
    // 8. KIỂM TRA TRÙNG SỐ ĐIỆN THOẠI KHI THÊM/SỬA
    // ==============================================================
    public boolean kiemTraTrungSDT(String sdt, String maKHLoiTru) {
        String sql = "SELECT maKhachHang FROM KhachHang WHERE soDienThoai = ? AND maKhachHang != ?";
        try (Connection con = SQLConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ps.setString(2, maKHLoiTru != null ? maKHLoiTru : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu bị trùng SĐT
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}