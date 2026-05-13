package com.example.demo.entity;

public class Categoria {

    private Integer idCategoria;
    private String  nombre;

    public Categoria() {}

    public Categoria(Integer idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre      = nombre;
    }

    public Integer getIdCategoria() { return idCategoria; }
    public void    setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void   setNombre(String nombre) { this.nombre = nombre; }
}
