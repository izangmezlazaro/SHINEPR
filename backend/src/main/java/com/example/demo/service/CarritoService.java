package com.example.demo.service;

import com.example.demo.dao.CarritoDAO;
import com.example.demo.dao.CarritoItemDAO;
import com.example.demo.dao.PerfumeCustomDAO;
import com.example.demo.dao.ProductoDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.dto.CarritoDTO;
import com.example.demo.dto.CarritoItemRequestDTO;
import com.example.demo.dto.CarritoItemResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CarritoService {

    private final CarritoDAO      carritoDAO;
    private final CarritoItemDAO  carritoItemDAO;
    private final UsuarioDAO      usuarioDAO;
    private final ProductoDAO     productoDAO;
    private final PerfumeCustomDAO perfumeCustomDAO;

    public CarritoService() {
        this.carritoDAO       = new CarritoDAO();
        this.carritoItemDAO   = new CarritoItemDAO();
        this.usuarioDAO       = new UsuarioDAO();
        this.productoDAO      = new ProductoDAO();
        this.perfumeCustomDAO = new PerfumeCustomDAO();
    }

    public CarritoDTO obtenerOCrearPorUsuario(Integer idUsuario) {
        try {
            Carrito carrito = carritoDAO.findByUsuarioId(idUsuario).orElseGet(() -> crearCarrito(idUsuario));
            return toDto(carrito);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public CarritoDTO agregarItem(Integer idUsuario, CarritoItemRequestDTO request) {
        try {
            validarItemExclusivo(request.getIdProducto(), request.getIdPerfCust());
            Carrito carrito = carritoDAO.findByUsuarioId(idUsuario).orElseGet(() -> crearCarrito(idUsuario));
            int cantidadAgregar = request.getCantidad() == null ? 1 : request.getCantidad();

            if (request.getIdProducto() != null) {
                Optional<CarritoItem> existing = carritoItemDAO.findByCarritoIdAndProductoId(
                        carrito.getId(), request.getIdProducto());
                if (existing.isPresent()) {
                    CarritoItem item = existing.get();
                    carritoItemDAO.updateCantidad(item.getId(), item.getCantidad() + cantidadAgregar);
                    return toDto(carrito);
                }
                Producto p = productoDAO.findById(request.getIdProducto())
                        .orElseThrow(() -> new EntityNotFoundException("Producto", request.getIdProducto()));
                CarritoItem item = new CarritoItem();
                item.setCarrito(carrito);
                item.setCantidad(cantidadAgregar);
                item.setProducto(p);
                carritoItemDAO.save(item);
            } else {
                PerfumeCustom pc = perfumeCustomDAO.findById(request.getIdPerfCust())
                        .orElseThrow(() -> new EntityNotFoundException("Perfume personalizado", request.getIdPerfCust()));
                CarritoItem item = new CarritoItem();
                item.setCarrito(carrito);
                item.setCantidad(cantidadAgregar);
                item.setPerfumeCustom(pc);
                carritoItemDAO.save(item);
            }

            return toDto(carrito);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void eliminarItem(Integer idUsuario, Integer idItem) {
        try {
            Carrito carrito = carritoDAO.findByUsuarioId(idUsuario)
                    .orElseThrow(() -> new EntityNotFoundException("Carrito de usuario", idUsuario));
            CarritoItem item = carritoItemDAO.findById(idItem)
                    .orElseThrow(() -> new EntityNotFoundException("Item de carrito", idItem));
            if (!item.getCarrito().getId().equals(carrito.getId())) {
                throw new BadRequestException("El item no pertenece al carrito del usuario");
            }
            carritoItemDAO.delete(idItem);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void vaciar(Integer idUsuario) {
        try {
            Carrito carrito = carritoDAO.findByUsuarioId(idUsuario)
                    .orElseThrow(() -> new EntityNotFoundException("Carrito de usuario", idUsuario));
            carritoItemDAO.deleteByCarritoId(carrito.getId());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Carrito findCarritoPorUsuario(Integer idUsuario) {
        try {
            Carrito carrito = carritoDAO.findByUsuarioId(idUsuario)
                    .orElseThrow(() -> new EntityNotFoundException("Carrito de usuario", idUsuario));
            carrito.setItems(carritoItemDAO.findByCarritoIdEnriquecido(carrito.getId()));
            return carrito;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    CarritoDTO toDto(Carrito carrito) {
        try {
            List<CarritoItem> items = carritoItemDAO.findByCarritoIdEnriquecido(carrito.getId());
            carrito.setItems(items);
            List<CarritoItemResponseDTO> itemDtos = items.stream().map(this::toItemDto).collect(Collectors.toList());
            BigDecimal total = itemDtos.stream().map(CarritoItemResponseDTO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new CarritoDTO(carrito.getId(), carrito.getUsuario().getId(), carrito.getCreadoEn(), itemDtos, total);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    CarritoItemResponseDTO toItemDto(CarritoItem item) {
        BigDecimal precio = obtenerPrecioUnitario(item);
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
        return new CarritoItemResponseDTO(
            item.getId(),
            item.getProducto() != null ? item.getProducto().getIdProducto() : null,
            item.getPerfumeCustom() != null ? item.getPerfumeCustom().getIdPerfCust() : null,
            obtenerNombreItem(item), item.getCantidad(), precio, subtotal, item.getImagenUrl());
    }

    BigDecimal obtenerPrecioUnitario(CarritoItem item) {
        validarItemExclusivo(
            item.getProducto() != null ? item.getProducto().getIdProducto() : null,
            item.getPerfumeCustom() != null ? item.getPerfumeCustom().getIdPerfCust() : null);
        return item.getProducto() != null
            ? item.getProducto().getPrecio()
            : item.getPerfumeCustom().getPrecioCalculado();
    }

    private String obtenerNombreItem(CarritoItem item) {
        if (item.getProducto() != null) return item.getProducto().getNombre();
        String nombre = item.getPerfumeCustom().getNombrePersonalizado();
        return (nombre == null || nombre.isBlank()) ? "Perfume personalizado" : nombre;
    }

    private Carrito crearCarrito(Integer idUsuario) {
        try {
            Usuario usuario = usuarioDAO.findById(idUsuario)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", idUsuario));
            Carrito carrito = new Carrito();
            carrito.setUsuario(usuario);
            return carritoDAO.save(carrito);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private void validarItemExclusivo(Integer idProducto, Integer idPerfCust) {
        boolean producto = idProducto != null;
        boolean custom   = idPerfCust != null;
        if (producto == custom) {
            throw new BadRequestException("Debe indicar exactamente un producto o un perfume personalizado");
        }
    }
}
