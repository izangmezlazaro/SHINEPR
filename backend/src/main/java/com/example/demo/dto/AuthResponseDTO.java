package com.example.demo.dto;

public class AuthResponseDTO {
    private String      token;
    private AuthUserDTO usuario;

    public AuthResponseDTO() {}
    public AuthResponseDTO(String token, AuthUserDTO usuario) {
        this.token   = token;
        this.usuario = usuario;
    }
    public String      getToken()   { return token; }
    public void        setToken(String token) { this.token = token; }
    public AuthUserDTO getUsuario() { return usuario; }
    public void        setUsuario(AuthUserDTO usuario) { this.usuario = usuario; }
}
