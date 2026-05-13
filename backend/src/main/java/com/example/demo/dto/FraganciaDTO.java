package com.example.demo.dto;

public class FraganciaDTO {
    private Integer idFragancia;
    private String  nombre;
    private String  familia;
    private Boolean esBase;

    public FraganciaDTO() {}
    public FraganciaDTO(Integer idFragancia, String nombre, String familia, Boolean esBase) {
        this.idFragancia = idFragancia; this.nombre = nombre;
        this.familia = familia; this.esBase = esBase;
    }
    public Integer getIdFragancia() { return idFragancia; }
    public void    setIdFragancia(Integer v) { this.idFragancia = v; }
    public String  getNombre()      { return nombre; }
    public void    setNombre(String v) { this.nombre = v; }
    public String  getFamilia()     { return familia; }
    public void    setFamilia(String v) { this.familia = v; }
    public Boolean getEsBase()      { return esBase; }
    public void    setEsBase(Boolean v) { this.esBase = v; }
}
