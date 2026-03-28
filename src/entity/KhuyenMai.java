package entity;

import java.time.LocalDate;

public class KhuyenMai {
    private String maKM;
    private String tenKM;
    private String loaiKM;
    private double giaTri;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private DoiTuongKM doiTuongApDung;
    private String trangThai;

    public KhuyenMai() {}

    public KhuyenMai(String maKM, String tenKM, String loaiKM, double giaTri,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, DoiTuongKM doiTuongApDung, String trangthai) {
        setMaKM(maKM);
        setTenKM(tenKM);
        setLoaiKM(loaiKM);
        setGiaTri(giaTri);
        setNgayBatDau(ngayBatDau);
        setNgayKetThuc(ngayKetThuc);
        setDoiTuongApDung(doiTuongApDung);
        setTrangThai(trangthai);
    }

    public String getMaKM() { 
    	return maKM; }
    
    public String getTenKM() { 
    	return tenKM; }
    
    public String getLoaiKM() { 
    	return loaiKM; }
    
    public double getGiaTri() { 
    	return giaTri; }
    
    public LocalDate getNgayBatDau() { 
    	return ngayBatDau; }
    
    public LocalDate getNgayKetThuc() { 
    	return ngayKetThuc; }
    
    public String getTrangThai() {
    	if ("Ngừng áp dụng".equals(trangThai)) {
            return "Ngừng áp dụng";
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(ngayBatDau)) return "Sắp diễn ra";
        if (today.isAfter(ngayKetThuc)) return "Đã kết thúc";

        return "Đang chạy";
    }
    
    
    public DoiTuongKM getDoiTuongApDung() {
    	return doiTuongApDung;
    }

    public void setMaKM(String maKM) {
        if (!maKM.matches("KM\\d{3}"))
            throw new IllegalArgumentException("Mã KM phải dạng KMXXX!");
        this.maKM = maKM;
    }

    public void setTenKM(String tenKM) {
        if (tenKM == null || tenKM.trim().isEmpty())
            throw new IllegalArgumentException("Tên chương trình không được rỗng!");
        this.tenKM = tenKM;
    }

    public void setLoaiKM(String loaiKM) {
        if (!loaiKM.equals("Giảm giá") && !loaiKM.equals("Giảm phần trăm"))
            throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ!");
        this.loaiKM = loaiKM;
    }

    public void setGiaTri(double giaTri) {
        if (giaTri <= 0)
            throw new IllegalArgumentException("Giá trị khuyến mãi không hợp lệ!");
        this.giaTri = giaTri;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        if (ngayBatDau == null)
            throw new IllegalArgumentException("Ngày bắt đầu không được rỗng!");
        this.ngayBatDau = ngayBatDau;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        if (ngayKetThuc == null || ngayKetThuc.isBefore(ngayBatDau))
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu!");
        this.ngayKetThuc = ngayKetThuc;
    }

    public void setDoiTuongApDung(DoiTuongKM doiTuongApDung) {
        this.doiTuongApDung = doiTuongApDung;
    }
    
    public void setTrangThai(String trangThai) {
    	this.trangThai = trangThai;
    }
}