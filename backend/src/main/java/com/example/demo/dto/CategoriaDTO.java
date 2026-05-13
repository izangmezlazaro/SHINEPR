package com.example.demo.dto;

public class CategoriaDTO {
    private Integer idCategoria;
    private String  nombre;

    public CategoriaDTO() {}
    public CategoriaDTO(Integer idCategoria, String nombre) {
        this.idCategoria = idCategoria; this.nombre = nombre;
    }
    public Integer getIdCategoria() { return idCategoria; }
    public void    setIdCategoria(Integer v) { this.idCategoria = v; }
    public String  getNombre()      { return nombre; }
    public void    setNombre(String v) { this.nombre = v; }
}
