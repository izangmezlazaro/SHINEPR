package com.example.demo.dto;

import java.math.BigDecimal;

public class ProductoRequestDTO {
    private String     sku;
    private String     nombre;
    private String     descripcion;
    private String     ingredientes;
    private String     modoUso;
    private BigDecimal precio;
    private Integer    stock;
    private String     genero;
    private String     tipoFragancia;
    private Integer    idCategoria;

    public ProductoRequestDTO() {}
    public String     getSku()           { return sku; }
    public void       setSku(String v)   { this.sku = v; }
    public String     getNombre()        { return nombre; }
    public void       setNombre(String v) { this.nombre = v; }
    public String     getDescripcion()   { return descripcion; }
    public void       setDescripcion(String v) { this.descripcion = v; }
    public String     getIngredientes()  { return ingredientes; }
    public void       setIngredientes(String v) { this.ingredientes = v; }
    public String     getModoUso()       { return modoUso; }
    public void       setModoUso(String v) { this.modoUso = v; }
    public BigDecimal getPrecio()        { return precio; }
    public void       setPrecio(BigDecimal v) { this.precio = v; }
    public Integer    getStock()         { return stock; }
    public void       setStock(Integer v) { this.stock = v; }
    public String     getGenero()        { return genero; }
    public void       setGenero(String v) { this.genero = v; }
    public String     getTipoFragancia() { return tipoFragancia; }
    public void       setTipoFragancia(String v) { this.tipoFragancia = v; }
    public Integer    getIdCategoria()   { return idCategoria; }
    public void       setIdCategoria(Integer v) { this.idCategoria = v; }
}
