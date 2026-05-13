package com.example.demo.entity;

public class Direccion {

    private Integer id;
    private Usuario usuario;
    private String  calle;
    private String  ciudad;
    private String  codigoPostal;

    public Direccion() {}

    public Direccion(Integer id, Usuario usuario, String calle, String ciudad, String codigoPostal) {
        this.id           = id;
        this.usuario      = usuario;
        this.calle        = calle;
        this.ciudad       = ciudad;
        this.codigoPostal = codigoPostal;
    }

    public Integer getId()           { return id; }
    public void    setId(Integer id) { this.id = id; }

    public Usuario getUsuario()      { return usuario; }
    public void    setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String  getCalle()        { return calle; }
    public void    setCalle(String calle) { this.calle = calle; }

    public String  getCiudad()       { return ciudad; }
    public void    setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String  getCodigoPostal() { return codigoPostal; }
    public void    setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
}
