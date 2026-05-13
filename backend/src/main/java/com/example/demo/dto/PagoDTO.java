package com.example.demo.dto;

import java.time.LocalDateTime;

public class PagoDTO {
    private Integer       idPago;
    private Integer       idPedido;
    private String        metodoPago;
    private LocalDateTime fechaPago;
    private String        estado;

    public PagoDTO() {}
    public PagoDTO(Integer idPago, Integer idPedido, String metodoPago, LocalDateTime fechaPago, String estado) {
        this.idPago = idPago; this.idPedido = idPedido; this.metodoPago = metodoPago;
        this.fechaPago = fechaPago; this.estado = estado;
    }
    public Integer       getIdPago()     { return idPago; }
    public void          setIdPago(Integer v) { this.idPago = v; }
    public Integer       getIdPedido()   { return idPedido; }
    public void          setIdPedido(Integer v) { this.idPedido = v; }
    public String        getMetodoPago() { return metodoPago; }
    public void          setMetodoPago(String v) { this.metodoPago = v; }
    public LocalDateTime getFechaPago()  { return fechaPago; }
    public void          setFechaPago(LocalDateTime v) { this.fechaPago = v; }
    public String        getEstado()     { return estado; }
    public void          setEstado(String v) { this.estado = v; }
}
