package com.example.demo.service;

import com.example.demo.dao.CategoriaDAO;
import com.example.demo.dao.ImagenProductoDAO;
import com.example.demo.dao.ProductoDAO;
import com.example.demo.dto.CategoriaDTO;
import com.example.demo.dto.ImagenProductoDTO;
import com.example.demo.dto.ProductoRequestDTO;
import com.example.demo.dto.ProductoResponseDTO;
import com.example.demo.entity.Categoria;
import com.example.demo.entity.ImagenProducto;
import com.example.demo.entity.Producto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductoService {

    private final ProductoDAO      productoDAO;
    private final CategoriaDAO     categoriaDAO;
    private final ImagenProductoDAO imagenDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.imagenDAO = new ImagenProductoDAO();
    }

    /**
     * Carga todos los productos con JOIN a categoría en 1 query,
     * luego carga todas las imágenes en 1 query batch (evita N+1).
     */
    public List<ProductoResponseDTO> listar() {
        try {
            List<Producto> productos = productoDAO.findAll();
            if (productos.isEmpty()) return List.of();

            List<Integer> ids = productos.stream().map(Producto::getIdProducto).collect(Collectors.toList());
            Map<Integer, List<ImagenProductoDTO>> imagenesPorProducto =
                imagenDAO.findByProductoIds(ids).stream()
                    .collect(Collectors.groupingBy(
                        img -> img.getProducto().getIdProducto(),
                        Collectors.mapping(
                            img -> new ImagenProductoDTO(img.getIdImagen(), img.getProducto().getIdProducto(),
                                                         img.getUrl(), img.getDescripcion()),
                            Collectors.toList())));

            return productos.stream()
                .map(p -> toResponseWithImages(p, imagenesPorProducto.getOrDefault(p.getIdProducto(), List.of())))
                .collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public ProductoResponseDTO obtenerPorId(Integer id) {
        try {
            return toResponse(findProducto(id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        try {
            Producto producto = new Producto();
            aplicarDatos(producto, request);
            return toResponse(productoDAO.save(producto));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request) {
        try {
            Producto producto = findProducto(id);
            aplicarDatos(producto, request);
            return toResponse(productoDAO.save(producto));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public ProductoResponseDTO upsertPorSku(ProductoRequestDTO request) {
        try {
            if (request.getSku() == null || request.getSku().isBlank()) {
                throw new BadRequestException("El SKU es obligatorio para hacer upsert");
            }
            Producto producto = productoDAO.findBySku(request.getSku()).orElseGet(Producto::new);
            aplicarDatos(producto, request);
            return toResponse(productoDAO.save(producto));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void eliminar(Integer id) {
        try {
            findProducto(id); // verifica que existe
            productoDAO.delete(id);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Producto findProducto(Integer id) throws SQLException {
        return productoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));
    }

    private void aplicarDatos(Producto p, ProductoRequestDTO req) throws SQLException {
        Categoria cat = categoriaDAO.findById(req.getIdCategoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoria", req.getIdCategoria()));
        p.setSku(req.getSku()); p.setNombre(req.getNombre());
        p.setDescripcion(req.getDescripcion()); p.setIngredientes(req.getIngredientes());
        p.setModoUso(req.getModoUso()); p.setPrecio(req.getPrecio());
        p.setStock(req.getStock()); p.setGenero(req.getGenero());
        p.setTipoFragancia(req.getTipoFragancia()); p.setCategoria(cat);
    }

    ProductoResponseDTO toResponse(Producto p) throws SQLException {
        List<ImagenProductoDTO> imgs = imagenDAO.findByProductoId(p.getIdProducto()).stream()
            .map(img -> new ImagenProductoDTO(img.getIdImagen(), p.getIdProducto(), img.getUrl(), img.getDescripcion()))
            .collect(Collectors.toList());
        return toResponseWithImages(p, imgs);
    }

    private ProductoResponseDTO toResponseWithImages(Producto p, List<ImagenProductoDTO> imagenes) {
        Categoria cat = p.getCategoria();
        return new ProductoResponseDTO(p.getIdProducto(), p.getSku(), p.getNombre(), p.getDescripcion(),
            p.getIngredientes(), p.getModoUso(), p.getPrecio(), p.getStock(), p.getGenero(),
            p.getTipoFragancia(), new CategoriaDTO(cat.getIdCategoria(), cat.getNombre()),
            p.getIdSubcategoria(), imagenes);
    }
}
