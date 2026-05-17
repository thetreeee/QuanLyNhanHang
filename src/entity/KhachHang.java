package entity;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private double tongChiTieu;
    private String hangThanhVien;

    public KhachHang() {}

    public KhachHang(String maKH, String hoTen, String soDienThoai, double tongChiTieu) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.tongChiTieu = tongChiTieu;
        tinhToanHang();
    }

    // Logic tự động tính hạng theo Tổng chi tiêu
    public void tinhToanHang() {
        if (this.tongChiTieu >= 20000000) {
            this.hangThanhVien = "Kim Cương";
        } else if (this.tongChiTieu >= 10000000) {
            this.hangThanhVien = "Vàng";
        } else {
            this.hangThanhVien = "Bạc";
        }
    }

    // Getter và Setter
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    
    public double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(double tongChiTieu) { 
        this.tongChiTieu = tongChiTieu; 
        tinhToanHang(); 
    }
    
    public String getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(String hangThanhVien) { this.hangThanhVien = hangThanhVien; }
}