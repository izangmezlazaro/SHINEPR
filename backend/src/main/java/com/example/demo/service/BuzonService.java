package com.example.demo.service;

import com.example.demo.dao.BuzonDAO;
import com.example.demo.entity.BuzonMensaje;
import com.example.demo.exception.BadRequestException;

import java.sql.SQLException;
import java.util.List;

public class BuzonService {

    private final BuzonDAO buzonDAO = new BuzonDAO();

    public BuzonMensaje enviar(String nombre, String apellidos, String asunto, String texto) {
        if (nombre == null || nombre.isBlank())
            throw new BadRequestException("El nombre es obligatorio");
        if (apellidos == null || apellidos.isBlank())
            throw new BadRequestException("Los apellidos son obligatorios");
        if (asunto == null || asunto.isBlank())
            throw new BadRequestException("El asunto es obligatorio");

        try {
            BuzonMensaje m = new BuzonMensaje();
            m.setNombre(nombre.trim());
            m.setApellidos(apellidos.trim());
            m.setAsunto(asunto.trim());
            m.setTexto(texto != null ? texto.trim() : "");
            return buzonDAO.save(m);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el mensaje", e);
        }
    }

    public List<BuzonMensaje> listar() {
        try {
            return buzonDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener los mensajes", e);
        }
    }
}
