package com.example.demo.dto;

import java.math.BigDecimal;

public class FrascoDTO {
    private Integer    idFrasco;
    private String     forma;
    private Integer    capacidadMl;
    private String     material;
    private BigDecimal precio;

    public FrascoDTO() {}
    public FrascoDTO(Integer idFrasco, String forma, Integer capacidadMl, String material, BigDecimal precio) {
        this.idFrasco = idFrasco; this.forma = forma; this.capacidadMl = capacidadMl;
        this.material = material; this.precio = precio;
    }
    public Integer    getIdFrasco()    { return idFrasco; }
    public void       setIdFrasco(Integer v) { this.idFrasco = v; }
    public String     getForma()       { return forma; }
    public void       setForma(String v) { this.forma = v; }
    public Integer    getCapacidadMl() { return capacidadMl; }
    public void       setCapacidadMl(Integer v) { this.capacidadMl = v; }
    public String     getMaterial()    { return material; }
    public void       setMaterial(String v) { this.material = v; }
    public BigDecimal getPrecio()      { return precio; }
    public void       setPrecio(BigDecimal v) { this.precio = v; }
}
