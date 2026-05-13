package com.example.demo.entity;

public class ImagenProducto {

    private Integer idImagen;
    private Producto producto;
    private String   url;
    private String   descripcion;

    public ImagenProducto() {}

    public ImagenProducto(Integer idImagen, Producto producto, String url, String descripcion) {
        this.idImagen   = idImagen;
        this.producto   = producto;
        this.url        = url;
        this.descripcion = descripcion;
    }

    public Integer  getIdImagen()   { return idImagen; }
    public void     setIdImagen(Integer idImagen) { this.idImagen = idImagen; }

    public Producto getProducto()   { return producto; }
    public void     setProducto(Producto producto) { this.producto = producto; }

    public String   getUrl()        { return url; }
    public void     setUrl(String url) { this.url = url; }

    public String   getDescripcion() { return descripcion; }
    public void     setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
