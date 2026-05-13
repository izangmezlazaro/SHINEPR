package com.example.demo.dto;

public class DireccionDTO {
    private Integer id;
    private Integer idUsuario;
    private String  calle;
    private String  ciudad;
    private String  codigoPostal;

    public DireccionDTO() {}
    public DireccionDTO(Integer id, Integer idUsuario, String calle, String ciudad, String codigoPostal) {
        this.id = id; this.idUsuario = idUsuario; this.calle = calle;
        this.ciudad = ciudad; this.codigoPostal = codigoPostal;
    }
    public Integer getId()           { return id; }
    public void    setId(Integer v)  { this.id = v; }
    public Integer getIdUsuario()    { return idUsuario; }
    public void    setIdUsuario(Integer v) { this.idUsuario = v; }
    public String  getCalle()        { return calle; }
    public void    setCalle(String v) { this.calle = v; }
    public String  getCiudad()       { return ciudad; }
    public void    setCiudad(String v) { this.ciudad = v; }
    public String  getCodigoPostal() { return codigoPostal; }
    public void    setCodigoPostal(String v) { this.codigoPostal = v; }
}
