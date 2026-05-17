package com.example.demo.entity;

import java.time.LocalDateTime;

public class BuzonMensaje {

    private Integer       idMensaje;
    private String        nombre;
    private String        apellidos;
    private String        asunto;
    private String        texto;
    private LocalDateTime fecha;

    public BuzonMensaje() {}

    public Integer       getIdMensaje()                      { return idMensaje; }
    public void          setIdMensaje(Integer idMensaje)     { this.idMensaje = idMensaje; }

    public String        getNombre()                         { return nombre; }
    public void          setNombre(String nombre)            { this.nombre = nombre; }

    public String        getApellidos()                      { return apellidos; }
    public void          setApellidos(String apellidos)      { this.apellidos = apellidos; }

    public String        getAsunto()                         { return asunto; }
    public void          setAsunto(String asunto)            { this.asunto = asunto; }

    public String        getTexto()                          { return texto; }
    public void          setTexto(String texto)              { this.texto = texto; }

    public LocalDateTime getFecha()                          { return fecha; }
    public void          setFecha(LocalDateTime fecha)       { this.fecha = fecha; }
}
