package com.example.demo.dto;

public class NotaOlfativaDTO {
    private Integer idNota;
    private Integer idFragancia;
    private String  nombre;
    private String  tipo;
    private String  urlImagen;

    public NotaOlfativaDTO() {}
    public NotaOlfativaDTO(Integer idNota, Integer idFragancia, String nombre, String tipo, String urlImagen) {
        this.idNota = idNota; this.idFragancia = idFragancia; this.nombre = nombre;
        this.tipo = tipo; this.urlImagen = urlImagen;
    }
    public Integer getIdNota()       { return idNota; }
    public void    setIdNota(Integer v) { this.idNota = v; }
    public Integer getIdFragancia()  { return idFragancia; }
    public void    setIdFragancia(Integer v) { this.idFragancia = v; }
    public String  getNombre()       { return nombre; }
    public void    setNombre(String v) { this.nombre = v; }
    public String  getTipo()         { return tipo; }
    public void    setTipo(String v) { this.tipo = v; }
    public String  getUrlImagen()    { return urlImagen; }
    public void    setUrlImagen(String v) { this.urlImagen = v; }
}
