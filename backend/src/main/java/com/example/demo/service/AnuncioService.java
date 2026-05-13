package com.example.demo.service;

import com.example.demo.dao.AnuncioDAO;
import com.example.demo.entity.Anuncio;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;

public class AnuncioService {

    private final AnuncioDAO anuncioDAO = new AnuncioDAO();

    public Anuncio crear(String titulo, String tag, String tagLabel, String mensaje, String autor) {
        if (titulo == null || titulo.isBlank())
            throw new BadRequestException("El título del anuncio es obligatorio");
        if (tag == null || tag.isBlank())
            throw new BadRequestException("El tag del anuncio es obligatorio");
        if (autor == null || autor.isBlank())
            throw new BadRequestException("El autor del anuncio es obligatorio");
        try {
            Anuncio a = new Anuncio();
            a.setTitulo(titulo.trim());
            a.setTag(tag.trim());
            a.setTagLabel(tagLabel != null ? tagLabel.trim() : tag.trim());
            a.setMensaje(mensaje != null ? mensaje.trim() : null);
            a.setAutor(autor.trim());
            return anuncioDAO.save(a);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el anuncio", e);
        }
    }

    public List<Anuncio> listar() {
        try {
            return anuncioDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener anuncios", e);
        }
    }

    public void eliminar(Integer id) {
        try {
            if (!anuncioDAO.delete(id))
                throw new EntityNotFoundException("Anuncio no encontrado con id: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el anuncio", e);
        }
    }
}
