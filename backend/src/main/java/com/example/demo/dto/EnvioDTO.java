package com.example.demo.dto;

public class EnvioDTO {
    private Integer idEnvio;
    private Integer idPedido;
    private String  estadoEnv;
    private String  numSeguimiento;

    public EnvioDTO() {}
    public EnvioDTO(Integer idEnvio, Integer idPedido, String estadoEnv, String numSeguimiento) {
        this.idEnvio = idEnvio; this.idPedido = idPedido;
        this.estadoEnv = estadoEnv; this.numSeguimiento = numSeguimiento;
    }
    public Integer getIdEnvio()          { return idEnvio; }
    public void    setIdEnvio(Integer v) { this.idEnvio = v; }
    public Integer getIdPedido()         { return idPedido; }
    public void    setIdPedido(Integer v) { this.idPedido = v; }
    public String  getEstadoEnv()        { return estadoEnv; }
    public void    setEstadoEnv(String v) { this.estadoEnv = v; }
    public String  getNumSeguimiento()   { return numSeguimiento; }
    public void    setNumSeguimiento(String v) { this.numSeguimiento = v; }
}
