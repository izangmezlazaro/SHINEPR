package com.example.demo.dto;

public class ImagenProductoDTO {
    private Integer idImagen;
    private Integer idProducto;
    private String  url;
    private String  descripcion;

    public ImagenProductoDTO() {}
    public ImagenProductoDTO(Integer idImagen, Integer idProducto, String url, String descripcion) {
        this.idImagen = idImagen; this.idProducto = idProducto;
        this.url = url; this.descripcion = descripcion;
    }
    public Integer getIdImagen()    { return idImagen; }
    public void    setIdImagen(Integer v) { this.idImagen = v; }
    public Integer getIdProducto()  { return idProducto; }
    public void    setIdProducto(Integer v) { this.idProducto = v; }
    public String  getUrl()         { return url; }
    public void    setUrl(String v) { this.url = v; }
    public String  getDescripcion() { return descripcion; }
    public void    setDescripcion(String v) { this.descripcion = v; }
}
