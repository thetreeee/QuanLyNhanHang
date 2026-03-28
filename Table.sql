CREATE DATABASE TuanTruongDB
GO
USE TuanTruongDB
GO
-- 1. B?ng NhanVien (B?ng cha cho c�c vai tr�)
CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100),
    ngaySinh DATE,
    gioiTinh NVARCHAR(10),
    soCCCD VARCHAR(20),
    luong DOUBLE PRECISION,
    matKhau VARCHAR(255)
);

-- 2. B?ng KhachHang
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100),
    soDienThoai VARCHAR(15)
);

-- 3. B?ng Ban (B�n)
CREATE TABLE Ban (
    maBan VARCHAR(20) PRIMARY KEY,
    soChoNgoi INT,
    trangThai NVARCHAR(50), -- S? d?ng t? Enumeration: CONTRONG, COKHACH, DADATBAN
    viTri NVARCHAR(100),
    tenBan NVARCHAR(50)
);

-- 4. B?ng MonAn
CREATE TABLE MonAn (
    maMon VARCHAR(20) PRIMARY KEY,
    tenMon NVARCHAR(100),
    donViTinh NVARCHAR(20),
    trangThai NVARCHAR(50),
    thanhPhan NVARCHAR(MAX)
);

-- 5. B?ng BangGia
CREATE TABLE BangGia (
    maBangGia VARCHAR(20) PRIMARY KEY,
    moTa NVARCHAR(255),
    ngayBatDau DATE,
    ngayKetThuc DATE
);

-- 6. B?ng KhuyenMai
CREATE TABLE KhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenKM NVARCHAR(100),
    loaiKM NVARCHAR(50),
    giaTri DOUBLE PRECISION,
    ngayBatDau DATE,
    ngayKetThuc DATE,
    doiTuongApDung NVARCHAR(100)
);

-- 7. B?ng ChiTietBangGia (Li�n k?t MonAn v� BangGia)
CREATE TABLE ChiTietBangGia (
    maBangGia VARCHAR(20),
    maMon VARCHAR(20),
    giaBan DOUBLE PRECISION,
    PRIMARY KEY (maBangGia, maMon),
    FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia),
    FOREIGN KEY (maMon) REFERENCES MonAn(maMon)
);

-- 8. B?ng DonDatBan (Reservation)
CREATE TABLE DonDatBan (
    maDon VARCHAR(20) PRIMARY KEY,
    ngayDat DATE,
    thoiGian DATETIME,
    maKhachHang VARCHAR(20),
    maNV VARCHAR(20), -- Nh�n vi�n l? t�n t?o ??n
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

-- 9. B?ng ChiTietDatBan (Nhi?u b�n cho 1 ??n ??t)
CREATE TABLE ChiTietDatBan (
    maDon VARCHAR(20),
    maBan VARCHAR(20),
    soLuongKhach INT,
    PRIMARY KEY (maDon, maBan),
    FOREIGN KEY (maDon) REFERENCES DonDatBan(maDon),
    FOREIGN KEY (maBan) REFERENCES Ban(maBan)
);

-- 10. B?ng DonDatMon (Order m�n t?i b�n)
CREATE TABLE DonDatMon (
    maDonDat VARCHAR(20) PRIMARY KEY,
    thoiGianDat DATETIME,
    ghiChu NVARCHAR(255),
    maNV VARCHAR(20), -- Nh�n vi�n ph?c v?
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

-- 11. B?ng ChiTietDatMon
CREATE TABLE ChiTietDatMon (
    maDonDat VARCHAR(20),
    maMon VARCHAR(20),
    soLuong INT,
    PRIMARY KEY (maDonDat, maMon),
    FOREIGN KEY (maDonDat) REFERENCES DonDatMon(maDonDat),
    FOREIGN KEY (maMon) REFERENCES MonAn(maMon)
);

-- 12. B?ng HoaDon (Billing)
CREATE TABLE HoaDon (
    maHD VARCHAR(20) PRIMARY KEY,
    ngayLap DATE,
    maKM VARCHAR(20),
    maNV VARCHAR(20), -- Thu ng�n
    maBan VARCHAR(20),
    phuongThucTT NVARCHAR(50),
    tongThanhTien DOUBLE PRECISION,
	FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    FOREIGN KEY (maBan) REFERENCES Ban(maBan)
);

-- 13. B?ng ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(20),
    maMon VARCHAR(20),
    soLuong INT,
    PRIMARY KEY (maHD, maMon),
    FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    FOREIGN KEY (maMon) REFERENCES MonAn(maMon)
);
ALTER TABLE ChiTietHoaDon
ADD thanhTien DOUBLE PRECISION;
ALTER TABLE BangGia
ADD trangThai NVARCHAR(50); 
ALTER TABLE NhanVien
ADD chucVu NVARCHAR(50);