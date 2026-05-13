package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Carrito {

    private Integer       id;
    private Usuario       usuario;
    private LocalDateTime creadoEn;
    private List<CarritoItem> items = new ArrayList<>();

    public Carrito() {}

    public Carrito(Integer id, Usuario usuario, LocalDateTime creadoEn) {
        this.id       = id;
        this.usuario  = usuario;
        this.creadoEn = (creadoEn == null) ? LocalDateTime.now() : creadoEn;
    }

    public Integer       getId()       { return id; }
    public void          setId(Integer id) { this.id = id; }

    public Usuario       getUsuario()  { return usuario; }
    public void          setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void          setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = (creadoEn == null) ? LocalDateTime.now() : creadoEn;
    }

    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { this.items = items; }
}
