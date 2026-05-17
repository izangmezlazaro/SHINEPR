package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Fichaje {

    private Integer       id;
    private Integer       idUsuario;
    private String        empleadoNombre; // de JOIN con usuario
    private String        empleadoEmail;  // de JOIN con usuario
    private LocalDate     fecha;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private String        estado;         // FICHADO | COMPLETADO

    public Fichaje() {}

    public Integer       getId()                         { return id; }
    public void          setId(Integer id)               { this.id = id; }

    public Integer       getIdUsuario()                  { return idUsuario; }
    public void          setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String        getEmpleadoNombre()                       { return empleadoNombre; }
    public void          setEmpleadoNombre(String empleadoNombre)  { this.empleadoNombre = empleadoNombre; }

    public String        getEmpleadoEmail()                        { return empleadoEmail; }
    public void          setEmpleadoEmail(String empleadoEmail)    { this.empleadoEmail = empleadoEmail; }

    public LocalDate     getFecha()                      { return fecha; }
    public void          setFecha(LocalDate fecha)       { this.fecha = fecha; }

    public LocalDateTime getHoraEntrada()                          { return horaEntrada; }
    public void          setHoraEntrada(LocalDateTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalDateTime getHoraSalida()                           { return horaSalida; }
    public void          setHoraSalida(LocalDateTime horaSalida)   { this.horaSalida = horaSalida; }

    public String        getEstado()                     { return estado; }
    public void          setEstado(String estado)        { this.estado = estado; }
}
