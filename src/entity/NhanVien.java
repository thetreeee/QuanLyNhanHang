package entity;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String soDienThoai; // ĐÃ THÊM: Số điện thoại
    private String gmail;
    private String chucVu;
    private double luong;
    private String matKhau;
    private String gioiTinh;
    private String trangThai; 

    // Constructor 7 tham số (Giữ nguyên để không báo lỗi các file cũ)
    public NhanVien(String maNV, String hoTen, String gmail, String chucVu, double luong, String matKhau, String gioiTinh) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.gmail = gmail;
        this.chucVu = chucVu;
        this.luong = luong;
        this.matKhau = matKhau;
        this.gioiTinh = gioiTinh;
    }

    // Constructor 8 tham số (Giữ nguyên)
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

    // CONSTRUCTOR 9 THAM SỐ (Bản đầy đủ nhất, dùng cho NhanVien_Dao mới)
    public NhanVien(String maNV, String hoTen, String soDienThoai, String gmail, String chucVu, double luong, String matKhau, String gioiTinh, String trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai; 
        this.gmail = gmail;
        this.chucVu = chucVu;
        this.luong = luong;
        this.matKhau = matKhau;
        this.gioiTinh = gioiTinh;
        this.trangThai = trangThai;
    }

    // --- GETTER ---
    public String getMaNV() { return maNV; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getGmail() { return gmail; }
    public String getChucVu() { return chucVu; }
    public double getLuong() { return luong; }
    public String getMatKhau() { return matKhau; }
    public String getGioiTinh() { return gioiTinh; }
    public String getTrangThai() { return trangThai; }

    // --- SETTER ---
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setGmail(String gmail) { this.gmail = gmail; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
    public void setLuong(double luong) { this.luong = luong; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public NhanVien(String maNV, String hoTen) {
        this.maNV = maNV;
        this.hoTen = hoTen;
    }
}