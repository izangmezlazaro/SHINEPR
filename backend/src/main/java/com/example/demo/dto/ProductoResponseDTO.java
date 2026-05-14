package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductoResponseDTO {
    private Integer    idProducto;
    private String     sku;
    private String     nombre;
    private String     descripcion;
    private String     ingredientes;
    private String     modoUso;
    private BigDecimal precio;
    private Integer    stock;
    private String     genero;
    private String     tipoFragancia;
    private CategoriaDTO categoria;
    private Integer      idSubcategoria;
    private List<ImagenProductoDTO> imagenes;

    public ProductoResponseDTO() {}
    public ProductoResponseDTO(Integer idProducto, String sku, String nombre, String descripcion,
                                String ingredientes, String modoUso, BigDecimal precio, Integer stock,
                                String genero, String tipoFragancia, CategoriaDTO categoria,
                                Integer idSubcategoria, List<ImagenProductoDTO> imagenes) {
        this.idProducto = idProducto; this.sku = sku; this.nombre = nombre;
        this.descripcion = descripcion; this.ingredientes = ingredientes; this.modoUso = modoUso;
        this.precio = precio; this.stock = stock; this.genero = genero;
        this.tipoFragancia = tipoFragancia; this.categoria = categoria;
        this.idSubcategoria = idSubcategoria; this.imagenes = imagenes;
    }
    public Integer    getIdProducto()    { return idProducto; }
    public void       setIdProducto(Integer v) { this.idProducto = v; }
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
    public CategoriaDTO getCategoria()   { return categoria; }
    public void       setCategoria(CategoriaDTO v) { this.categoria = v; }
    public Integer    getIdSubcategoria()                       { return idSubcategoria; }
    public void       setIdSubcategoria(Integer idSubcategoria) { this.idSubcategoria = idSubcategoria; }
    public List<ImagenProductoDTO> getImagenes() { return imagenes; }
    public void       setImagenes(List<ImagenProductoDTO> v) { this.imagenes = v; }
}
