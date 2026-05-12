package entity;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private double tongChiTieu; // Gốc để tính điểm và hạng
    private int diemHienTai;
    private String hangThanhVien;

    public KhachHang() {}

    public KhachHang(String maKH, String hoTen, String soDienThoai, double tongChiTieu) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.tongChiTieu = tongChiTieu;
        tinhToanHangVaDiem();
    }

    // Logic tự động: 1 triệu = 2 điểm
    public void tinhToanHangVaDiem() {
        this.diemHienTai = (int) (this.tongChiTieu / 500000); // 1.000.000 / 500.000 = 2 điểm
        if (this.diemHienTai <= 10) this.hangThanhVien = "Bạc";
        else if (this.diemHienTai <= 20) this.hangThanhVien = "Vàng";
        else this.hangThanhVien = "Kim Cương";
    }

    // Getter và Setter...
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(double tongChiTieu) { this.tongChiTieu = tongChiTieu; tinhToanHangVaDiem(); }
    public int getDiemHienTai() { return diemHienTai; }
    public String getHangThanhVien() { return hangThanhVien; }
}