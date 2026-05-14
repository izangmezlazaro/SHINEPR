package com.example.demo.dto;

public class SubcategoriaDTO {

    private Integer idSubcategoria;
    private String  nombre;
    private Integer idCategoria;

    public SubcategoriaDTO() {}

    public SubcategoriaDTO(Integer idSubcategoria, String nombre, Integer idCategoria) {
        this.idSubcategoria = idSubcategoria;
        this.nombre         = nombre;
        this.idCategoria    = idCategoria;
    }

    public Integer getIdSubcategoria()                       { return idSubcategoria; }
    public void    setIdSubcategoria(Integer idSubcategoria) { this.idSubcategoria = idSubcategoria; }

    public String  getNombre()               { return nombre; }
    public void    setNombre(String nombre)  { this.nombre = nombre; }

    public Integer getIdCategoria()                    { return idCategoria; }
    public void    setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
}
