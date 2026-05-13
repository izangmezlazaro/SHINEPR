package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {
    private Integer idPedido;
    private Integer idUsuario;
    private Integer idDireccion;
    private String  estado;
    private BigDecimal total;
    private LocalDateTime fecha;
    private List<DetallePedidoDTO> detalles;

    public PedidoResponseDTO() {}
    public PedidoResponseDTO(Integer idPedido, Integer idUsuario, Integer idDireccion,
                              String estado, BigDecimal total, LocalDateTime fecha,
                              List<DetallePedidoDTO> detalles) {
        this.idPedido = idPedido; this.idUsuario = idUsuario; this.idDireccion = idDireccion;
        this.estado = estado; this.total = total; this.fecha = fecha; this.detalles = detalles;
    }
    public Integer    getIdPedido()    { return idPedido; }
    public void       setIdPedido(Integer v) { this.idPedido = v; }
    public Integer    getIdUsuario()   { return idUsuario; }
    public void       setIdUsuario(Integer v) { this.idUsuario = v; }
    public Integer    getIdDireccion() { return idDireccion; }
    public void       setIdDireccion(Integer v) { this.idDireccion = v; }
    public String     getEstado()      { return estado; }
    public void       setEstado(String v) { this.estado = v; }
    public BigDecimal getTotal()       { return total; }
    public void       setTotal(BigDecimal v) { this.total = v; }
    public LocalDateTime getFecha()    { return fecha; }
    public void       setFecha(LocalDateTime v) { this.fecha = v; }
    public List<DetallePedidoDTO> getDetalles() { return detalles; }
    public void       setDetalles(List<DetallePedidoDTO> v) { this.detalles = v; }
}
