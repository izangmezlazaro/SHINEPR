package com.example.demo.entity;

import java.math.BigDecimal;

public class Frasco {

    private Integer    idFrasco;
    private String     forma;
    private Integer    capacidadMl;
    private String     material;
    private BigDecimal precio;

    public Frasco() {}

    public Frasco(Integer idFrasco, String forma, Integer capacidadMl, String material, BigDecimal precio) {
        this.idFrasco   = idFrasco;
        this.forma      = forma;
        this.capacidadMl = capacidadMl;
        this.material   = material;
        this.precio     = precio;
    }

    public Integer    getIdFrasco()   { return idFrasco; }
    public void       setIdFrasco(Integer id) { this.idFrasco = id; }

    public String     getForma()      { return forma; }
    public void       setForma(String forma) { this.forma = forma; }

    public Integer    getCapacidadMl() { return capacidadMl; }
    public void       setCapacidadMl(Integer capacidadMl) { this.capacidadMl = capacidadMl; }

    public String     getMaterial()   { return material; }
    public void       setMaterial(String material) { this.material = material; }

    public BigDecimal getPrecio()     { return precio; }
    public void       setPrecio(BigDecimal precio) { this.precio = precio; }
}
