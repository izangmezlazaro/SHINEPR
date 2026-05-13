package com.example.demo.dto;

public class AuthUserDTO {
    private Integer id;
    private String  nombre;
    private String  email;
    private String  telefono;
    private String  rol;

    public AuthUserDTO() {}
    public AuthUserDTO(Integer id, String nombre, String email, String telefono, String rol) {
        this.id       = id;
        this.nombre   = nombre;
        this.email    = email;
        this.telefono = telefono;
        this.rol      = rol;
    }
    public Integer getId()       { return id; }
    public void    setId(Integer id) { this.id = id; }
    public String  getNombre()   { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }
    public String  getEmail()    { return email; }
    public void    setEmail(String email) { this.email = email; }
    public String  getTelefono() { return telefono; }
    public void    setTelefono(String telefono) { this.telefono = telefono; }
    public String  getRol()      { return rol; }
    public void    setRol(String rol) { this.rol = rol; }
}
