package entity;

public class Ban {
    private String maBan;
    private String tenBan;
    private int soGhe; // DB là soChoNgoi, nhưng Java dùng soGhe theo mô tả
    private String trangThai;
    private String viTri;

    // 3.1 Constructor mặc nhiên
    public Ban() {
        this.maBan = "B001";
        this.tenBan = "Bàn chưa đặt tên";
        this.soGhe = 2;
        this.trangThai = "Trống";
        this.viTri = "Tầng 1";
    }

    // 3.2 Constructor đầy đủ tham số
    public Ban(String maBan, String tenBan, int soGhe, String trangThai, String viTri) {
        setMaBan(maBan);
        setTenBan(tenBan);
        setSoGhe(soGhe);
        setTrangThai(trangThai);
        setViTri(viTri);
    }

    // 3.3 Copy constructor
    public Ban(Ban banOther) {
        this.maBan = banOther.maBan;
        this.tenBan = banOther.tenBan;
        this.soGhe = banOther.soGhe;
        this.trangThai = banOther.trangThai;
        this.viTri = banOther.viTri;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        // Ràng buộc 1.1: Bắt đầu bằng B và sau đó là 3 chữ số (VD: B001)
        if (maBan == null || !maBan.matches("^B\\d{3}$")) {
            throw new IllegalArgumentException("Mã bàn phải có định dạng Bxxx (ví dụ: B001)");
        }
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        // Ràng buộc 2.3
        if (tenBan == null || tenBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bàn không được rỗng");
        }
        this.tenBan = tenBan;
    }

    public int getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(int soGhe) {
        // Ràng buộc 2.5
        if (soGhe <= 0) {
            throw new IllegalArgumentException("Số ghế phải lớn hơn 0");
        }
        this.soGhe = soGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        // Ràng buộc 2.7
        if (!trangThai.equals("Trống") && !trangThai.equals("Đang sử dụng") && !trangThai.equals("Đã đặt trước")) {
            throw new IllegalArgumentException("Trạng thái bàn không hợp lệ");
        }
        this.trangThai = trangThai;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        if (viTri == null || viTri.trim().isEmpty()) {
            throw new IllegalArgumentException("Vị trí không được rỗng");
        }
        this.viTri = viTri;
    }

    // 4. Phương thức toString()
    @Override
    public String toString() {
        return "Bàn [Mã bàn: " + maBan + ", Tên bàn: " + tenBan + ", Số ghế: " + soGhe 
                + ", Trạng thái: " + trangThai + ", Vị trí: " + viTri + "]";
    }
}