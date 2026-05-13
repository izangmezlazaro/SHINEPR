package com.example.demo.entity;

public class CarritoItem {

    private Integer      id;
    private Carrito      carrito;
    private Producto     producto;
    private PerfumeCustom perfumeCustom;
    private Integer      cantidad = 1;

    public CarritoItem() {}

    public CarritoItem(Integer id, Carrito carrito, Producto producto,
                       PerfumeCustom perfumeCustom, Integer cantidad) {
        this.id           = id;
        this.carrito      = carrito;
        this.producto     = producto;
        this.perfumeCustom = perfumeCustom;
        this.cantidad     = (cantidad == null) ? 1 : cantidad;
    }

    public Integer      getId()           { return id; }
    public void         setId(Integer id) { this.id = id; }

    public Carrito      getCarrito()      { return carrito; }
    public void         setCarrito(Carrito carrito) { this.carrito = carrito; }

    public Producto     getProducto()     { return producto; }
    public void         setProducto(Producto producto) { this.producto = producto; }

    public PerfumeCustom getPerfumeCustom() { return perfumeCustom; }
    public void          setPerfumeCustom(PerfumeCustom pc) { this.perfumeCustom = pc; }

    public Integer      getCantidad()     { return cantidad; }
    public void         setCantidad(Integer cantidad) {
        this.cantidad = (cantidad == null) ? 1 : cantidad;
    }
}
