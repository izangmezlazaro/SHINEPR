package com.example.demo.dto;

/**
 * Representa un evento en el feed de actividad reciente de la intranet.
 * Diseñado para ser escalable: añadir nuevos tipos de evento no requiere
 * cambios en el frontend, solo agregar más filas al UNION en ActividadDAO.
 */
public class ActivityItemDTO {

    /** Tipo de evento: "pedido", "fichaje", "anuncio", "buzon" */
    private String tipo;

    /** Texto descriptivo del evento para mostrar al usuario */
    private String descripcion;

    /** Nombre del usuario/autor relacionado con el evento */
    private String usuario;

    /** ISO-8601 timestamp del evento */
    private String timestamp;

    /** Color hexadecimal para el punto del timeline */
    private String color;

    /** Identificador del icono SVG a usar en el frontend */
    private String icono;

    /** ID del objeto relacionado (pedido, fichaje, etc.) */
    private Integer refId;

    public ActivityItemDTO() {}

    public ActivityItemDTO(String tipo, String descripcion, String usuario,
                           String timestamp, String color, String icono, Integer refId) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.timestamp = timestamp;
        this.color = color;
        this.icono = icono;
        this.refId = refId;
    }

    public String getTipo()        { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getUsuario()     { return usuario; }
    public String getTimestamp()   { return timestamp; }
    public String getColor()       { return color; }
    public String getIcono()       { return icono; }
    public Integer getRefId()      { return refId; }

    public void setTipo(String tipo)               { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setUsuario(String usuario)         { this.usuario = usuario; }
    public void setTimestamp(String timestamp)     { this.timestamp = timestamp; }
    public void setColor(String color)             { this.color = color; }
    public void setIcono(String icono)             { this.icono = icono; }
    public void setRefId(Integer refId)            { this.refId = refId; }
}
