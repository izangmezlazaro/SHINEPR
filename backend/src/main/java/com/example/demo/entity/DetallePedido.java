package com.example.demo.entity;

import java.math.BigDecimal;

public class DetallePedido {

    private Integer      id;
    private Pedido       pedido;
    private Producto     producto;
    private PerfumeCustom perfumeCustom;
    private Integer      cantidad;
    private BigDecimal   precioUnitario;

    public DetallePedido() {}

    public DetallePedido(Integer id, Pedido pedido, Producto producto,
                         PerfumeCustom perfumeCustom, Integer cantidad, BigDecimal precioUnitario) {
        this.id              = id;
        this.pedido          = pedido;
        this.producto        = producto;
        this.perfumeCustom   = perfumeCustom;
        this.cantidad        = cantidad;
        this.precioUnitario  = precioUnitario;
    }

    public Integer      getId()             { return id; }
    public void         setId(Integer id)   { this.id = id; }

    public Pedido       getPedido()         { return pedido; }
    public void         setPedido(Pedido pedido) { this.pedido = pedido; }

    public Producto     getProducto()       { return producto; }
    public void         setProducto(Producto producto) { this.producto = producto; }

    public PerfumeCustom getPerfumeCustom() { return perfumeCustom; }
    public void          setPerfumeCustom(PerfumeCustom pc) { this.perfumeCustom = pc; }

    public Integer      getCantidad()       { return cantidad; }
    public void         setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal   getPrecioUnitario() { return precioUnitario; }
    public void         setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
