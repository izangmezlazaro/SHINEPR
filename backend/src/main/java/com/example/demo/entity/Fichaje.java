package com.example.demo.entity;

import java.time.LocalDateTime;

public class Fichaje {

    private Integer       id;
    private String        empleadoEmail;
    private String        empleadoNombre;
    private String        tipo;       // ENTRADA | SALIDA
    private LocalDateTime fechaHora;

    public Fichaje() {}

    public Integer       getId()              { return id; }
    public void          setId(Integer id)    { this.id = id; }

    public String        getEmpleadoEmail()                    { return empleadoEmail; }
    public void          setEmpleadoEmail(String empleadoEmail){ this.empleadoEmail = empleadoEmail; }

    public String        getEmpleadoNombre()                     { return empleadoNombre; }
    public void          setEmpleadoNombre(String empleadoNombre){ this.empleadoNombre = empleadoNombre; }

    public String        getTipo()             { return tipo; }
    public void          setTipo(String tipo)  { this.tipo = tipo; }

    public LocalDateTime getFechaHora()                    { return fechaHora; }
    public void          setFechaHora(LocalDateTime fechaHora){ this.fechaHora = fechaHora; }
}
