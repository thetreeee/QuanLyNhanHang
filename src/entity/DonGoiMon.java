package entity;

public class DonGoiMon {
    private String tenMon;
    private int soLuong;
    private double gia;

    public DonGoiMon(String tenMon, int soLuong, double gia) {
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.gia = gia;
    }

    public String getTenMon() { return tenMon; }
    public int getSoLuong() { return soLuong; }
    public double getGia() { return gia; }

    public void tangSL() { soLuong++; }
    public void giamSL() { if (soLuong > 1) soLuong--; }

    public double getThanhTien() {
        return soLuong * gia;
    }

    @Override
    public String toString() {
        return tenMon + " x" + soLuong + " - " + getThanhTien() + "đ";
    }
}