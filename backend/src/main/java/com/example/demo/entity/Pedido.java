package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private Integer        idPedido;
    private Usuario        usuario;
    private Direccion      direccion;
    private String         estado = "pendiente";
    private BigDecimal     total;
    private LocalDateTime  fecha;
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {}

    public Pedido(Integer idPedido, Usuario usuario, Direccion direccion,
                  String estado, BigDecimal total, LocalDateTime fecha) {
        this.idPedido  = idPedido;
        this.usuario   = usuario;
        this.direccion = direccion;
        this.estado    = (estado == null || estado.isBlank()) ? "pendiente" : estado;
        this.total     = total;
        this.fecha     = (fecha == null) ? LocalDateTime.now() : fecha;
    }

    public Integer       getIdPedido()  { return idPedido; }
    public void          setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public Usuario       getUsuario()   { return usuario; }
    public void          setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Direccion     getDireccion() { return direccion; }
    public void          setDireccion(Direccion direccion) { this.direccion = direccion; }

    public String        getEstado()    { return estado; }
    public void          setEstado(String estado) {
        this.estado = (estado == null || estado.isBlank()) ? "pendiente" : estado;
    }

    public BigDecimal    getTotal()     { return total; }
    public void          setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getFecha()     { return fecha; }
    public void          setFecha(LocalDateTime fecha) {
        this.fecha = (fecha == null) ? LocalDateTime.now() : fecha;
    }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}
