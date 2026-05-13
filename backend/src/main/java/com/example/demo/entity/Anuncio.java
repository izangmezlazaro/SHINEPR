package com.example.demo.entity;

import java.time.LocalDateTime;

public class Anuncio {

    private Integer       id;
    private String        titulo;
    private String        tag;
    private String        tagLabel;
    private String        mensaje;
    private String        autor;
    private LocalDateTime fecha;

    public Anuncio() {}

    public Integer       getId()               { return id; }
    public void          setId(Integer id)     { this.id = id; }

    public String        getTitulo()                { return titulo; }
    public void          setTitulo(String titulo)   { this.titulo = titulo; }

    public String        getTag()             { return tag; }
    public void          setTag(String tag)   { this.tag = tag; }

    public String        getTagLabel()                  { return tagLabel; }
    public void          setTagLabel(String tagLabel)   { this.tagLabel = tagLabel; }

    public String        getMensaje()                { return mensaje; }
    public void          setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String        getAutor()               { return autor; }
    public void          setAutor(String autor)   { this.autor = autor; }

    public LocalDateTime getFecha()               { return fecha; }
    public void          setFecha(LocalDateTime fecha){ this.fecha = fecha; }
}
