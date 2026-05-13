package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminPedidoDTO {
    private Integer idPedido;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal total;
    private String nombreUsuario;
    private String emailUsuario;
    private List<DetallePedidoDTO> detalles;

    public AdminPedidoDTO() {}

    public Integer       getIdPedido()      { return idPedido; }
    public void          setIdPedido(Integer v) { this.idPedido = v; }
    public LocalDateTime getFecha()          { return fecha; }
    public void          setFecha(LocalDateTime v) { this.fecha = v; }
    public String        getEstado()         { return estado; }
    public void          setEstado(String v) { this.estado = v; }
    public BigDecimal    getTotal()          { return total; }
    public void          setTotal(BigDecimal v) { this.total = v; }
    public String        getNombreUsuario()  { return nombreUsuario; }
    public void          setNombreUsuario(String v) { this.nombreUsuario = v; }
    public String        getEmailUsuario()   { return emailUsuario; }
    public void          setEmailUsuario(String v) { this.emailUsuario = v; }
    public List<DetallePedidoDTO> getDetalles() { return detalles; }
    public void          setDetalles(List<DetallePedidoDTO> v) { this.detalles = v; }
}
