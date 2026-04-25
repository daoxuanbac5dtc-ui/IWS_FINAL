package org.example.iws_websitesneaker.Dto;

public class UserDto {
    private Long id;
    private String email;
    private String vaiTro;
    private String maTaiKhoan;

    public UserDto() {}

    public UserDto(Long id, String email, String vaiTro, String maTaiKhoan) {
        this.id = id;
        this.email = email;
        this.vaiTro = vaiTro;
        this.maTaiKhoan = maTaiKhoan;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
}
