package com.example.demo.dto;

import java.math.BigDecimal;

public class DetallePedidoDTO {
    private Integer id;
    private Integer idProducto;
    private Integer idPerfCust;
    private String  nombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String  imagenUrl;

    public DetallePedidoDTO() {}
    public DetallePedidoDTO(Integer id, Integer idProducto, Integer idPerfCust, String nombre,
                             Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.id = id; this.idProducto = idProducto; this.idPerfCust = idPerfCust;
        this.nombre = nombre; this.cantidad = cantidad;
        this.precioUnitario = precioUnitario; this.subtotal = subtotal;
    }
    public Integer    getId()             { return id; }
    public void       setId(Integer v)    { this.id = v; }
    public Integer    getIdProducto()     { return idProducto; }
    public void       setIdProducto(Integer v) { this.idProducto = v; }
    public Integer    getIdPerfCust()     { return idPerfCust; }
    public void       setIdPerfCust(Integer v) { this.idPerfCust = v; }
    public String     getNombre()         { return nombre; }
    public void       setNombre(String v) { this.nombre = v; }
    public Integer    getCantidad()       { return cantidad; }
    public void       setCantidad(Integer v) { this.cantidad = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void       setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }
    public BigDecimal getSubtotal()       { return subtotal; }
    public void       setSubtotal(BigDecimal v) { this.subtotal = v; }
    public String     getImagenUrl()      { return imagenUrl; }
    public void       setImagenUrl(String v) { this.imagenUrl = v; }
}
