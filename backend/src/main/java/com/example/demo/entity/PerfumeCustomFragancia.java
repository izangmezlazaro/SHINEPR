package com.example.demo.entity;

public class PerfumeCustomFragancia {

    private Integer      idPerfCust;
    private Integer      idFragancia;
    private PerfumeCustom perfumeCustom;
    private Fragancia    fragancia;
    private Integer      orden;

    public PerfumeCustomFragancia() {}

    public PerfumeCustomFragancia(Integer idPerfCust, Integer idFragancia,
                                   PerfumeCustom perfumeCustom, Fragancia fragancia, Integer orden) {
        this.idPerfCust   = idPerfCust;
        this.idFragancia  = idFragancia;
        this.perfumeCustom = perfumeCustom;
        this.fragancia    = fragancia;
        this.orden        = orden;
    }

    public Integer       getIdPerfCust()    { return idPerfCust; }
    public void          setIdPerfCust(Integer id) { this.idPerfCust = id; }

    public Integer       getIdFragancia()   { return idFragancia; }
    public void          setIdFragancia(Integer id) { this.idFragancia = id; }

    public PerfumeCustom getPerfumeCustom() { return perfumeCustom; }
    public void          setPerfumeCustom(PerfumeCustom pc) { this.perfumeCustom = pc; }

    public Fragancia     getFragancia()     { return fragancia; }
    public void          setFragancia(Fragancia f) { this.fragancia = f; }

    public Integer       getOrden()         { return orden; }
    public void          setOrden(Integer orden) { this.orden = orden; }
}
