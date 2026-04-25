package org.example.iws_websitesneaker.Dto;

public class LoginResponse {
    private String token;
    private UserDto user;
    private long expiresIn;

    public LoginResponse() {}

    public LoginResponse(String token, UserDto user, long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
