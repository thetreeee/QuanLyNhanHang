package entity;

import java.time.LocalDateTime;
import java.util.List;

public class DonDatMon {
    private String maDonDat;
    private LocalDateTime thoiGianDat;
    private String ghiChu;
    private String maNV;
    private String maBan;

    private List<ChiTietDatMon> dsChiTiet;

    public DonDatMon() {}

    public DonDatMon(String maDonDat, LocalDateTime thoiGianDat, String ghiChu, String maNV, String maBan) {
        this.maDonDat = maDonDat;
        this.thoiGianDat = thoiGianDat;
        this.ghiChu = ghiChu;
        this.maNV = maNV;
        this.maBan = maBan;
    }

    // getter setter
    public String getMaDonDat() { return maDonDat; }
    public void setMaDonDat(String maDonDat) { this.maDonDat = maDonDat; }

    public LocalDateTime getThoiGianDat() { return thoiGianDat; }
    public void setThoiGianDat(LocalDateTime thoiGianDat) { this.thoiGianDat = thoiGianDat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    
    

    public String getMaBan() {
		return maBan;
	}

	public void setMaBan(String maBan) {
		this.maBan = maBan;
	}

	public List<ChiTietDatMon> getDsChiTiet() { return dsChiTiet; }
    public void setDsChiTiet(List<ChiTietDatMon> dsChiTiet) { this.dsChiTiet = dsChiTiet; }
    
}