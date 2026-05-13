package com.example.demo.dto;

public class EstadoEnvioRequestDTO {
    private String estadoEnv;
    private String numSeguimiento;

    public EstadoEnvioRequestDTO() {}
    public String getEstadoEnv()       { return estadoEnv; }
    public void   setEstadoEnv(String v) { this.estadoEnv = v; }
    public String getNumSeguimiento()  { return numSeguimiento; }
    public void   setNumSeguimiento(String v) { this.numSeguimiento = v; }
}
