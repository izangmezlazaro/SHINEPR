package com.example.demo.dto;

public class PedidoRequestDTO {
    private Integer idUsuario;
    private Integer idDireccion;

    public PedidoRequestDTO() {}
    public Integer getIdUsuario()    { return idUsuario; }
    public void    setIdUsuario(Integer v) { this.idUsuario = v; }
    public Integer getIdDireccion()  { return idDireccion; }
    public void    setIdDireccion(Integer v) { this.idDireccion = v; }
}
