package entity;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String gmail;
    private String chucVu;
    private double luong;
    private String matKhau;
    private String gioiTinh;

    public NhanVien(String maNV, String hoTen, String gmail, String chucVu, double luong, String matKhau, String gioiTinh) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.gmail = gmail;
        this.chucVu = chucVu;
        this.luong = luong;
        this.matKhau = matKhau;
        this.gioiTinh = gioiTinh;
    }

    public String getMaNV() { return maNV; }
    public String getHoTen() { return hoTen; }
    public String getGmail() { return gmail; }
    public String getChucVu() { return chucVu; }
    public double getLuong() { return luong; }
    public String getMatKhau() { return matKhau; }
    public String getGioiTinh() { return gioiTinh; }
}