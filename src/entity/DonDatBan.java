package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class DonDatBan {
    private String maDon;
    private String maBan;
    private LocalDate ngayDat;
    private LocalTime thoiGian;
    private int soLuongKhach;
    private String ghiChu;
    private String trangThai;
    private String tenKhachHang;
    private String soDienThoai;
    // Constructor mặc định
    public DonDatBan() {
    }

    // Constructor đầy đủ tham số để dùng khi lấy dữ liệu từ Database hoặc tạo mới
    public DonDatBan(String maDon, String maBan, LocalDate ngayDat, LocalTime thoiGian, int soLuongKhach, String ghiChu, String trangThai) {
        this.maDon = maDon;
        this.maBan = maBan;
        this.ngayDat = ngayDat;
        this.thoiGian = thoiGian;
        this.soLuongKhach = soLuongKhach;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    // --- GETTER AND SETTER ---

    public String getMaDon() {
        return maDon;
    }

    public void setMaDon(String maDon) {
        // Định dạng mã đơn thường là Dxxx hoặc theo quy tắc của nhóm bạn
        this.maDon = maDon;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public LocalDate getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }

    public LocalTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public void setSoLuongKhach(int soLuongKhach) {
        if (soLuongKhach > 0) {
            this.soLuongKhach = soLuongKhach;
        }
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
    // Phương thức bổ trợ để đưa dữ liệu vào Vector hoặc Object[] cho JTable
    public Object[] toRowTable() {
        return new Object[] {
            maDon, 
            maBan, 
            ngayDat, 
            thoiGian, 
            soLuongKhach, 
            ghiChu, 
            trangThai
        };
    }

    @Override
    public String toString() {
        return "DonDatBan [maDon=" + maDon + ", maBan=" + maBan + ", trangThai=" + trangThai + "]";
    }
}