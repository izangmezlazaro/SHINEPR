package com.example.demo.entity;

public class Envio {

    private Integer idEnvio;
    private Pedido  pedido;
    private String  estadoEnv = "preparando";
    private String  numSeguimiento;

    public Envio() {}

    public Envio(Integer idEnvio, Pedido pedido, String estadoEnv, String numSeguimiento) {
        this.idEnvio        = idEnvio;
        this.pedido         = pedido;
        this.estadoEnv      = (estadoEnv == null || estadoEnv.isBlank()) ? "preparando" : estadoEnv;
        this.numSeguimiento = numSeguimiento;
    }

    public Integer getIdEnvio()           { return idEnvio; }
    public void    setIdEnvio(Integer id) { this.idEnvio = id; }

    public Pedido  getPedido()            { return pedido; }
    public void    setPedido(Pedido pedido) { this.pedido = pedido; }

    public String  getEstadoEnv()         { return estadoEnv; }
    public void    setEstadoEnv(String estadoEnv) {
        this.estadoEnv = (estadoEnv == null || estadoEnv.isBlank()) ? "preparando" : estadoEnv;
    }

    public String  getNumSeguimiento()    { return numSeguimiento; }
    public void    setNumSeguimiento(String numSeguimiento) { this.numSeguimiento = numSeguimiento; }
}
