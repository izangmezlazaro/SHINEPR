package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CarritoDTO {
    private Integer id;
    private Integer idUsuario;
    private LocalDateTime creadoEn;
    private List<CarritoItemResponseDTO> items;
    private BigDecimal total;

    public CarritoDTO() {}
    public CarritoDTO(Integer id, Integer idUsuario, LocalDateTime creadoEn,
                      List<CarritoItemResponseDTO> items, BigDecimal total) {
        this.id = id; this.idUsuario = idUsuario; this.creadoEn = creadoEn;
        this.items = items; this.total = total;
    }
    public Integer getId()        { return id; }
    public void    setId(Integer id) { this.id = id; }
    public Integer getIdUsuario() { return idUsuario; }
    public void    setIdUsuario(Integer v) { this.idUsuario = v; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void    setCreadoEn(LocalDateTime v) { this.creadoEn = v; }
    public List<CarritoItemResponseDTO> getItems() { return items; }
    public void    setItems(List<CarritoItemResponseDTO> v) { this.items = v; }
    public BigDecimal getTotal() { return total; }
    public void    setTotal(BigDecimal v) { this.total = v; }
}
