package entity;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String gmail;
    private String chucVu;
    private double luong;
    private String matKhau;
    private String gioiTinh;
    
    // Thêm biến trạng thái để phục vụ tính năng Xóa mềm (Soft Delete)
    private String trangThai; 

    // Constructor 7 tham số (Giữ nguyên để không làm lỗi code ở các form cũ)
    public NhanVien(String maNV, String hoTen, String gmail, String chucVu, double luong, String matKhau, String gioiTinh) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.gmail = gmail;
        this.chucVu = chucVu;
        this.luong = luong;
        this.matKhau = matKhau;
        this.gioiTinh = gioiTinh;
    }

    // Constructor 8 tham số (Đầy đủ tất cả thông tin)
    public NhanVien(String maNV, String hoTen, String gmail, String chucVu, double luong, String matKhau, String gioiTinh, String trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.gmail = gmail;
        this.chucVu = chucVu;
        this.luong = luong;
        this.matKhau = matKhau;
        this.gioiTinh = gioiTinh;
        this.trangThai = trangThai;
    }

    // Các Getter hiện có
    public String getMaNV() { return maNV; }
    public String getHoTen() { return hoTen; }
    public String getGmail() { return gmail; }
    public String getChucVu() { return chucVu; }
    public double getLuong() { return luong; }
    public String getMatKhau() { return matKhau; }
    public String getGioiTinh() { return gioiTinh; }

    // --- THÊM GETTER VÀ SETTER CHO TRẠNG THÁI ---
    public String getTrangThai() { 
        return trangThai; 
    }
    
    public void setTrangThai(String trangThai) { 
        this.trangThai = trangThai; 
    }

    // (Tùy chọn) Thêm một vài Setter khác nếu sau này bạn cần cập nhật từng trường riêng lẻ
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setGmail(String gmail) { this.gmail = gmail; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
    public void setLuong(double luong) { this.luong = luong; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
}