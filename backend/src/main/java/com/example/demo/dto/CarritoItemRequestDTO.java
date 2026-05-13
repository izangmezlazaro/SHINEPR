package com.example.demo.dto;

public class CarritoItemRequestDTO {
    private Integer idProducto;
    private Integer idPerfCust;
    private Integer cantidad;

    public CarritoItemRequestDTO() {}
    public Integer getIdProducto() { return idProducto; }
    public void    setIdProducto(Integer v) { this.idProducto = v; }
    public Integer getIdPerfCust() { return idPerfCust; }
    public void    setIdPerfCust(Integer v) { this.idPerfCust = v; }
    public Integer getCantidad()   { return cantidad; }
    public void    setCantidad(Integer v) { this.cantidad = v; }
}
