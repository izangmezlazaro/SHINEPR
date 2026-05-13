package com.example.demo.entity;

public class Usuario {

    private Integer id;
    private String  nombre;
    private String  email;
    private String  password;
    private String  telefono;
    private String  rol = "cliente";
    private Integer puntos = 0;

    public Usuario() {}

    public Usuario(Integer id, String nombre, String email, String password, String telefono, String rol) {
        this.id       = id;
        this.nombre   = nombre;
        this.email    = email;
        this.password = password;
        this.telefono = telefono;
        this.rol      = (rol == null || rol.isBlank()) ? "cliente" : rol;
    }

    public Integer getId()       { return id; }
    public void    setId(Integer id) { this.id = id; }

    public String getNombre()    { return nombre; }
    public void   setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail()     { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getPassword()  { return password; }
    public void   setPassword(String password) { this.password = password; }

    public String getTelefono()  { return telefono; }
    public void   setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol()       { return rol; }
    public void   setRol(String rol) { this.rol = (rol == null || rol.isBlank()) ? "cliente" : rol; }

    public Integer getPuntos()   { return puntos == null ? 0 : puntos; }
    public void    setPuntos(Integer puntos) { this.puntos = puntos == null ? 0 : puntos; }
}
