package com.example.demo.dto;

public class PerfumeCustomFraganciaDTO {
    private Integer idFragancia;
    private String  nombreFragancia;
    private Integer orden;

    public PerfumeCustomFraganciaDTO() {}
    public PerfumeCustomFraganciaDTO(Integer idFragancia, String nombreFragancia, Integer orden) {
        this.idFragancia = idFragancia; this.nombreFragancia = nombreFragancia; this.orden = orden;
    }
    public Integer getIdFragancia()      { return idFragancia; }
    public void    setIdFragancia(Integer v) { this.idFragancia = v; }
    public String  getNombreFragancia()  { return nombreFragancia; }
    public void    setNombreFragancia(String v) { this.nombreFragancia = v; }
    public Integer getOrden()            { return orden; }
    public void    setOrden(Integer v)   { this.orden = v; }
}
