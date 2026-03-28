package entity;

import java.util.Date;

public class BangGia {
    private String maBangGia;
    private String moTa;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private String trangThai;

    public BangGia() {}

    public BangGia(String maBangGia, String moTa, Date ngayBatDau, Date ngayKetThuc, String trangThai) throws Exception {
        setMaBangGia(maBangGia);
        setNgayKetThuc(ngayBatDau, ngayKetThuc); // Kiểm tra ràng buộc ngày
        this.moTa = moTa;
        this.ngayBatDau = ngayBatDau;
        this.trangThai = trangThai;
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    // Ràng buộc mã: Phải bắt đầu bằng BG và theo sau là 3 chữ số
    public void setMaBangGia(String maBangGia) throws Exception {
        if (maBangGia != null && maBangGia.matches("^BG\\d{3}$")) {
            this.maBangGia = maBangGia;
        } else {
            throw new Exception("Mã bảng giá phải có định dạng BGXXX (VD: BG001)");
        }
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    // Ràng buộc ngày: Kết thúc phải sau Bắt đầu
    public void setNgayKetThuc(Date ngayBatDau, Date ngayKetThuc) throws Exception {
        if (ngayKetThuc != null && ngayBatDau != null && ngayKetThuc.after(ngayBatDau)) {
            this.ngayKetThuc = ngayKetThuc;
        } else {
            throw new Exception("Ngày kết thúc phải sau ngày bắt đầu!");
        }
    }

    // Các Getter/Setter còn lại...
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}