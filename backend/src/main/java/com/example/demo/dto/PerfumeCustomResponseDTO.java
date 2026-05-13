package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class PerfumeCustomResponseDTO {
    private Integer    idPerfCust;
    private Integer    idUsuario;
    private FrascoDTO  frasco;
    private String     nombrePersonalizado;
    private String     intensidad;
    private BigDecimal precioCalculado;
    private List<PerfumeCustomFraganciaDTO> fragancias;

    public PerfumeCustomResponseDTO() {}
    public PerfumeCustomResponseDTO(Integer idPerfCust, Integer idUsuario, FrascoDTO frasco,
                                     String nombrePersonalizado, String intensidad,
                                     BigDecimal precioCalculado, List<PerfumeCustomFraganciaDTO> fragancias) {
        this.idPerfCust = idPerfCust; this.idUsuario = idUsuario; this.frasco = frasco;
        this.nombrePersonalizado = nombrePersonalizado; this.intensidad = intensidad;
        this.precioCalculado = precioCalculado; this.fragancias = fragancias;
    }
    public Integer    getIdPerfCust()          { return idPerfCust; }
    public void       setIdPerfCust(Integer v) { this.idPerfCust = v; }
    public Integer    getIdUsuario()           { return idUsuario; }
    public void       setIdUsuario(Integer v)  { this.idUsuario = v; }
    public FrascoDTO  getFrasco()              { return frasco; }
    public void       setFrasco(FrascoDTO v)   { this.frasco = v; }
    public String     getNombrePersonalizado()  { return nombrePersonalizado; }
    public void       setNombrePersonalizado(String v) { this.nombrePersonalizado = v; }
    public String     getIntensidad()          { return intensidad; }
    public void       setIntensidad(String v)  { this.intensidad = v; }
    public BigDecimal getPrecioCalculado()     { return precioCalculado; }
    public void       setPrecioCalculado(BigDecimal v) { this.precioCalculado = v; }
    public List<PerfumeCustomFraganciaDTO> getFragancias() { return fragancias; }
    public void       setFragancias(List<PerfumeCustomFraganciaDTO> v) { this.fragancias = v; }
}
