package com.example.demo.service;

import com.example.demo.dao.UsuarioDAO;
import com.example.demo.dto.AuthLoginRequestDTO;
import com.example.demo.dto.AuthRegisterRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.AuthUserDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.security.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final JwtUtil    jwtUtil;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
        this.jwtUtil    = JwtUtil.getInstance();
    }

    public AuthResponseDTO register(AuthRegisterRequestDTO request) {
        return registerWithRol(request, "cliente");
    }

    public AuthResponseDTO registerStaff(AuthRegisterRequestDTO request) {
        String rol = request.getRol();
        if (!"empleado".equals(rol) && !"admin".equals(rol)) {
            throw new BadRequestException("Rol de staff inválido. Usa 'empleado' o 'admin'.");
        }
        return registerWithRol(request, rol);
    }

    private AuthResponseDTO registerWithRol(AuthRegisterRequestDTO request, String rol) {
        try {
            if (usuarioDAO.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Ya existe una cuenta con ese email.");
            }
            Usuario usuario = new Usuario();
            usuario.setNombre(request.getNombre());
            usuario.setEmail(request.getEmail());
            usuario.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            usuario.setTelefono(request.getTelefono());
            usuario.setRol(rol);
            usuario = usuarioDAO.save(usuario);
            String token = jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
            return new AuthResponseDTO(token, toUserDto(usuario));
        } catch (SQLException e) {
            throw new RuntimeException("Error de base de datos en registro", e);
        }
    }

    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        try {
            Usuario usuario = usuarioDAO.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("No existe ninguna cuenta con ese email."));
            if (!BCrypt.checkpw(request.getPassword(), usuario.getPassword())) {
                throw new BadRequestException("Contraseña incorrecta.");
            }
            String token = jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
            return new AuthResponseDTO(token, toUserDto(usuario));
        } catch (SQLException e) {
            throw new RuntimeException("Error de base de datos en login", e);
        }
    }

    private AuthUserDTO toUserDto(Usuario usuario) {
        return new AuthUserDTO(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getTelefono(), usuario.getRol(), usuario.getPuntos());
    }
}
