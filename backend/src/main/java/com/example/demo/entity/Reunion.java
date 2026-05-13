package com.example.demo.entity;

import java.time.LocalDateTime;

public class Reunion {

    private Integer       id;
    private String        titulo;
    private String        fecha;      // YYYY-MM-DD
    private String        hora;       // HH:mm
    private String        plataforma;
    private String        asistentes;
    private String        color;
    private String        creadoPor;
    private LocalDateTime creadoEn;

    public Reunion() {}

    public Integer       getId()               { return id; }
    public void          setId(Integer id)     { this.id = id; }

    public String        getTitulo()                { return titulo; }
    public void          setTitulo(String titulo)   { this.titulo = titulo; }

    public String        getFecha()               { return fecha; }
    public void          setFecha(String fecha)   { this.fecha = fecha; }

    public String        getHora()              { return hora; }
    public void          setHora(String hora)   { this.hora = hora; }

    public String        getPlataforma()                    { return plataforma; }
    public void          setPlataforma(String plataforma)   { this.plataforma = plataforma; }

    public String        getAsistentes()                    { return asistentes; }
    public void          setAsistentes(String asistentes)   { this.asistentes = asistentes; }

    public String        getColor()               { return color; }
    public void          setColor(String color)   { this.color = color; }

    public String        getCreadoPor()                   { return creadoPor; }
    public void          setCreadoPor(String creadoPor)   { this.creadoPor = creadoPor; }

    public LocalDateTime getCreadoEn()                    { return creadoEn; }
    public void          setCreadoEn(LocalDateTime c)     { this.creadoEn = c; }
}
