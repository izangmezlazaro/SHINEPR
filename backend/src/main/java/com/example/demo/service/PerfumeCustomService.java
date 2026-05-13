package com.example.demo.service;

import com.example.demo.dao.FraganciaDAO;
import com.example.demo.dao.FrascoDAO;
import com.example.demo.dao.PerfumeCustomDAO;
import com.example.demo.dao.PerfumeCustomFraganciaDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.dto.FrascoDTO;
import com.example.demo.dto.PerfumeCustomFraganciaDTO;
import com.example.demo.dto.PerfumeCustomRequestDTO;
import com.example.demo.dto.PerfumeCustomResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PerfumeCustomService {

    private final PerfumeCustomDAO         perfumeCustomDAO;
    private final PerfumeCustomFraganciaDAO fraganciaRelDAO;
    private final UsuarioDAO               usuarioDAO;
    private final FrascoDAO                frascoDAO;
    private final FraganciaDAO             fraganciaDAO;

    public PerfumeCustomService() {
        this.perfumeCustomDAO = new PerfumeCustomDAO();
        this.fraganciaRelDAO  = new PerfumeCustomFraganciaDAO();
        this.usuarioDAO       = new UsuarioDAO();
        this.frascoDAO        = new FrascoDAO();
        this.fraganciaDAO     = new FraganciaDAO();
    }

    public List<PerfumeCustomResponseDTO> listarPorUsuario(Integer idUsuario) {
        try {
            return perfumeCustomDAO.findByUsuarioId(idUsuario).stream()
                    .map(pc -> toResponse(pc))
                    .collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PerfumeCustomResponseDTO obtenerPorId(Integer id) {
        try {
            return toResponse(findPerfumeCustom(id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PerfumeCustomResponseDTO crear(PerfumeCustomRequestDTO request) {
        try {
            Usuario usuario = usuarioDAO.findById(request.getIdUsuario())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getIdUsuario()));
            Frasco frasco = frascoDAO.findById(request.getIdFrasco())
                    .orElseThrow(() -> new EntityNotFoundException("Frasco", request.getIdFrasco()));

            PerfumeCustom pc = new PerfumeCustom();
            pc.setUsuario(usuario);
            pc.setFrasco(frasco);
            pc.setNombrePersonalizado(request.getNombrePersonalizado());
            pc.setIntensidad(request.getIntensidad());
            pc.setPrecioCalculado(request.getPrecioCalculado());

            for (PerfumeCustomFraganciaDTO dto : request.getFragancias()) {
                Fragancia fragancia = fraganciaDAO.findById(dto.getIdFragancia())
                        .orElseThrow(() -> new EntityNotFoundException("Fragancia", dto.getIdFragancia()));
                PerfumeCustomFragancia rel = new PerfumeCustomFragancia();
                rel.setFragancia(fragancia);
                rel.setOrden(dto.getOrden());
                pc.getFragancias().add(rel);
            }

            return toResponse(perfumeCustomDAO.save(pc));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    PerfumeCustom findPerfumeCustom(Integer id) throws SQLException {
        return perfumeCustomDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Perfume personalizado", id));
    }

    PerfumeCustomResponseDTO toResponse(PerfumeCustom pc) {
        try {
            // Cargar datos completos de frasco si son solo un ID
            Frasco frasco = pc.getFrasco();
            if (frasco.getForma() == null) {
                frasco = frascoDAO.findById(frasco.getIdFrasco()).orElse(frasco);
            }

            // Cargar relaciones fragancia
            List<PerfumeCustomFragancia> rels = pc.getFragancias().isEmpty()
                    ? fraganciaRelDAO.findByPerfumeCustomId(pc.getIdPerfCust())
                    : pc.getFragancias();

            // Enriquecer con nombre de fragancia si falta
            for (PerfumeCustomFragancia rel : rels) {
                if (rel.getFragancia() != null && rel.getFragancia().getNombre() == null) {
                    fraganciaDAO.findById(rel.getFragancia().getIdFragancia()).ifPresent(rel::setFragancia);
                }
            }

            List<PerfumeCustomFraganciaDTO> fragDtos = rels.stream()
                    .sorted(Comparator.comparingInt(r -> r.getOrden() == null ? Integer.MAX_VALUE : r.getOrden()))
                    .map(r -> new PerfumeCustomFraganciaDTO(
                            r.getFragancia().getIdFragancia(),
                            r.getFragancia().getNombre(),
                            r.getOrden()))
                    .collect(Collectors.toList());

            return new PerfumeCustomResponseDTO(
                    pc.getIdPerfCust(), pc.getUsuario().getId(),
                    new FrascoDTO(frasco.getIdFrasco(), frasco.getForma(), frasco.getCapacidadMl(),
                                  frasco.getMaterial(), frasco.getPrecio()),
                    pc.getNombrePersonalizado(), pc.getIntensidad(), pc.getPrecioCalculado(), fragDtos);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
