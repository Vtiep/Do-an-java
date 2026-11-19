
package com.qlsv;

import java.time.LocalDate;

public class SinhVien {

    private String maSV;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String maLop;

    public SinhVien(String maSV, String hoTen,
            LocalDate ngaySinh, String gioiTinh,
            String email, String soDienThoai,
            String diaChi, String maLop) {

        this.maSV = maSV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.maLop = maLop;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public String getEmail() {
        return email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getMaLop() {
        return maLop;
    }
}
