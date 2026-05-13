package com.example.demo.entity;

public class Fragancia {

    private Integer idFragancia;
    private String  nombre;
    private String  familia;
    private Boolean esBase = false;

    public Fragancia() {}

    public Fragancia(Integer idFragancia, String nombre, String familia, Boolean esBase) {
        this.idFragancia = idFragancia;
        this.nombre      = nombre;
        this.familia     = familia;
        this.esBase      = (esBase == null) ? false : esBase;
    }

    public Integer getIdFragancia()  { return idFragancia; }
    public void    setIdFragancia(Integer id) { this.idFragancia = id; }

    public String  getNombre()       { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }

    public String  getFamilia()      { return familia; }
    public void    setFamilia(String familia) { this.familia = familia; }

    public Boolean getEsBase()       { return esBase; }
    public void    setEsBase(Boolean esBase) { this.esBase = (esBase == null) ? false : esBase; }
}
