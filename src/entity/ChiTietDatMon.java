package entity;

public class ChiTietDatMon {
    private String maDonDat;
    private String maMon;
    private int soLuong;
    private float donGia;

    private MonAn monAn; 

    public ChiTietDatMon() {}

    public ChiTietDatMon(String maDonDat, String maMon, int soLuong, float donGia) {
        this.maDonDat = maDonDat;
        this.maMon = maMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaDonDat() { return maDonDat; }
    public String getMaMon() { return maMon; }
    public int getSoLuong() { return soLuong; }

    
    public float getDonGia() {
		return donGia;
	}

	public void setDonGia(float donGia) {
		this.donGia = donGia;
	}

	public MonAn getMonAn() { return monAn; }
    public void setMonAn(MonAn monAn) { this.monAn = monAn; }
}