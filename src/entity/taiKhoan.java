package entity;

public class taiKhoan {
    private String tenDangNhap;
    private String matKhau;

    public taiKhoan(String tenDangNhap, String matKhau) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
    }

 
    public static boolean kienTraDinhDangMatKhau(String password) {
        return password.length() >= 5 && password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
}