package entity;

import java.util.Objects;

public class MonAn {
    private String maMon;
    private String tenMon;
    private String donViTinh;
    private String trangThai;
    private String loaiMon;
    private String hinhAnh;
    private String giaBanStr;
    private double giaBan;

    public MonAn() {}

    public MonAn(String maMon, String tenMon, String donViTinh, String trangThai, String loaiMon, String hinhAnh) {
        setMaMon(maMon); // Sử dụng setter để kiểm tra ràng buộc ngay khi khởi tạo
        this.tenMon = tenMon;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.loaiMon = loaiMon;
        this.hinhAnh = hinhAnh;
    }

    public String getMaMon() { return maMon; }

    public void setMaMon(String maMon) {
        // Ràng buộc: Bắt đầu bằng chữ M và theo sau là đúng 3 chữ số
        if (maMon != null && maMon.matches("^M\\d{3}$")) {
            this.maMon = maMon;
        } else {
            throw new IllegalArgumentException("Mã món không hợp lệ! Định dạng chuẩn là MXXX (VD: M001)");
        }
    }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLoaiMon() { return loaiMon; }
    public void setLoaiMon(String loaiMon) { this.loaiMon = loaiMon; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public void setGiaBanStr(String giaBanStr) {
        this.giaBanStr = giaBanStr;
    }
    public String getGiaBanStr() {
        if (this.giaBan <= 0) return "Chờ thiết lập";
        return String.format("%,.0f VNĐ", this.giaBan);
    }
    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonAn monAn = (MonAn) o;
        return Objects.equals(maMon, monAn.maMon);
    }

    @Override
    public int hashCode() { return Objects.hash(maMon); }
}