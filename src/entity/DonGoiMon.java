package entity;

public class DonGoiMon {

    private String maMon;     
    private String tenMon;    
    private int soLuong;
    private double gia;
    private String ghiChu;   

    // ===== CONSTRUCTOR =====
    public DonGoiMon(String maMon, String tenMon, int soLuong, double gia) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.gia = gia;
        this.ghiChu = "";
    }

    public DonGoiMon(String maMon, String tenMon, int soLuong, double gia, String ghiChu) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.gia = gia;
        this.ghiChu = ghiChu;
    }

    // ===== GETTER =====
    public String getMaMon() {
        return maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public double getGia() {
        return gia;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    // ===== SETTER =====
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // ===== LOGIC =====
    public void tangSL() {
        soLuong++;
    }

    public void giamSL() {
        if (soLuong > 1) {
            soLuong--;
        }
    }

    public double getThanhTien() {
        return soLuong * gia;
    }

    // ===== DEBUG =====
    @Override
    public String toString() {
        return tenMon + " x" + soLuong +
                " - " + getThanhTien() + "đ" +
                (ghiChu != null && !ghiChu.isEmpty() ? " (" + ghiChu + ")" : "");
    }
}