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
        if (!"ENTRADA".equals(tipo) && !"SALIDA".equals(tipo))
            throw new BadRequestException("El tipo debe ser ENTRADA o SALIDA");

        String email  = empleadoEmail.trim().toLowerCase();
        String nombre = (empleadoNombre != null && !empleadoNombre.isBlank()) ? empleadoNombre.trim() : email;

        try {
            if ("ENTRADA".equals(tipo)) {
                return fichajeDAO.registrarEntrada(email, nombre);
            } else {
                int updated = fichajeDAO.registrarSalida(email);
                if (updated == 0)
                    throw new BadRequestException("No hay un fichaje de entrada abierto para hoy.");
                Fichaje f = new Fichaje();
                f.setEmpleadoEmail(email);
                f.setEmpleadoNombre(nombre);
                f.setEstado("COMPLETADO");
                return f;
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el fichaje: " + e.getMessage(), e);
        }
    }

    public List<Fichaje> listarPorFecha(String fechaStr) {
        try {
            LocalDate fecha = (fechaStr != null && !fechaStr.isBlank())
                    ? LocalDate.parse(fechaStr)
                    : LocalDate.now();
            return fichajeDAO.findByFecha(fecha);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener fichajes: " + e.getMessage(), e);
        }
    }

    public List<Fichaje> listarTodos() {
        try {
            return fichajeDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener fichajes: " + e.getMessage(), e);
        }
    }
}
