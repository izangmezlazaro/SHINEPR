package com.example.demo.entity;

import java.math.BigDecimal;

public class Producto {

    private Integer    idProducto;
    private String     sku;
    private String     nombre;
    private String     descripcion;
    private String     ingredientes;
    private String     modoUso;
    private BigDecimal precio;
    private Integer    stock = 0;
    private String     genero;
    private String     tipoFragancia;
    private Categoria  categoria;
    private Integer    idSubcategoria;

    public Producto() {}

    public Producto(Integer idProducto, String sku, String nombre, String descripcion,
                    String ingredientes, String modoUso, BigDecimal precio, Integer stock,
                    String genero, String tipoFragancia, Categoria categoria) {
        this.idProducto    = idProducto;
        this.sku           = sku;
        this.nombre        = nombre;
        this.descripcion   = descripcion;
        this.ingredientes  = ingredientes;
        this.modoUso       = modoUso;
        this.precio        = precio;
        this.stock         = (stock == null) ? 0 : stock;
        this.genero        = genero;
        this.tipoFragancia = tipoFragancia;
        this.categoria     = categoria;
    }

    public Integer    getIdProducto()   { return idProducto; }
    public void       setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String     getSku()          { return sku; }
    public void       setSku(String sku) { this.sku = sku; }

    public String     getNombre()       { return nombre; }
    public void       setNombre(String nombre) { this.nombre = nombre; }

    public String     getDescripcion()  { return descripcion; }
    public void       setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String     getIngredientes() { return ingredientes; }
    public void       setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }

    public String     getModoUso()      { return modoUso; }
    public void       setModoUso(String modoUso) { this.modoUso = modoUso; }

    public BigDecimal getPrecio()       { return precio; }
    public void       setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer    getStock()        { return stock; }
    public void       setStock(Integer stock) { this.stock = (stock == null) ? 0 : stock; }

    public String     getGenero()       { return genero; }
    public void       setGenero(String genero) { this.genero = genero; }

    public String     getTipoFragancia() { return tipoFragancia; }
    public void       setTipoFragancia(String tipoFragancia) { this.tipoFragancia = tipoFragancia; }

    public Categoria  getCategoria()    { return categoria; }
    public void       setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Integer    getIdSubcategoria()                        { return idSubcategoria; }
    public void       setIdSubcategoria(Integer idSubcategoria)  { this.idSubcategoria = idSubcategoria; }
}
