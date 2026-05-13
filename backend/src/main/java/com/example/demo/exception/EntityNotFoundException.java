package com.example.demo.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " con id " + id + " no encontrado");
    }
    public EntityNotFoundException(String message) {
        super(message);
    }
}
