package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class PerfumeCustomRequestDTO {
    private Integer idUsuario;
    private Integer idFrasco;
    private String  nombrePersonalizado;
    private String  intensidad;
    private BigDecimal precioCalculado;
    private List<PerfumeCustomFraganciaDTO> fragancias;

    public PerfumeCustomRequestDTO() {}
    public Integer    getIdUsuario()          { return idUsuario; }
    public void       setIdUsuario(Integer v) { this.idUsuario = v; }
    public Integer    getIdFrasco()           { return idFrasco; }
    public void       setIdFrasco(Integer v)  { this.idFrasco = v; }
    public String     getNombrePersonalizado() { return nombrePersonalizado; }
    public void       setNombrePersonalizado(String v) { this.nombrePersonalizado = v; }
    public String     getIntensidad()         { return intensidad; }
    public void       setIntensidad(String v) { this.intensidad = v; }
    public BigDecimal getPrecioCalculado()    { return precioCalculado; }
    public void       setPrecioCalculado(BigDecimal v) { this.precioCalculado = v; }
    public List<PerfumeCustomFraganciaDTO> getFragancias() { return fragancias; }
    public void       setFragancias(List<PerfumeCustomFraganciaDTO> v) { this.fragancias = v; }
}
