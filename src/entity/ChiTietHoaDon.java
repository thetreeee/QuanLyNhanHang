package entity;

public class ChiTietHoaDon {
    private String maHD;
    private String maMon;
    private MonAn monAn;
    private int soLuong;
    private double donGia; // Sẽ được tính ngược từ thanhTien / soLuong

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maHD, String maMon, MonAn monAn, int soLuong, double donGia) {
        this.maHD = maHD;
        this.maMon = maMon;
        this.monAn = monAn;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }
    public MonAn getMonAn() { return monAn; }
    public void setMonAn(MonAn monAn) { this.monAn = monAn; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}