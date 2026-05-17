package com.example.demo.service;

import com.example.demo.dto.AdminPedidoDTO;
import com.example.demo.dao.DetallePedidoDAO;
import com.example.demo.dao.DireccionDAO;
import com.example.demo.dao.ImagenProductoDAO;
import com.example.demo.dao.PedidoDAO;
import com.example.demo.dao.ProductoDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.dto.DetallePedidoDTO;
import com.example.demo.dto.PedidoRequestDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PedidoService {

    private final PedidoDAO          pedidoDAO;
    private final UsuarioDAO         usuarioDAO;
    private final DireccionDAO       direccionDAO;
    private final DetallePedidoDAO   detallePedidoDAO;
    private final CarritoService     carritoService;
    private final ProductoDAO        productoDAO;
    private final ImagenProductoDAO  imagenProductoDAO;

    public PedidoService() {
        this.pedidoDAO          = new PedidoDAO();
        this.usuarioDAO         = new UsuarioDAO();
        this.direccionDAO       = new DireccionDAO();
        this.detallePedidoDAO   = new DetallePedidoDAO();
        this.carritoService     = new CarritoService();
        this.productoDAO        = new ProductoDAO();
        this.imagenProductoDAO  = new ImagenProductoDAO();
    }

    public List<PedidoResponseDTO> listarPorUsuario(Integer idUsuario) {
        try {
            return pedidoDAO.findByUsuarioId(idUsuario).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PedidoResponseDTO obtenerPorId(Integer id) {
        try {
            return toResponse(findPedido(id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PedidoResponseDTO crearDesdeCarrito(PedidoRequestDTO request) {
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Usuario usuario = usuarioDAO.findById(request.getIdUsuario())
                        .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getIdUsuario()));
                Direccion direccion = direccionDAO.findById(request.getIdDireccion())
                        .orElseThrow(() -> new EntityNotFoundException("Direccion", request.getIdDireccion()));

                if (!direccion.getUsuario().getId().equals(usuario.getId())) {
                    throw new BadRequestException("La direccion no pertenece al usuario indicado");
                }

                Carrito carrito = carritoService.findCarritoPorUsuario(usuario.getId());
                if (carrito.getItems().isEmpty()) {
                    throw new BadRequestException("No se puede crear un pedido con el carrito vacío");
                }

                // Validar stock antes de crear el pedido
                for (CarritoItem item : carrito.getItems()) {
                    if (item.getProducto() != null) {
                        Producto producto = item.getProducto();
                        int stockDisponible = producto.getStock() == null ? 0 : producto.getStock();
                        if (stockDisponible < item.getCantidad()) {
                            throw new BadRequestException(
                                "Stock insuficiente para \"" + producto.getNombre() +
                                "\". Disponible: " + stockDisponible + ", solicitado: " + item.getCantidad());
                        }
                    }
                }

                Pedido pedido = new Pedido();
                pedido.setUsuario(usuario);
                pedido.setDireccion(direccion);
                pedido.setEstado("pendiente");
                pedido.setTotal(BigDecimal.ZERO);

                BigDecimal total = BigDecimal.ZERO;
                List<DetallePedido> detalles = new ArrayList<>();
                for (CarritoItem item : carrito.getItems()) {
                    BigDecimal precioUnitario = carritoService.obtenerPrecioUnitario(item);
                    DetallePedido detalle = new DetallePedido();
                    detalle.setPedido(pedido);
                    detalle.setProducto(item.getProducto());
                    detalle.setPerfumeCustom(item.getPerfumeCustom());
                    detalle.setCantidad(item.getCantidad());
                    detalle.setPrecioUnitario(precioUnitario);
                    detalles.add(detalle);
                    total = total.add(precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad())));
                }
                pedido.setTotal(total);

                // Insertar pedido en la transacción compartida
                pedidoDAO.save(pedido, conn);

                // Insertar detalles y reducir stock en la misma transacción
                for (DetallePedido detalle : detalles) {
                    detalle.setPedido(pedido);
                    detallePedidoDAO.save(detalle, conn);
                    if (detalle.getProducto() != null) {
                        productoDAO.decrementarStock(
                            detalle.getProducto().getIdProducto(), detalle.getCantidad(), conn);
                    }
                }

                // Vaciar carrito (usa su propio conn)
                conn.commit();
                carritoService.vaciar(usuario.getId());

                pedido.setDetalles(detalles);
                return toResponse(pedido);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<AdminPedidoDTO> listarTodosAdmin() {
        try {
            List<AdminPedidoDTO> pedidos = pedidoDAO.findAllAdmin();
            for (AdminPedidoDTO dto : pedidos) {
                List<DetallePedido> detalles = detallePedidoDAO.findByPedidoId(dto.getIdPedido());
                List<Integer> productoIds = detalles.stream()
                        .filter(d -> d.getProducto() != null)
                        .map(d -> d.getProducto().getIdProducto())
                        .collect(Collectors.toList());
                Map<Integer, String> imagenPorProducto = imagenProductoDAO.findByProductoIds(productoIds)
                        .stream()
                        .collect(Collectors.toMap(
                            img -> img.getProducto().getIdProducto(),
                            img -> img.getUrl(),
                            (url1, url2) -> url1));
                dto.setDetalles(detalles.stream().map(d -> toDetalleDto(d, imagenPorProducto)).collect(Collectors.toList()));
            }
            return pedidos;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void actualizarEstado(Integer idPedido, String estado) {
        try {
            pedidoDAO.updateEstado(idPedido, estado);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Pedido findPedido(Integer id) throws SQLException {
        return pedidoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido", id));
    }

    PedidoResponseDTO toResponse(Pedido pedido) {
        try {
            List<DetallePedido> detalles = pedido.getDetalles().isEmpty()
                    ? detallePedidoDAO.findByPedidoId(pedido.getIdPedido())
                    : pedido.getDetalles();

            List<Integer> productoIds = detalles.stream()
                    .filter(d -> d.getProducto() != null)
                    .map(d -> d.getProducto().getIdProducto())
                    .collect(Collectors.toList());

            Map<Integer, String> imagenPorProducto = imagenProductoDAO.findByProductoIds(productoIds)
                    .stream()
                    .collect(Collectors.toMap(
                        img -> img.getProducto().getIdProducto(),
                        img -> img.getUrl(),
                        (url1, url2) -> url1));

            return new PedidoResponseDTO(
                    pedido.getIdPedido(), pedido.getUsuario().getId(), pedido.getDireccion().getId(),
                    pedido.getEstado(), pedido.getTotal(), pedido.getFecha(),
                    detalles.stream().map(d -> toDetalleDto(d, imagenPorProducto)).collect(Collectors.toList()));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private DetallePedidoDTO toDetalleDto(DetallePedido d, Map<Integer, String> imagenPorProducto) {
        BigDecimal precioUnit = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
        int cantidad = d.getCantidad() != null ? d.getCantidad() : 1;
        BigDecimal subtotal = precioUnit.multiply(BigDecimal.valueOf(cantidad));
        String nombre;
        if (d.getProducto() != null) {
            nombre = d.getProducto().getNombre();
        } else if (d.getPerfumeCustom() != null) {
            String np = d.getPerfumeCustom().getNombrePersonalizado();
            nombre = (np == null || np.isBlank()) ? "Perfume personalizado" : np;
        } else {
            nombre = "Producto sin referencia";
        }
        DetallePedidoDTO dto = new DetallePedidoDTO(d.getId(),
            d.getProducto() != null ? d.getProducto().getIdProducto() : null,
            d.getPerfumeCustom() != null ? d.getPerfumeCustom().getIdPerfCust() : null,
            nombre, d.getCantidad(), d.getPrecioUnitario(), subtotal);
        if (d.getProducto() != null) {
            dto.setImagenUrl(imagenPorProducto.get(d.getProducto().getIdProducto()));
        }
        return dto;
    }
}
