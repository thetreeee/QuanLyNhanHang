package entity;

public class Ban {
    private String maBan;
    private String tenBan;
    private int soGhe; 
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
        if (maBan == null || !maBan.matches("^B\\d{3}$")) {
            throw new IllegalArgumentException("Mã bàn phải có định dạng Bxxx (ví dụ: B001)");
        }
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        if (tenBan == null || tenBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bàn không được rỗng");
        }
        this.tenBan = tenBan;
    }

    public int getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(int soGhe) {
        if (soGhe <= 0) {
            throw new IllegalArgumentException("Số ghế phải lớn hơn 0");
        }
        this.soGhe = soGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    /**
     * CẬP NHẬT: Nới lỏng ràng buộc để chấp nhận các trạng thái tiếng Việt
     * giúp hệ thống đổi màu tự động không bị văng lỗi.
     */
    public void setTrangThai(String trangThai) {
        if (trangThai == null) {
            this.trangThai = "Trống";
            return;
        }

        String tt = trangThai.trim();
        // Chấp nhận tất cả các trạng thái mà dự án đang dùng để hiển thị màu (Xanh, Vàng, Đỏ)
        if (tt.equalsIgnoreCase("Trống") || 
            tt.equalsIgnoreCase("Đã đặt") || 
            tt.equalsIgnoreCase("Đã đặt trước") || 
            tt.equalsIgnoreCase("Đang sử dụng") || 
            tt.equalsIgnoreCase("Đang dùng")) {
            
            this.trangThai = tt;
        } else {
            // Nếu dữ liệu DB lạ, mặc định về Trống thay vì làm sập chương trình
            this.trangThai = "Trống";
        }
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

    @Override
    public String toString() {
        return "Bàn [Mã bàn: " + maBan + ", Tên bàn: " + tenBan + ", Số ghế: " + soGhe 
                + ", Trạng thái: " + trangThai + ", Vị trí: " + viTri + "]";
    }
}