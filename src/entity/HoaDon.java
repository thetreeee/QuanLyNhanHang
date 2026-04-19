package entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHD;
    private LocalDateTime ngayLap;
    private KhuyenMai khuyenMai;
    private NhanVien nhanVien;
    private Ban ban;
    private String phuongThucTT;
    private double tongTien; // Map với cột tongThanhTien trong SQL

    public HoaDon() {}

    public HoaDon(String maHD, LocalDateTime ngayLap, KhuyenMai khuyenMai, NhanVien nhanVien, Ban ban, String phuongThucTT, double tongTien) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.khuyenMai = khuyenMai;
        this.nhanVien = nhanVien;
        this.ban = ban;
        this.phuongThucTT = phuongThucTT;
        this.tongTien = tongTien;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Ban getBan() { return ban; }
    public void setBan(Ban ban) { this.ban = ban; }
    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}