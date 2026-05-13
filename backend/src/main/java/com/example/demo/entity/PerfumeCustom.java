package com.example.demo.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PerfumeCustom {

    private Integer    idPerfCust;
    private Usuario    usuario;
    private Frasco     frasco;
    private String     nombrePersonalizado;
    private String     intensidad;
    private BigDecimal precioCalculado;
    private List<PerfumeCustomFragancia> fragancias = new ArrayList<>();

    public PerfumeCustom() {}

    public PerfumeCustom(Integer idPerfCust, Usuario usuario, Frasco frasco,
                         String nombrePersonalizado, String intensidad, BigDecimal precioCalculado) {
        this.idPerfCust          = idPerfCust;
        this.usuario             = usuario;
        this.frasco              = frasco;
        this.nombrePersonalizado = nombrePersonalizado;
        this.intensidad          = intensidad;
        this.precioCalculado     = precioCalculado;
    }

    public Integer    getIdPerfCust()          { return idPerfCust; }
    public void       setIdPerfCust(Integer id){ this.idPerfCust = id; }

    public Usuario    getUsuario()             { return usuario; }
    public void       setUsuario(Usuario u)    { this.usuario = u; }

    public Frasco     getFrasco()              { return frasco; }
    public void       setFrasco(Frasco f)      { this.frasco = f; }

    public String     getNombrePersonalizado() { return nombrePersonalizado; }
    public void       setNombrePersonalizado(String n) { this.nombrePersonalizado = n; }

    public String     getIntensidad()          { return intensidad; }
    public void       setIntensidad(String i)  { this.intensidad = i; }

    public BigDecimal getPrecioCalculado()     { return precioCalculado; }
    public void       setPrecioCalculado(BigDecimal p) { this.precioCalculado = p; }

    public List<PerfumeCustomFragancia> getFragancias() { return fragancias; }
    public void setFragancias(List<PerfumeCustomFragancia> fragancias) { this.fragancias = fragancias; }
}
