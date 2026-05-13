package com.example.demo.entity;

import java.time.LocalDateTime;

public class Pago {

    private Integer       idPago;
    private Pedido        pedido;
    private String        metodoPago;
    private LocalDateTime fechaPago;
    private String        estado = "pendiente";

    public Pago() {}

    public Pago(Integer idPago, Pedido pedido, String metodoPago,
                LocalDateTime fechaPago, String estado) {
        this.idPago     = idPago;
        this.pedido     = pedido;
        this.metodoPago = metodoPago;
        this.fechaPago  = (fechaPago == null) ? LocalDateTime.now() : fechaPago;
        this.estado     = (estado == null || estado.isBlank()) ? "pendiente" : estado;
    }

    public Integer       getIdPago()    { return idPago; }
    public void          setIdPago(Integer idPago) { this.idPago = idPago; }

    public Pedido        getPedido()    { return pedido; }
    public void          setPedido(Pedido pedido) { this.pedido = pedido; }

    public String        getMetodoPago() { return metodoPago; }
    public void          setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void          setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = (fechaPago == null) ? LocalDateTime.now() : fechaPago;
    }

    public String        getEstado()    { return estado; }
    public void          setEstado(String estado) {
        this.estado = (estado == null || estado.isBlank()) ? "pendiente" : estado;
    }
}
