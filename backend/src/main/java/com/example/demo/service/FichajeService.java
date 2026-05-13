package com.example.demo.service;

import com.example.demo.dao.FichajeDAO;
import com.example.demo.entity.Fichaje;
import com.example.demo.exception.BadRequestException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FichajeService {

    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public Fichaje registrar(String empleadoEmail, String empleadoNombre, String tipo) {
        if (empleadoEmail == null || empleadoEmail.isBlank())
            throw new BadRequestException("El email del empleado es obligatorio");
        if (empleadoNombre == null || empleadoNombre.isBlank())
            throw new BadRequestException("El nombre del empleado es obligatorio");
        if (!"ENTRADA".equals(tipo) && !"SALIDA".equals(tipo))
            throw new BadRequestException("El tipo debe ser ENTRADA o SALIDA");
        try {
            Fichaje f = new Fichaje();
            f.setEmpleadoEmail(empleadoEmail.trim().toLowerCase());
            f.setEmpleadoNombre(empleadoNombre.trim());
            f.setTipo(tipo);
            return fichajeDAO.save(f);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el fichaje", e);
        }
    }

    public List<Fichaje> listarPorFecha(String fechaStr) {
        try {
            LocalDate fecha = (fechaStr != null && !fechaStr.isBlank())
                    ? LocalDate.parse(fechaStr)
                    : LocalDate.now();
            return fichajeDAO.findByFecha(fecha);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener fichajes", e);
        }
    }

    public List<Fichaje> listarTodos() {
        try {
            return fichajeDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener fichajes", e);
        }
    }
}
