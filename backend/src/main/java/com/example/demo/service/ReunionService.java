package com.example.demo.service;

import com.example.demo.dao.ReunionDAO;
import com.example.demo.entity.Reunion;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;

public class ReunionService {

    private final ReunionDAO reunionDAO = new ReunionDAO();

    public Reunion crear(String titulo, String fecha, String hora, String plataforma,
                         String asistentes, String color, String creadoPor) {
        if (titulo == null || titulo.isBlank())
            throw new BadRequestException("El título de la reunión es obligatorio");
        if (fecha == null || fecha.isBlank())
            throw new BadRequestException("La fecha de la reunión es obligatoria");
        if (hora == null || hora.isBlank())
            throw new BadRequestException("La hora de la reunión es obligatoria");
        if (plataforma == null || plataforma.isBlank())
            throw new BadRequestException("La plataforma es obligatoria");
        try {
            Reunion r = new Reunion();
            r.setTitulo(titulo.trim());
            r.setFecha(fecha.trim());
            r.setHora(hora.trim());
            r.setPlataforma(plataforma.trim());
            r.setAsistentes(asistentes != null ? asistentes.trim() : null);
            r.setColor(color != null && !color.isBlank() ? color.trim() : "rose");
            r.setCreadoPor(creadoPor != null ? creadoPor.trim() : null);
            return reunionDAO.save(r);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la reunión", e);
        }
    }

    public List<Reunion> listar() {
        try {
            return reunionDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener reuniones", e);
        }
    }

    public void eliminar(Integer id) {
        try {
            if (!reunionDAO.delete(id))
                throw new EntityNotFoundException("Reunión no encontrada con id: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la reunión", e);
        }
    }
}
