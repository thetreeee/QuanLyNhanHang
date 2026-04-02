package entity;

public class MonAnTT {
	private String maMon;
    private String tenMon;
    private String hinhAnh;
    private double gia;
    
    public MonAnTT() {}

    public MonAnTT(String maMon, String tenMon, String hinhAnh, double gia) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.hinhAnh = hinhAnh;
        this.gia = gia;
    }

    public String getMaMon() { return maMon; }
    public String getTenMon() { return tenMon; }
    public String getHinhAnh() { return hinhAnh; }
    public double getGia() { return gia; }
}
