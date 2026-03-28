package entity;

public class ChiTietBangGia {
    private BangGia bangGia;
    private MonAn monAn;
    private double giaBan;

    public ChiTietBangGia() {}

    public ChiTietBangGia(BangGia bangGia, MonAn monAn, double giaBan) throws Exception {
        this.bangGia = bangGia;
        this.monAn = monAn;
        setGiaBan(giaBan);
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) throws Exception {
        if (giaBan >= 0) {
            this.giaBan = giaBan;
        } else {
            throw new Exception("Giá bán không được âm!");
        }
    }

    // Getter/Setter cho bangGia và monAn
    public BangGia getBangGia() { return bangGia; }
    public void setBangGia(BangGia bangGia) { this.bangGia = bangGia; }
    public MonAn getMonAn() { return monAn; }
    public void setMonAn(MonAn monAn) { this.monAn = monAn; }
}