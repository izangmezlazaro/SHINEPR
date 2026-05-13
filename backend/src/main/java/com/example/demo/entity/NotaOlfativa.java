package com.example.demo.entity;

public class NotaOlfativa {

    private Integer   idNota;
    private Fragancia fragancia;
    private String    nombre;
    private String    tipo;
    private String    urlImagen;

    public NotaOlfativa() {}

    public NotaOlfativa(Integer idNota, Fragancia fragancia, String nombre, String tipo, String urlImagen) {
        this.idNota    = idNota;
        this.fragancia = fragancia;
        this.nombre    = nombre;
        this.tipo      = tipo;
        this.urlImagen = urlImagen;
    }

    public Integer   getIdNota()    { return idNota; }
    public void      setIdNota(Integer id) { this.idNota = id; }

    public Fragancia getFragancia() { return fragancia; }
    public void      setFragancia(Fragancia fragancia) { this.fragancia = fragancia; }

    public String    getNombre()    { return nombre; }
    public void      setNombre(String nombre) { this.nombre = nombre; }

    public String    getTipo()      { return tipo; }
    public void      setTipo(String tipo) { this.tipo = tipo; }

    public String    getUrlImagen() { return urlImagen; }
    public void      setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
}
