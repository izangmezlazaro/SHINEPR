package com.example.demo.dto;

public class AuthResponseDTO {
    private String      token;
    private AuthUserDTO user;

    public AuthResponseDTO() {}
    public AuthResponseDTO(String token, AuthUserDTO user) {
        this.token = token;
        this.user  = user;
    }
    public String      getToken() { return token; }
    public void        setToken(String token) { this.token = token; }
    public AuthUserDTO getUser()  { return user; }
    public void        setUser(AuthUserDTO user) { this.user = user; }
}
