package entity;

public class NhanVien {
	private String maNV;
	private String hoTen;
	private java.time.LocalDate ngaySinh; // ĐÃ THÊM: Ngày sinh
	private String soDienThoai;
	private String gmail;
	private String chucVu;
	private String matKhau;
	private String gioiTinh;
	private String trangThai;

	// Constructor (Giữ nguyên để không báo lỗi các file cũ)
	public NhanVien(String maNV, String hoTen, String gmail, String chucVu, String matKhau,
			String gioiTinh) {
		this.maNV = maNV;
		this.hoTen = hoTen;
		this.gmail = gmail;
		this.chucVu = chucVu;
		this.matKhau = matKhau;
		this.gioiTinh = gioiTinh;
	}

	// Thêm hàm khởi tạo rỗng
	public NhanVien() {
	}

	// Thêm hàm khởi tạo có tham số Mã NV
	public NhanVien(String maNV) {
		this.maNV = maNV; // (Chú ý: Nếu biến của anh tên là maNhanVien thì sửa thành: this.maNhanVien =
							// maNV;)
	}

	// Thêm hàm setMaNV
	public void setMaNV(String maNV) {
		this.maNV = maNV; // (Tương tự, đổi thành this.maNhanVien nếu cần)
	}

	// Constructor 8 tham số (Giữ nguyên)
	public NhanVien(String maNV, String hoTen, String gmail, String chucVu, String matKhau,
			String gioiTinh, String trangThai) {
		this.maNV = maNV;
		this.hoTen = hoTen;
		this.gmail = gmail;
		this.chucVu = chucVu;
		this.matKhau = matKhau;
		this.gioiTinh = gioiTinh;
		this.trangThai = trangThai;
	}

	// CONSTRUCTOR THAM SỐ (Bản đầy đủ nhất, dùng cho NhanVien_Dao mới)
	public NhanVien(String maNV, String hoTen, java.time.LocalDate ngaySinh, String soDienThoai, String gmail, String chucVu,
			String matKhau, String gioiTinh, String trangThai) {
		this.maNV = maNV;
		this.hoTen = hoTen;
		this.ngaySinh = ngaySinh;
		this.soDienThoai = soDienThoai;
		this.gmail = gmail;
		this.chucVu = chucVu;
		this.matKhau = matKhau;
		this.gioiTinh = gioiTinh;
		this.trangThai = trangThai;
	}

	// --- GETTER ---
	public String getMaNV() {
		return maNV;
	}

	public String getHoTen() {
		return hoTen;
	}

	public java.time.LocalDate getNgaySinh() {
		return ngaySinh;
	}

	public String getSoDienThoai() {
		return soDienThoai;
	}

	public String getGmail() {
		return gmail;
	}

	public String getChucVu() {
		return chucVu;
	}

	public String getMatKhau() {
		return matKhau;
	}

	public String getGioiTinh() {
		return gioiTinh;
	}

	public String getTrangThai() {
		return trangThai;
	}

	// --- SETTER ---
	public void setNgaySinh(java.time.LocalDate ngaySinh) {
		this.ngaySinh = ngaySinh;
	}

	public void setSoDienThoai(String soDienThoai) {
		this.soDienThoai = soDienThoai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}

	public void setGmail(String gmail) {
		this.gmail = gmail;
	}

	public void setChucVu(String chucVu) {
		this.chucVu = chucVu;
	}

	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}

	public void setGioiTinh(String gioiTinh) {
		this.gioiTinh = gioiTinh;
	}

	public NhanVien(String maNV, String hoTen) {
		this.maNV = maNV;
		this.hoTen = hoTen;
	}

	@Override
	public String toString() {
		return "NhanVien [maNV=" + maNV + ", hoTen=" + hoTen + ", ngaySinh=" + ngaySinh + ", soDienThoai=" + soDienThoai + ", gmail=" + gmail
				+ ", chucVu=" + chucVu + ", matKhau=" + matKhau + ", gioiTinh=" + gioiTinh
				+ ", trangThai=" + trangThai + "]";
	}
}