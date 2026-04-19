package entity;

import java.util.Objects;

public class Ban {
	private String maBan;
	private String tenBan;
	private int soGhe;
	private String trangThai;
	private String viTri;
	
	// --- CẬP NHẬT MỚI: QUẢN LÝ KHỐI BÀN GỘP ---
	private Integer maKhoi;      // null nếu là bàn đơn, có số nếu thuộc một khối
	private String maBanChinh;   // Mã của bàn "trưởng nhóm" trong khối đó

	// 1. Constructor có tham số Mã Bàn (dùng nhanh)
	public Ban(String maBan) {
		this.maBan = maBan;
	}

	// 2. Constructor mặc nhiên
	public Ban() {
		this.maBan = "B001";
		this.tenBan = "Bàn chưa đặt tên";
		this.soGhe = 2;
		this.trangThai = "Trống";
		this.viTri = "Tầng 1";
		this.maKhoi = null;
		this.maBanChinh = null;
	}

	// 3. Constructor đầy đủ tham số (bao gồm cả Khối)
	public Ban(String maBan, String tenBan, int soGhe, String trangThai, String viTri, Integer maKhoi, String maBanChinh) {
		setMaBan(maBan);
		setTenBan(tenBan);
		setSoGhe(soGhe);
		setTrangThai(trangThai);
		setViTri(viTri);
		this.maKhoi = maKhoi;
		this.maBanChinh = maBanChinh;
	}

	// 4. Copy constructor
	public Ban(Ban banOther) {
		this.maBan = banOther.maBan;
		this.tenBan = banOther.tenBan;
		this.soGhe = banOther.soGhe;
		this.trangThai = banOther.trangThai;
		this.viTri = banOther.viTri;
		this.maKhoi = banOther.maKhoi;
		this.maBanChinh = banOther.maBanChinh;
	}

	// --- GETTERS & SETTERS ---

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

	public void setTrangThai(String trangThai) {
		if (trangThai == null) {
			this.trangThai = "Trống";
			return;
		}
		String tt = trangThai.trim();
		// Nới lỏng kiểm tra để hỗ trợ các trạng thái linh hoạt trong dự án
		if (tt.equalsIgnoreCase("Trống") || tt.equalsIgnoreCase("Đã đặt") || 
			tt.equalsIgnoreCase("Đang sử dụng") || tt.equalsIgnoreCase("Đang dùng")) {
			this.trangThai = tt;
		} else {
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

	public Integer getMaKhoi() {
		return maKhoi;
	}

	public void setMaKhoi(Integer maKhoi) {
		this.maKhoi = maKhoi;
	}

	public String getMaBanChinh() {
		return maBanChinh;
	}

	public void setMaBanChinh(String maBanChinh) {
		this.maBanChinh = maBanChinh;
	}

	// --- PHƯƠNG THỨC BỔ TRỢ ---

	@Override
	public String toString() {
		return "Ban [maBan=" + maBan + ", tenBan=" + tenBan + ", soGhe=" + soGhe + 
			   ", trangThai=" + trangThai + ", viTri=" + viTri + 
			   ", maKhoi=" + maKhoi + ", maBanChinh=" + maBanChinh + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Ban ban = (Ban) o;
		return Objects.equals(maBan, ban.maBan);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maBan);
	}
}